package com.example.grupo34_atdd.modules.cursos.application;

import com.example.grupo34_atdd.modules.cursos.domain.CursoComAvaliacao;
import com.example.grupo34_atdd.modules.cursos.domain.QuestaoQuiz;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CatalogoCursos {

    public static final String SLUG_PYTHON = "introducao-python";
    public static final String TITULO_PYTHON = "Introdução ao Python";
    public static final String SLUG_CICD = "introducao-cicd";
    public static final String TITULO_CICD = "Introdução a CI/CD";
    public static final String SLUG_CSHARP = "introducao-csharp";
    public static final String TITULO_CSHARP = "Introdução ao C#";
    private static final String VIDEO_PYTHON = "https://www.youtube-nocookie.com/embed/g_R_Asf6Co0";
    private static final String VIDEO_CICD = "https://www.youtube-nocookie.com/embed/95r8nJLq5zU";
    private static final String VIDEO_CSHARP = "https://www.youtube-nocookie.com/embed/HOpCMKNJEFg";

    private final List<CursoComAvaliacao> cursos = List.of(
            criarCursoPython(),
            criarCursoCicd(),
            criarCursoCsharp()
    );

    public List<CursoComAvaliacao> listar() {
        return cursos;
    }

    public CursoComAvaliacao buscar(String slug) {
        return cursos.stream()
                .filter(curso -> Objects.equals(curso.slug(), slug))
                .findFirst()
                .orElse(null);
    }

    private static CursoComAvaliacao criarCursoPython() {
        return new CursoComAvaliacao(SLUG_PYTHON, TITULO_PYTHON,
                "Primeiros passos com Python, VS Code, comandos, variáveis e operações.", VIDEO_PYTHON,
                List.of(
                        q("q1", "O que é Python?", List.of("Uma linguagem de programação", "Um banco de dados", "Um sistema operacional", "Um navegador"), 0),
                        q("q2", "Qual extensão é usada nos arquivos de código Python?", List.of(".java", ".py", ".html", ".sql"), 1),
                        q("q3", "Qual comando é apresentado para exibir informações no terminal?", List.of("show()", "echo()", "print()", "display()"), 2),
                        q("q4", "Como textos são escritos nos exemplos de código Python?", List.of("Entre aspas", "Entre colchetes", "Com o prefixo #", "Sem nenhum delimitador"), 0),
                        q("q5", "Em que ordem Python executa comandos de um arquivo, conforme a aula?", List.of("De baixo para cima", "Em ordem aleatória", "De cima para baixo", "Somente os comandos print"), 2),
                        q("q6", "Como passar mais de uma informação para print na mesma linha?", List.of("Separando por vírgulas", "Usando ponto e vírgula", "Usando barras", "Criando um arquivo novo"), 0),
                        q("q7", "Qual símbolo inicia um comentário em Python?", List.of("//", "<!--", "#", "**"), 2),
                        q("q8", "O que acontece com o texto de um comentário durante a execução?", List.of("É executado como comando", "É ignorado pelo Python", "É exibido sempre", "É transformado em variável"), 1),
                        q("q9", "No exemplo da aula, qual operação calcula o lucro a partir de faturamento e custo?", List.of("Faturamento + custo", "Faturamento - custo", "Faturamento × custo", "Custo ÷ faturamento"), 1),
                        q("q10", "Um valor como 750,1 é classificado como qual tipo no exemplo?", List.of("Inteiro", "Texto", "Booleano", "Número decimal"), 3)
                ));
    }

    private static CursoComAvaliacao criarCursoCicd() {
        return new CursoComAvaliacao(SLUG_CICD, TITULO_CICD,
                "Conceitos de integração contínua, entrega contínua e automação do commit ao deploy.", VIDEO_CICD,
                List.of(
                        q("q1", "O que significa CI em CI/CD?", List.of("Integração contínua", "Instalação de código", "Controle interno", "Interface de comandos"), 0),
                        q("q2", "Qual prática ajuda a integrar mudanças de código com frequência?", List.of("Guardar alterações sem enviar", "Fazer commits pequenos e frequentes", "Evitar revisão de código", "Juntar todas as mudanças no fim do semestre"), 1),
                        q("q3", "O que uma etapa de CI costuma executar após uma mudança?", List.of("Somente abrir o editor", "Apagar a versão anterior", "Compilar e executar testes automatizados", "Desativar o controle de versão"), 2),
                        q("q4", "Para que serve uma pipeline CI/CD?", List.of("Organizar e automatizar etapas do desenvolvimento até a entrega", "Substituir o código-fonte", "Criar usuários no sistema", "Desenhar a interface do programa"), 0),
                        q("q5", "O que deve acontecer se os testes automatizados falharem?", List.of("Publicar mesmo assim", "Ignorar o resultado", "Interromper o avanço até corrigir o problema", "Apagar os testes"), 2),
                        q("q6", "Qual é a ideia de entrega contínua (Continuous Delivery)?", List.of("Manter a aplicação sempre pronta para ser publicada", "Publicar somente uma vez por ano", "Fazer todo teste manualmente", "Evitar gerar versões da aplicação"), 0),
                        q("q7", "O que caracteriza a implantação contínua (Continuous Deployment)?", List.of("A equipe copia arquivos manualmente", "Toda mudança aprovada pela pipeline pode ser publicada automaticamente", "O sistema não possui ambientes", "O código é executado apenas no computador de quem programou"), 1),
                        q("q8", "Qual é a função da etapa de build?", List.of("Criar uma versão executável ou distribuível da aplicação", "Escolher quem pode usar o sistema", "Editar as respostas do quiz", "Substituir o banco de dados"), 0),
                        q("q9", "O que é feito em uma etapa de deploy?", List.of("A aplicação é disponibilizada em um ambiente", "O código deixa de ser versionado", "Os testes são removidos", "A documentação é apagada"), 0),
                        q("q10", "Qual benefício a automação da pipeline oferece?", List.of("Elimina a necessidade de testar", "Torna a entrega mais repetível e reduz etapas manuais", "Garante que nunca haverá falhas", "Substitui a equipe de desenvolvimento"), 1)
                ));
    }

    private static CursoComAvaliacao criarCursoCsharp() {
        return new CursoComAvaliacao(SLUG_CSHARP, TITULO_CSHARP,
                "Primeiros passos com a linguagem C#: variáveis, tipos, comandos e estruturas básicas.", VIDEO_CSHARP,
                List.of(
                        q("q1", "O que é C#?", List.of("Uma linguagem de programação", "Um banco de dados", "Um sistema operacional", "Um navegador de internet"), 0),
                        q("q2", "Qual tipo é usado normalmente para guardar um número inteiro?", List.of("string", "int", "bool", "char[]"), 1),
                        q("q3", "Qual tipo representa um texto?", List.of("int", "double", "string", "bool"), 2),
                        q("q4", "Qual comando escreve uma linha no console em C#?", List.of("Console.WriteLine()", "printLine()", "System.Show()", "echoText()"), 0),
                        q("q5", "Qual símbolo atribui um valor a uma variável?", List.of("==", "=", "!=", ">="), 1),
                        q("q6", "Quais valores uma variável do tipo bool pode guardar?", List.of("Somente números", "Texto e números", "true ou false", "Datas e horários"), 2),
                        q("q7", "Qual sinal normalmente encerra uma instrução em C#?", List.of(":", ",", ".", ";"), 3),
                        q("q8", "Para que servem as chaves { } no código?", List.of("Delimitar um bloco de código", "Declarar um comentário", "Converter texto em número", "Finalizar o programa"), 0),
                        q("q9", "Para que serve a estrutura if?", List.of("Repetir uma instrução um número fixo de vezes", "Executar uma parte do código conforme uma condição", "Criar um arquivo", "Mostrar uma mensagem sempre"), 1),
                        q("q10", "Para que serve um laço for?", List.of("Declarar um texto", "Encerrar a aplicação", "Repetir instruções seguindo uma condição", "Comparar duas strings"), 2)
                ));
    }

    private static QuestaoQuiz q(String id, String enunciado, List<String> alternativas, int correta) {
        return new QuestaoQuiz(id, enunciado, alternativas, correta);
    }
}
