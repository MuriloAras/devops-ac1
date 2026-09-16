<template>
  <div class="dashboard-page">
    <!-- Header -->
    <header class="header">
      <div class="header-logo">
        <img src="../assets/coin.png" alt="moeda" class="header-coin" />
        <span class="header-title">Super Cursos</span>
      </div>
      <div class="header-info">
        <span class="header-name">Olá, {{ aluno.nome }} 👋</span>
        <button class="btn-sair" @click="$emit('sair')">Sair</button>
      </div>
    </header>

    <!-- Saldo de Moedas -->
    <section class="saldo-section">
      <div class="saldo-card">
        <img src="../assets/coin.png" alt="moeda" class="saldo-coin" />
        <div>
          <p class="saldo-label">Seu saldo</p>
          <p class="saldo-valor">{{ aluno.saldoMoedas }} moedas</p>
        </div>
      </div>
    </section>

    <!-- Conteúdo principal -->
    <main class="main-content">
      <!-- Trocar Moedas -->
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

      <!-- Cursos Adquiridos -->
      <section class="card cursos-card">
        <h2 class="section-title">📚 Meus Cursos</h2>

        <div v-if="aluno.cursosAdquiridos && aluno.cursosAdquiridos.length > 0" class="cursos-lista">
          <div
            v-for="(curso, i) in aluno.cursosAdquiridos"
            :key="i"
            class="curso-item"
          >
            <img src="../assets/coin.png" alt="moeda" class="curso-coin" />
            <span>{{ curso }}</span>
          </div>
        </div>

        <div v-else class="cursos-vazio">
          <p>Você ainda não adquiriu nenhum curso.</p>
          <p class="cursos-vazio-sub">Troque suas moedas e comece a aprender!</p>
        </div>
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

const emit = defineEmits(['sair', 'atualizar'])

const nomeCurso = ref('')
const trocando = ref(false)
const msgTroca = ref('')
const msgTrocaSucesso = ref(false)

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

/* Header */
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

/* Saldo */
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

/* Main */
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

/* Cards */
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

/* Cursos */
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
