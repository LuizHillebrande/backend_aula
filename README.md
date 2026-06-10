# Cassino Online — Backend REST

Backend REST em Java (Spring Boot + Maven) para integração com o frontend React + TypeScript.

**Autor:** Luiz Fernando Hillebrande

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (local)

## Banco de dados (PostgreSQL)

**Nome do banco:** `cassino`

### Criar no DBeaver

1. Abra o DBeaver → conecte no PostgreSQL local (host `localhost`, porta `5432`, usuário `postgres`, senha a sua — padrão do projeto: `postgres`)
2. Clique direito em **Databases** → **Create** → **Database**
3. Nome: `cassino` → OK

Ou pelo terminal:

```bash
psql -U postgres -c "CREATE DATABASE cassino;"
```

As tabelas (`usuarios`, `apostas`, `rodadas_crash`) são criadas automaticamente pelo Hibernate na primeira execução (`ddl-auto=update`).

### Credenciais padrão (`application.properties`)

| Campo    | Valor |
|----------|-------|
| URL      | `jdbc:postgresql://localhost:5432/cassino` |
| Usuário  | `luizhill` (no Mac com Homebrew, use seu login do macOS — **não** `postgres`) |
| Senha    | (vazio) |

Se instalou PostgreSQL de outro jeito (Docker, instalador oficial), o usuário pode ser `postgres`. Ajuste em `src/main/resources/application.properties`.

## Como rodar

```bash
./mvnw spring-boot:run
```

A API sobe em **http://localhost:8080**.

## CORS

Origem permitida: `http://localhost:5173` (frontend Vite).

## Estrutura do banco

### `usuarios`

| Coluna | Tipo           | Descrição              |
|--------|----------------|------------------------|
| id     | BIGINT PK      | Identificador          |
| nome   | VARCHAR NOT NULL | Nome do usuário      |
| cpf    | VARCHAR NOT NULL UNIQUE | CPF           |
| saldo  | DECIMAL(15,2)  | Saldo atual (default 0)|

### `apostas`

| Coluna     | Tipo           | Descrição                    |
|------------|----------------|------------------------------|
| id         | BIGINT PK AUTO | Identificador                |
| usuario_id | BIGINT FK      | Referência a `usuarios.id`   |
| jogo       | VARCHAR        | Ex.: `"Crash"`               |
| valor      | DECIMAL(15,2)  | Valor apostado               |
| resultado  | VARCHAR        | `"Vitória"` ou `"Derrota"`   |
| lucro      | DECIMAL(15,2)  | Positivo ou negativo         |
| created_at | TIMESTAMP      | Data/hora da aposta          |

### `rodadas_crash`

| Coluna              | Tipo           | Descrição                         |
|---------------------|----------------|-----------------------------------|
| id                  | VARCHAR PK     | UUID da rodada                    |
| usuario_id          | BIGINT FK      | Referência a `usuarios.id`        |
| valor               | DECIMAL(15,2)  | Valor apostado (já debitado)      |
| multiplicador_crash | DECIMAL(15,2)  | Ponto de crash sorteado (secreto) |
| iniciada_em         | TIMESTAMP      | Início da rodada                  |
| status              | VARCHAR        | ATIVA, RETIRADA ou CRASHOU        |
| aposta_id           | BIGINT FK      | Aposta gerada ao encerrar         |

## Seed inicial

Usuário `id=1`: Luiz Fernando, CPF `123.456.789-00`, saldo `1000.00`.

Histórico com 2 apostas Crash (vitória e derrota), conforme mock do frontend.

## Endpoints

Base path: `/api`

### 1. Buscar usuário

`GET /api/usuarios/{id}`

**Resposta 200:**

```json
{
  "id": 1,
  "nome": "Luiz Fernando",
  "cpf": "123.456.789-00",
  "saldo": 1000.00
}
```

```bash
curl http://localhost:8080/api/usuarios/1
```

### 2. Depósito

`POST /api/usuarios/{id}/deposito`

**Body:**

```json
{ "valor": 100.00 }
```

**Resposta 200:** usuário atualizado.

```bash
curl -X POST http://localhost:8080/api/usuarios/1/deposito \
  -H "Content-Type: application/json" \
  -d '{"valor": 100.00}'
```

### 3. Histórico de apostas

`GET /api/usuarios/{id}/apostas`

**Resposta 200:** array ordenado por `id` DESC.

```bash
curl http://localhost:8080/api/usuarios/1/apostas
```

### 4. Crash — iniciar rodada

`POST /api/usuarios/{id}/apostas/crash/iniciar`

Debita o valor e sorteia o ponto de crash no servidor (1,01 a 10,00). O frontend **não** recebe o crash point.

**Body:**

```json
{ "valor": 100.00 }
```

**Resposta 201:**

```json
{
  "rodadaId": "uuid-da-rodada",
  "usuario": { "id": 1, "nome": "...", "cpf": "...", "saldo": 900.00 }
}
```

### 5. Crash — estado da rodada (polling)

`GET /api/usuarios/{id}/apostas/crash/rodadas/{rodadaId}`

O servidor calcula o multiplicador atual com base no tempo (+0,50x por segundo). Quando atinge o crash point, encerra automaticamente com derrota.

**Rodada ativa (200):**

```json
{
  "rodadaId": "uuid",
  "status": "ATIVA",
  "multiplicadorAtual": 1.45
}
```

**Rodada crashou (200):**

```json
{
  "rodadaId": "uuid",
  "status": "CRASHOU",
  "multiplicadorAtual": 1.80,
  "multiplicadorCrash": 1.80,
  "aposta": { "id": 3, "jogo": "Crash", "valor": 100.00, "resultado": "Derrota", "lucro": -100.00 },
  "usuario": { "id": 1, "saldo": 900.00, ... }
}
```

Poll a cada ~100ms enquanto `status === "ATIVA"`.

### 6. Crash — retirar (cashout)

`POST /api/usuarios/{id}/apostas/crash/retirar`

Retira no multiplicador **atual do servidor** (anti-cheat). Vitória se ainda estiver **antes** do crash point.

**Body:**

```json
{ "rodadaId": "uuid-da-rodada" }
```

**Resposta 200 (vitória):**

```json
{
  "rodadaId": "uuid",
  "status": "RETIRADA",
  "multiplicadorAtual": 1.65,
  "multiplicadorCrash": 1.80,
  "aposta": { "resultado": "Vitória", "lucro": 65.00, ... },
  "usuario": { "saldo": 1065.00, ... }
}
```

Regras:
- `lucro = valor × (multiplicadorAtual − 1)` na retirada
- Se não retirar antes do crash → perde o valor apostado
- Se retirar no ou após o crash → derrota

```bash
curl -X POST http://localhost:8080/api/usuarios/1/apostas/crash/iniciar \
  -H "Content-Type: application/json" \
  -d '{"valor": 100.00}'

curl http://localhost:8080/api/usuarios/1/apostas/crash/rodadas/{rodadaId}

curl -X POST http://localhost:8080/api/usuarios/1/apostas/crash/retirar \
  -H "Content-Type: application/json" \
  -d '{"rodadaId": "uuid-da-rodada"}'
```

## Erros

Respostas de erro em JSON:

```json
{ "mensagem": "Saldo insuficiente" }
```

| Status | Situação                          |
|--------|-----------------------------------|
| 400    | Validação ou regra de negócio     |
| 404    | Usuário não encontrado            |

## Arquitetura

```
src/main/java/com/unifil/cassino/
├── config/          # CORS
├── controller/      # REST controllers
├── dto/             # request/response
├── entity/          # JPA entities
├── exception/       # GlobalExceptionHandler
├── repository/      # Spring Data JPA
├── service/         # Regras de negócio
└── DataLoader.java  # Seed inicial
```

## Testes

```bash
./mvnw test
```
