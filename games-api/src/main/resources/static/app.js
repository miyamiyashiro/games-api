const state = {
  baseUrl: localStorage.getItem("gamesApiBaseUrl") || "https://games-api-3rqr.onrender.com",
  apiKey: localStorage.getItem("gamesApiKey") || "",
  usuarioId: localStorage.getItem("gamesApiUsuarioId") || "",
  idempotencyKey: `frontend-demo-idempotencia-${Date.now()}`,
  idempotencyEmail: `frontend-idempotencia-${Date.now()}@email.com`
};

const $ = (selector) => document.querySelector(selector);

const elements = {
  baseUrl: $("#baseUrl"),
  saveBaseUrl: $("#saveBaseUrl"),
  apiHealth: $("#apiHealth"),
  gamesGrid: $("#gamesGrid"),
  searchTitle: $("#searchTitle"),
  searchGames: $("#searchGames"),
  loadGames: $("#loadGames"),
  loadCatalogs: $("#loadCatalogs"),
  publishersList: $("#publishersList"),
  platformsList: $("#platformsList"),
  gameForm: $("#gameForm"),
  userForm: $("#userForm"),
  apiKey: $("#apiKey"),
  copyKey: $("#copyKey"),
  responseLog: $("#responseLog"),
  clearLog: $("#clearLog"),
  test401: $("#test401"),
  test409First: $("#test409First"),
  test409Second: $("#test409Second"),
  test429: $("#test429"),
  loadVersions: $("#loadVersions"),
  versionOne: $("#versionOne"),
  versionTwo: $("#versionTwo")
};

elements.baseUrl.value = state.baseUrl;
elements.apiKey.value = state.apiKey;
elements.userForm.email.value = `frontend-${Date.now()}@email.com`;

function normalizeBaseUrl(value) {
  return value.trim().replace(/\/+$/, "");
}

function endpoint(path) {
  return `${state.baseUrl}${path}`;
}

function logResponse(label, status, data, headers = {}) {
  const payload = {
    chamada: label,
    status,
    headers,
    resposta: data
  };
  elements.responseLog.textContent = JSON.stringify(payload, null, 2);
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function request(path, options = {}) {
  const response = await fetch(endpoint(path), options);
  const data = await parseResponse(response);
  const headers = {
    retryAfter: response.headers.get("Retry-After"),
    rateLimitLimit: response.headers.get("X-RateLimit-Limit"),
    rateLimitRemaining: response.headers.get("X-RateLimit-Remaining"),
    wwwAuthenticate: response.headers.get("WWW-Authenticate")
  };

  return {
    ok: response.ok,
    status: response.status,
    data,
    headers
  };
}

function extractItems(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (payload?._embedded) {
    const firstList = Object.values(payload._embedded).find(Array.isArray);
    return firstList || [];
  }

  return [];
}

function itemTitle(item) {
  return item.titulo || item.nome || item.email || `Registro #${item.id || "sem ID"}`;
}

function renderGames(items) {
  elements.gamesGrid.innerHTML = "";

  if (!items.length) {
    elements.gamesGrid.innerHTML = '<div class="empty-state">Nenhum jogo encontrado.</div>';
    return;
  }

  items.forEach((game) => {
    const card = document.createElement("article");
    card.className = "game-card";

    const editora = game.editora?.nome || "Editora nao informada";
    const plataformas = Array.isArray(game.plataformas)
      ? game.plataformas.map((platform) => platform.nome).join(", ")
      : "Plataformas nao informadas";

    card.innerHTML = `
      <span class="badge">${game.categoria || "SEM_CATEGORIA"}</span>
      <strong>${itemTitle(game)}</strong>
      <div class="meta-list">
        <span>ID: ${game.id ?? "-"}</span>
        <span>Editora: ${editora}</span>
        <span>Plataformas: ${plataformas || "-"}</span>
      </div>
    `;
    elements.gamesGrid.appendChild(card);
  });
}

function renderList(target, items) {
  target.innerHTML = "";

  if (!items.length) {
    target.innerHTML = "<li>Nenhum registro encontrado.</li>";
    return;
  }

  items.slice(0, 8).forEach((item) => {
    const li = document.createElement("li");
    li.textContent = `#${item.id ?? "-"} - ${itemTitle(item)}`;
    target.appendChild(li);
  });
}

async function loadGames() {
  elements.gamesGrid.innerHTML = '<div class="empty-state">Carregando jogos...</div>';

  try {
    const result = await request("/jogos?page=0&size=8");
    renderGames(extractItems(result.data));
    logResponse("GET /jogos?page=0&size=8", result.status, result.data, result.headers);
    elements.apiHealth.textContent = result.ok ? "Online" : `HTTP ${result.status}`;
  } catch (error) {
    elements.gamesGrid.innerHTML = '<div class="error-state">Nao foi possivel carregar os jogos.</div>';
    elements.apiHealth.textContent = "Erro de conexao";
    logResponse("GET /jogos", "erro", error.message);
  }
}

async function searchGames() {
  const title = elements.searchTitle.value.trim();
  if (!title) {
    await loadGames();
    return;
  }

  const result = await request(`/jogos/busca?titulo=${encodeURIComponent(title)}`);
  renderGames(extractItems(result.data));
  logResponse(`GET /jogos/busca?titulo=${title}`, result.status, result.data, result.headers);
}

async function loadCatalogs() {
  const [publishers, platforms] = await Promise.all([
    request("/editoras?page=0&size=8"),
    request("/plataformas?page=0&size=8")
  ]);

  renderList(elements.publishersList, extractItems(publishers.data));
  renderList(elements.platformsList, extractItems(platforms.data));
  logResponse("GET /editoras + GET /plataformas", `${publishers.status}/${platforms.status}`, {
    editoras: publishers.data,
    plataformas: platforms.data
  });
}

async function createUser(event) {
  event.preventDefault();

  const body = {
    nome: elements.userForm.nome.value.trim(),
    email: elements.userForm.email.value.trim()
  };

  const userResult = await request("/usuarios", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": `frontend-usuario-${Date.now()}`
    },
    body: JSON.stringify(body)
  });

  if (!userResult.ok) {
    logResponse("POST /usuarios", userResult.status, userResult.data, userResult.headers);
    return;
  }

  const usuarioId = userResult.data?.id;
  state.usuarioId = usuarioId;
  localStorage.setItem("gamesApiUsuarioId", usuarioId);

  const keyResult = await request(`/usuarios/${usuarioId}/api-key`, {
    method: "POST"
  });

  if (keyResult.data?.apiKey) {
    state.apiKey = keyResult.data.apiKey;
    elements.apiKey.value = state.apiKey;
    localStorage.setItem("gamesApiKey", state.apiKey);
  }

  logResponse("POST /usuarios + POST /usuarios/{id}/api-key", `${userResult.status}/${keyResult.status}`, {
    usuario: userResult.data,
    chave: keyResult.data
  }, keyResult.headers);
}

async function createGame(event) {
  event.preventDefault();

  const form = new FormData(elements.gameForm);
  const body = {
    titulo: form.get("titulo"),
    categoria: form.get("categoria"),
    editoraId: Number(form.get("editoraId")),
    plataformaIds: [Number(form.get("plataformaId"))]
  };

  const result = await request("/jogos", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-API-Key": elements.apiKey.value.trim(),
      "Idempotency-Key": `frontend-jogo-${Date.now()}`
    },
    body: JSON.stringify(body)
  });

  logResponse("POST /jogos", result.status, result.data, result.headers);

  if (result.ok) {
    await loadGames();
  }
}

async function test401() {
  const result = await request("/editoras", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": `frontend-401-${Date.now()}`
    },
    body: JSON.stringify({ nome: `Editora sem chave ${Date.now()}` })
  });

  logResponse("POST /editoras sem X-API-Key", result.status, result.data, result.headers);
}

async function test409First() {
  const result = await request("/usuarios", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": state.idempotencyKey
    },
    body: JSON.stringify({
      nome: "Idempotencia Original",
      email: state.idempotencyEmail
    })
  });

  logResponse("POST /usuarios com Idempotency-Key", result.status, result.data, result.headers);
}

async function test409Second() {
  const result = await request("/usuarios", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": state.idempotencyKey
    },
    body: JSON.stringify({
      nome: "Idempotencia Alterada",
      email: state.idempotencyEmail
    })
  });

  logResponse("POST /usuarios mesma chave com JSON alterado", result.status, result.data, result.headers);
}

async function test429() {
  const results = [];

  for (let index = 1; index <= 12; index += 1) {
    const result = await request("/jogos?page=0&size=1");
    results.push({
      tentativa: index,
      status: result.status,
      retryAfter: result.headers.retryAfter,
      restante: result.headers.rateLimitRemaining
    });

    if (result.status === 429) {
      break;
    }
  }

  logResponse("12 chamadas para testar rate limiting", "resultado", results);
}

async function loadVersions() {
  const [v1, v2] = await Promise.all([
    request("/api/v1/status"),
    request("/api/v2/status")
  ]);

  elements.versionOne.textContent = JSON.stringify(v1.data, null, 2);
  elements.versionTwo.textContent = JSON.stringify(v2.data, null, 2);
  logResponse("GET /api/v1/status + GET /api/v2/status", `${v1.status}/${v2.status}`, {
    v1: v1.data,
    v2: v2.data
  });
}

elements.saveBaseUrl.addEventListener("click", () => {
  state.baseUrl = normalizeBaseUrl(elements.baseUrl.value);
  elements.baseUrl.value = state.baseUrl;
  localStorage.setItem("gamesApiBaseUrl", state.baseUrl);
  loadGames();
});

elements.loadGames.addEventListener("click", loadGames);
elements.searchGames.addEventListener("click", searchGames);
elements.searchTitle.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    event.preventDefault();
    searchGames();
  }
});
elements.loadCatalogs.addEventListener("click", loadCatalogs);
elements.userForm.addEventListener("submit", createUser);
elements.gameForm.addEventListener("submit", createGame);
elements.test401.addEventListener("click", test401);
elements.test409First.addEventListener("click", test409First);
elements.test409Second.addEventListener("click", test409Second);
elements.test429.addEventListener("click", test429);
elements.loadVersions.addEventListener("click", loadVersions);
elements.clearLog.addEventListener("click", () => {
  elements.responseLog.textContent = "Nenhuma chamada executada ainda.";
});
elements.copyKey.addEventListener("click", async () => {
  await navigator.clipboard.writeText(elements.apiKey.value);
  elements.copyKey.textContent = "Copiado";
  setTimeout(() => {
    elements.copyKey.textContent = "Copiar";
  }, 1400);
});
elements.apiKey.addEventListener("input", () => {
  state.apiKey = elements.apiKey.value.trim();
  localStorage.setItem("gamesApiKey", state.apiKey);
});

loadGames();
loadCatalogs();
loadVersions();
