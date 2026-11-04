# Desafio 3 — Serviço Python de IA demonstrativo

O Core Java consome o serviço FastAPI por HTTP. O serviço apresenta recomendação determinística e uma busca local de trechos do material de Python para demonstrar o limite de RAG. Não há modelo LLM, embeddings, base vetorial ou serviço externo pago; a implementação é um mini-lab repetível sem chave secreta.

Rotas no Core:

- `GET /api/ia/recomendacoes`
- `POST /api/ia/assistente`, corpo `{"pergunta":"Para que serve print no Python?"}`

Se o serviço Python não responder, o Core retorna uma resposta alternativa limitada e incrementa `supercursos_ai_fallback_total`. Os fluxos de cadastro, compra e avaliação não dependem do serviço de IA.

Evidências: testes Python em `ai-service/tests/`, testes Java de contingência em `AiServiceClientTest`, serviço `ai-service` no Compose e scrape Prometheus `/metrics`.
