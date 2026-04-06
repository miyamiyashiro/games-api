# 🎮 Acervo Nerdola API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.1-green?style=for-the-badge&logo=springboot" alt="Spring Boot 3">
  <img src="https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/Render-Live-brightgreen?style=for-the-badge&logo=render" alt="Render Live">
</p>

> **Status do Projeto:** 🟢 Link Online: [https://games-api-3rqr.onrender.com/swagger-ui/index.html](https://games-api-3rqr.onrender.com/swagger-ui/index.html)

API robusta desenvolvida para a gestão de acervos de jogos de tabuleiro e RPG. O projeto foca em boas práticas de design de API, utilizando o nível 3 da maturidade de Richardson (HATEOAS).

## 🛠️ Tecnologias Utilizadas
- **Java 21** (LTS)
- **Spring Boot 3.4.1**
- **Spring Data JPA** (Persistência)
- **H2 Database** (Banco em memória para testes)
- **Spring HATEOAS** (Links dinâmicos)
- **Bean Validation** (Validação de dados)
- **Docker** (Containerização)
- **SpringDoc/Swagger** (Documentação)

## 🏗️ Arquitetura e Funcionalidades
- **Modelo de Dados**: Possui relacionamentos complexos como `@ManyToOne` (Jogos -> Editoras) e `@OneToMany`.
- **HATEOAS**: Todos os recursos retornam links de navegação automática.
- **Data Seeding**: Banco de dados populado automaticamente ao iniciar (`LoadDatabase`).
- **Global Exception Handling**: Tratamento de erros centralizado para retornos HTTP precisos.

## 📖 Exemplo de Uso (Endpoints)

### Listar Jogos (Com Paginação e HATEOAS)
`GET /jogos?page=0&size=5`

**Resposta de Exemplo:**
```json
{
  "_embedded": {
    "jogoList": [
      {
        "id": 1,
        "titulo": "Catan",
        "categoria": "TABULEIRO",
        "_links": {
          "self": { "href": "[https://games-api-3rqr.onrender.com/jogos/1](https://games-api-3rqr.onrender.com/jogos/1)" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "[https://games-api-3rqr.onrender.com/jogos?page=0&size=5](https://games-api-3rqr.onrender.com/jogos?page=0&size=5)" }
  }
}
