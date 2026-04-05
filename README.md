# 🎮 Acervo Nerdola API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3-green?style=for-the-badge&logo=springboot" alt="Spring Boot 3">
  <img src="https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker" alt="Docker">
</p>

API desenvolvida para a gestão de acervo de jogos de tabuleiro e RPG, permitindo o controle de usuários, editoras e o ciclo completo de empréstimos.

## 🛠️ Funcionalidades
- **Gestão de Acervo**: Cadastro completo de jogos com categorias (Tabuleiro/RPG).
- **Controle de Usuários**: Registro de jogadores e histórico.
- **Sistema de Empréstimos**: Registro de retirada e data de devolução.
- **HATEOAS**: Navegação facilitada entre recursos através de links dinâmicos.
- **Paginação**: Endpoints otimizados para grandes volumes de dados.

## 🚀 Como Executar o Projeto

### Localmente
1. Certifique-se de ter o **Java 21** instalado.
2. Clone o repositório e abra no IntelliJ.
3. Execute a classe `GamesApiApplication`.
4. Acesse: `http://localhost:8080`

### Via Docker (Opcional)
```bash
docker build -t games-api .
docker run -p 8080:8080 games-api

## 📖 Documentação da API
A documentação interativa (Swagger) pode ser acessada em tempo de execução:
👉 http://localhost:8080/swagger-ui/index.html

Desenvolvido por Luana Miyashiro como projeto da disciplina de Programação de Dispositivos Móveis.
