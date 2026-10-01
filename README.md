# API de Votação

API REST desenvolvida em Java e Spring Boot para gerenciamento de pautas e sessões de votação.

A aplicação permite cadastrar pautas, abrir sessões de votação, registrar votos de associados e consultar o resultado de cada votação.

## Tecnologias

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Docker / Docker Compose
- Maven
- JUnit 5
- Mockito
- MockMvc
- Testcontainers
- Swagger / OpenAPI
- k6

---

## Funcionalidades

A API possui as seguintes funcionalidades:

- Cadastro de pautas
- Abertura de sessão de votação
- Duração configurável da sessão
- Duração padrão de 1 minuto quando não informada
- Registro de votos `SIM` ou `NAO`
- Controle para impedir voto duplicado do mesmo associado na mesma pauta
- Bloqueio de votos após o encerramento da sessão
- Consulta do resultado da votação
- Validação da elegibilidade do associado
- Tratamento padronizado de erros
- Persistência dos dados em PostgreSQL
- Versionamento da API através de `/api/v1`
- Documentação com Swagger / OpenAPI
- Testes automatizados
- Testes de integração com PostgreSQL real
- Teste de carga e performance com k6

---

## Arquitetura

O projeto segue uma organização em camadas:

```text
com.desafio.votacao
├── client
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

### Controller

Responsável pela exposição dos endpoints REST e tratamento das requisições HTTP.

### Service

Contém as regras de negócio da aplicação.

### Repository

Responsável pelo acesso ao banco de dados através do Spring Data JPA.

### Entity

Representa as entidades persistidas no PostgreSQL.

### DTO

Define os contratos de entrada e saída da API.

### Client

Responsável pela abstração da integração com o serviço externo de validação do associado.

---

## Banco de dados

A aplicação utiliza PostgreSQL.

A evolução do banco é controlada através do Flyway.

Migrations disponíveis:

```text
V1__create_pauta_table.sql
V2__create_sessao_votacao_table.sql
V3__create_voto_table.sql
```

Algumas regras críticas também são garantidas pelo próprio banco.

### Uma sessão por pauta

```sql
UNIQUE (pauta_id)
```

### Um voto por associado em cada pauta

```sql
UNIQUE (pauta_id, associado_id)
```

Essa estratégia evita inconsistências mesmo em cenários de requisições concorrentes.

---

## Como executar

### Pré-requisitos

É necessário possuir:

- Java 21
- Maven
- Docker
- Docker Compose

### 1. Subir o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

O PostgreSQL será disponibilizado em:

```text
localhost:5432
```

Configuração padrão:

```text
database: votacao
username: votacao
password: votacao
```

### 2. Compilar e executar os testes

```bash
mvn clean install
```

### 3. Executar a aplicação

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

---

# Endpoints

## Criar pauta

```http
POST /api/v1/pautas
```

Exemplo:

```json
{
  "titulo": "Nova pauta",
  "descricao": "Descrição da pauta"
}
```

Resposta:

```http
201 Created
```

---

## Abrir sessão de votação

```http
POST /api/v1/pautas/{pautaId}/sessoes
```

É possível informar a duração da sessão em minutos:

```json
{
  "duracaoMinutos": 5
}
```

Caso nenhuma duração seja informada:

```json
{}
```

a sessão será aberta com duração padrão de **1 minuto**.

Resposta:

```http
201 Created
```

---

## Registrar voto

```http
POST /api/v1/pautas/{pautaId}/votos
```

Exemplo:

```json
{
  "associadoId": "ASSOCIADO-001",
  "opcao": "SIM"
}
```

Opções permitidas:

```text
SIM
NAO
```

Resposta:

```http
201 Created
```

Um associado pode votar apenas uma vez em cada pauta.

---

## Consultar resultado

```http
GET /api/v1/pautas/{pautaId}/votos/resultado
```

Exemplo de resposta:

```json
{
  "pautaId": 1,
  "sim": 3,
  "nao": 2,
  "total": 5
}
```

---

# Validação do associado

Como parte do requisito adicional do desafio, foi criada uma abstração para representar a consulta a um serviço externo responsável por verificar se o associado pode votar.

A estrutura utilizada é:

```text
VotoService
    ↓
AssociadoClient
    ↓
FakeAssociadoClient
```

O client simula os possíveis retornos do serviço externo:

```text
ABLE_TO_VOTE
UNABLE_TO_VOTE
```

Também é simulada a situação de CPF inválido.

Quando o associado está apto:

```text
ABLE_TO_VOTE
```

o fluxo de votação continua normalmente.

Quando o associado não está apto ou o CPF é considerado inválido, o voto não é registrado.

A utilização de uma interface permite substituir futuramente a implementação fake por um client HTTP real sem alterar a regra principal do `VotoService`.

---

# Tratamento de erros

A API utiliza tratamento centralizado de exceptions através de `@RestControllerAdvice`.

Alguns status utilizados:

```text
400 Bad Request
404 Not Found
409 Conflict
```

Exemplo de erro de validação:

```json
{
  "timestamp": "2026-09-28T10:00:00-03:00",
  "status": 400,
  "message": "Dados inválidos",
  "errors": {
    "titulo": "Título é obrigatório"
  }
}
```

---

# Swagger / OpenAPI

A documentação interativa da API está disponível através do Swagger UI.

Com a aplicação executando:

```text
http://localhost:8080/swagger-ui.html
```

Especificação OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

O Swagger apresenta os endpoints agrupados em:

```text
Pautas
Sessões de votação
Votos
```

---

# Testes automatizados

O projeto possui testes unitários, testes de controller e testes de integração.

São utilizados:

- JUnit 5
- Mockito
- MockMvc
- Testcontainers

Atualmente o projeto possui **28 testes automatizados**.

Os testes cobrem cenários como:

- criação de pauta
- abertura de sessão
- duração padrão da sessão
- validações de entrada
- sessão inexistente
- sessão encerrada
- voto válido
- voto duplicado
- associado não apto
- CPF inválido
- resultado da votação
- respostas HTTP `400`, `404` e `409`

## Testes de integração

Os testes de integração utilizam Testcontainers para iniciar um PostgreSQL real durante a execução dos testes.

Dessa forma são validados:

```text
Spring Boot
    ↓
PostgreSQL real
    ↓
Flyway
    ↓
Hibernate / JPA
    ↓
Repositories
```

Também são testadas diretamente no PostgreSQL as constraints responsáveis por impedir:

- dois votos do mesmo associado na mesma pauta
- duas sessões de votação para a mesma pauta

Para executar todos os testes:

```bash
mvn clean test
```

ou:

```bash
mvn clean install
```

---

# Teste de carga e performance

Foi utilizado o **k6** para realizar um teste de carga sobre o endpoint de registro de votos.

Durante o cenário, a carga foi aumentada gradualmente até **100 usuários virtuais concorrentes**.

Para evitar que a aleatoriedade do `FakeAssociadoClient` interferisse na medição, foi criado o profile `performance`.

Nesse profile, o `PerformanceAssociadoClient` retorna sempre:

```text
ABLE_TO_VOTE
```

permitindo que o teste meça principalmente o comportamento da API, persistência e banco de dados sob carga.

A estrutura utilizada durante o teste foi:

```text
k6
 ↓
API Spring Boot
 ↓
VotoService
 ↓
PostgreSQL
```

## Cenário executado

O teste foi configurado com aumento gradual de carga:

```text
0 → 10 usuários
10 → 50 usuários
50 → 100 usuários
100 → 0 usuários
```

A execução principal teve duração aproximada de **1 minuto e 40 segundos**.

## Resultados

| Métrica | Resultado |
| --- | ---: |
| Usuários virtuais máximos | 100 |
| Requisições HTTP | 37.145 |
| Votos registrados com sucesso | 37.131 |
| Taxa de sucesso | 99,96% |
| Taxa de erro | 0,03% |
| Throughput médio | ~322 req/s |
| Tempo médio de resposta | 8,73 ms |
| p90 | 12,91 ms |
| p95 | 14,96 ms |
| Tempo máximo observado | 378,75 ms |

Os thresholds definidos para o teste foram:

```text
p95 < 1000 ms
taxa de erro < 1%
```

Ambos foram atendidos.

Durante a execução ocorreram 12 timeouts de comunicação, representando aproximadamente **0,03%** das requisições.

O script utilizado está disponível em:

```text
performance/vote-load-test.js
```

## Como executar o teste de carga

Primeiro, execute a aplicação utilizando o profile de performance:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=performance
```

Em outro terminal, execute o k6 através do Docker:

```bash
docker run --rm -i grafana/k6 run - < performance/vote-load-test.js
```

No Windows com IntelliJ também é possível configurar:

```text
Active profiles: performance
```

Esse profile existe exclusivamente para tornar a validação do associado determinística durante o teste de carga.

---

# Decisões técnicas

## Regra de voto único no banco

Além da validação realizada pela aplicação, foi criada uma constraint:

```sql
UNIQUE (pauta_id, associado_id)
```

Essa decisão permite garantir a regra mesmo quando duas requisições concorrentes tentarem registrar um voto ao mesmo tempo.

---

## Uma sessão por pauta

Foi adotada a regra de uma única sessão de votação para cada pauta.

Essa regra também é garantida pelo banco:

```sql
UNIQUE (pauta_id)
```

---

## Contagem realizada pelo banco

O resultado da votação utiliza queries de contagem diretamente no PostgreSQL.

Exemplo conceitual:

```text
COUNT SIM
COUNT NAO
```

Em vez de carregar todos os votos para memória.

Essa abordagem reduz o volume de dados transferido para a aplicação e é mais adequada para uma quantidade elevada de votos.

---

## Controle de concorrência

As verificações realizadas pela aplicação fornecem respostas amigáveis ao cliente.

Entretanto, regras críticas também são protegidas através de constraints no banco de dados, evitando condições de corrida.

A aplicação também trata possíveis violações de integridade durante a persistência, mantendo a regra mesmo quando requisições concorrentes ultrapassam a validação inicial.

---

## Integração externa desacoplada

A validação do associado foi abstraída através da interface:

```text
AssociadoClient
```

O `VotoService` depende dessa abstração e não diretamente da implementação fake.

Isso permite substituir futuramente:

```text
FakeAssociadoClient
```

por uma implementação HTTP real sem alterar a regra principal de votação.

---

## Profile de performance

Durante testes de carga, respostas aleatórias do serviço fake poderiam produzir erros que não representam degradação de performance.

Por esse motivo foi criado:

```text
PerformanceAssociadoClient
```

ativado somente através do profile:

```text
performance
```

Na execução normal continua sendo utilizado o comportamento fake definido para o desafio.

---

## Versionamento

Os endpoints foram criados utilizando versionamento pela URL:

```text
/api/v1
```

Isso permite que futuras versões da API sejam introduzidas mantendo compatibilidade com consumidores existentes.

---

# Status do projeto

Funcionalidades principais concluídas:

- [x] Cadastro de pauta
- [x] Abertura de sessão
- [x] Duração padrão da sessão
- [x] Registro de voto
- [x] Controle de voto duplicado
- [x] Encerramento automático por horário
- [x] Resultado da votação
- [x] Persistência PostgreSQL
- [x] Flyway
- [x] Docker
- [x] Tratamento de erros
- [x] Versionamento da API
- [x] Validação de associado
- [x] Testes unitários
- [x] Testes de controller
- [x] Testes de integração com PostgreSQL
- [x] Swagger / OpenAPI
- [x] Teste de carga e performance

---

## Autor

Projeto desenvolvido como desafio técnico para desenvolvimento Backend Java.