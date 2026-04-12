# Games API

> **Status do Projeto:** **LIVE** > Documentação Oficial: [https://games-api-3rqr.onrender.com/swagger-ui/index.html](https://games-api-3rqr.onrender.com/swagger-ui/index.html)

API desenvolvida para gestão de acervos de jogos de tabuleiro e RPG, com foco em Hipermídia (HATEOAS) e conteinerização.
Luana Miyashiro Salles de Oliveira

---

## Modelagem de Dados (Entidades)

O sistema foi modelado para garantir integridade referencial e escalabilidade:

* **Jogo**: Registro central com título, categoria (Enum) e vínculos com Editora e Plataformas.
* **Usuário**: Gestão de clientes com validação de campos obrigatórios e e-mail.
* **Editora**: Mapeamento das publicadoras dos jogos (One-to-Many com Jogo).
* **Empréstimo**: Controle de transações de retirada e devolução (Many-to-One com Usuário e Jogo).
* **Categoria**: Enumeração (RPG, TABULEIRO, CARTAS, etc).
* **Plataforma**: Consoles e sistemas onde o jogo está disponível (Many-to-Many com Jogo).


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
* **Spring Data JPA**: Abstração de persistência com banco de dados H2 (In-memory).
* **Springdoc OpenAPI (Swagger)**: Documentação técnica detalhada e testável.
* **Bean Validation**: Validação rigorosa de dados na entrada da API.

---


## Documentação dos Endpoints

Abaixo, os principais recursos disponíveis. Todos suportam **HATEOAS**.

| Recurso | Tipo de Consulta Personalizada | Endpoint Exemplo |
| :--- | :--- | :--- |
| `Jogos` | `Busca por Título (Case Insensitive)` | GET /jogos/busca?titulo=catan. |
| `Usuários` | `Busca exata por E-mail` | GET /usuarios/email/barbara@gmail.com. |
| `Editoras` | `Busca por parte do Nome` | GET /editoras/busca?nome=galapagos. |
| `Plataformas` | `Busca por Nome do Console` | GET /plataformas/busca?nome=PS5. |
| `Empréstimos` | `Filtro por Data de Registro` | GET /emprestimos/data?data=2026-04-11. |


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

