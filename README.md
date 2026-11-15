# Core Bancário Simplificado

[![CI](https://github.com/Marconcin01/core-bancario-simplificado/actions/workflows/ci.yml/badge.svg)](https://github.com/Marconcin01/core-bancario-simplificado/actions/workflows/ci.yml)

API RESTful de um núcleo bancário simplificado, desenvolvida como projeto de
portfólio para demonstrar boas práticas de back-end com Java e Spring Boot.

O projeto também inclui o painel web **Facilities Bank**, que consome a API
Spring Boot para demonstrar as operações em uma interface visual.

Documentação interativa dos endpoints via Swagger UI, disponível em
`http://localhost:8080/swagger-ui.html` com a aplicação rodando.

> Projeto de portfólio para fins didáticos. A chave Pix é usada apenas para
> localizar contas dentro deste projeto; transferências não são enviadas pela
> rede Pix do Banco Central. A aplicação não implementa autenticação e não
> deve ser usada para movimentar dinheiro real.

## Funcionalidades

- Cadastro de clientes.
- Abertura de contas bancárias com saldo inicial (por cliente).
- Consulta de saldo de uma conta.
- Consulta de extrato (histórico de lançamentos) paginado de uma conta.
- Transferência de valores entre contas, com validação estrita de saldo
  disponível e consistência garantida por transação (`@Transactional`).
- Cadastro, atualização e remoção de uma chave Pix vinculada à conta.
- Transferência interna para uma conta identificada pela chave Pix.
- Registro de depósitos Pix ou TED simulados, lançados no saldo e no extrato.
- Painel web responsivo para consultar saldo e extrato, cadastrar clientes e
  contas e iniciar transferências.

## Stack técnica

| Camada          | Tecnologia                          |
|-----------------|--------------------------------------|
| Linguagem       | Java 17+                             |
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
├── frontend/
│   └── index.html              # Painel web Facilities Bank
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
- Docker e Docker Compose (ou PostgreSQL em execução localmente)
- Python 3 (para servir a interface web)

### 2. Iniciar o banco de dados

```bash
docker compose up -d postgres
```

O Compose cria o banco `core_bancario_db` com as credenciais locais usadas pela
aplicação. Para usar outro PostgreSQL, edite as configurações em
`src/main/resources/application.properties`.

### 3. Rodar a API

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. As tabelas são criadas/atualizadas
automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`) — em
produção, prefira uma ferramenta de migration como Flyway ou Liquibase.

### 4. Abrir o painel web

Em outro terminal, na raiz do projeto:

```bash
python -m http.server 8765 --directory frontend
```

Acesse `http://localhost:8765` e use **Clientes e contas** para cadastrar um
cliente e abrir uma conta. Depois, use **Conectar API** para selecionar uma
conta, ou abra a conta recém-criada, que será conectada automaticamente. O
Swagger também fica disponível em `http://localhost:8080/swagger-ui.html`.

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
| PUT    | `/api/contas/{numero}/chave-pix`     | Cadastra ou atualiza a chave Pix da conta      |
| DELETE | `/api/contas/{numero}/chave-pix`     | Remove a chave Pix da conta                    |
| POST   | `/api/contas/{numero}/depositos`     | Registra depósito Pix ou TED simulado          |
| GET    | `/api/contas/{numero}/saldo`         | Consulta o saldo disponível de uma conta      |
| GET    | `/api/contas/{numero}/extrato`       | Consulta o extrato paginado de uma conta      |

**Exemplo de requisição — `POST /api/contas`**

```json
{
  "clienteId": 1,
  "saldoInicial": 500.00,
  "chavePix": "maria.souza@email.com"
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

O campo `chavePix` é opcional e único. Também é possível vincular uma chave a
uma conta já aberta usando `PUT /api/contas/{numero}/chave-pix` com o JSON
`{"chavePix":"maria.souza@email.com"}`.
Para removê-la, use `DELETE /api/contas/{numero}/chave-pix`.

A interface também permite vincular uma chave Pix a uma conta já existente
pelo menu **Chave Pix**. Nesta versão, a chave identifica contas dentro do
próprio banco de dados do projeto; a transferência é uma simulação interna e
não envia pagamentos pela rede Pix do Banco Central. O atalho **Depositar**
exibe a chave Pix ou os dados de TED da conta conectada e registra um crédito
simulado no saldo e no extrato; ele não confirma recebimentos externos.

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

Para transferir usando uma chave Pix, informe `chavePixDestino` no lugar de
`numeroContaDestino`:

```json
{
  "numeroContaOrigem": "11111111",
  "chavePixDestino": "maria.souza@email.com",
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

