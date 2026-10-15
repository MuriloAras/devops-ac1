def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

def mvnw(String arguments) {
    if (isUnix()) {
        sh "chmod +x mvnw && ./mvnw ${arguments}"
    } else {
        bat "call mvnw.cmd ${arguments}"
    }
}

pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '10'))
    }
    parameters {
        booleanParam(name: 'PUSH_DOCKER_HUB', defaultValue: false,
            description: 'Publicar API e frontend no Docker Hub (opcional).')
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: '',
            description: 'Seu usuario/organizacao no Docker Hub; usado apenas se a publicacao estiver habilitada.')
    }
    environment {
        COMPOSE_FILE = 'docker-compose.homol.yml'
        COMPOSE_PROJECT_NAME = 'supercursos-homol'
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = 'dockerhub-supercursos'
    }
    stages {
        stage('0 - Preflight do Ambiente') {
            steps {
                script {
                    runCmd('java -version')
                    runCmd('git --version')
                    runCmd('docker version')
                    runCmd('docker compose version')
                    echo 'O ambiente de execucao precisa de JDK 21 e acesso ao Docker. Node e Cypress rodam em containers.'
                }
            }
        }
        stage('1 - Checkout') {
            steps {
                script {
                    def revision = checkout scm
                    env.SOURCE_COMMIT = revision.GIT_COMMIT
                    writeFile file: 'evidencias/a2/p2/execucao.properties',
                        text: "job=${env.JOB_NAME}\nbuild=${env.BUILD_NUMBER}\nurl=${env.BUILD_URL}\ncommit=${env.SOURCE_COMMIT}\nimage_tag=${env.IMAGE_TAG}\n"
                    runCmd('docker compose -f docker-compose.homol.yml config --quiet')
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'evidencias/a2/p2/execucao.properties', allowEmptyArchive: false
                }
            }
        }
        stage('2 - CI - JUnit + PostgreSQL + JaCoCo') {
            steps {
                script {
                    mvnw('-B -ntp clean test failsafe:integration-test failsafe:verify jacoco:report')
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/**,target/surefire-reports/**,target/failsafe-reports/**',
                        allowEmptyArchive: true
                    junit testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/TEST-*.xml',
                        allowEmptyResults: false
                    script {
                        if (fileExists('target/jacoco.exec')) {
                            jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes',
                                sourcePattern: 'src/main/java'
                        }
                    }
                }
            }
        }
        stage('3 - CI - Relatorio PMD') {
            steps { script { mvnw('-B -ntp pmd:pmd -DskipTests') } }
            post {
                success {
                    archiveArtifacts artifacts: 'target/site/pmd.html,target/pmd.xml', allowEmptyArchive: false
                }
            }
        }
        stage('4 - CI - Package') {
            steps { script { mvnw('-B -ntp package -DskipTests') } }
            post { success { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true } }
        }
        stage('5 - CD - Build dos Containers') {
            steps { script { runCmd('docker compose -f docker-compose.homol.yml build api frontend') } }
        }
        stage('6 - Registry - Docker Hub (opcional)') {
            when { expression { return params.PUSH_DOCKER_HUB } }
            steps {
                script {
                    if (!(params.DOCKERHUB_NAMESPACE ==~ /[a-z0-9][a-z0-9_-]*/)) {
                        error('Informe um DOCKERHUB_NAMESPACE valido para publicar as imagens.')
                    }
                    withEnv(["DOCKER_CONFIG=${env.WORKSPACE}/.docker-ci"]) {
                        withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                            try {
                                if (isUnix()) {
                                    sh 'printf "%s" "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USER" --password-stdin'
                                } else {
                                    bat 'powershell -NoProfile -Command "$env:DOCKER_PASSWORD | docker login --username $env:DOCKER_USER --password-stdin; exit $LASTEXITCODE"'
                                }
                                for (service in ['api', 'frontend']) {
                                    def published = "${params.DOCKERHUB_NAMESPACE}/supercursos-${service}:${env.IMAGE_TAG}"
                                    runCmd("docker tag supercursos-${service}:${env.IMAGE_TAG} ${published}")
                                    runCmd("docker push ${published}")
                                }
                            } finally {
                                runCmd('docker logout')
                                dir('.docker-ci') { deleteDir() }
                            }
                        }
                    }
                }
            }
        }
        stage('7 - CD - Deploy HOMOL') {
            steps {
                script {
                    runCmd('docker compose -f docker-compose.homol.yml up -d --wait --wait-timeout 180 postgres pgadmin api frontend prometheus grafana')
                }
            }
        }
        stage('8 - CD - Health Check HOMOL') {
            steps {
                script {
                    writeFile file: 'evidencias/a2/p3/health.json', text: ''
                    runCmd('docker compose -f docker-compose.homol.yml exec -T api curl --fail --silent --show-error http://localhost:8080/actuator/health > evidencias/a2/p3/health.json')
                    runCmd('docker compose -f docker-compose.homol.yml ps > evidencias/a2/p3/containers.txt')
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'evidencias/a2/p3/health.json,evidencias/a2/p3/containers.txt',
                        allowEmptyArchive: false
                }
            }
        }
        stage('9 - CD - Cypress E2E em Container') {
            steps {
                script {
                    dir('frontend/cypress/results') { deleteDir() }
                    runCmd('docker compose -f docker-compose.homol.yml --profile e2e run --rm --no-deps cypress')
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'frontend/cypress/results/**,frontend/cypress/screenshots/**,frontend/cypress/videos/**',
                        allowEmptyArchive: true
                    junit testResults: 'frontend/cypress/results/*.xml', allowEmptyResults: false
                }
            }
        }
        stage('10 - Observabilidade') {
            steps {
                echo 'API: http://localhost:8082/actuator/health'
                echo 'Super Cursos: http://localhost:3002'
                echo 'pgAdmin: http://localhost:5051'
                echo 'Prometheus: http://localhost:9092/targets'
                echo 'Grafana: http://localhost:3003/d/supercursos-homol'
                echo 'Confirme target UP, consulta up=1 e os quatro paineis. URLs sozinhas nao comprovam a coleta.'
            }
        }
    }
    post {
        failure {
            script {
                if (fileExists('docker-compose.homol.yml')) {
                    try {
                        runCmd('docker compose -f docker-compose.homol.yml ps')
                        runCmd('docker compose -f docker-compose.homol.yml logs --tail=80 api frontend postgres')
                    } catch (ignored) {
                        echo 'Diagnostico Docker indisponivel; consulte a primeira falha no console.'
                    }
                }
            }
        }
    }
}
