<template>
  <div>
    <LoginView
      v-if="!alunoLogado"
      @login-sucesso="onLoginSucesso"
    />
    <DashboardView
      v-else-if="!cursoSelecionado"
      :aluno="alunoLogado"
      @sair="onSair"
      @atualizar="onAtualizar"
      @acessar-curso="onAcessarCurso"
      @abrir-ranking="onAbrirRanking"
    />
    <CursoView
      v-if="alunoLogado && cursoSelecionado"
      :aluno="alunoLogado"
      :curso-slug="cursoSelecionado"
      @voltar="cursoSelecionado = null"
      @pontuacao-atualizada="onPontuacaoAtualizada"
    />
    <RankingView
      v-if="alunoLogado && rankingAberto"
      @fechar="rankingAberto = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginView from './components/LoginView.vue'
import DashboardView from './components/DashboardView.vue'
import CursoView from './components/CursoView.vue'
import RankingView from './components/RankingView.vue'

const alunoLogado = ref(null)
const cursoSelecionado = ref(null)
const rankingAberto = ref(false)

function onLoginSucesso(aluno) {
  alunoLogado.value = aluno
}

function onSair() {
  alunoLogado.value = null
  cursoSelecionado.value = null
  rankingAberto.value = false
}

function onAtualizar(alunoAtualizado) {
  alunoLogado.value = alunoAtualizado
}

function onAcessarCurso(slug) {
  cursoSelecionado.value = slug
  rankingAberto.value = false
}

function onAbrirRanking() {
  rankingAberto.value = true
  cursoSelecionado.value = null
}

function onPontuacaoAtualizada() {
  rankingAberto.value = false
}
</script>
