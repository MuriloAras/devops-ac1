import com.example.grupo34_atdd.domain.Aluno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlunoTest {

    @Test
    @DisplayName("Cenário Murilo - Deve trocar moedas por curso quando saldo suficiente (3 moedas)")
    void deveTrocarMoedasPorCursoQuandoSaldoSuficiente() {
        // Arrange (Dado que tenha 3 moedas disponíveis)
        Aluno aluno = new Aluno(3);

        // Act (Quando selecionar o curso e clicar em 'Trocar')
        aluno.trocarMoedasPorCurso("Java Avançado");

        // Assert (Então o curso deve ser adicionado e as moedas removidas)
        assertTrue(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(0, aluno.getSaldoMoedas());
    }

    @Test
    @DisplayName("Cenário Beatriz - Não deve trocar moedas quando saldo for insuficiente (2 moedas)")
    void naoDeveTrocarMoedasPorCursoQuandoSaldoInsuficiente() {
        // Arrange (Dado que tenha 2 moedas disponíveis)
        Aluno aluno = new Aluno(2);

        // Act (Quando selecionar o curso e clicar em 'Trocar')
        aluno.trocarMoedasPorCurso("Java Avançado");

        // Assert (Então o curso não deve ser adicionado e as moedas permanecem)
        assertFalse(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(2, aluno.getSaldoMoedas());
    }

    @Test
    @DisplayName("Cenário Pedro - Deve trocar moedas por curso quando saldo superior (5 moedas)")
    void deveTrocarMoedasPorCursoQuandoSaldoSuperior() {
        // Arrange (Dado que tenha 5 moedas disponíveis)
        Aluno aluno = new Aluno(5);

        // Act (Quando selecionar o curso e clicar em 'Trocar')
        aluno.trocarMoedasPorCurso("Java Avançado");

        // Assert (Então o curso deve ser adicionado e apenas 3 moedas removidas)
        assertTrue(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(2, aluno.getSaldoMoedas());
    }
}
