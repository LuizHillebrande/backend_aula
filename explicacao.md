# Guia do vídeo técnico — Cassino Online (Backend)

**Autor:** Luiz Fernando Hillebrande  
**Objetivo do vídeo:** mostrar o backend Java funcionando integrado ao React, com dados reais no **PostgreSQL**, explicando a arquitetura em camadas.

> **Banco:** PostgreSQL, database `cassino`. Crie no DBeaver antes de subir o backend.

---

## Antes de gravar (checklist)

- [ ] PostgreSQL instalado, banco **`cassino`** criado no DBeaver
- [ ] Backend: `./mvnw spring-boot:run`
- [ ] Frontend: `npm run dev` em `http://localhost:5173`
- [ ] DBeaver ou pgAdmin aberto nas tabelas
- [ ] DevTools do navegador (aba Network) aberto
- [ ] IDE com o projeto backend aberto

---

## Criar banco no DBeaver

1. DBeaver → **New Database Connection** → **PostgreSQL**
2. Host `localhost`, Port `5432`, Database `postgres`, User **`luizhill`** (seu login macOS), Password vazio
3. **Test Connection** → Finish
4. Clique direito na conexão → **Create** → **Database** → nome **`cassino`** → OK
5. Rode `./mvnw spring-boot:run` — Hibernate cria as tabelas automaticamente
6. Atualize o DBeaver: `cassino` → Schemas → public → Tables

**Terminal (alternativa):**
```bash
psql -U luizhill -c "CREATE DATABASE cassino;"
```

---

## Roteiro sugerido (~10–12 min)

| Min | O quê |
|-----|-------|
| 0–1 | Apresentação: projeto cassino, Spring Boot + React + PostgreSQL |
| 1–2 | Inicialização: `CassinoApplication`, `pom.xml`, `application.properties` |
| 2–3 | Pastas e arquitetura (controller → service → repository → entity → dto) |
| 3–5 | **Fluxo completo:** depósito no front → API → banco → tela atualiza |
| 5–7 | Mostrar tabelas no PostgreSQL antes/depois do depósito |
| 7–9 | Explicar Entity, DTO, validações, erros |
| 9–11 | Crash (opcional mas forte): apostar, polling, retirar ou crashar |
| 11–12 | Conclusão: decisões técnicas e o que aprendeu |

---

## 1. Inicialização da aplicação

**O que falar:**
- `CassinoApplication.java` tem o `main` → chama `SpringApplication.run()` → sobe o servidor embarcado (Tomcat) na **porta 8080**
- Spring Boot lê `application.properties` e cria beans (Controller, Service, Repository, conexão JPA)

**Arquivos para mostrar na tela:**

| Arquivo | O que explicar |
|---------|----------------|
| `CassinoApplication.java` | Classe principal, `@SpringBootApplication` |
| `pom.xml` | Dependências: `spring-boot-starter-web`, `data-jpa`, `validation`, `postgresql` |
| `application.properties` | Porta 8080, conexão PostgreSQL (`jdbc:postgresql://localhost:5432/cassino`) |
| `DataLoader.java` | Seed inicial (usuário id=1 e 2 apostas mock) na primeira execução |

**Comando para o vídeo:**
```bash
./mvnw spring-boot:run
```

---

## 2. Integração front-end ↔ back-end

**Demonstrações obrigatórias (faça pelo menos 2):**

### A) Depósito (melhor para o fluxo completo)
1. Tela Conta ou modal de depósito no React
2. Usuário digita valor → clica depositar
3. Front chama: `POST http://localhost:8080/api/usuarios/1/deposito` body `{ "valor": 100 }`
4. Back retorna `{ id, nome, cpf, saldo }` atualizado
5. Header/saldo na tela muda

### B) Listar histórico
1. Tela de histórico
2. Front chama: `GET /api/usuarios/1/apostas`
3. Mostra array de apostas do banco

### C) Buscar usuário
1. Página Conta
2. `GET /api/usuarios/1` → nome, cpf, saldo

**O que explicar durante a demo:**
- Qual **tela** dispara a ação
- Qual **URL + método HTTP**
- O que vai no **body** (JSON)
- O que volta na **response**
- Como o React **atualiza o state** com a resposta

**Mostre a aba Network** do navegador durante o depósito — isso prova a integração real.

---

## 3. Banco de dados (PostgreSQL)

**Tabelas do projeto:**

| Tabela | O que guarda |
|--------|--------------|
| `usuarios` | id, nome, cpf, saldo |
| `apostas` | id, usuario_id, jogo, valor, resultado, lucro, created_at |
| `rodadas_crash` | id (uuid), usuario_id, valor, multiplicador_crash, iniciada_em, status, aposta_id |

**O que mostrar no DBeaver/pgAdmin:**
1. `SELECT * FROM usuarios;` → saldo 1000.00
2. Faz depósito de 100 no front
3. `SELECT * FROM usuarios;` de novo → saldo 1100.00
4. `SELECT * FROM apostas ORDER BY id DESC;` → histórico real

**Frase importante para falar:**  
*"Os dados não são mock no React — vêm do PostgreSQL via JPA/Hibernate."*

---

## 4. Arquitetura — o que cada camada faz NO SEU projeto

```
Front (React) → Controller → Service → Repository → Entity → PostgreSQL
                  ↑ DTO entra/sai
```

### Controller — `UsuarioController.java`
- **Papel:** recebe HTTP, devolve JSON. **Não tem regra de negócio.**
- Endpoints em `/api/usuarios`
- Usa `@Valid` nos bodies
- Delega para `UsuarioService`, `ApostaService`, `CrashService`

### Service — `UsuarioService`, `ApostaService`, `CrashService`
- **Papel:** regras de negócio
- Exemplos no seu código:
  - Depósito: valor > 0, soma ao saldo
  - Crash: debita aposta, sorteia crash, calcula multiplicador no tempo, vitória/derrota
- Chama Repository para persistir

### Repository — `UsuarioRepository`, `ApostaRepository`, `RodadaCrashRepository`
- **Papel:** acesso ao banco
- Extends `JpaRepository` → `findById`, `save`, etc.
- `ApostaRepository.findByUsuarioIdOrderByIdDesc` → consulta customizada

### Entity — `Usuario`, `Aposta`, `RodadaCrash`
- **Papel:** representam tabelas
- Anotações: `@Entity`, `@Table`, `@Id`, `@Column`, `@ManyToOne`, `@OneToOne`, `@GeneratedValue`
- Relacionamento: `Aposta` → `@ManyToOne Usuario`; `RodadaCrash` → `@ManyToOne Usuario`

### DTO — pasta `dto/request` e `dto/response`
- **Papel:** formato da API (o que entra/sai pro front)
- **Request:** `DepositoRequest`, `CrashIniciarRequest`, `CrashRetirarRequest`
- **Response:** `UsuarioResponse`, `ApostaResponse`, `CrashEstadoResponse`, `ErroResponse`
- **Por que não expor Entity?** Controla o JSON, esconde campos internos, desacopla banco da API

---

## 5. Fluxo completo — use o DEPÓSITO

Fale assim, mostrando cada arquivo:

1. **Front:** usuário deposita R$ 100
2. **POST** `/api/usuarios/1/deposito` + body JSON
3. **Controller** recebe `DepositoRequest` com `@Valid`
4. **UsuarioService.depositar()** valida valor > 0, busca usuário, soma saldo
5. **UsuarioRepository.save()** grava no PostgreSQL
6. **UsuarioResponse** montado e devolvido
7. **Front** atualiza saldo na tela

Abra os 4 arquivos na ordem: Controller → Service → Repository → Entity.

---

## 6. Organização de pastas

```
src/main/java/com/unifil/cassino/
├── CassinoApplication.java    → main
├── DataLoader.java            → seed inicial
├── config/CorsConfig.java     → libera front localhost:5173
├── controller/                → endpoints REST
├── service/                   → regras de negócio
├── repository/                → JPA / banco
├── entity/                    → tabelas
├── dto/request/               → JSON que entra
├── dto/response/              → JSON que sai
└── exception/                 → erros padronizados
```

---

## 7. Endpoints principais

| Método | URL | Função |
|--------|-----|--------|
| GET | `/api/usuarios/{id}` | Busca usuário |
| POST | `/api/usuarios/{id}/deposito` | Deposita (body: `{ valor }`) |
| GET | `/api/usuarios/{id}/apostas` | Histórico de apostas |
| POST | `/api/usuarios/{id}/apostas/crash/iniciar` | Inicia rodada Crash |
| GET | `/api/usuarios/{id}/apostas/crash/rodadas/{rodadaId}` | Multiplicador atual (polling) |
| POST | `/api/usuarios/{id}/apostas/crash/retirar` | Cashout (body: `{ rodadaId }`) |

**Crash em 1 frase:** backend sorteia quando crasha; front faz polling; se não retirar a tempo, perde; se retirar, ganha `valor × (multiplicador − 1)`.

---

## 8. Validações e erros

**Mostrar no código:**

| Onde | O quê |
|------|-------|
| `DepositoRequest` | `@NotNull`, `@DecimalMin("0.01")` |
| `UsuarioService` | valor ≤ 0 → `RegraNegocioException` |
| `UsuarioService` | usuário não existe → `RecursoNaoEncontradoException` |
| `GlobalExceptionHandler` | captura exceções → JSON `{ "mensagem": "..." }` com status 400/404 |

**Demo rápida de erro:** tentar depositar 0 ou valor negativo → front mostra mensagem de erro.

---

## 9. Decisões — o que justificar (fale com suas palavras)

| Decisão | Justificativa |
|---------|---------------|
| Controller / Service / Repository separados | Single Responsibility — cada camada tem um papel |
| DTOs em português | Front React já usa `nome`, `saldo`, `resultado` |
| JPA + Hibernate | Mapeia Entity → tabela sem SQL manual |
| PostgreSQL | Banco relacional real, exigido pelo professor e usado em dev |
| CORS em `CorsConfig` | Front (5173) pode chamar API (8080) |
| Regra do Crash na Service | Controller fino; lógica de jogo fica testável |
| `GlobalExceptionHandler` | Erros padronizados pro front tratar |

---

## O que NÃO fazer (perde ponto)

- Só mostrar tela funcionando sem abrir código
- Só testar no Postman, sem front
- Dizer que dados vêm do banco mas usar mock/localStorage no React
- Ler roteiro decorado sem entender
- Não abrir PostgreSQL
- Não explicar diferença Entity vs DTO
- Colocar regra de negócio no Controller e dizer que "é assim mesmo"

---

## Frases prontas (use como base, não decore)

**Abertura:**  
*"Este é o backend do cassino online, feito em Java 17 com Spring Boot 3. Ele expõe uma API REST na porta 8080 e persiste dados no PostgreSQL via JPA."*

**Sobre camadas:**  
*"O Controller só recebe e responde HTTP. A Service concentra as regras — por exemplo, validar saldo e calcular lucro no Crash. O Repository conversa com o banco através do JpaRepository."*

**Sobre DTO:**  
*"A Entity representa a tabela usuarios no banco. O UsuarioResponse é o que eu devolvo pro React — campos controlados, JSON em português."*

**Sobre banco:**  
*"Quando faço um depósito, o UsuarioService altera o saldo e o Repository faz save. Se eu consultar a tabela usuarios no PostgreSQL, o valor já está atualizado."*

**Fechamento:**  
*"A arquitetura em camadas deixa o código organizado, testável e desacoplado do front-end, que consome apenas os endpoints JSON da API."*

---

## Ordem de arquivos para abrir durante o vídeo

1. `CassinoApplication.java`
2. `pom.xml`
3. `application.properties`
4. `UsuarioController.java`
5. `DepositoRequest.java` + `UsuarioResponse.java`
6. `UsuarioService.java`
7. `UsuarioRepository.java`
8. `Usuario.java` (entity)
9. `GlobalExceptionHandler.java`
10. (Opcional) `CrashService.java`
11. DBeaver → tabelas
12. Navegador → front + Network

---

## Resumo em 3 linhas

1. **Grave** backend + front + PostgreSQL juntos, mostrando código e Network.
2. **Explique** o fluxo depósito: Front → Controller → DTO → Service → Repository → Entity → banco → response → tela.
3. **Justifique** por que separou camadas, usou DTO e PostgreSQL.

Se souber explicar o depósito de ponta a ponta olhando o código, você cobre 80% da rubrica. O Crash é diferencial extra.
