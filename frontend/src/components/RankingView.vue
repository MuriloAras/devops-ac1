<template>
  <div class="overlay" @click.self="$emit('fechar')">
    <section class="ranking-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-ranking">
      <header class="ranking-header">
        <div>
          <p class="eyebrow">SUPER CURSOS</p>
          <h1 id="titulo-ranking">🏆 Ranking de alunos</h1>
          <p class="subtitulo">Soma das melhores notas em cada curso.</p>
        </div>
        <button class="btn-fechar" aria-label="Fechar ranking" @click="$emit('fechar')">×</button>
      </header>

      <p v-if="carregando" class="estado">Carregando ranking...</p>
      <p v-else-if="erro" class="estado erro">{{ erro }}</p>
      <div v-else-if="ranking.length" class="tabela-wrapper">
        <table>
          <thead><tr><th>Posição</th><th>Aluno</th><th>Pontos</th></tr></thead>
          <tbody>
            <tr v-for="linha in ranking" :key="linha.alunoId" :class="{ primeiro: linha.posicao === 1 }">
              <td><span class="medalha">{{ medalha(linha.posicao) }}</span>{{ linha.posicao }}º</td>
              <td>{{ linha.nome }}</td>
              <td class="pontos">{{ linha.pontos.toLocaleString('pt-BR') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else class="estado">O ranking será preenchido quando os alunos concluírem avaliações.</p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'

defineEmits(['fechar'])

const ranking = ref([])
const carregando = ref(true)
const erro = ref('')

onMounted(async () => {
  try {
    const res = await fetch('/api/ranking')
    if (!res.ok) throw new Error()
    ranking.value = await res.json()
  } catch (e) {
    erro.value = 'Não foi possível carregar o ranking.'
  } finally {
    carregando.value = false
  }
})

function medalha(posicao) {
  if (posicao === 1) return '🥇'
  if (posicao === 2) return '🥈'
  if (posicao === 3) return '🥉'
  return '🏅'
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(10, 10, 14, 0.72);
}

.ranking-modal {
  width: min(760px, 100%);
  max-height: min(85vh, 800px);
  overflow: auto;
  padding: clamp(22px, 4vw, 36px);
  border-radius: 22px;
  background: #fff;
  color: #202124;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.3);
}

.ranking-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.eyebrow {
  margin-bottom: 6px;
  color: #bf3756;
  font-size: 0.75rem;
  letter-spacing: 0.12em;
}

.ranking-header h1 {
  font-size: clamp(1.35rem, 3vw, 1.8rem);
}

.subtitulo,
.estado {
  margin-top: 8px;
  color: #666;
}

.btn-fechar {
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #f2f2f4;
  color: #444;
  font-size: 1.7rem;
  line-height: 1;
}

.tabela-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  color: #666;
  font-size: 0.85rem;
}

td:first-child {
  width: 130px;
}

.medalha {
  margin-right: 8px;
}

.pontos {
  text-align: right;
  font-weight: 700;
}

th:last-child {
  text-align: right;
}

.primeiro {
  background: #fff8df;
}

.erro {
  color: #a92745;
}
</style>
