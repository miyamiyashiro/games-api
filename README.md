# Games API

> **Status do Projeto:** LIVE  
> **Documentacao Oficial:** [Swagger UI](https://games-api-3rqr.onrender.com/swagger-ui/index.html)  
> **Colecao Postman:** [`games-api/postman/Games API.postman_collection.json`](games-api/postman/Games%20API.postman_collection.json)

API desenvolvida para gestão de acervos e empréstimos de jogos de tabuleiro e RPG, com foco nos requisitos do projeto final: REST, relacionamentos JPA, HATEOAS, validação, documentação OpenAPI, deploy, autenticação por API Key, idempotência, rate limiting, CORS e versionamento.

Autora: Luana Miyashiro Salles de Oliveira

---

## Links

* **Swagger UI:** <https://games-api-3rqr.onrender.com/swagger-ui/index.html>
* **Base da API:** <https://games-api-3rqr.onrender.com>
* **Endpoint versionado v1:** <https://games-api-3rqr.onrender.com/api/v1/status>
* **Endpoint versionado v2:** <https://games-api-3rqr.onrender.com/api/v2/status>

---

## Checklist da Parte I

| Requisito | Como foi atendido |
| :--- | :--- |
| Projeto Maven | Projeto Spring Boot com `pom.xml` e Maven Wrapper |
| API REST | Controllers REST para jogos, usuários, editoras, plataformas, empréstimos e detalhes |
| CRUD | Recursos principais possuem operações de criação, consulta, atualização e exclusão |
| HATEOAS | Respostas usam `EntityModel`, `CollectionModel` e `PagedModel` com links |
| Paginação | Listagens usam `Pageable` e `PagedResourcesAssembler` |
| One-to-One | `Jogo` possui `DetalhesJogo` |
| One-to-Many | `Editora` possui vários `Jogo`; `Usuario` possui vários `Emprestimo` |
| Many-to-One | `Jogo` pertence a uma `Editora`; `Emprestimo` pertence a um `Usuario` e a um `Jogo` |
| Many-to-Many | `Jogo` possui várias `Plataforma` e `Plataforma` possui vários `Jogo` |
| Enum | `Categoria` classifica os jogos |
| Validação | DTOs de entrada usam Bean Validation, como `@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Min` e `@Max` |
| Consultas personalizadas | Busca por título, e-mail, nome, data e ID do jogo |
| Tratamento de erros | `GlobalExceptionHandler` padroniza respostas de erro |
| Swagger/OpenAPI | Documentação disponível em `/swagger-ui/index.html` |
| Postman | Coleção com roteiro de testes da Parte I e Parte II |
| Deploy | API publicada no Render com Docker |

---

## Checklist da Parte II

| Requisito | Como foi atendido |
| :--- | :--- |
| `HTTP 401 Unauthorized` | Escritas protegidas exigem `X-API-Key`; chave ausente ou inválida retorna `401` |
| API Key | Usuário cria conta, gera chave em `POST /usuarios/{id}/api-key` e usa o header `X-API-Key` |
| `HTTP 429 Too Many Requests` | Rate limit por IP com bloqueio de 30 segundos ao exceder o limite |
| Idempotência | `POST`, `PUT` e `PATCH` aceitam `Idempotency-Key` |
| `HTTP 409 Conflict` | Mesma chave de idempotência com JSON diferente retorna `409` |
| CORS | Permite chamadas web, headers customizados e métodos REST |
| Versionamento | Dois contratos versionados: `GET /api/v1/status` e `GET /api/v2/status` |

---

## Modelagem de Dados

O sistema foi modelado para demonstrar os principais tipos de relacionamento exigidos na avaliação:

* **Jogo:** registro central do acervo, com título, categoria, editora, plataformas e detalhes complementares.
* **DetalhesJogo:** informações complementares de um jogo, em relacionamento One-to-One com Jogo.
* **Usuário:** cliente que pode realizar empréstimos e gerar uma chave de API.
* **Editora:** publicadora dos jogos, em relacionamento One-to-Many com Jogo.
* **Empréstimo:** controle de retirada e devolução, em relacionamento Many-to-One com Usuário e Jogo.
* **Plataforma:** meio ou sistema onde o jogo esta disponível, em relacionamento Many-to-Many com Jogo.
* **Categoria:** enum com os tipos de jogos cadastrados.

---

## Tecnologias

* Java 21
* Spring Boot 3.4.1
* Maven
* Spring Web
* Spring Data JPA / Hibernate
* H2 Database
* Bean Validation
* Spring HATEOAS
* Springdoc OpenAPI / Swagger
* Docker
* Render

---

## Principais Endpoints

| Recurso | Endpoint | Observacao |
| :--- | :--- | :--- |
| Jogos | `GET /jogos?page=0&size=5` | Lista paginada com HATEOAS |
| Jogos | `GET /jogos/busca?titulo=catan` | Consulta personalizada por título |
| Usuarios | `GET /usuarios/email/luana@email.com` | Consulta personalizada por e-mail |
| Editoras | `GET /editoras/busca?nome=galapagos` | Consulta personalizada por nome |
| Plataformas | `GET /plataformas/busca?nome=tabuleiro` | Consulta personalizada por nome |
| Emprestimos | `GET /emprestimos/data?data=2026-04-11` | Consulta personalizada por data |
| DetalhesJogo | `GET /detalhes-jogos/jogo/1` | Consulta personalizada pelo ID do jogo |
| API Key | `POST /usuarios/{id}/api-key` | Gera a chave usada no header `X-API-Key` |
| Versionamento | `GET /api/v1/status` | Versão simples do endpoint |
| Versionamento | `GET /api/v2/status` | Versão expandida do endpoint |

---

## Autenticacao com Chave de API

Operações sensíveis de escrita exigem o header `X-API-Key`. As consultas `GET`, o cadastro de usuário e a geração da chave ficam públicos para permitir o fluxo inicial.

Fluxo:

1. Crie um usuário com `POST /usuarios`.
2. Gere a chave com `POST /usuarios/{id}/api-key`.
3. Envie a chave no header `X-API-Key` ao criar, atualizar ou excluir recursos protegidos.

Se a chave estiver ausente ou inválida, a API retorna `HTTP 401 Unauthorized`.

Exemplo de header:

```http
X-API-Key: sua-chave-gerada
```

---

## Idempotência

Operações `POST`, `PUT` e `PATCH` aceitam o header `Idempotency-Key`. A chave identifica uma tentativa de escrita e evita duplicidade quando a mesma requisição é enviada mais de uma vez.

Comportamento esperado:

* Chave nova: a API processa normalmente a operação.
* Mesma chave, mesmo endpoint e mesmo JSON: a API retorna `HTTP 200 OK` e ignora o novo processamento.
* Mesma chave com JSON alterado: a API retorna `HTTP 409 Conflict`.
* Mesma chave usada em outro endpoint ou método: a API retorna `HTTP 409 Conflict`.

Exemplo de header:

```http
Idempotency-Key: demo-idempotencia-001
```

---

## Rate Limiting

A API limita cada IP a 10 requisições por minuto. Se o limite for excedido, o cliente recebe `HTTP 429 Too Many Requests` e fica bloqueado por 30 segundos.

Headers retornados:

* `X-RateLimit-Limit`: limite total da janela.
* `X-RateLimit-Remaining`: requisições restantes na janela atual.
* `X-RateLimit-Reset`: momento aproximado de reset da janela, em epoch seconds.
* `Retry-After`: segundos restantes de bloqueio, enviado nas respostas `429`.

Para testar rapidamente, envie mais de 10 requisicoes seguidas para qualquer endpoint, por exemplo `GET /jogos`.

---

## CORS

A API permite requisições cross-origin para todos os endpoints, incluindo chamadas feitas por frontends web.

Configuracao aplicada:

* Origens permitidas: qualquer origem (`*`).
* Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`.
* Headers permitidos: `Content-Type`, `Accept`, `Authorization`, `X-API-Key` e `Idempotency-Key`.
* Headers expostos: headers de rate limit, `Retry-After` e `WWW-Authenticate`.

---

## Versionamento

A API demonstra versionamento por URL com duas versões do endpoint de status:

* `GET /api/v1/status`: contrato simples com versão, status e mensagem.
* `GET /api/v2/status`: contrato expandido com informações dos recursos avançados.

---

## Roteiro Para Demonstração

1. Abrir o Swagger em <https://games-api-3rqr.onrender.com/swagger-ui/index.html>.
2. Mostrar a descrição inicial com os requisitos da Parte I e Parte II.
3. Executar `GET /jogos?page=0&size=5` e mostrar paginação e links HATEOAS.
4. Executar uma consulta personalizada, como `GET /jogos/busca?titulo=catan`.
5. Criar um usuário com `POST /usuarios`.
6. Gerar a chave com `POST /usuarios/{id}/api-key`.
7. Tentar criar uma editora sem `X-API-Key` e demonstrar o erro `401`.
8. Repetir a criaçãoo com `X-API-Key` e demonstrar sucesso.
9. Enviar duas requisições com a mesma `Idempotency-Key`, alterando o JSON na segunda, e demonstrar `409`.
10. Enviar mais de 10 requisições seguidas para demonstrar `429` e o header `Retry-After`.
11. Mostrar o preflight `OPTIONS` na colecao Postman para demonstrar CORS.
12. Comparar `GET /api/v1/status` com `GET /api/v2/status` para demonstrar versionamento.

---

## Como Usar a Coleção Postman

1. Importe o arquivo [`games-api/postman/Games API.postman_collection.json`](games-api/postman/Games%20API.postman_collection.json).
2. Confirme se a variável `baseUrl` está como `https://games-api-3rqr.onrender.com`.
3. Execute primeiro a pasta **Parte II - API Key 401** para criar usuário e salvar a `apiKey`.
4. Execute a pasta **Parte II - Idempotencia 409** para demonstrar conflito por JSON alterado.
5. Execute várias vezes a requisição da pasta **Parte II - Rate Limiting 429** para acionar o bloqueio.

---

## Deploy

O projeto esta publicado no Render:

<https://games-api-3rqr.onrender.com/swagger-ui/index.html>

Para replicar o deploy:

* Configure o projeto no Render usando Docker.
* Use `games-api` como Root Directory.
* O Dockerfile compila o projeto com Maven e executa o JAR com Java 21.
