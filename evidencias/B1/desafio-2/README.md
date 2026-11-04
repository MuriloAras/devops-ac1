# Desafio 2 — Porta de persistência do Ranking

O caso de uso do Ranking e o caso de avaliação dependem de `ResultadoAvaliacaoPort`, definido no módulo `ranking/domain`. `ResultadoAvaliacaoJpaAdapter` traduz esse contrato para Spring Data/JPA. Os testes unitários substituem a porta por mocks; o teste PostgreSQL usa o adaptador real. Assim, a regra/caso de uso pode ser testado sem banco e a persistência pode ser trocada sem alterar o contrato do Core.

Evidências executáveis:

- `mvn test`: testes unitários dos casos de uso com a porta substituída.
- `mvn verify`: integração PostgreSQL confirma que o adaptador JPA persiste o recorde e expõe os pontos no ranking.

O mini-lab aplica Ports & Adapters ao limite que precisava de substituição. Não transforma todos os módulos em uma implementação completa de DDD/Clean Architecture: essa estrutura adicional não é necessária para testar a hipótese.
