package com.example.grupo34_atdd.repository;

import com.example.grupo34_atdd.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.entity.AlunoEntity;
import com.example.grupo34_atdd.service.AlunoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"homol", "integration"})
@Import(AlunoPostgresIT.PostgresConfiguration.class)
class AlunoPostgresIT {

    @Autowired
    private AlunoRepository repository;

    @Autowired
    private AlunoService service;

    @Autowired
    private DataSource dataSource;

    @LocalServerPort
    private int port;

    @BeforeEach
    void limparDadosDoContainerDeTeste() {
        repository.deleteAll();
    }

    @Test
    void deveUsarPostgres16Real() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
            assertEquals(16, connection.getMetaData().getDatabaseMajorVersion());
            assertTrue(connection.getCatalog().startsWith("supercursos_test"));
        }
    }

    @Test
    void devePersistirAlunoECursosNaTabelaDeRelacionamento() {
        AlunoEntity aluno = new AlunoEntity("Pedro", "persistencia@example.test", "senha-teste", 5);
        aluno.getCursosAdquiridos().add("DevOps");

        AlunoEntity salvo = repository.saveAndFlush(aluno);
        AlunoEntity recarregado = repository.findByEmail("persistencia@example.test").orElseThrow();

        assertNotNull(salvo.getId());
        assertEquals(salvo.getId(), recarregado.getId());
        assertEquals(5, recarregado.getSaldoMoedas());
        assertEquals(List.of("DevOps"), recarregado.getCursosAdquiridos());
        assertTrue(repository.existsByEmail("persistencia@example.test"));
        assertFalse(repository.existsByEmail("inexistente@example.test"));
    }

    @ParameterizedTest(name = "Saldo inicial {0}, saldo persistido apos a troca {1}")
    @CsvSource({"3, 0", "5, 2"})
    void devePersistirTrocaComSaldoSuficiente(int saldoInicial, int saldoEsperado) {
        AlunoResponseDTO aluno = cadastrar("troca@example.test", saldoInicial);

        AlunoResponseDTO resposta = service.trocarMoedas(aluno.getId(), "Java Avancado");
        AlunoEntity persistido = repository.findById(aluno.getId()).orElseThrow();

        assertEquals(saldoEsperado, resposta.getSaldoMoedas());
        assertEquals(saldoEsperado, persistido.getSaldoMoedas());
        assertEquals(List.of("Java Avancado"), persistido.getCursosAdquiridos());
    }

    @Test
    void deveManterSaldoECursosQuandoTrocaForRecusada() {
        AlunoResponseDTO aluno = cadastrar("sem-saldo@example.test", 2);

        ResponseStatusException falha = assertThrows(ResponseStatusException.class,
                () -> service.trocarMoedas(aluno.getId(), "Java Avancado"));

        AlunoEntity persistido = repository.findById(aluno.getId()).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, falha.getStatusCode());
        assertEquals(2, persistido.getSaldoMoedas());
        assertTrue(persistido.getCursosAdquiridos().isEmpty());
    }

    @Test
    void deveAplicarRestricaoDeEmailUnicoNoPostgres() {
        cadastrar("unico@example.test", 3);

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new AlunoEntity(
                        "Outro aluno", "unico@example.test", "senha-teste", 5)));

        assertEquals(1, repository.count());
        assertEquals(3, repository.findByEmail("unico@example.test").orElseThrow().getSaldoMoedas());
    }

    @Test
    void deveExporSaudeEMetricasComBancoDisponivel() throws Exception {
        HttpResponse<String> health = get("/actuator/health");
        assertEquals(200, health.statusCode());
        assertTrue(health.body().contains("\"status\":\"UP\""));

        HttpResponse<String> prometheus = get("/actuator/prometheus");
        assertEquals(200, prometheus.statusCode());
        assertTrue(prometheus.body().contains("jvm_memory_used_bytes"));
        assertTrue(prometheus.body().contains("application=\"supercursos\""));
        assertTrue(prometheus.body().contains("environment=\"homol\""));
    }

    @Test
    void deveGerarOpenApiCompativelComSpringBoot4() throws Exception {
        HttpResponse<String> apiDocs = get("/v3/api-docs");
        assertEquals(200, apiDocs.statusCode());
        assertTrue(apiDocs.body().contains("/api/alunos/{id}/trocar-moedas"));
        assertTrue(apiDocs.body().contains("/api/alunos/login"));
    }

    private AlunoResponseDTO cadastrar(String email, int saldo) {
        return service.cadastrar(new AlunoRequestDTO("Aluno de teste", email, "senha-teste", saldo));
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .GET().build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("supercursos_test")
                    .withUsername("supercursos_test")
                    .withPassword("test-only-password");
        }
    }
}
