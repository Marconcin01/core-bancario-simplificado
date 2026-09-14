# Core Bancário Simplificado

[![CI](https://github.com/Marconcin01/core-bancario-simplificado/actions/workflows/ci.yml/badge.svg)](https://github.com/Marconcin01/core-bancario-simplificado/actions/workflows/ci.yml)

API RESTful de um núcleo bancário simplificado, desenvolvida como projeto de
portfólio para demonstrar boas práticas de back-end com Java e Spring Boot.

Documentação interativa dos endpoints via Swagger UI, disponível em
`http://localhost:8080/swagger-ui.html` com a aplicação rodando.

## Funcionalidades

- Cadastro de clientes.
- Abertura de contas bancárias com saldo inicial (por cliente).
- Consulta de saldo de uma conta.
- Consulta de extrato (histórico de lançamentos) paginado de uma conta.
- Transferência de valores entre contas, com validação estrita de saldo
  disponível e consistência garantida por transação (`@Transactional`).

## Stack técnica

| Camada          | Tecnologia                          |
|-----------------|--------------------------------------|
| Linguagem       | Java 17+ (compilado/testado em 21)   |
| Framework       | Spring Boot 3.3                      |
| Persistência    | Spring Data JPA / Hibernate          |
| Banco de dados  | PostgreSQL                           |
| Validação       | Bean Validation (Jakarta Validation) |
| Boilerplate     | Lombok                               |
| Testes          | JUnit 5, Mockito, AssertJ            |

## Estrutura do projeto

```
core-bancario-simplificado/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/renan/corebancario/
    │   │   ├── CoreBancarioApplication.java
    │   │   ├── model/            # Entidades JPA: Cliente, Conta, Transacao, TipoTransacao
    │   │   ├── repository/       # Interfaces Spring Data JPA
    │   │   ├── dto/               # Records de request/response (entrada e saída da API)
    │   │   ├── service/           # Regras de negócio (ClienteService, ContaService, TransferenciaService)
    │   │   ├── controller/        # Endpoints REST
    │   │   └── exception/         # Exceções de negócio + GlobalExceptionHandler (@RestControllerAdvice)
    │   └── resources/
    │       └── application.properties
    └── test/
        ├── java/com/renan/corebancario/service/TransferenciaServiceTest.java
        └── resources/application-test.properties
```

### Por que essa organização?

O projeto segue uma arquitetura em camadas (*Controller → Service → Repository*),
comum em aplicações Spring Boot de médio porte:

- **`model`**: entidades JPA, mapeadas 1:1 com as tabelas do PostgreSQL.
- **`repository`**: apenas contratos de acesso a dados (Spring Data JPA gera a
  implementação). `ContaRepository` expõe um método com lock pessimista
  (`buscarPorIdComLock`), usado nas transferências para evitar condição de
  corrida.
- **`dto`**: a API nunca expõe as entidades JPA diretamente — os DTOs (como
  `record`s imutáveis) desacoplam o contrato da API do modelo de persistência
  e centralizam as regras de Bean Validation.
- **`service`**: contém toda a regra de negócio. Cada classe tem uma única
  responsabilidade (SRP): `ClienteService` cuida do cadastro de clientes,
  `ContaService` cuida do ciclo de vida da conta (abertura, saldo, extrato) e
  `TransferenciaService` isola a operação mais crítica do sistema.
- **`controller`**: apenas orquestra requisição → serviço → resposta HTTP; não
  contém lógica de negócio.
- **`exception`**: exceções de negócio específicas (`SaldoInsuficienteException`,
  `RecursoNaoEncontradoException`, `RegraDeNegocioException`) traduzidas para
  respostas HTTP padronizadas por um `@RestControllerAdvice` central.

## Como executar

### 1. Pré-requisitos

- JDK 17 ou superior
- Maven 3.8+
- PostgreSQL em execução localmente (ou em container)

### 2. Criar o banco de dados

```sql
CREATE DATABASE core_bancario_db;
```

### 3. Ajustar credenciais

Edite `src/main/resources/application.properties` com o usuário/senha do seu
PostgreSQL, caso sejam diferentes de `postgres` / `postgres`.

### 4. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. As tabelas são criadas/atualizadas
automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`) — em
produção, prefira uma ferramenta de migration como Flyway ou Liquibase.

### 5. Rodar os testes

```bash
mvn test
```

## Endpoints da API

### Clientes

| Método | Endpoint            | Descrição                     |
|--------|----------------------|--------------------------------|
| POST   | `/api/clientes`      | Cadastra um novo cliente       |
| GET    | `/api/clientes`      | Lista todos os clientes        |
| GET    | `/api/clientes/{id}` | Busca um cliente pelo id       |

**Exemplo de requisição — `POST /api/clientes`**

```json
{
  "nome": "Maria Souza",
  "cpf": "12345678900",
  "email": "maria.souza@email.com"
}
```

### Contas

| Método | Endpoint                            | Descrição                                   |
|--------|--------------------------------------|-----------------------------------------------|
| POST   | `/api/contas`                        | Abre uma conta para um cliente existente      |
| GET    | `/api/contas/{id}`                   | Busca uma conta pelo id                       |
| GET    | `/api/contas?clienteId={id}`         | Lista as contas de um cliente                 |
| GET    | `/api/contas/{numero}/saldo`         | Consulta o saldo disponível de uma conta      |
| GET    | `/api/contas/{numero}/extrato`       | Consulta o extrato paginado de uma conta      |

**Exemplo de requisição — `POST /api/contas`**

```json
{
  "clienteId": 1,
  "saldoInicial": 500.00
}
```

**Exemplo — `GET /api/contas/11111111/extrato?pagina=0&tamanhoPagina=10`**

```json
{
  "numeroConta": "11111111",
  "saldoAtual": 470.00,
  "paginaAtual": 0,
  "totalPaginas": 1,
  "totalDeLancamentos": 2,
  "lancamentos": [
    {
      "id": 2,
      "tipo": "TRANSFERENCIA_DEBITO",
      "valor": 30.00,
      "contaOrigemNumero": "11111111",
      "contaDestinoNumero": "22222222",
      "descricao": "Transferencia de 11111111 para 22222222",
      "dataHora": "2026-09-14T10:15:30"
    },
    {
      "id": 1,
      "tipo": "DEPOSITO_INICIAL",
      "valor": 500.00,
      "contaOrigemNumero": null,
      "contaDestinoNumero": null,
      "descricao": "Deposito de saldo inicial na abertura da conta",
      "dataHora": "2026-09-14T09:00:00"
    }
  ]
}
```

### Transferências

| Método | Endpoint              | Descrição                        |
|--------|------------------------|------------------------------------|
| POST   | `/api/transferencias`  | Transfere um valor entre contas    |

**Exemplo de requisição — `POST /api/transferencias`**

```json
{
  "numeroContaOrigem": "11111111",
  "numeroContaDestino": "22222222",
  "valor": 30.00
}
```

**Resposta de sucesso — `200 OK`**

```json
{
  "mensagem": "Transferencia realizada com sucesso",
  "numeroContaOrigem": "11111111",
  "numeroContaDestino": "22222222",
  "valorTransferido": 30.00,
  "saldoAtualContaOrigem": 470.00,
  "dataHora": "2026-09-14T10:15:30"
}
```

**Resposta de erro — saldo insuficiente (`422 Unprocessable Entity`)**

```json
{
  "timestamp": "2026-09-14T10:16:00",
  "status": 422,
  "erro": "Saldo insuficiente",
  "mensagem": "Saldo insuficiente na conta 11111111 para transferir 10000.00",
  "path": "/api/transferencias",
  "camposInvalidos": null
}
```

## Decisões de design que valem destaque na entrevista

- **Consistência transacional**: `TransferenciaService.transferir` é anotado
  com `@Transactional` — débito, crédito e os dois lançamentos de extrato
  ocorrem como uma única unidade atômica; qualquer falha no meio do processo
  reverte tudo (rollback automático).
- **Concorrência**: além da validação de saldo em código, o repositório usa
  lock pessimista (`SELECT ... FOR UPDATE`) nas duas contas envolvidas antes
  de qualquer alteração, evitando que duas transferências simultâneas
  corrompam o saldo. As duas contas são sempre bloqueadas na mesma ordem
  (menor id primeiro) para prevenir deadlock entre transferências cruzadas.
- **DTOs em vez de expor entidades**: a API nunca serializa entidades JPA
  diretamente, evitando N+1 acidental por lazy loading e desacoplando o
  contrato da API do modelo de dados.
- **Tratamento de erros centralizado**: um único `@RestControllerAdvice`
  garante que toda a API responda erros no mesmo formato (`ErroResponse`),
  incluindo os erros de validação do Bean Validation.
- **Extensibilidade**: a criação de outro tipo de lançamento (ex.: saque,
  depósito posterior) exige apenas um novo valor no enum `TipoTransacao` e um
  novo método de serviço — a estrutura de extrato já suporta.

## Possíveis evoluções futuras

- Migrations versionadas com Flyway em vez de `ddl-auto=update`.
- Autenticação/autorização (Spring Security + JWT).
- Paginação e filtros mais ricos na listagem de clientes/contas.
- Testes de integração com Testcontainers (PostgreSQL real em container).
- Idempotência na transferência (chave de idempotência por requisição).
