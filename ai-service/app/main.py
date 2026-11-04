from collections import Counter
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from prometheus_client import Counter as PromCounter, make_asgi_app

app = FastAPI(title="Super Cursos AI Lab", version="0.1.0")
app.mount("/metrics", make_asgi_app())
requests_total = PromCounter("ai_lab_requests_total", "AI lab requests", ["operation"])

COURSE_TEXT = [
    ("python-intro", "Python é uma linguagem de programação. Arquivos Python usam a extensão .py. "
     "A função print mostra conteúdo na tela. Strings representam textos entre aspas. "
     "O código é executado de cima para baixo. Comentários começam com #."),
    ("python-vscode", "O VS Code pode editar arquivos .py. O terminal executa o programa Python. "
     "A função print aceita vários argumentos separados por vírgula."),
]


class Question(BaseModel):
    question: str = Field(min_length=3, max_length=500)


def retrieve(question: str) -> tuple[str, str]:
    words = {word.lower().strip(".,?!:;") for word in question.split() if len(word) > 2}
    ranked = sorted(COURSE_TEXT, key=lambda item: sum(word in item[1].lower() for word in words), reverse=True)
    if not ranked or not any(word in ranked[0][1].lower() for word in words):
        return "Não encontrei esse assunto no material deste curso. Tente perguntar sobre Python, print ou arquivos .py.", "sem_fonte"
    return ranked[0][1], ranked[0][0]


@app.get("/health")
def health():
    return {"status": "UP", "mode": "local-retrieval-demo"}


@app.get("/recommendations")
def recommendations():
    requests_total.labels(operation="recommendations").inc()
    return {"courses": ["introducao-python"], "mode": "rule-based-demo"}


@app.post("/assist")
def assist(request: Question):
    requests_total.labels(operation="assist").inc()
    answer, source = retrieve(request.question)
    return {"answer": answer, "source": source, "mode": "retrieval-demo"}
