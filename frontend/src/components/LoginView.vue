<template>
  <div class="login-page">
    <!-- Logo -->
    <div class="logo">
      <span class="logo-text">Super</span>
      <img src="../assets/coin.png" alt="moeda" class="logo-coin" />
      <span class="logo-text">Cursos</span>
    </div>

    <!-- Card de Login -->
    <div class="login-card">
      <p v-if="erro" class="erro-msg">{{ erro }}</p>

      <div class="campo">
        <label for="email">E-mail</label>
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="seu@email.com"
          @keydown.enter="fazerLogin"
        />
      </div>

      <div class="campo">
        <label for="senha">Senha</label>
        <input
          id="senha"
          v-model="senha"
          type="password"
          placeholder="••••••••"
          @keydown.enter="fazerLogin"
        />
      </div>

      <button
        class="btn-login"
        :disabled="carregando"
        @click="fazerLogin"
      >
        {{ carregando ? 'Entrando...' : 'Fazer Login' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['login-sucesso'])

const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)

async function fazerLogin() {
  if (!email.value || !senha.value) {
    erro.value = 'Por favor, preencha e-mail e senha.'
    return
  }

  erro.value = ''
  carregando.value = true

  try {
    const res = await fetch('/api/alunos/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, senha: senha.value })
    })

    if (!res.ok) {
      erro.value = 'E-mail ou senha inválidos. Tente novamente.'
      return
    }

    const aluno = await res.json()
    emit('login-sucesso', aluno)
  } catch (e) {
    erro.value = 'Erro de conexão com o servidor. Verifique se o backend está rodando.'
  } finally {
    carregando.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  width: 100%;
  padding: 40px 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-family: 'FredokaOne', sans-serif;
  font-size: 2.4rem;
  color: #ffffff;
  letter-spacing: 1px;
}

.logo-coin {
  width: 52px;
  height: 52px;
  object-fit: contain;
  filter: drop-shadow(0 2px 6px rgba(245, 200, 66, 0.5));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-6px); }
}

.login-card {
  background: #ffffff;
  border: 2.5px solid #f05472;
  border-radius: 20px;
  padding: 40px 44px;
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 8px 32px rgba(240, 84, 114, 0.15);
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.campo label {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a1a;
}

.btn-login {
  background: #f05472;
  color: #ffffff;
  font-size: 1.1rem;
  font-weight: 700;
  border-radius: 50px;
  padding: 13px;
  margin-top: 6px;
  transition: background 0.2s, transform 0.15s;
  letter-spacing: 0.5px;
}

.btn-login:hover:not(:disabled) {
  background: #d63f5e;
  transform: translateY(-2px);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.erro-msg {
  background: #fff0f3;
  border: 1px solid #f05472;
  border-radius: 8px;
  color: #c0234b;
  font-size: 0.9rem;
  padding: 10px 14px;
  text-align: center;
}
</style>
