package com.example.grupo34_atdd.modules.cursos.application;

import com.example.grupo34_atdd.modules.cursos.domain.CursoComAvaliacao;
import com.example.grupo34_atdd.modules.cursos.domain.QuestaoQuiz;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CatalogoCursos {

    public static final String SLUG_PYTHON = "introducao-python";
    public static final String TITULO_PYTHON = "Introdução ao Python";
    private static final String VIDEO_PYTHON = "https://www.youtube-nocookie.com/embed/g_R_Asf6Co0";

    private final Map<String, CursoComAvaliacao> cursos = Map.of(
            SLUG_PYTHON, criarCursoPython()
    );

    public List<CursoComAvaliacao> listar() {
        return List.copyOf(cursos.values());
    }

    public CursoComAvaliacao buscar(String slug) {
        return cursos.get(slug);
    }

    private static CursoComAvaliacao criarCursoPython() {
        return new CursoComAvaliacao(SLUG_PYTHON, TITULO_PYTHON,
                "Primeiros passos com Python, VS Code, comandos, variáveis e operações.", VIDEO_PYTHON,
                List.of(
                        q("q1", "O que é Python?", List.of("Uma linguagem de programação", "Um banco de dados", "Um sistema operacional", "Um navegador"), 0),
                        q("q2", "Qual extensão é usada nos arquivos de código Python?", List.of(".java", ".py", ".html", ".sql"), 1),
                        q("q3", "Qual comando é apresentado para exibir informações no terminal?", List.of("show()", "echo()", "print()", "display()"), 2),
                        q("q4", "Como textos são escritos nos exemplos de código Python?", List.of("Entre aspas", "Entre colchetes", "Com o prefixo #", "Sem nenhum delimitador"), 0),
                        q("q5", "Em que ordem Python executa comandos de um arquivo, conforme a aula?", List.of("De baixo para cima", "Em ordem aleatória", "De cima para baixo", "Somente os comandos print"), 2),
                        q("q6", "Como passar mais de uma informação para print na mesma linha?", List.of("Separando por vírgulas", "Usando ponto e vírgula", "Usando barras", "Criando um arquivo novo"), 0),
                        q("q7", "Qual símbolo inicia um comentário em Python?", List.of("//", "<!--", "#", "**"), 2),
                        q("q8", "O que acontece com o texto de um comentário durante a execução?", List.of("É executado como comando", "É ignorado pelo Python", "É exibido sempre", "É transformado em variável"), 1),
                        q("q9", "No exemplo da aula, qual operação calcula o lucro a partir de faturamento e custo?", List.of("Faturamento + custo", "Faturamento - custo", "Faturamento × custo", "Custo ÷ faturamento"), 1),
                        q("q10", "Um valor como 750,1 é classificado como qual tipo no exemplo?", List.of("Inteiro", "Texto", "Booleano", "Número decimal"), 3)
                ));
    }

    private static QuestaoQuiz q(String id, String enunciado, List<String> alternativas, int correta) {
        return new QuestaoQuiz(id, enunciado, alternativas, correta);
    }
}
