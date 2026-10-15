/// <reference types="cypress" />

const senha = 'Aluno-E2E-2026!'

function cadastrarAluno(saldoMoedas) {
  const identificador = `${Date.now()}-${Cypress._.random(100000, 999999999)}`
  const aluno = {
    nome: `Aluno E2E ${saldoMoedas}`,
    email: `e2e-${identificador}@supercursos.test`,
    senha,
    saldoMoedas,
  }

  return cy.request('POST', '/api/alunos', aluno).then(({ status, body }) => {
    expect(status, 'cadastro isolado pela API').to.eq(201)
    expect(body.saldoMoedas).to.eq(saldoMoedas)
    expect(body.cursosAdquiridos).to.deep.eq([])
    return { ...body, senha }
  })
}

function preencherLogin(aluno, senhaInformada = aluno.senha) {
  cy.get('#email').clear().type(aluno.email)
  cy.get('#senha').clear().type(senhaInformada, { log: false })
  cy.contains('button', 'Fazer Login').click()
}

function entrar(aluno) {
  cy.intercept('POST', '/api/alunos/login').as('loginValido')
  preencherLogin(aluno)
  cy.wait('@loginValido').its('response.statusCode').should('eq', 200)
  cy.get('.header-name').should('contain.text', aluno.nome)
  cy.get('.saldo-valor').should('have.text', `${aluno.saldoMoedas} moedas`)
}

function adquirirCurso(aluno, nomeCurso, saldoEsperado) {
  cy.intercept('POST', `/api/alunos/${aluno.id}/trocar-moedas`).as('troca')
  cy.get('#curso').type(nomeCurso)
  cy.contains('button', 'Trocar Moedas').should('be.enabled').click()
  cy.wait('@troca').then(({ response }) => {
    expect(response.statusCode).to.eq(200)
    expect(response.body.saldoMoedas).to.eq(saldoEsperado)
    expect(response.body.cursosAdquiridos).to.deep.eq([nomeCurso])
  })
  cy.get('.saldo-valor').should('have.text', `${saldoEsperado} moedas`)
  cy.get('.msg-ok').should('contain.text', 'adquirido com sucesso')
  cy.get('.curso-item').should('have.length', 1).and('contain.text', nomeCurso)
  cy.request(`/api/alunos/${aluno.id}`).its('body').should((persistido) => {
    expect(persistido.saldoMoedas).to.eq(saldoEsperado)
    expect(persistido.cursosAdquiridos).to.deep.eq([nomeCurso])
  })
}

describe('Super Cursos — autenticação e troca de moedas', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('recusa senha inválida e mantém a tela de login', () => {
    cadastrarAluno(3).then((aluno) => {
      cy.intercept('POST', '/api/alunos/login').as('loginInvalido')
      preencherLogin(aluno, 'Senha-Incorreta!')
      cy.wait('@loginInvalido').its('response.statusCode').should('eq', 401)
      cy.get('.erro-msg').should('contain.text', 'E-mail ou senha inválidos')
      cy.get('.dashboard-page').should('not.exist')
      cy.screenshot('01-login-invalido-bloqueado', { capture: 'viewport' })
    })
  })

  it('autentica aluno válido e exibe seu saldo e lista de cursos', () => {
    cadastrarAluno(5).then((aluno) => {
      entrar(aluno)
      cy.get('.cursos-vazio').should('contain.text', 'Você ainda não adquiriu nenhum curso')
      cy.screenshot('02-login-valido-saldo-inicial', { capture: 'viewport' })
    })
  })

  it('troca 3 moedas por um curso e termina com saldo zero', () => {
    cadastrarAluno(3).then((aluno) => {
      entrar(aluno)
      adquirirCurso(aluno, 'Docker do Zero', 0)
      cy.contains('button', 'Trocar Moedas').should('be.disabled')
      cy.screenshot('03-troca-3-moedas-saldo-zero', { capture: 'viewport' })
    })
  })

  it('troca 5 moedas por um curso e preserva as 2 moedas restantes', () => {
    cadastrarAluno(5).then((aluno) => {
      entrar(aluno)
      adquirirCurso(aluno, 'Pipeline Jenkins', 2)
      cy.screenshot('04-troca-5-moedas-saldo-dois', { capture: 'viewport' })
    })
  })

  it('bloqueia saldo 2 na interface e na API sem alterar saldo ou cursos', () => {
    cadastrarAluno(2).then((aluno) => {
      entrar(aluno)
      cy.get('#curso').type('Curso sem saldo')
      cy.contains('button', 'Trocar Moedas').should('be.disabled')
      cy.get('.aviso-saldo').should('contain.text', 'Saldo insuficiente')

      cy.request({
        method: 'POST',
        url: `/api/alunos/${aluno.id}/trocar-moedas`,
        body: { nomeCurso: 'Curso sem saldo' },
        failOnStatusCode: false,
      }).its('status').should('eq', 400)
      cy.request(`/api/alunos/${aluno.id}`).its('body').should((persistido) => {
        expect(persistido.saldoMoedas).to.eq(2)
        expect(persistido.cursosAdquiridos).to.deep.eq([])
      })
      cy.get('.saldo-valor').should('have.text', '2 moedas')
      cy.get('.cursos-vazio').should('be.visible')
      cy.screenshot('05-saldo-insuficiente-sem-alteracoes', { capture: 'viewport' })
    })
  })

  it('mantém o curso e o saldo após sair e entrar novamente', () => {
    cadastrarAluno(5).then((aluno) => {
      entrar(aluno)
      adquirirCurso(aluno, 'Monitoramento com Grafana', 2)
      cy.contains('button', 'Sair').click()
      cy.get('#email').should('be.visible')
      entrar({ ...aluno, saldoMoedas: 2 })
      cy.get('.curso-item').should('have.length', 1).and('contain.text', 'Monitoramento com Grafana')
      cy.screenshot('06-curso-e-saldo-persistidos-apos-login', { capture: 'viewport' })
    })
  })
})
