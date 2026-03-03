# Implementation Plan: Motor Financeiro

**Branch**: `001-motor-financeiro` | **Date**: 2026-03-03 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification — sistema de gestão financeira pessoal com integração Open Finance, categorização automática por IA e dashboard analítico.

---

## Summary

Motor Financeiro é um SaaS de gestão financeira pessoal que conecta contas bancárias via Open Finance (Pluggy), sincroniza transações automaticamente, categoriza por IA (GPT-4o-mini), e apresenta dashboards com análises de gastos. A stack é .NET 10 + Angular 19 + PostgreSQL + MongoDB + Redis, seguindo Modular Monolith com Clean Architecture, deploy via Docker Compose em VPS.

---

## Technical Context

**Language/Version**: C# / .NET 10 (LTS)
**Primary Dependencies**: ASP.NET Core, Entity Framework Core 10, MediatR, Polly, Hangfire, FluentValidation, Serilog
**Storage**: PostgreSQL 17 (dados financeiros) + MongoDB 8 (logs/auditoria) + Redis 7 (cache)
**Testing**: xUnit + FluentAssertions + Testcontainers + Cypress
**Target Platform**: Web (SPA Angular 19+ servida via nginx, API ASP.NET Core)
**Project Type**: Web application (Modular Monolith — backend + frontend no mesmo repo)
**Performance Goals**: API <500ms p95, dashboard <3s, busca <1s, 100 usuários simultâneos
**Constraints**: Dev solo (~15 pontos por fase de 2 semanas), VPS com 8GB RAM, custos externos <$50/mês
**Scale/Scope**: 1-100 usuários, 500k+ transações, 10 stories, 31 endpoints REST, 9 entidades PostgreSQL

---

## Constitution Check

*GATE: A constituição do projeto é um template não preenchido. Nenhum princípio específico foi definido.*

**Status**: N/A — sem gates a validar. Seguindo princípios gerais do CLAUDE.md:
- **No Spec, No Code**: ✅ Spec completa com 10 User Stories e 57 FRs
- **Atomicidade**: ✅ Plano decomposto em fases incrementais
- **Modularidade**: ✅ Clean Architecture com módulos isolados (Auth, OpenFinance, Categorization, Dashboard)
- **Verificabilidade**: ✅ Meta de 90% cobertura de testes, test pyramid definida

---

## Project Structure

### Documentation (this feature)

```text
workspace/motor-financeiro/specs/
├── plan.md              # Este arquivo
├── spec.md              # Especificação da feature
├── research.md          # Decisões técnicas e alternativas
├── data-model.md        # Modelo de dados detalhado
├── quickstart.md        # Guia de setup do ambiente
├── contracts/
│   └── api-v1.md        # Contrato da API REST (31 endpoints)
└── tasks.md             # (gerado pelo /speckit.tasks)
```

### Source Code (repository root)

```text
workspace/motor-financeiro/
├── src/
│   ├── MotorFinanceiro.Api/              # ASP.NET Core Web API (entry point)
│   │   ├── Controllers/                   # API Controllers (Auth, Connections, Transactions, etc.)
│   │   ├── Middleware/                     # Auth, RateLimit, Logging, ErrorHandling
│   │   ├── Filters/                       # Validation, Exception filters
│   │   ├── Keys/                          # RSA key pair para JWT (gitignored)
│   │   └── Program.cs                     # DI configuration, pipeline setup
│   │
│   ├── MotorFinanceiro.Domain/           # Domain Layer (zero dependências externas)
│   │   ├── Entities/                      # User, Transaction, Account, Category, Connection, etc.
│   │   ├── ValueObjects/                  # Money, Email, UserId
│   │   ├── Enums/                         # TransactionType, ConnectionStatus, SyncStatus
│   │   ├── Interfaces/                    # IRepository, IEncryptionService, ICategorizer, etc.
│   │   └── Events/                        # Domain events (UserRegistered, TransactionsSynced, etc.)
│   │
│   ├── MotorFinanceiro.Application/      # Application Layer (use cases)
│   │   ├── Auth/                          # Commands: Register, Login, Refresh, ForgotPassword, etc.
│   │   ├── OpenFinance/                   # Commands: Connect, Sync, Revoke, Import
│   │   ├── Categorization/               # Commands: Categorize, CorrectCategory, ApplySimilar
│   │   ├── Dashboard/                     # Queries: Summary, SpendingByCategory, Trends, Comparison
│   │   ├── Common/                        # Behaviors (validation, logging, audit)
│   │   └── DTOs/                          # Request/Response objects
│   │
│   ├── MotorFinanceiro.Infrastructure/   # Infrastructure Layer
│   │   ├── Persistence/
│   │   │   ├── PostgreSQL/                # EF Core DbContext, Configurations, Migrations
│   │   │   └── MongoDB/                   # Audit, Logging repositories
│   │   ├── Services/
│   │   │   ├── Pluggy/                    # IOpenFinanceProvider → Pluggy REST API
│   │   │   ├── OpenAI/                    # ICategorizer → GPT-4o-mini
│   │   │   ├── Resend/                    # IEmailSender → Resend API
│   │   │   └── Encryption/               # IEncryptionService → AES-256-GCM
│   │   ├── BackgroundJobs/               # Hangfire job definitions
│   │   └── Caching/                       # Redis cache implementation
│   │
│   └── MotorFinanceiro.Web/             # Angular SPA
│       └── src/app/
│           ├── core/                      # Guards, interceptors, auth service
│           ├── shared/                    # Shared components, pipes, directives
│           └── features/
│               ├── auth/                  # Login, Register, ForgotPassword
│               ├── dashboard/             # Dashboard, Charts, Summary cards
│               ├── transactions/          # List, Detail, Search, Filters
│               ├── accounts/              # Connected accounts, Sync status
│               ├── categories/            # Category management
│               └── settings/              # Profile, Preferences
│
├── tests/
│   ├── MotorFinanceiro.Domain.Tests/     # Unit: domain logic, value objects
│   ├── MotorFinanceiro.Application.Tests/ # Unit: use cases (mocked infra)
│   ├── MotorFinanceiro.Infrastructure.Tests/ # Integration: Testcontainers (PG, Mongo, Redis)
│   ├── MotorFinanceiro.Api.Tests/        # Integration: WebApplicationFactory
│   └── MotorFinanceiro.E2E.Tests/        # E2E: Cypress/Playwright
│
├── docker/
│   ├── docker-compose.yml                # Produção
│   ├── docker-compose.dev.yml            # Desenvolvimento (PG + Mongo + Redis)
│   ├── Dockerfile.api                    # .NET API multi-stage build
│   └── Dockerfile.web                    # Angular + nginx
│
├── docs/                                  # Product docs (brief, PRD, architecture)
└── specs/                                 # Technical specs (Spec-Kit)
```

**Structure Decision**: Web application com Modular Monolith — backend .NET e frontend Angular no mesmo repositório. Cada módulo de negócio (Auth, OpenFinance, Categorization, Dashboard) é isolado com Clean Architecture (Domain → Application → Infrastructure → API). Comunicação entre módulos via MediatR (events/commands).

---

## Complexity Tracking

> Nenhuma violação de constituição identificada (constituição é template).

| Aspecto | Justificativa |
|---------|--------------|
| 3 bancos de dados (PG + Mongo + Redis) | Cada um otimizado para seu workload: financeiro (ACID), logs (append-only), cache (in-memory). Todos têm Docker images oficiais, overhead operacional mínimo |
| Clean Architecture (4 projetos .NET) | Investimento inicial que paga em testabilidade e manutenibilidade. CLI do .NET gera boilerplate automaticamente |
| 31 endpoints REST | Necessário para cobrir 57 requisitos funcionais de 10 user stories. API bem estruturada com versionamento |

---

## Artefatos Gerados

| Artefato | Caminho | Status |
|----------|---------|--------|
| Research | [research.md](research.md) | Completo |
| Data Model | [data-model.md](data-model.md) | Completo |
| API Contract | [contracts/api-v1.md](contracts/api-v1.md) | Completo |
| Quickstart | [quickstart.md](quickstart.md) | Completo |
| Tasks | tasks.md | Próximo passo (`/speckit.tasks`) |

---

## Próximos Passos

1. **`/speckit.tasks`** — Gerar tasks.md com decomposição de implementação
2. **`/speckit.implement`** — Executar tasks com loop TDD (Ralph)
3. Stories a implementar na ordem de prioridade:
   - Story 1: Cadastro e Autenticação Segura (P1)
   - Story 2: Conexão Open Finance (P2)
   - Story 3: Sync de Transações (P3)
   - Story 4: Categorização IA (P4)
   - Story 5: Dashboard (P5)
   - Stories 6-10: Features complementares
