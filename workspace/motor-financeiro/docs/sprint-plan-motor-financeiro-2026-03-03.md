# Sprint Plan: Motor Financeiro

**Date:** 2026-03-03
**Scrum Master:** rafael.giovannini
**Project Level:** 2 (Medium)
**Total Stories:** 24
**Total Points:** 90
**Planned Phases:** 6
**Team:** 1 desenvolvedor solo (Pleno)
**Formato:** Fases de entrega (milestones), sem sprints rígidos

---

## Executive Summary

Plano de implementação do Motor Financeiro organizado em 6 fases de entrega sequenciais, respeitando dependências técnicas e priorizando valor incremental. Como projeto solo sem equipe, as fases substituem sprints formais — cada fase tem ~2 semanas de trabalho estimado, mas sem timeboxing rígido. O critério de avanço é a conclusão das stories da fase, não o calendário.

**Key Metrics:**
- Total Stories: 24 (3 infraestrutura + 21 funcionalidades)
- Total Points: 90
- Fases: 6
- Capacidade Estimada: ~15 pontos por fase (5h produtivas/dia, 1pt ≈ 3h)
- Estimativa de Conclusão: ~12 semanas (3 meses)

**Decisão Arquitetural:** API construída 100% com ASP.NET Core Controllers (`[ApiController]`), sem Minimal APIs. Garante consistência e padrão único em todos os endpoints.

---

## Story Inventory

### Infrastructure Stories

#### STORY-INF-001: Project Scaffolding

**Epic:** Infrastructure
**Priority:** Must Have
**Points:** 3

**User Story:**
Como desenvolvedor,
Quero a estrutura do projeto criada com Clean Architecture,
Para que eu possa começar a implementar funcionalidades sobre uma base sólida.

**Acceptance Criteria:**
- [ ] .NET 10 solution com camadas: Domain, Application, Infrastructure, API
- [ ] Angular 19+ app inicializado com standalone components
- [ ] Docker Compose configurado (postgres, mongodb, redis, api, spa)
- [ ] Estrutura de pastas conforme CLAUDE.md (`src/`, `tests/`, `specs/`, `docs/`)
- [ ] `.editorconfig`, `global.json`, `Directory.Build.props` configurados
- [ ] Swagger/OpenAPI configurado no API project

**Technical Notes:**
- ASP.NET Core Controllers (`[ApiController]`) para todos os endpoints — sem Minimal APIs
- Angular Material como UI framework base
- Docker Compose com health checks em todos os containers

**Dependencies:** Nenhuma (ponto de partida)

---

#### STORY-INF-002: Database Schema & Migrations

**Epic:** Infrastructure
**Priority:** Must Have
**Points:** 5

**User Story:**
Como desenvolvedor,
Quero o schema do banco de dados criado com migrations,
Para que as entidades de domínio estejam persistidas corretamente.

**Acceptance Criteria:**
- [ ] Entity Framework Core 10 configurado com PostgreSQL
- [ ] Todas as 9 entidades mapeadas (User, RefreshToken, Connection, Account, Transaction, Category, CategorizationRule, SyncHistory, UserPreference)
- [ ] Migrations iniciais criadas e aplicáveis
- [ ] Indexes estratégicos criados (conforme architecture doc)
- [ ] Row-Level Security configurado no PostgreSQL
- [ ] MongoDB collections criadas (audit_logs, app_logs) com TTL indexes
- [ ] Seed data: categorias padrão (Moradia, Alimentação, Transporte, etc.)
- [ ] UUID v7 como tipo de ID em todas as entidades

**Technical Notes:**
- Usar value objects para Money, Email (EF Core owned types)
- Configurar connection pooling (Npgsql, pool size = 20)
- Full-text search index (GIN) para description de transações

**Dependencies:** STORY-INF-001

---

#### STORY-INF-003: Core/Shared Infrastructure

**Epic:** Infrastructure
**Priority:** Must Have
**Points:** 5

**User Story:**
Como desenvolvedor,
Quero os serviços cross-cutting implementados,
Para que todos os módulos tenham encryption, logging e auditoria desde o início.

**Acceptance Criteria:**
- [ ] `IEncryptionService` com AES-256-GCM (encrypt/decrypt strings)
- [ ] `IAuditService` com log para MongoDB (who, what, when, where)
- [ ] Serilog configurado com sink MongoDB (structured JSON)
- [ ] Correlation ID middleware (gera no request, propaga via AsyncLocal)
- [ ] Error handling: Result pattern + ProblemDetails (RFC 7807)
- [ ] `ICurrentUser` extrai user_id do JWT (injetável em serviços)
- [ ] MediatR configurado para comunicação entre módulos
- [ ] Secret masking em logs (tokens, passwords → [REDACTED])
- [ ] FluentValidation pipeline configurado
- [ ] Global exception handler middleware

**Technical Notes:**
- Encryption key via variável de ambiente (não no código)
- Log levels configuráveis por módulo via appsettings.json
- Testes unitários para EncryptionService e AuditService

**Dependencies:** STORY-INF-001, STORY-INF-002

---

### EPIC-001: Autenticação e Gestão de Usuário

#### STORY-001: Cadastro de Usuário

**Epic:** EPIC-001 — Autenticação e Gestão de Usuário
**Priority:** Must Have
**Points:** 5

**User Story:**
Como usuário novo,
Quero me cadastrar com email e senha,
Para ter acesso ao sistema de gestão financeira.

**Acceptance Criteria:**
- [ ] Endpoint `POST /api/v1/auth/register` funcional
- [ ] Validação de formato de email e força de senha (min 8 chars, maiúscula, número, especial)
- [ ] Password hashing com Argon2id (memory: 64MB, iterations: 3, parallelism: 1)
- [ ] Email de confirmação enviado via Resend
- [ ] Token de confirmação com expiração (24h)
- [ ] Endpoint `GET /api/v1/auth/confirm-email` funcional
- [ ] Duplicatas de email rejeitadas com mensagem clara
- [ ] Dados sensíveis armazenados encriptados
- [ ] Frontend: formulário de registro com validação inline
- [ ] Frontend: página de confirmação de email
- [ ] Testes unitários e de integração (≥90% cobertura)

**Technical Notes:**
- FluentValidation para validação de input
- Registro de audit log (ação: USER_REGISTERED)
- Rate limiting no endpoint de registro (prevenir spam)

**Dependencies:** STORY-INF-003

---

#### STORY-002: Login/Logout com Sessão Segura

**Epic:** EPIC-001 — Autenticação e Gestão de Usuário
**Priority:** Must Have
**Points:** 5

**User Story:**
Como usuário registrado,
Quero fazer login de forma segura,
Para acessar minhas finanças com confiança.

**Acceptance Criteria:**
- [ ] Endpoint `POST /api/v1/auth/login` retorna JWT (RS256, 15min) + refresh token
- [ ] Refresh token: opaque string (64 bytes), hash SHA-256 no DB, 7 dias TTL
- [ ] Endpoint `POST /api/v1/auth/refresh` com token rotation
- [ ] Reuse detection: token reutilizado → revoga toda a família
- [ ] Endpoint `POST /api/v1/auth/logout` invalida sessão (blacklist em Redis)
- [ ] Rate limiting: max 5 tentativas falhadas por minuto/IP (Redis sliding window)
- [ ] Frontend: formulário de login
- [ ] Frontend: access token em memória, refresh token em httpOnly secure SameSite=Strict cookie
- [ ] Frontend: interceptor HTTP para auto-refresh silencioso
- [ ] Frontend: redirect para login quando sessão expira
- [ ] Audit log: LOGIN, LOGOUT, LOGIN_FAILED
- [ ] Testes: token lifecycle completo, rate limiting, reuse detection

**Technical Notes:**
- RSA key pair para assinatura JWT (gerar e armazenar em env var)
- Redis para rate limiting counters e token blacklist
- Auth guard no Angular para rotas protegidas

**Dependencies:** STORY-001

---

#### STORY-003: Recuperação de Senha

**Epic:** EPIC-001 — Autenticação e Gestão de Usuário
**Priority:** Should Have
**Points:** 3

**User Story:**
Como usuário,
Quero recuperar minha senha caso a esqueça,
Para não perder acesso à minha conta.

**Acceptance Criteria:**
- [ ] Endpoint `POST /api/v1/auth/forgot-password` aceita email e envia link de recuperação
- [ ] Token de reset temporário com expiração de 1 hora
- [ ] Endpoint `POST /api/v1/auth/reset-password` define nova senha
- [ ] Sessões anteriores invalidadas após troca de senha (revoga todos os refresh tokens)
- [ ] Frontend: formulário "esqueci minha senha"
- [ ] Frontend: formulário de nova senha (via link do email)
- [ ] Não revelar se email existe ou não (segurança)
- [ ] Testes: fluxo completo, token expirado, reuso de token

**Technical Notes:**
- Email enviado via Resend
- Audit log: PASSWORD_RESET_REQUESTED, PASSWORD_RESET_COMPLETED

**Dependencies:** STORY-002

---

#### STORY-004: Visualização de Perfil e Dados da Conta

**Epic:** EPIC-001 — Autenticação e Gestão de Usuário
**Priority:** Should Have
**Points:** 3

**User Story:**
Como usuário,
Quero visualizar e editar meus dados pessoais,
Para manter minhas informações atualizadas.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/users/me` retorna dados do perfil
- [ ] Endpoint `PATCH /api/v1/users/me` atualiza nome e dados pessoais
- [ ] Alteração de email requer confirmação por email (novo email)
- [ ] Endpoint `DELETE /api/v1/users/me` inicia exclusão de conta (LGPD)
- [ ] Soft delete (30 dias) + hard delete automático via Hangfire job
- [ ] Endpoint `GET /api/v1/users/me/export` exporta todos os dados (JSON)
- [ ] Frontend: página de perfil com formulário de edição
- [ ] Frontend: modal de confirmação para exclusão de conta
- [ ] Audit log: PROFILE_UPDATED, ACCOUNT_DELETION_REQUESTED, DATA_EXPORTED
- [ ] Testes: CRUD, LGPD flows (export + delete)

**Technical Notes:**
- Hard delete cascading: PostgreSQL (user + all related data) + MongoDB (audit logs)
- Export inclui: dados pessoais, contas, transações, categorias, regras

**Dependencies:** STORY-002

---

### EPIC-002: Integração Open Finance

#### STORY-005: Conexão com Instituição Financeira via Open Finance

**Epic:** EPIC-002 — Integração Open Finance
**Priority:** Must Have
**Points:** 8

**User Story:**
Como usuário,
Quero conectar meus bancos via Open Finance,
Para que minhas movimentações sejam capturadas automaticamente.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/institutions` lista instituições suportadas (via Pluggy)
- [ ] Endpoint `POST /api/v1/connections` inicia fluxo de consentimento (retorna connect URL)
- [ ] OAuth redirect para Pluggy funcional
- [ ] Callback/webhook de sucesso processa e armazena conexão
- [ ] Tokens Pluggy encriptados com AES-256 antes de salvar no DB
- [ ] Endpoint `GET /api/v1/connections` lista conexões do usuário
- [ ] Endpoint `GET /api/v1/connections/{id}` retorna detalhes
- [ ] `IOpenFinanceProvider` interface (abstração sobre Pluggy)
- [ ] Polly retry (3x backoff exponencial) + circuit breaker
- [ ] Frontend: tela de seleção de instituição
- [ ] Frontend: fluxo de redirect OAuth e retorno
- [ ] Frontend: confirmação de conexão bem-sucedida
- [ ] Audit log: CONNECTION_CREATED, CONNECTION_FAILED
- [ ] Testes de integração com mock da Pluggy API

**Technical Notes:**
- Pluggy Connect Widget pode ser embeddable no frontend (verificar SDK)
- Webhook endpoint para notificações da Pluggy
- Circuit breaker: abre após 5 falhas consecutivas, half-open após 30s

**Dependencies:** STORY-002 (autenticação necessária)

---

#### STORY-006: Listagem de Contas Bancárias Conectadas

**Epic:** EPIC-002 — Integração Open Finance
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero ver todas as minhas contas bancárias conectadas com seus saldos,
Para ter visão rápida de onde estão meus recursos.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/accounts` lista todas as contas conectadas
- [ ] Exibe: nome da conta, tipo (corrente, poupança), instituição, saldo, moeda
- [ ] Saldos atualizados na sincronização
- [ ] Frontend: componente de lista de contas com ícones das instituições
- [ ] Frontend: saldo formatado em BRL
- [ ] Isolamento de dados: apenas contas do usuário autenticado
- [ ] Testes: listing, ownership validation

**Technical Notes:**
- Dados de contas vêm da Pluggy API durante sync
- Cache em Redis (TTL: 5 min) para reduzir queries

**Dependencies:** STORY-005

---

#### STORY-007: Sincronização Automática de Movimentações

**Epic:** EPIC-002 — Integração Open Finance
**Priority:** Must Have
**Points:** 5

**User Story:**
Como usuário,
Quero que minhas transações sejam sincronizadas periodicamente,
Para ter dados sempre atualizados sem intervenção manual.

**Acceptance Criteria:**
- [ ] Hangfire recurring job: sincroniza a cada 6 horas (configurável)
- [ ] Busca transações da Pluggy API por conexão
- [ ] Deduplicação por hash SHA-256 (external_id + account_id)
- [ ] Endpoint `POST /api/v1/sync` para sincronização manual
- [ ] Endpoint `GET /api/v1/sync/history` retorna histórico de syncs
- [ ] Retry com backoff exponencial (Polly) para falhas temporárias
- [ ] Máximo 3 retries antes de marcar como falha
- [ ] Batch processing: transações em chunks de 100
- [ ] Job concurrency limit: 1 sync por conexão
- [ ] Erros logados com detalhes, usuário notificado na UI
- [ ] Frontend: indicador de status da sync, botão "sincronizar agora"
- [ ] Frontend: histórico de sincronizações (última sync, status, quantidade)
- [ ] Testes: dedup, error scenarios, scheduling

**Technical Notes:**
- Após sync completa: disparar evento MediatR para categorização em batch
- Invalidar cache Redis do dashboard após novas transações
- SyncHistory registra: status (success/partial/failed), transactions_count, error_message

**Dependencies:** STORY-005, STORY-006

---

#### STORY-008: Gestão de Consentimentos

**Epic:** EPIC-002 — Integração Open Finance
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero gerenciar os consentimentos das minhas contas conectadas,
Para manter controle sobre quem acessa meus dados bancários.

**Acceptance Criteria:**
- [ ] Lista de consentimentos ativos com data de expiração
- [ ] Alerta quando consentimento está próximo de expirar (7 dias)
- [ ] Endpoint `POST /api/v1/connections/{id}/renew` redireciona para re-OAuth
- [ ] Endpoint `DELETE /api/v1/connections/{id}` revoga consentimento na Pluggy + DB
- [ ] Status claro: ativo, expirado, revogado
- [ ] Dados já importados permanecem após revogação
- [ ] Frontend: lista de consentimentos com status visual (badge)
- [ ] Frontend: botões renovar/revogar com confirmação
- [ ] Frontend: alerta visual para consentimentos próximos de expirar
- [ ] Audit log: CONSENT_RENEWED, CONSENT_REVOKED
- [ ] Testes: lifecycle states, revogação

**Technical Notes:**
- Hangfire job diário para verificar expiração de consentimentos
- Notificação na UI (não email na Fase 1)

**Dependencies:** STORY-005

---

#### STORY-009: Importação Manual de Movimentações (OFX/CSV)

**Epic:** EPIC-002 — Integração Open Finance
**Priority:** Should Have
**Points:** 5

**User Story:**
Como usuário,
Quero importar extratos bancários manualmente via arquivo,
Para ter histórico financeiro mesmo de bancos sem Open Finance.

**Acceptance Criteria:**
- [ ] Endpoint `POST /api/v1/import` aceita upload de OFX e CSV
- [ ] Parser para formato OFX (Open Financial Exchange)
- [ ] Parser para CSV (com detecção de colunas: data, descrição, valor)
- [ ] Endpoint `POST /api/v1/import/preview` retorna preview das transações extraídas
- [ ] Deduplicação com transações já existentes (hash)
- [ ] Validação: extensão, MIME type, tamanho máximo (10MB)
- [ ] Frontend: componente de upload com drag-and-drop
- [ ] Frontend: tabela de preview com opção de confirmar/cancelar
- [ ] Transações importadas são categorizáveis normalmente
- [ ] Testes: vários formatos OFX/CSV, edge cases, arquivos inválidos

**Technical Notes:**
- Content scanning para arquivos suspeitos
- Associar transações importadas à conta "manual" (criada automaticamente)
- Trigger categorização automática após import confirmado

**Dependencies:** STORY-002, STORY-INF-002

---

### EPIC-003: Motor de Categorização IA

#### STORY-010: Gestão de Categorias Customizáveis

**Epic:** EPIC-003 — Motor de Categorização IA
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero criar e personalizar categorias de transação,
Para organizar meus gastos da forma que faz sentido para mim.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/categories` lista categorias (padrão + custom)
- [ ] Endpoint `POST /api/v1/categories` cria categoria customizada (nome, cor, ícone)
- [ ] Endpoint `PATCH /api/v1/categories/{id}` edita categoria
- [ ] Endpoint `DELETE /api/v1/categories/{id}` exclui (move transações para "Outros" ou categoria escolhida)
- [ ] Categorias padrão pré-definidas não podem ser excluídas (apenas desativadas)
- [ ] Limite de 50 categorias customizadas por usuário
- [ ] Frontend: lista de categorias com cores/ícones
- [ ] Frontend: modal de criar/editar categoria
- [ ] Testes: CRUD, constraints, deletion com migração de transações

**Technical Notes:**
- Categorias padrão: Moradia, Alimentação, Transporte, Saúde, Educação, Lazer, Utilidades, Renda, Transferência, Outros
- Seed via migration (user_id = NULL para padrão do sistema)
- Audit log: CATEGORY_CREATED, CATEGORY_UPDATED, CATEGORY_DELETED

**Dependencies:** STORY-INF-002

---

#### STORY-011: Categorização Automática de Transações

**Epic:** EPIC-003 — Motor de Categorização IA
**Priority:** Must Have
**Points:** 5

**User Story:**
Como usuário,
Quero que minhas transações sejam categorizadas automaticamente,
Para não precisar classificar manualmente cada uma.

**Acceptance Criteria:**
- [ ] `ICategorizer` interface (Strategy pattern)
- [ ] Implementação GPT-4o-mini: envia descrição + merchant → recebe categoria + confiança
- [ ] Batch categorization: até 20 transações por request à API
- [ ] Cada transação recebe: category_id, confidence_score (0-1)
- [ ] Acurácia mínima >= 80% sem intervenção manual
- [ ] Transações com confiança < 0.7 marcadas para revisão
- [ ] Hangfire job: categoriza em batch após cada sync
- [ ] Regras de categorização locais consultadas ANTES de chamar API (economia)
- [ ] Fallback: se API indisponível, marcar como "Outros" + low confidence
- [ ] Frontend: indicador de confiança na lista de transações
- [ ] Frontend: filtro "precisa revisão" (low confidence)
- [ ] Testes com mock de respostas da OpenAI

**Technical Notes:**
- Prompt engineering: enviar descrição, valor, data → receber categoria do set permitido
- Cache de resultados (mesma descrição exata → mesma categoria) por 30 dias
- Retry 2x para falhas da API, com Polly

**Dependencies:** STORY-010, STORY-007

---

#### STORY-012: Correção Manual de Categoria

**Epic:** EPIC-003 — Motor de Categorização IA
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero corrigir a categoria de uma transação,
Para que o sistema aprenda e melhore com o tempo.

**Acceptance Criteria:**
- [ ] Endpoint `PATCH /api/v1/transactions/{id}/category` altera categoria
- [ ] Correção registrada no histórico (categoria original → nova categoria)
- [ ] Flag `is_manual_category = true` na transação
- [ ] Endpoint `POST /api/v1/transactions/{id}/apply-similar` aplica mesma categoria a transações com mesma descrição/merchant
- [ ] Frontend: dropdown de categoria com 1-2 cliques na lista de transações
- [ ] Frontend: botão "aplicar a similares" com preview de quantas serão afetadas
- [ ] Audit log: CATEGORY_CORRECTED (old → new)
- [ ] Testes: correção, apply-similar, histórico

**Technical Notes:**
- Dados de correção alimentam o feedback loop (STORY-013)
- Apply-similar: buscar transações com mesma `description` e `is_manual_category = false`

**Dependencies:** STORY-011

---

#### STORY-013: Aprendizado Baseado em Correções (Feedback Loop)

**Epic:** EPIC-003 — Motor de Categorização IA
**Priority:** Should Have
**Points:** 3

**User Story:**
Como usuário,
Quero que o sistema melhore a categorização com base nas minhas correções,
Para que com o tempo eu precise corrigir cada vez menos.

**Acceptance Criteria:**
- [ ] Sistema identifica padrões: mesma descrição/merchant → mesma categoria (após N correções)
- [ ] Após 3 correções consistentes para um padrão, cria `CategorizationRule`
- [ ] Regras são por usuário (personalização individual)
- [ ] Regras consultadas ANTES da API de IA (economia + personalização)
- [ ] Prioridade: Regra local > Cache API > API call
- [ ] Acurácia melhora progressivamente com o uso
- [ ] Testes: criação de regra após N correções, prioridade de resolução

**Technical Notes:**
- Tabela `CategorizationRule`: pattern (description ILIKE), category_id, corrections_count
- Pattern matching: normalizar descrição (lowercase, trim) antes de comparar
- Não requer UI específica (backend-only logic)

**Dependencies:** STORY-012

---

### EPIC-004: Dashboard e Visualização Financeira

#### STORY-014: Visão Consolidada de Saldos Multi-Banco

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero ver o saldo consolidado de todas as minhas contas em um único lugar,
Para ter visão geral imediata da minha situação financeira.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/dashboard/summary` retorna saldos consolidados
- [ ] Saldo total (soma de todas as contas) em destaque
- [ ] Saldo individual por conta com identificação do banco
- [ ] Data/hora da última sincronização visível
- [ ] Variação de saldo em relação ao período anterior (positiva/negativa)
- [ ] Frontend: dashboard com cards de saldo (total + por conta)
- [ ] Frontend: indicadores de variação (seta + cor verde/vermelho)
- [ ] Cache Redis (TTL: 5 min), invalidado após sync
- [ ] Testes: cálculos de saldo, variação, cache invalidation

**Technical Notes:**
- Summary pré-computado e cacheado no Redis
- Invalidação via MediatR notification (SyncCompletedEvent)

**Dependencies:** STORY-006, STORY-007

---

#### STORY-015: Visualização de Movimentações por Período

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Must Have
**Points:** 5

**User Story:**
Como usuário,
Quero visualizar a lista de transações filtrada por período,
Para entender minhas movimentações em qualquer intervalo de tempo.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/transactions` com paginação (offset-based, max 50/página)
- [ ] Filtros pré-definidos: hoje, última semana, último mês, último trimestre
- [ ] Filtro customizado com data início/fim
- [ ] Ordenação: data (padrão: mais recente), valor, categoria
- [ ] Retorna total de entradas e saídas do período
- [ ] Frontend: tabela de transações responsiva
- [ ] Frontend: chips de filtro rápido + date picker customizado
- [ ] Frontend: paginação com indicador de total
- [ ] Frontend: sort por coluna (clicável)
- [ ] Testes: filtros, paginação, sorting, edge cases

**Technical Notes:**
- Query params: `?from=&to=&page=&pageSize=&sort=&direction=`
- Índice composto `(user_id, date DESC)` garante performance

**Dependencies:** STORY-007

---

#### STORY-016: Visualização de Gastos por Categoria

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Must Have
**Points:** 3

**User Story:**
Como usuário,
Quero ver gráficos de distribuição de gastos por categoria,
Para entender para onde meu dinheiro está indo.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/dashboard/spending-by-category` com filtro de período
- [ ] Retorna: categoria, valor total, percentual
- [ ] Frontend: gráfico de pizza/donut (percentual por categoria)
- [ ] Frontend: gráfico de barras (valores absolutos por categoria)
- [ ] Frontend: filtro por período aplicável aos gráficos
- [ ] Interação: clicar em categoria filtra as transações correspondentes
- [ ] Frontend: exibe valor e percentual por categoria ao hover
- [ ] Testes: agregações, filtros de período

**Technical Notes:**
- Usar ngx-charts (baseado em D3.js)
- Considerar apenas transações de saída (type = debit) para gráfico de gastos
- Cache Redis (TTL: 5 min)

**Dependencies:** STORY-011

---

#### STORY-017: Filtro por Conta Bancária

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Must Have
**Points:** 2

**User Story:**
Como usuário,
Quero filtrar todas as visualizações por conta bancária,
Para analisar finanças de contas específicas ou de todas juntas.

**Acceptance Criteria:**
- [ ] Seletor de conta disponível no dashboard e em todas as telas de visualização
- [ ] Opção "Todas as contas" como padrão
- [ ] Query param `accountId` suportado em todos os endpoints relevantes
- [ ] Filtro persiste durante a sessão de navegação (Angular service/state)
- [ ] Saldos, gráficos e lista de transações atualizam conforme conta selecionada
- [ ] Testes: filter propagation, "todas as contas" vs conta específica

**Technical Notes:**
- Implementar como Angular service/signal que propaga para todos os componentes
- Backend: param opcional `accountId` nos endpoints de dashboard e transações

**Dependencies:** STORY-006

---

#### STORY-018: Detalhamento de Transação Individual

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Must Have
**Points:** 2

**User Story:**
Como usuário,
Quero ver todos os detalhes de uma transação,
Para entender completamente cada movimentação.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/transactions/{id}` retorna todos os detalhes
- [ ] Exibe: banco de origem, data/hora, valor, categoria, descrição original
- [ ] Exibe se categorização foi automática ou manual
- [ ] Informação de confiança da classificação IA (score + label)
- [ ] Opção de alterar categoria diretamente na tela de detalhes
- [ ] Frontend: modal ou página de detalhes da transação
- [ ] Ownership validation: 404 se transação não pertence ao usuário
- [ ] Testes: detalhamento, ownership

**Technical Notes:**
- Reutilizar componente de correção de categoria do STORY-012
- Incluir histórico de mudanças de categoria (se houver)

**Dependencies:** STORY-015

---

#### STORY-019: Tendências e Comparativo Mensal

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Should Have
**Points:** 3

**User Story:**
Como usuário,
Quero comparar meus gastos entre meses,
Para identificar tendências e ajustar hábitos financeiros.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/dashboard/trends` retorna gastos totais por mês (últimos 6-12 meses)
- [ ] Endpoint `GET /api/v1/dashboard/comparison` retorna comparativo mês atual vs anterior por categoria
- [ ] Frontend: gráfico de linha (tendência mensal)
- [ ] Frontend: comparativo com indicadores de aumento/diminuição (setas + cores)
- [ ] Média móvel de gastos como referência visual
- [ ] Testes: aggregation over months, edge cases (mês sem dados)

**Technical Notes:**
- Query otimizada com GROUP BY month/year no PostgreSQL
- Cache Redis (TTL: 30 min) — dados históricos mudam pouco

**Dependencies:** STORY-016

---

#### STORY-020: Busca/Pesquisa em Transações

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Should Have
**Points:** 3

**User Story:**
Como usuário,
Quero buscar transações por texto ou valor,
Para encontrar rapidamente movimentações específicas.

**Acceptance Criteria:**
- [ ] Full-text search na descrição de transações (PostgreSQL tsvector/tsquery)
- [ ] Combinável com filtros existentes (período, conta, categoria)
- [ ] Busca por faixa de valor (de/até)
- [ ] Frontend: campo de busca com debounce (300ms)
- [ ] Frontend: resultados integrados na lista de transações existente
- [ ] Testes: busca por texto, por valor, combinada com filtros

**Technical Notes:**
- Usar índice GIN já criado no STORY-INF-002
- Configuração de dicionário 'portuguese' para tsvector (stemming pt-BR)
- Query params: `?search=&minAmount=&maxAmount=`

**Dependencies:** STORY-015

---

#### STORY-021: Configuração de Preferências do Usuário

**Epic:** EPIC-004 — Dashboard e Visualização Financeira
**Priority:** Could Have
**Points:** 2

**User Story:**
Como usuário,
Quero configurar preferências pessoais,
Para que o sistema exiba informações no formato que prefiro.

**Acceptance Criteria:**
- [ ] Endpoint `GET /api/v1/users/me/preferences` retorna preferências
- [ ] Endpoint `PATCH /api/v1/users/me/preferences` atualiza preferências
- [ ] Configuração de moeda padrão (BRL default)
- [ ] Configuração de fuso horário (America/Sao_Paulo default)
- [ ] Configuração de idioma (pt-BR default)
- [ ] Preferências persistem entre sessões
- [ ] Frontend: página de configurações com selectores
- [ ] Testes: CRUD de preferências

**Technical Notes:**
- Tabela `UserPreference` com defaults sensatos
- Aplicar preferências na formatação de moeda/datas no frontend

**Dependencies:** STORY-004

---

## Sprint Allocation

### Sprint 1 (Fase 1): Foundation — 13/15 points

**Goal:** Projeto scaffoldado com infraestrutura completa, bancos configurados e camada Core operacional

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-INF-001: Project Scaffolding | 3 | Must Have |
| 2 | STORY-INF-002: Database Schema & Migrations | 5 | Must Have |
| 3 | STORY-INF-003: Core/Shared Infrastructure | 5 | Must Have |
| **Total** | | **13** | |

**Deliverable:** Ambiente de desenvolvimento funcional com Docker Compose, schema de banco criado, serviços de encryption/audit/logging operacionais. Base pronta para implementação de features.

**Risks:**
- Setup de Docker Compose com 5 containers pode ter issues de networking
- Configuração de RLS no PostgreSQL requer testes cuidadosos

---

### Sprint 2 (Fase 2): Autenticação Completa — 16/15 points

**Goal:** Sistema de autenticação completo com registro, login seguro (JWT + refresh rotation), recuperação de senha e gestão de perfil

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-001: Cadastro de Usuário | 5 | Must Have |
| 2 | STORY-002: Login/Logout com Sessão Segura | 5 | Must Have |
| 3 | STORY-003: Recuperação de Senha | 3 | Should Have |
| 4 | STORY-004: Perfil e Dados da Conta | 3 | Should Have |
| **Total** | | **16** | |

**Deliverable:** Usuário pode se cadastrar, confirmar email, fazer login, recuperar senha, ver/editar perfil e excluir conta. Sistema de auth completo e seguro.

**Risks:**
- Integração com Resend (email) pode ter delays no free tier
- Refresh token rotation + reuse detection é lógica complexa

---

### Sprint 3 (Fase 3): Open Finance Core — 16/15 points

**Goal:** Integração com Pluggy funcional: conectar banco, ver contas e sincronizar transações automaticamente

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-005: Conexão via Open Finance (Pluggy) | 8 | Must Have |
| 2 | STORY-006: Listagem de Contas Bancárias | 3 | Must Have |
| 3 | STORY-007: Sincronização Automática de Movimentações | 5 | Must Have |
| **Total** | | **16** | |

**Deliverable:** Usuário conecta banco real, vê contas com saldos, transações são sincronizadas automaticamente a cada 6h. Core data pipeline funcional.

**Risks:**
- **Alto:** Integração com Pluggy API — primeira vez, OAuth flow externo, possíveis limitações do sandbox
- Deduplicação de transações precisa ser robusta (IDs da Pluggy)

**Dependencies externas:**
- Acesso ao sandbox/dev da Pluggy (API key)

---

### Sprint 4 (Fase 4): Categorização IA + Consentimentos — 14/15 points

**Goal:** Motor de categorização IA operacional com auto-classificação, correção manual e gestão completa de consentimentos Open Finance

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-008: Gestão de Consentimentos | 3 | Must Have |
| 2 | STORY-010: Gestão de Categorias Customizáveis | 3 | Must Have |
| 3 | STORY-011: Categorização Automática (GPT-4o-mini) | 5 | Must Have |
| 4 | STORY-012: Correção Manual de Categoria | 3 | Must Have |
| **Total** | | **14** | |

**Deliverable:** Transações são categorizadas automaticamente por IA, usuário pode corrigir categorias, e consentimentos Open Finance são gerenciáveis (renovar/revogar).

**Risks:**
- Prompt engineering para acurácia >= 80% pode precisar de iteração
- Custo de API OpenAI (estimar volume de chamadas)

**Dependencies externas:**
- API key OpenAI com crédito

---

### Sprint 5 (Fase 5): Dashboard Core — 15/15 points

**Goal:** Dashboard funcional com saldos consolidados, lista de transações filtráveis, gráficos de gastos por categoria e detalhamento

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-014: Visão Consolidada de Saldos | 3 | Must Have |
| 2 | STORY-015: Transações com Filtro por Período | 5 | Must Have |
| 3 | STORY-016: Gastos por Categoria (Gráficos) | 3 | Must Have |
| 4 | STORY-017: Filtro por Conta Bancária | 2 | Must Have |
| 5 | STORY-018: Detalhamento de Transação | 2 | Must Have |
| **Total** | | **15** | |

**Deliverable:** Dashboard completo com visão consolidada, transações filtráveis por período e conta, gráficos de gastos por categoria, e detalhamento de transação individual. **MVP funcional!**

**Risks:**
- Frontend-heavy: muitos componentes Angular + gráficos
- Performance de queries agregadas com volume grande

---

### Sprint 6 (Fase 6): Polish & Should Have Features — 16/15 points

**Goal:** Features complementares que enriquecem o produto: importação manual, feedback loop de IA, tendências, busca e preferências

**Stories:**
| # | Story | Points | Priority |
|---|-------|--------|----------|
| 1 | STORY-009: Importação Manual OFX/CSV | 5 | Should Have |
| 2 | STORY-013: Feedback Loop / Aprendizado | 3 | Should Have |
| 3 | STORY-019: Tendências e Comparativo Mensal | 3 | Should Have |
| 4 | STORY-020: Busca em Transações | 3 | Should Have |
| 5 | STORY-021: Configuração de Preferências | 2 | Could Have |
| **Total** | | **16** | |

**Deliverable:** Produto completo com todas as funcionalidades planejadas. Import manual como fallback, IA que aprende com correções, gráficos de tendência, busca full-text e preferências customizáveis.

**Risks:**
- Parser OFX pode ter edge cases com diferentes bancos
- Escopo pode ser reduzido se fases anteriores atrasarem (Should/Could Have)

---

## Epic Traceability

| Epic ID | Epic Name | Stories | Total Points | Fase |
|---------|-----------|---------|--------------|------|
| Infrastructure | Setup & Core | STORY-INF-001, INF-002, INF-003 | 13 | Fase 1 |
| EPIC-001 | Autenticação e Gestão de Usuário | STORY-001, 002, 003, 004 | 16 | Fase 2 |
| EPIC-002 | Integração Open Finance | STORY-005, 006, 007, 008, 009 | 24 | Fases 3, 4, 6 |
| EPIC-003 | Motor de Categorização IA | STORY-010, 011, 012, 013 | 14 | Fases 4, 6 |
| EPIC-004 | Dashboard e Visualização | STORY-014, 015, 016, 017, 018, 019, 020, 021 | 23 | Fases 5, 6 |
| **Total** | | **24 stories** | **90 points** | **6 fases** |

---

## Requirements Coverage

### Functional Requirements → Stories

| FR ID | FR Name | Story | Fase | Priority |
|-------|---------|-------|------|----------|
| FR-001 | Cadastro de Usuário | STORY-001 | 2 | Must Have |
| FR-002 | Login/Logout com Sessão Segura | STORY-002 | 2 | Must Have |
| FR-003 | Recuperação de Senha | STORY-003 | 2 | Should Have |
| FR-004 | Conexão via Open Finance | STORY-005 | 3 | Must Have |
| FR-005 | Listagem de Contas Conectadas | STORY-006 | 3 | Must Have |
| FR-006 | Sincronização Automática | STORY-007 | 3 | Must Have |
| FR-007 | Gestão de Consentimentos | STORY-008 | 4 | Must Have |
| FR-008 | Importação Manual OFX/CSV | STORY-009 | 6 | Should Have |
| FR-009 | Categorização Automática | STORY-011 | 4 | Must Have |
| FR-010 | Correção Manual de Categoria | STORY-012 | 4 | Must Have |
| FR-011 | Aprendizado (Feedback Loop) | STORY-013 | 6 | Should Have |
| FR-012 | Gestão de Categorias | STORY-010 | 4 | Must Have |
| FR-013 | Saldos Consolidados | STORY-014 | 5 | Must Have |
| FR-014 | Visualização por Período | STORY-015 | 5 | Must Have |
| FR-015 | Gastos por Categoria | STORY-016 | 5 | Must Have |
| FR-016 | Filtro por Conta | STORY-017 | 5 | Must Have |
| FR-017 | Tendências Mensais | STORY-019 | 6 | Should Have |
| FR-018 | Busca em Transações | STORY-020 | 6 | Should Have |
| FR-019 | Detalhamento de Transação | STORY-018 | 5 | Must Have |
| FR-020 | Preferências do Usuário | STORY-021 | 6 | Could Have |
| FR-021 | Perfil e Dados da Conta | STORY-004 | 2 | Should Have |

**Coverage:** 21/21 FRs (100%)

### NFR Coverage (Architecture-level)

| NFR | Addressed By | Implementation |
|-----|--------------|----------------|
| NFR-001 (API < 500ms) | STORY-INF-003 + todas | Redis cache, indexes, async pipeline |
| NFR-002 (Dashboard < 3s) | STORY-014 | Lazy loading, cache, CDN |
| NFR-003 (Sync < 30s) | STORY-007 | Hangfire, batch processing |
| NFR-004 (Encryption repouso) | STORY-INF-003 | IEncryptionService AES-256 |
| NFR-005 (Encryption trânsito) | STORY-INF-001 | TLS 1.3, HSTS |
| NFR-006 (LGPD) | STORY-004 | Export + delete + consent |
| NFR-007 (Auth segura) | STORY-002 | JWT RS256, Argon2, rate limit |
| NFR-008 (OWASP Top 10) | STORY-INF-003 + todas | EF Core, Angular sanitize, CSP |
| NFR-009 (Capacidade) | STORY-INF-001 | Connection pooling, indexes |
| NFR-010 (Disponibilidade) | STORY-INF-001 | Docker restart, health checks |
| NFR-011 (Backup) | Infra (Docker) | pg_dump + mongodump diário |
| NFR-012 (Resiliência) | STORY-005, 007 | Polly retry + circuit breaker |
| NFR-013 (Responsividade) | STORY-014-020 | Angular Material, CSS Grid |
| NFR-014 (Acessibilidade) | STORY-014-020 | Semantic HTML, ARIA, contrast |
| NFR-015 (Testes 90%) | Todas as stories | xUnit, Testcontainers, Jasmine |
| NFR-016 (Logging) | STORY-INF-003 | Serilog + MongoDB + correlation ID |
| NFR-017 (Auditoria) | STORY-INF-003 | IAuditService, append-only MongoDB |
| NFR-018 (Proteção tokens) | STORY-INF-003, 005 | AES-256, env vars, masking |
| NFR-019 (Isolamento) | STORY-INF-002, 003 | RLS, ICurrentUser, UUID v7 |
| NFR-020 (Sanitização) | STORY-INF-003 + todas | FluentValidation, EF Core, Angular |

---

## Risks and Mitigation

### High

| Risk | Impact | Mitigation |
|------|--------|------------|
| Integração Pluggy API (primeira vez) | Atraso na Fase 3, bloqueio de dados reais | Começar com sandbox early, criar mocks para testes, ter fallback de import manual |
| Acurácia IA < 80% | Funcionalidade core degradada | Iterar prompts, testar com dados reais, feedback loop para melhoria contínua |

### Medium

| Risk | Impact | Mitigation |
|------|--------|------------|
| Refresh token rotation + reuse detection | Bugs de auth, sessões perdidas | TDD rigoroso, testes de lifecycle completo |
| Performance de queries agregadas | Dashboard lento | Redis cache, materialized views, EXPLAIN ANALYZE |
| Custo OpenAI API | Custo mensal elevado | Batch (20/request), regras locais, cache de resultados |

### Low

| Risk | Impact | Mitigation |
|------|--------|------------|
| Parser OFX com edge cases | Importação parcial | Testar com extratos variados, fallback gracioso |
| Bundle size Angular | First load lento | Lazy loading, tree shaking, CDN |

---

## Dependencies

### External Dependencies

| Dependency | Required By | Status | Action |
|------------|-------------|--------|--------|
| Pluggy API (sandbox access) | Fase 3 | Pending | Criar conta dev na Pluggy |
| OpenAI API key | Fase 4 | Pending | Criar conta + crédito |
| Resend API key | Fase 2 | Pending | Criar conta (free tier) |
| Hostinger VPS | Deploy | Pending | Contratar VPS KVM 2 |
| Domínio + Cloudflare | Deploy | Pending | Registrar domínio, configurar DNS |

### Story Dependencies (Execution Order)

```
STORY-INF-001 → STORY-INF-002 → STORY-INF-003
                                       ↓
                    STORY-001 → STORY-002 → STORY-003
                        ↓           ↓           ↓
                   STORY-010   STORY-005    STORY-004
                        ↓         ↓    ↓
                   STORY-011  STORY-006  STORY-008
                        ↓         ↓
                   STORY-012  STORY-007
                        ↓         ↓
                   STORY-013  STORY-009
                        ↓
           STORY-014, 015, 016, 017, 018
                        ↓
              STORY-019, 020, 021
```

---

## Definition of Done

Para uma story ser considerada completa:
- [ ] Código implementado e commitado (commits atômicos)
- [ ] Testes unitários escritos e passando (≥90% cobertura backend)
- [ ] Testes de integração para fluxos críticos passando
- [ ] Spec criada/atualizada em `specs/` (No Spec, No Code)
- [ ] Endpoints documentados no Swagger/OpenAPI
- [ ] Logging estruturado implementado (correlation ID, níveis corretos)
- [ ] Audit log para ações sensíveis
- [ ] Isolamento de dados validado (ownership check)
- [ ] Sanitização de inputs implementada
- [ ] Frontend responsivo (desktop + mobile)
- [ ] Docker Compose funcional com todos os serviços

---

## Next Steps

**Imediato:** Iniciar Fase 1 (Foundation)

```
Opções:
1. /bmad:create-story STORY-INF-001  → Criar story detalhada
2. /bmad:dev-story STORY-INF-001     → Implementar diretamente
3. /speckit.specify                   → Gerar spec técnica da story
4. /bmad:workflow-status              → Ver progresso atual
```

**Recomendado:** Começar com `/bmad:dev-story STORY-INF-001` para scaffoldar o projeto.

**Cadência sugerida (solo):**
- Completar 1-2 stories por semana
- Review semanal do progresso vs. plano
- Ajustar escopo se necessário (Could Have → Won't Have)

---

## Story Point Calibration (Referência)

| Points | Tempo (Pleno) | Exemplo neste projeto |
|--------|---------------|----------------------|
| 2 | 4-6h | STORY-017: Filtro por Conta (param + dropdown) |
| 3 | 6-9h | STORY-003: Recuperação de Senha (2 endpoints + 2 forms + email) |
| 5 | 10-15h | STORY-002: Login/JWT (auth pipeline completo FE + BE) |
| 8 | 16-24h | STORY-005: Open Finance (nova API externa, OAuth, webhook) |

---

**This plan was created using BMAD Method v6 - Phase 4 (Implementation Planning)**

*To continue: Run `/bmad:workflow-status` to see your progress and next recommended workflow.*
