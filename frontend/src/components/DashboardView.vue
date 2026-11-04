<template>
  <div class="dashboard-page">
    <header class="header">
      <div class="header-logo">
        <img src="../assets/coin.png" alt="moeda" class="header-coin" />
        <span class="header-title">Super Cursos</span>
      </div>
      <div class="header-info">
        <span class="header-name">Olá, {{ aluno.nome }} 👋</span>
        <button class="btn-ranking" @click="$emit('abrir-ranking')">🏆 Ranking</button>
        <button class="btn-sair" @click="$emit('sair')">Sair</button>
      </div>
    </header>

    <section class="saldo-section">
      <div class="saldo-card">
        <img src="../assets/coin.png" alt="moeda" class="saldo-coin" />
        <div>
          <p class="saldo-label">Seu saldo</p>
          <p class="saldo-valor">{{ aluno.saldoMoedas }} moedas</p>
        </div>
      </div>
    </section>

    <main class="main-content">
      <section class="card trocar-card">
        <h2 class="section-title">🎓 Trocar Moedas por Curso</h2>
        <p class="section-sub">Cada curso custa <strong>3 moedas</strong></p>

        <div class="campo">
          <label for="curso">Nome do Curso</label>
          <input
            id="curso"
            v-model="nomeCurso"
            type="text"
            placeholder="Ex: Spring Boot Expert"
            @keydown.enter="trocarMoedas"
          />
        </div>

        <p v-if="msgTroca" :class="['msg', msgTrocaSucesso ? 'msg-ok' : 'msg-erro']">
          {{ msgTroca }}
        </p>

        <button
          class="btn-trocar"
          :disabled="trocando || aluno.saldoMoedas < 3"
          @click="trocarMoedas"
        >
          {{ trocando ? 'Trocando...' : '🪙 Trocar Moedas' }}
        </button>

        <p v-if="aluno.saldoMoedas < 3" class="aviso-saldo">
          ⚠️ Saldo insuficiente para trocar por um curso.
        </p>
      </section>

      <section class="card cursos-card">
        <h2 class="section-title">📚 Meus Cursos</h2>

        <div v-if="aluno.cursosAdquiridos && aluno.cursosAdquiridos.length > 0" class="cursos-lista">
          <div
            v-for="(curso, i) in aluno.cursosAdquiridos"
            :key="i"
            class="curso-item"
          >
            <img src="../assets/coin.png" alt="moeda" class="curso-coin" />
            <div class="curso-info">
              <span>{{ curso }}</span>
              <button
                v-if="cursoDoCatalogo(curso)"
                class="btn-acessar"
                data-testid="course-access"
                @click="$emit('acessar-curso', cursoDoCatalogo(curso).slug)"
              >
                Assistir e responder
              </button>
              <small v-else>Conteúdo em breve</small>
            </div>
          </div>
        </div>

        <div v-else class="cursos-vazio">
          <p>Você ainda não adquiriu nenhum curso.</p>
          <p class="cursos-vazio-sub">Troque suas moedas e comece a aprender!</p>
        </div>
      </section>

      <section class="card catalogo-card">
        <h2 class="section-title">🎬 Catálogo de cursos</h2>
        <p class="section-sub">Cada curso tem um vídeo e uma avaliação com 10 questões.</p>
        <div v-if="carregandoCatalogo" class="cursos-vazio">Carregando cursos...</div>
        <div v-else-if="catalogoCursos.length" class="catalogo-lista">
          <article v-for="curso in catalogoCursos" :key="curso.slug" class="catalogo-item">
            <div>
              <h3>{{ curso.titulo }}</h3>
              <p>{{ curso.descricao }}</p>
            </div>
            <button
              v-if="cursoAdquirido(curso.titulo)"
              class="btn-acessar"
              data-testid="course-access"
              @click="$emit('acessar-curso', curso.slug)"
            >
              Acessar curso
            </button>
            <button
              v-else
              class="btn-comprar"
              data-testid="course-purchase"
              :disabled="comprandoCatalogo || aluno.saldoMoedas < 3"
              @click="adquirirCurso(curso.titulo)"
            >
              {{ comprandoCatalogo ? 'Adquirindo...' : 'Adquirir por 3 moedas' }}
            </button>
          </article>
        </div>
        <p v-if="msgCatalogo" :class="['msg', msgCatalogoSucesso ? 'msg-ok' : 'msg-erro']">
          {{ msgCatalogo }}
        </p>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  aluno: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['sair', 'atualizar', 'acessar-curso', 'abrir-ranking'])

const nomeCurso = ref('')
const trocando = ref(false)
const msgTroca = ref('')
const msgTrocaSucesso = ref(false)
const catalogoCursos = ref([])
const carregandoCatalogo = ref(true)
const comprandoCatalogo = ref(false)
const msgCatalogo = ref('')
const msgCatalogoSucesso = ref(false)

loadCatalogo()

async function loadCatalogo() {
  try {
    const res = await fetch('/api/cursos')
    if (res.ok) catalogoCursos.value = await res.json()
  } catch (e) {
    msgCatalogo.value = 'Não foi possível carregar o catálogo.'
  } finally {
    carregandoCatalogo.value = false
  }
}

function cursoAdquirido(titulo) {
  return props.aluno.cursosAdquiridos?.some(curso => curso.toLowerCase() === titulo.toLowerCase()) ?? false
}

function cursoDoCatalogo(nome) {
  return catalogoCursos.value.find(curso => curso.titulo.toLowerCase() === nome.toLowerCase())
}

async function adquirirCurso(titulo) {
  comprandoCatalogo.value = true
  msgCatalogo.value = ''
  try {
    const res = await fetch(`/api/alunos/${props.aluno.id}/trocar-moedas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nomeCurso: titulo })
    })
    if (!res.ok) {
      msgCatalogo.value = res.status === 400
        ? 'Saldo insuficiente para adquirir este curso.'
        : 'Não foi possível adquirir o curso.'
      msgCatalogoSucesso.value = false
      return
    }
    emit('atualizar', await res.json())
    msgCatalogo.value = `Curso "${titulo}" adquirido. Já pode assistir ao vídeo e fazer a avaliação.`
    msgCatalogoSucesso.value = true
  } catch (e) {
    msgCatalogo.value = 'Erro de conexão com o servidor.'
    msgCatalogoSucesso.value = false
  } finally {
    comprandoCatalogo.value = false
  }
}

async function trocarMoedas() {
  if (!nomeCurso.value.trim()) {
    msgTroca.value = 'Digite o nome do curso.'
    msgTrocaSucesso.value = false
    return
  }

  trocando.value = true
  msgTroca.value = ''

  try {
    const res = await fetch(`/api/alunos/${props.aluno.id}/trocar-moedas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nomeCurso: nomeCurso.value.trim() })
    })

    if (!res.ok) {
      const err = await res.text()
      msgTroca.value = err.includes('insuficiente')
        ? 'Saldo insuficiente para trocar por este curso.'
        : 'Erro ao realizar a troca.'
      msgTrocaSucesso.value = false
      return
    }

    const alunoAtualizado = await res.json()
    msgTroca.value = `✅ "${nomeCurso.value}" adquirido com sucesso!`
    msgTrocaSucesso.value = true
    nomeCurso.value = ''
    emit('atualizar', alunoAtualizado)
  } catch (e) {
    msgTroca.value = 'Erro de conexão com o servidor.'
    msgTrocaSucesso.value = false
  } finally {
    trocando.value = false
  }
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  width: 100%;
  background: #1a1a1a;
  display: flex;
  flex-direction: column;
}

.header {
  background: #111111;
  border-bottom: 2px solid #f05472;
  padding: 14px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-coin {
  width: 36px;
  height: 36px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-5px); }
}

.header-title {
  font-size: 1.6rem;
  color: #ffffff;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-ranking,
.btn-acessar,
.btn-comprar {
  border-radius: 50px;
  padding: 8px 16px;
  font-weight: 700;
  transition: transform 0.15s, background 0.2s;
}

.btn-ranking {
  background: #f5c842;
  color: #1a1a1a;
}

.btn-acessar,
.btn-comprar {
  background: #f05472;
  color: #fff;
  white-space: nowrap;
}

.btn-ranking:hover,
.btn-acessar:hover:not(:disabled),
.btn-comprar:hover:not(:disabled) {
  transform: translateY(-2px);
}

.btn-acessar:disabled,
.btn-comprar:disabled {
  background: #bbb;
  cursor: not-allowed;
}

.header-name {
  color: #cccccc;
  font-size: 1rem;
}

.btn-sair {
  background: transparent;
  border: 1.5px solid #f05472;
  color: #f05472;
  border-radius: 50px;
  padding: 6px 18px;
  font-family: 'FredokaOne', sans-serif;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-sair:hover {
  background: #f05472;
  color: #fff;
}

.saldo-section {
  padding: 24px 32px 0;
}

.saldo-card {
  background: linear-gradient(135deg, #f5c842, #e8a800);
  border-radius: 16px;
  padding: 20px 28px;
  display: flex;
  align-items: center;
  gap: 18px;
  max-width: 360px;
  box-shadow: 0 4px 20px rgba(245, 200, 66, 0.3);
}

.saldo-coin {
  width: 56px;
  height: 56px;
}

.saldo-label {
  font-size: 0.9rem;
  color: rgba(0,0,0,0.6);
  letter-spacing: 0.5px;
}

.saldo-valor {
  font-size: 2rem;
  color: #1a1a1a;
  font-weight: 700;
  line-height: 1;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 24px 32px 40px;
}

@media (max-width: 700px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

.card {
  background: #ffffff;
  border-radius: 20px;
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 1.3rem;
  color: #1a1a1a;
}

.section-sub {
  font-size: 0.95rem;
  color: #666;
  margin-top: -8px;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.campo label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
}

.btn-trocar {
  background: #f05472;
  color: #ffffff;
  font-size: 1.05rem;
  border-radius: 50px;
  padding: 12px;
  transition: background 0.2s, transform 0.15s;
  font-family: 'FredokaOne', sans-serif;
}

.btn-trocar:hover:not(:disabled) {
  background: #d63f5e;
  transform: translateY(-2px);
}

.btn-trocar:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.aviso-saldo {
  font-size: 0.85rem;
  color: #e05a00;
  text-align: center;
}

.msg {
  font-size: 0.9rem;
  border-radius: 8px;
  padding: 10px 14px;
  text-align: center;
}

.msg-ok {
  background: #f0fff4;
  border: 1px solid #38a169;
  color: #276749;
}

.msg-erro {
  background: #fff0f3;
  border: 1px solid #f05472;
  color: #c0234b;
}

.cursos-lista {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.curso-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff8e1;
  border: 1px solid #f5c842;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 0.95rem;
  color: #333;
  transition: transform 0.15s;
}

.curso-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.curso-info small {
  color: #777;
}

.catalogo-card {
  grid-column: 1 / -1;
}

.catalogo-lista {
  display: grid;
  gap: 12px;
}

.catalogo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.catalogo-item h3 {
  color: #222;
  margin-bottom: 6px;
}

.catalogo-item p {
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 580px) {
  .catalogo-item,
  .curso-info {
    align-items: flex-start;
    flex-direction: column;
  }
}

.curso-item:hover {
  transform: translateX(4px);
}

.curso-coin {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.cursos-vazio {
  text-align: center;
  padding: 32px 0;
  color: #999;
}

.cursos-vazio p {
  font-size: 1rem;
}

.cursos-vazio-sub {
  font-size: 0.85rem;
  margin-top: 6px;
  color: #bbb;
}
</style>
