# Games API

> **Status do Projeto:** **LIVE** > Documentação Oficial: [https://games-api-3rqr.onrender.com/swagger-ui/index.html](https://games-api-3rqr.onrender.com/swagger-ui/index.html)

API desenvolvida para gestão de acervos de jogos de tabuleiro e RPG, com foco em Hipermídia (HATEOAS) e conteinerização.
Luana Miyashiro Salles de Oliveira

---

## Modelagem de Dados (Entidades)

O sistema foi modelado para garantir integridade referencial e escalabilidade:

* **Jogo**: Possui título, categoria (Enum) e vínculo com uma Editora.
* **Usuário**: Registro de clientes com validação de e-mail único.
* **Editora**: Gerencia as marcas que publicam os jogos.
* **Empréstimo**: Controla o ciclo de retirada e devolução de itens do acervo.
* **Categoria**: Enumeração (RPG, TABULEIRO, CARTAS, etc).

---

## Guia de Deploy (Render + Docker)
Para replicar o deploy deste projeto no Render, siga estas etapas:

* **Repositório**: Suba o código garantindo que o Dockerfile esteja na raiz da pasta do projeto.
* **Root Directory**: No painel do Render, configure o "Root Directory" como games-api.
* **Runtime**: Selecione Docker como ambiente de execução.
* **Build**: O Render usará o arquivo Dockerfile automaticamente para compilar o Java 21.


---

## Tecnologias e Padrões
* **Java 21 & Spring Boot 3.4.1**: Versões estáveis e modernas.
* **HATEOAS**: Nível 3 de Maturidade de Richardson.
* **JPA/Hibernate**: Mapeamento objeto-relacional automático.
* **Global Exception Handling**: Tratamento de erros customizado.

## Documentação dos Endpoints

Abaixo, os principais recursos disponíveis. Todos suportam **HATEOAS**.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/jogos` | Lista todos os jogos com paginação. |
| `GET` | `/jogos/busca` | **Consulta Personalizada** por título. |
| `GET` | `/jogos/{id}` | Detalha um jogo específico e seus links. |
| `POST` | `/jogos` | Cadastra um novo jogo (Status 201). |
| `PUT` | `/jogos/{id}` | Atualização completa dos dados do jogo. |
| `DELETE` | `/jogos/{id}` | Remoção física do registro no banco H2. |
| `POST` | `/usuarios` | Cadastra um novo usuário com validação `@Valid`. |
| `POST` | `/emprestimos` | Registra um empréstimo com data automática. |
| `POST` | `/emprestimos` | Realiza empréstimo com data automática. |


### Exemplo de Resposta (HATEOAS)
```json
{
  "id": 1,
  "titulo": "Catan",
  "categoria": "TABULEIRO",
  "_links": {
    "self": { "href": "[https://games-api-3rqr.onrender.com/jogos/1](https://games-api-3rqr.onrender.com/jogos/1)" },
    "lista": { "href": "[https://games-api-3rqr.onrender.com/jogos?page=0&size=20](https://games-api-3rqr.onrender.com/jogos?page=0&size=20)" }
  }
}

