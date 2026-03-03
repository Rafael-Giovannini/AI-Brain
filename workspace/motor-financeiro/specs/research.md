# Research: Motor Financeiro

**Branch**: `001-motor-financeiro` | **Date**: 2026-03-03
**Status**: Completo — todas as decisões técnicas resolvidas

---

## Contexto

Pesquisa técnica para resolver as decisões de stack e dependências do Motor Financeiro — sistema de gestão financeira pessoal com integração Open Finance, categorização automática por IA e dashboard analítico.

Todas as decisões respeitam a **arquitetura já definida** em `docs/architecture-motor-financeiro-2026-03-03.md`.

---

## Decisão 1: Stack Backend

**Decisão:** .NET 10 (ASP.NET Core) com C#

**Racional:**
- Performance excepcional (top 10 TechEmpower benchmarks)
- Clean Architecture bem estabelecida no ecossistema .NET (MediatR, FluentValidation)
- Entity Framework Core 10 para PostgreSQL com migrations robustas
- Ecossistema maduro de segurança (ASP.NET Identity, Data Protection API)
- Suporte nativo a background jobs (Hangfire), resilience (Polly), structured logging (Serilog)
- .NET 10 é LTS (Nov 2025), estável para produção em 2026

**Alternativas consideradas:**
- Node.js + NestJS: Bom ecossistema, SDK Pluggy oficial em Node.js. Descartado porque .NET tem melhor performance, type safety mais forte, e ecossistema mais completo para Clean Architecture
- Python + FastAPI: Excelente para prototipagem. Descartado pela ausência de SDK Pluggy oficial e necessidade de frontend separado
- Node.js + Next.js full-stack: Alta produtividade, mas limitado para backend complexo (jobs, circuit breaker, rate limiting)

**Nota sobre Pluggy:** Pluggy não tem SDK oficial .NET, mas a REST API é bem documentada. Será consumida via `HttpClient` tipado com DTOs mapeados da documentação OpenAPI da Pluggy.

---

## Decisão 2: Stack Frontend

**Decisão:** Angular 19+ com TypeScript

**Racional:**
- Framework opinionated com estrutura clara — produtividade para dev solo
- Módulos/standalone components facilitam organização por feature
- Angular Material + CDK para UI consistente e responsiva
- RxJS para dados real-time (status de sync)
- CLI poderoso para geração de código (`ng generate`)
- TypeScript strict mode garante type safety end-to-end com DTOs do backend

**Bibliotecas principais:**
| Lib | Função |
|-----|--------|
| Angular Material | UI components, tema, responsividade |
| ngx-charts ou ng2-charts | Gráficos (pizza/donut, barras, linhas de tendência) |
| NgRx / Signals | State management |
| Angular HTTP Client | Comunicação API com interceptors |
| angular-auth-oidc-client | Gerenciamento JWT no frontend |

**Alternativas consideradas:**
- React + Vite (SPA): Menor curva de aprendizado, HMR mais rápido. Mas sem estrutura opinativa — dev solo precisa tomar mais decisões de arquitetura
- React + Next.js: SSR desnecessário para dashboard autenticado (100% área logada)

---

## Decisão 3: ORM e Banco de Dados

**Decisão:** Entity Framework Core 10 com PostgreSQL 17

**Racional:**
- Migrations type-safe e versionadas
- LINQ queries fortemente tipadas com proteção contra SQL injection
- Suporte a PostgreSQL features avançadas (FTS via `to_tsvector`, GIN indexes, RLS)
- Eager loading com `Include()` previne N+1
- Connection pooling via Npgsql (pool size = 20)
- Testcontainers para testes de integração com DB real

**Estratégia de performance:**
- Índices compostos: `(user_id, date)`, `(user_id, category_id)`, `(user_id, account_id)`
- Índice GIN para full-text search: `to_tsvector('portuguese', description)`
- Deduplicação por hash: índice em `external_id_hash`
- Paginação obrigatória (max 50 itens/página)
- Materialized views para aggregations de dashboard (futuro)
- Row-Level Security para isolamento de dados por usuário

**MongoDB 8 para logs/auditoria:**
- Schema-less para logs estruturados com campos variáveis
- Append-only collections para audit trail imutável
- TTL indexes para rotação automática (90 dias logs, 365 dias audit)
- Write-heavy separado do banco principal

**Redis 7 para cache:**
- Dashboard summary: TTL 5 min
- Categories list: TTL 30 min
- Institutions list: TTL 24h
- Invalidação event-driven via MediatR notifications

---

## Decisão 4: Autenticação e Segurança

**Decisão:** Implementação custom com JWT (RS256) + Refresh Token Rotation

**Racional:**
- Sistema financeiro exige auditabilidade total do fluxo de autenticação
- Refresh token rotation com detecção de reutilização requer controle sobre o modelo de dados
- Nenhuma biblioteca opinada (Auth.js, Lucia) oferece o nível de controle necessário

**Stack de segurança:**
| Componente | Tecnologia | Justificativa |
|-----------|-----------|---------------|
| JWT sign/verify | ASP.NET Core JWT Bearer | Nativo, auditado, RS256 |
| Password hash | Argon2id (Konscious.Security) | Vencedor PHC, memory-hard, OWASP #1 |
| Params Argon2 | memory: 64MB, iterations: 3, parallelism: 1 | OWASP recommended |
| Rate limiting | Redis sliding window | Distribuído, persistente entre restarts |
| Encriptação tokens | AES-256-GCM (Data Protection API) | Authenticated encryption |
| Validação de inputs | FluentValidation | Type-safe, composável |
| Security headers | Middleware (HSTS, CSP, X-Frame-Options) | Defense in depth |
| Audit logging | MongoDB append-only collection | Imutável, TTL 365 dias |

**Fluxo de refresh token rotation:**
1. Login → gera access token (15min) + refresh token opaco (7 dias)
2. Refresh token é hasheado (SHA-256) e salvo no PostgreSQL com `family_id`
3. Cada renovação gera novo par, invalida token anterior
4. Se token já invalidado for reutilizado → revoga toda a família → forçar logout
5. Audit log para toda operação de token

**Políticas de rate limiting:**
| Endpoint | Limite | Janela | Chave |
|----------|--------|--------|-------|
| POST /auth/login | 5 | 1 min | IP |
| POST /auth/register | 3 | 10 min | IP |
| POST /auth/forgot-password | 3 | 15 min | IP |
| GET/* (geral) | 100 | 1 min | user_id |

---

## Decisão 5: Background Jobs e Sincronização

**Decisão:** Hangfire com PostgreSQL storage

**Racional:**
- Dashboard web embutido para monitoramento
- Suporte a cron jobs periódicos (sync a cada 6h)
- Jobs sob demanda (sync manual)
- Retry com backoff automático
- Usa PostgreSQL como storage — zero dependências adicionais para o job queue
- Controle de concorrência por tipo de job
- Integração nativa com DI do .NET

**Jobs planejados:**
| Job | Trigger | Concorrência | Retry |
|-----|---------|-------------|-------|
| SyncTransactionsJob | Cron `0 */6 * * *` + manual | 1 por connection | 3x backoff |
| CategorizeTransactionsJob | Event (pós-sync) | 1 global | 3x backoff |
| CheckConsentExpiryJob | Cron diário | 1 global | Sem retry |
| CleanupDeletedAccountsJob | Cron diário | 1 global | 3x backoff |
| ExportUserDataJob | On-demand | 1 por user | 3x backoff |

---

## Decisão 6: Resiliência de Integrações Externas

**Decisão:** Polly para retry + circuit breaker

**Racional:**
- Padrão da indústria .NET para resilience
- Retry + Circuit Breaker + Timeout combinados em pipelines
- Configuração declarativa por política
- Integração nativa com HttpClientFactory

**Configurações por serviço externo:**
| API | Timeout | Retries | Backoff | Circuit (threshold) | Reset |
|-----|---------|---------|---------|---------------------|-------|
| Pluggy | 10s | 3 | 1s, 4s, 16s | 5 falhas em 60s | 30s |
| OpenAI | 30s | 3 | 2s, 8s, 32s | 5 falhas em 60s | 60s |
| Resend | 5s | 2 | 1s, 4s | 3 falhas em 60s | 30s |

**Fallback strategies:**
- Pluggy indisponível: dados em cache (Redis) permanecem acessíveis, sync registra falha
- OpenAI indisponível: transações categorizadas como "Outros" com confiança 0.0, regras locais continuam
- Resend indisponível: emails enfileirados para retry posterior

---

## Decisão 7: Integração Open Finance (Pluggy)

**Decisão:** Consumir Pluggy REST API via HttpClient tipado (.NET)

**Racional:**
- Pluggy não tem SDK oficial .NET; SDK oficial existe apenas para Node.js
- REST API bem documentada com OpenAPI spec
- HttpClient tipado com DTOs é idiomático em .NET e permite controle total
- Polly integrado via HttpClientFactory para resilience

**Fluxo de integração:**
1. Backend gera Connect Token via Pluggy API (clientId + clientSecret)
2. Frontend abre Pluggy Connect Widget com o token
3. Usuário autoriza na instituição (OAuth 2.0)
4. Widget retorna `itemId` ao backend
5. Backend busca contas e transações via `itemId`

**Endpoints Pluggy utilizados:**
| Endpoint | Método | Uso |
|----------|--------|-----|
| `/auth` | POST | Autenticar com clientId/secret |
| `/connect_token` | POST | Gerar token para widget |
| `/connectors` | GET | Listar instituições |
| `/items/{id}` | GET | Status da conexão |
| `/items/{id}` | PATCH | Forçar re-sync |
| `/items/{id}` | DELETE | Revogar consentimento |
| `/accounts` | GET | Listar contas do item |
| `/transactions` | GET | Listar transações (paginado) |

**Dados retornados pela Pluggy (mapeamento):**
| Campo Pluggy | Campo Motor | Observação |
|-------------|-------------|------------|
| Transaction.id | external_id | Para deduplicação (hash SHA-256) |
| Transaction.description | description | Texto livre do banco |
| Transaction.amount | amount | Negativo = débito, positivo = crédito |
| Transaction.type | type | DEBIT / CREDIT |
| Transaction.date | date | ISO 8601 |
| Transaction.currencyCode | currency | BRL, USD, etc. |
| Transaction.category | — | Ignorado (usamos IA própria) |
| Account.type | type | BANK / CREDIT |
| Account.subtype | subtype | CHECKING / SAVINGS |
| Account.balance | balance | Saldo atual |

**Sandbox:** Credenciais de teste fornecidas no dashboard Pluggy (dashboard.pluggy.ai). Conectores sandbox simulam bancos reais com dados fictícios.

---

## Decisão 8: Categorização por IA

**Decisão:** OpenAI GPT-4o-mini via REST API com Strategy Pattern

**Racional:**
- Custo baixo (~$5/mês para volume pessoal)
- Qualidade alta para classificação de texto curto (descrições de transação)
- Batch de até 20 transações por request para reduzir chamadas
- Strategy Pattern (`ICategorizer`) permite trocar provider sem mudar código de negócio

**Pipeline de categorização (prioridade):**
1. Regras locais do usuário (N≥3 correções consistentes → regra automática)
2. Cache de categorizações anteriores
3. API OpenAI GPT-4o-mini
4. Fallback: "Outros" com confiança 0.0

**Configuração:**
- Modelo: `gpt-4o-mini`
- Max tokens: ~150 por request
- Temperature: 0.1 (determinístico)
- Batch size: 20 transações por request
- Timeout: 30s
- Retry: 3x com backoff exponencial

---

## Decisão 9: Deploy e Infraestrutura

**Decisão:** Docker Compose em VPS (Hostinger KVM 2)

**Racional:**
- Single docker-compose.yml encapsula toda a stack
- Cloud-agnostic (pode migrar para Azure/AWS sem mudar aplicação)
- Mínimo overhead operacional para dev solo
- Custo controlado (VPS ~$12/mês vs cloud managed services)

**Containers:**
| Container | Imagem | Recurso |
|-----------|--------|---------|
| motor-api | .NET 10 custom | 512MB RAM |
| motor-spa | nginx + Angular build | 128MB RAM |
| postgres | postgres:17-alpine | 512MB RAM |
| mongodb | mongo:8 | 256MB RAM |
| redis | redis:7-alpine | 128MB RAM |
| **Total** | | **~1.5GB RAM** |

**CI/CD:** GitHub Actions → build → test → push image → deploy via SSH

---

## Decisão 10: Testes

**Decisão:** xUnit + FluentAssertions + Testcontainers + Cypress

**Racional:**
- xUnit é o framework de teste padrão do ecossistema .NET
- FluentAssertions para assertions legíveis
- Testcontainers para testes de integração com DB real (PostgreSQL, MongoDB, Redis)
- Cypress/Playwright para E2E
- Meta: 90% cobertura backend

**Pirâmide de testes:**
- 60% Unit (Domain + Application)
- 30% Integration (Infrastructure + API)
- 10% E2E (Cypress)

---

## Resumo de Stack Técnica

| Camada | Tecnologia |
|--------|-----------|
| **Backend** | .NET 10, ASP.NET Core, C# |
| **Frontend** | Angular 19+, TypeScript, Angular Material |
| **ORM** | Entity Framework Core 10 |
| **DB Principal** | PostgreSQL 17 |
| **DB Logs** | MongoDB 8 |
| **Cache** | Redis 7 |
| **Jobs** | Hangfire (PostgreSQL storage) |
| **Resilience** | Polly |
| **Logging** | Serilog → MongoDB |
| **CQRS/Events** | MediatR |
| **Validação** | FluentValidation |
| **Open Finance** | Pluggy REST API (HttpClient tipado) |
| **IA** | OpenAI GPT-4o-mini |
| **Email** | Resend |
| **Auth** | JWT RS256 + Refresh Token Rotation + Argon2id |
| **Testes** | xUnit, FluentAssertions, Testcontainers, Cypress |
| **Deploy** | Docker Compose, VPS Hostinger, GitHub Actions |
| **CDN/DNS** | Cloudflare |
