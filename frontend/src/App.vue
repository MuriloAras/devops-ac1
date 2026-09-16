<template>
  <div>
    <LoginView
      v-if="!alunoLogado"
      @login-sucesso="onLoginSucesso"
    />
    <DashboardView
      v-else
      :aluno="alunoLogado"
      @sair="onSair"
      @atualizar="onAtualizar"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginView from './components/LoginView.vue'
import DashboardView from './components/DashboardView.vue'

const alunoLogado = ref(null)

function onLoginSucesso(aluno) {
  alunoLogado.value = aluno
}

function onSair() {
  alunoLogado.value = null
}

function onAtualizar(alunoAtualizado) {
  alunoLogado.value = alunoAtualizado
}
</script>
