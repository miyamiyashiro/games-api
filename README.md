# Games API

> **Status do Projeto:** LIVE  
> **Documentacao Oficial:** [Swagger UI](https://games-api-3rqr.onrender.com/swagger-ui/index.html)

API desenvolvida para gestao de acervos de jogos de tabuleiro e RPG, com foco em REST, HATEOAS, validacao de dados, documentacao OpenAPI e deploy conteinerizado.

Autora: Luana Miyashiro Salles de Oliveira

---

## Modelagem de Dados

O sistema foi modelado para demonstrar os principais tipos de relacionamento exigidos na avaliacao:

* **Jogo**: registro central do acervo, com titulo, categoria, editora, plataformas e detalhes complementares.
* **DetalhesJogo**: informacoes complementares de um jogo, em relacionamento One-to-One com Jogo.
* **Usuario**: cliente que pode realizar emprestimos.
* **Editora**: publicadora dos jogos, em relacionamento One-to-Many com Jogo.
* **Emprestimo**: controle de retirada e devolucao, em relacionamento Many-to-One com Usuario e Jogo.
* **Plataforma**: meio ou sistema onde o jogo esta disponivel, em relacionamento Many-to-Many com Jogo.
* **Categoria**: enum com os tipos de jogos cadastrados.

---

## Tecnologias

* Java 21
* Spring Boot 3.4.1
* Spring Data JPA / Hibernate
* H2 Database
* Bean Validation
* Spring HATEOAS
* Springdoc OpenAPI / Swagger
* Docker

---

## Principais Endpoints

| Recurso | Consulta personalizada | Exemplo |
| :--- | :--- | :--- |
| `Jogos` | Busca por parte do titulo | `GET /jogos/busca?titulo=catan` |
| `Usuarios` | Busca exata por e-mail | `GET /usuarios/email/luana@email.com` |
| `Editoras` | Busca por parte do nome | `GET /editoras/busca?nome=galapagos` |
| `Plataformas` | Busca por parte do nome | `GET /plataformas/busca?nome=tabuleiro` |
| `Emprestimos` | Filtro por data | `GET /emprestimos/data?data=2026-04-11` |
| `DetalhesJogo` | Busca pelo ID do jogo | `GET /detalhes-jogos/jogo/1` |

---

## Recursos Avancados

### Autenticacao com Chave de API

Operacoes sensiveis de escrita exigem o header `X-API-Key`. As consultas `GET`, o cadastro de usuario e a geracao da chave ficam publicos para permitir o fluxo inicial.

Fluxo sugerido:

1. Crie um usuario com `POST /usuarios`.
2. Gere a chave com `POST /usuarios/{id}/api-key`.
3. Envie a chave no header `X-API-Key` ao criar, atualizar ou excluir recursos protegidos.

Se a chave estiver ausente ou invalida, a API retorna `HTTP 401 Unauthorized`.

### Rate Limiting

A API limita cada IP a 10 requisicoes por minuto. Se o limite for excedido, o cliente recebe `HTTP 429 Too Many Requests` e fica bloqueado por 30 segundos.

Headers retornados:

* `X-RateLimit-Limit`: limite total da janela.
* `X-RateLimit-Remaining`: requisicoes restantes na janela atual.
* `X-RateLimit-Reset`: momento aproximado de reset da janela, em epoch seconds.
* `Retry-After`: segundos restantes de bloqueio, enviado nas respostas `429`.

Para testar rapidamente, envie mais de 10 requisicoes seguidas para qualquer endpoint, por exemplo `GET /jogos`.

### Idempotencia

Operacoes `POST`, `PUT` e `PATCH` aceitam o header `Idempotency-Key`. A chave identifica uma tentativa de escrita e evita duplicidade quando a mesma requisicao e enviada mais de uma vez.

Comportamento esperado:

* Chave nova: a API processa normalmente a operacao.
* Mesma chave, mesmo endpoint e mesmo JSON: a API retorna `HTTP 200 OK` e ignora o novo processamento.
* Mesma chave com JSON alterado: a API retorna `HTTP 409 Conflict`.
* Mesma chave usada em outro endpoint ou metodo: a API retorna `HTTP 409 Conflict`.

Para testar, envie duas requisicoes `POST /usuarios` com a mesma `Idempotency-Key`. Na segunda tentativa, altere algum campo do JSON para ver o conflito `409`.

---

## Roteiro Sugerido Para Demonstracao

1. Listar editoras e plataformas ja carregadas pela base inicial.
2. Criar uma nova editora.
3. Criar uma nova plataforma.
4. Criar um jogo usando `editoraId` e `plataformaIds`.
5. Criar detalhes para esse jogo usando `jogoId`.
6. Criar um usuario.
7. Criar um emprestimo usando `usuarioId` e `jogoId`.
8. Demonstrar uma consulta personalizada e mostrar os links HATEOAS na resposta.

---

## Deploy

O projeto esta publicado no Render:

[https://games-api-3rqr.onrender.com/swagger-ui/index.html](https://games-api-3rqr.onrender.com/swagger-ui/index.html)

Para replicar o deploy:

* Configure o projeto no Render usando Docker.
* Use `games-api` como Root Directory.
* O Dockerfile compila o projeto com Maven e executa o JAR com Java 21.
