import com.example.grupo34_atdd.domain.Aluno;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class AlunoTest {
    @Test
    void deveTrocarMoedasPorCursoQuandoSaldoSuficiente() {
        // Dado que tenha 3 moedas disponíveis
        Aluno aluno = new Aluno(3);

        // Quando selecionar o curso e clicar em "Trocar"
        aluno.trocarMoedasPorCurso("Java Avançado");

        // Então o curso deve ser adicionado e as moedas devem ser removidas
        assertTrue(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(0, aluno.getSaldoMoedas());
    }
    @Test
    void naoDeveTrocarMoedasPorCursoQuandoSaldoInsuficiente() {
        // Dado que tenha 2 moedas disponíveis
        Aluno aluno = new Aluno(2);

        // Quando selecionar o curso e clicar em "Trocar"
        aluno.trocarMoedasPorCurso("Java Avançado");

        // Então o curso não deve ser adicionado e as moedas não devem ser removidas
        assertFalse(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(2, aluno.getSaldoMoedas());
    }


}
