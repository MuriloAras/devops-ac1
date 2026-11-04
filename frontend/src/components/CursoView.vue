<template>
  <main class="curso-page">
    <header class="curso-header">
      <button class="btn-voltar" @click="$emit('voltar')">← Meus cursos</button>
      <div>
        <p class="eyebrow">SUPER CURSOS</p>
        <h1>{{ curso?.titulo || 'Curso' }}</h1>
        <p v-if="curso" class="descricao">{{ curso.descricao }}</p>
      </div>
      <span v-if="resultado" class="pontos-atual">🏆 {{ alunoPontuacao }} pontos no ranking</span>
    </header>

    <p v-if="erro" class="alerta erro">{{ erro }}</p>
    <p v-else-if="carregando" class="alerta">Carregando vídeo e avaliação...</p>

    <template v-else-if="curso">
      <section class="painel-video" data-testid="course-video">
        <div class="video-wrapper">
          <iframe
            :src="curso.videoUrl"
            :title="`Vídeo do curso ${curso.titulo}`"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            referrerpolicy="strict-origin-when-cross-origin"
            allowfullscreen
          ></iframe>
        </div>
        <p>Assista à aula e responda às 10 questões sobre o conteúdo.</p>
      </section>

      <form class="avaliacao" data-testid="course-quiz" @submit.prevent="enviarAvaliacao">
        <div class="avaliacao-topo">
          <div>
            <p class="eyebrow">AVALIAÇÃO DO CURSO</p>
            <h2>Teste o que você aprendeu</h2>
          </div>
          <span>10 questões · 100 pontos por acerto</span>
        </div>

        <fieldset v-for="(questao, indice) in curso.questoes" :key="questao.id" class="questao">
          <legend><span class="numero">{{ indice + 1 }}</span>{{ questao.enunciado }}</legend>
          <label v-for="(alternativa, alternativaIndice) in questao.alternativas"
                 :key="alternativaIndice" class="alternativa">
            <input v-model.number="respostas[indice]" type="radio" :name="questao.id" :value="alternativaIndice" />
            <span>{{ alternativa }}</span>
          </label>
        </fieldset>

        <p v-if="erroAvaliacao" class="alerta erro">{{ erroAvaliacao }}</p>
        <section v-if="resultado" class="resultado">
          <p class="resultado-atual">Nesta tentativa: <strong>{{ resultado.acertosNestaTentativa }}/10</strong> corretas, <strong>{{ resultado.pontosNestaTentativa }}</strong> pontos.</p>
          <p>Melhor resultado neste curso: <strong>{{ resultado.melhorPontuacaoNoCurso }}</strong> pontos.</p>
          <p>Total considerado no ranking: <strong>{{ resultado.pontuacaoTotalNoRanking }}</strong> pontos.</p>
          <p class="resultado-nota">O ranking soma a melhor pontuação de cada curso. Você pode refazer a avaliação; uma nota menor não reduz seu recorde.</p>
        </section>
        <button class="btn-enviar" data-testid="submit-quiz" type="submit" :disabled="enviando">
          {{ enviando ? 'Corrigindo...' : resultado ? 'Refazer avaliação' : 'Enviar respostas' }}
        </button>
      </form>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const props = defineProps({
  aluno: { type: Object, required: true },
  cursoSlug: { type: String, required: true }
})

const emit = defineEmits(['voltar', 'pontuacao-atualizada'])
const curso = ref(null)
const carregando = ref(true)
const enviando = ref(false)
const erro = ref('')
const erroAvaliacao = ref('')
const respostas = ref(Array(10).fill(null))
const resultado = ref(null)
const alunoPontuacao = computed(() => resultado.value?.pontuacaoTotalNoRanking ?? 0)

onMounted(carregarCurso)

async function carregarCurso() {
  try {
    const res = await fetch(`/api/alunos/${props.aluno.id}/cursos/${props.cursoSlug}`)
    if (!res.ok) {
      erro.value = res.status === 403 ? 'Você precisa adquirir este curso para acessá-lo.' : 'Não foi possível carregar o curso.'
      return
    }
    curso.value = await res.json()
  } catch (e) {
    erro.value = 'Erro de conexão ao carregar o curso.'
  } finally {
    carregando.value = false
  }
}

async function enviarAvaliacao() {
  erroAvaliacao.value = ''
  if (respostas.value.some(resposta => resposta === null)) {
    erroAvaliacao.value = 'Responda às 10 questões antes de enviar.'
    return
  }

  enviando.value = true
  try {
    const res = await fetch(`/api/alunos/${props.aluno.id}/cursos/${props.cursoSlug}/avaliacoes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ respostas: respostas.value })
    })
    if (!res.ok) {
      erroAvaliacao.value = 'Não foi possível corrigir a avaliação. Tente novamente.'
      return
    }
    resultado.value = await res.json()
    emit('pontuacao-atualizada')
  } catch (e) {
    erroAvaliacao.value = 'Erro de conexão ao enviar as respostas.'
  } finally {
    enviando.value = false
  }
}
</script>

<style scoped>
.curso-page {
  min-height: 100vh;
  width: 100%;
  padding: 28px clamp(18px, 5vw, 72px) 56px;
  background: #f3f4f7;
  color: #202124;
}

.curso-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
  margin: 0 auto 28px;
  max-width: 1120px;
}

.btn-voltar {
  padding: 10px 16px;
  border-radius: 24px;
  color: #c43454;
  background: #fff;
  border: 1px solid #f05472;
}

.eyebrow {
  margin-bottom: 6px;
  color: #bf3756;
  font-size: 0.75rem;
  letter-spacing: 0.12em;
}

.curso-header h1,
.avaliacao h2 {
  font-size: clamp(1.4rem, 3vw, 2rem);
}

.descricao {
  color: #666;
  margin-top: 6px;
}

.pontos-atual {
  color: #735500;
  font-weight: 700;
}

.painel-video,
.avaliacao {
  max-width: 1120px;
  margin: 0 auto 24px;
  padding: clamp(18px, 3vw, 32px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(27, 32, 43, 0.08);
}

.video-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 14px;
  background: #111;
}

.video-wrapper iframe {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 0;
}

.painel-video > p {
  color: #666;
  margin-top: 14px;
}

.avaliacao-topo {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8ec;
}

.avaliacao-topo > span {
  color: #666;
}

.questao {
  border: 0;
  border-bottom: 1px solid #eee;
  padding: 22px 0;
  min-width: 0;
}

.questao legend {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-weight: 700;
  line-height: 1.45;
  width: 100%;
}

.numero {
  display: inline-grid;
  place-items: center;
  min-width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff1f4;
  color: #c43454;
}

.alternativa {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 12px 0 0 38px;
  cursor: pointer;
  color: #444;
}

.alternativa input {
  accent-color: #f05472;
  width: auto;
  margin-top: 4px;
}

.btn-enviar {
  width: 100%;
  margin-top: 22px;
  padding: 14px;
  color: #fff;
  background: #f05472;
  border-radius: 28px;
  font-size: 1rem;
}

.btn-enviar:disabled {
  opacity: 0.65;
  cursor: wait;
}

.resultado {
  margin-top: 20px;
  padding: 18px;
  border-radius: 12px;
  background: #fff8df;
  color: #4b3b08;
  line-height: 1.7;
}

.resultado-nota {
  color: #725d19;
  font-size: 0.9rem;
}

.alerta {
  max-width: 1120px;
  margin: 20px auto;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.erro {
  color: #a92745;
  background: #fff0f3;
}

@media (max-width: 760px) {
  .curso-header {
    grid-template-columns: 1fr;
    align-items: start;
    gap: 14px;
  }

  .avaliacao-topo {
    align-items: start;
    flex-direction: column;
  }
}
</style>
