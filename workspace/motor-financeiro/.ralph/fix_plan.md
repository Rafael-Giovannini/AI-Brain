# Ralph Fix Plan — Motor Financeiro

## Phase 0: Project Scaffolding (P0 — Prerequisite) ✅
- [x] Criar solution .NET e projetos (Domain, Application, Infrastructure, Api)
- [x] Configurar Docker Compose dev (PostgreSQL 17 + MongoDB 8 + Redis 7)
- [x] Configurar EF Core + DbContext + primeira migration
- [x] Configurar MediatR, FluentValidation, Serilog
- [x] Configurar Angular 19 SPA com proxy para API
- [x] Configurar estrutura de testes (xUnit + FluentAssertions + Testcontainers)

## Phase 0.5: Infrastructure Fixes ✅
- [x] Fix port mismatch: launchSettings 5014 → 5000 (match proxy.conf.json + Kestrel)
- [x] Remove Class1.cs placeholders from Domain/Application/Infrastructure
- [x] Wire FluentValidation into MediatR pipeline (ValidationBehavior)
- [x] Generate initial EF Core migration (InitialCreate: users + refresh_tokens)
- [x] Add JWT authentication middleware to Program.cs

## Phase 1: Story 1 — Cadastro e Autenticação Segura (P1 — Critical) ✅
- [x] Entidades Domain: User (existed), RefreshToken
- [x] Interface IUserRepository (existed), IRefreshTokenRepository, ITokenService, IPasswordHasher, IEmailSender
- [x] Command: RegisterUser (FR-001) + validator (email format + password strength)
- [x] Command: ConfirmEmail (FR-002)
- [x] Command: Login (FR-003) — JWT access + refresh token rotation
- [x] Command: RefreshToken (FR-003, FR-004) — detecção de reuso (family revocation)
- [x] Command: Logout (FR-003) — invalidar família de tokens
- [x] Command: ForgotPassword (FR-006) — sem revelar existência do email
- [x] Command: ResetPassword (FR-006) + validator
- [x] Invalidar sessões após troca de senha (FR-007) — via RevokeAllByUserId
- [x] Infrastructure: PasswordHasher (PBKDF2-SHA512), TokenService (JWT HMAC-SHA256), NoOpEmailSender
- [x] Infrastructure: RefreshToken EF configuration + repository
- [x] API: AuthController (register, login, refresh, logout, confirm-email, forgot-password, reset-password)
- [x] Middleware: Auth JWT (HMAC-SHA256) — configurado em Program.cs
- [x] Testes unitários Domain (12 tests) + Application (16 tests) = 28 tests passing
- [ ] Middleware: RateLimiting por IP (FR-005)
- [ ] Testes integração API (auth endpoints)

## Phase 2: Story 2 — Conexão Open Finance (P2)
- [ ] Entidades: Connection (status, tokens encriptados)
- [ ] Interface IOpenFinanceProvider, IEncryptionService
- [ ] Command: ListInstitutions (FR-012)
- [ ] Command: InitiateConsent (FR-013)
- [ ] Command: CompleteConsent — callback OAuth (FR-014)
- [ ] Query: ListConnections (FR-015)
- [ ] Command: RenewConsent / RevokeConsent (FR-017)
- [ ] Alerta expiração 7 dias (FR-016)
- [ ] Retry + Circuit Breaker com Polly (FR-018)
- [ ] Detecção de conexão duplicada (FR-018a)
- [ ] Detecção de revogação externa (FR-018b)
- [ ] AES-256-GCM encryption service (FR-014)
- [ ] Testes (open finance)

## Phase 3: Story 3 — Sync de Transações (P3)
- [ ] Entidades: Account, Transaction, SyncHistory
- [ ] Query: ListAccounts (FR-019, FR-020)
- [ ] Command: SyncTransactions — Hangfire job (FR-021, FR-024)
- [ ] Command: ManualSync (FR-022)
- [ ] Deduplicação de transações (FR-023)
- [ ] Limite 1 sync por conexão (FR-025)
- [ ] Histórico de sincronizações (FR-026)
- [ ] Testes (sync)

## Phase 4: Story 4 — Categorização por IA (P4)
- [ ] Entidades: Category, CategorizationRule
- [ ] Categorias padrão seed (FR-031)
- [ ] Interface ICategorizer → OpenAI GPT-4o-mini
- [ ] Command: CategorizeTransactions (FR-035, FR-036, FR-037)
- [ ] Command: CorrectCategory + ApplyToSimilar (FR-038)
- [ ] Auto-create rule após 3 correções (FR-039)
- [ ] Fallback "Outros" quando IA indisponível (FR-040)
- [ ] Testes (categorization)

## Phase 5: Story 5 — Dashboard Financeiro (P5)
- [ ] Query: DashboardSummary — saldo consolidado + variação (FR-041, FR-042)
- [ ] Query: SpendingByCategory — gráficos pizza/barra (FR-043)
- [ ] Query: TransactionList — paginada + ordenável (FR-046, FR-047)
- [ ] Filtros: período, conta (FR-044, FR-045)
- [ ] Drill-down categoria → transações (FR-050)
- [ ] Angular: Dashboard page + charts
- [ ] Testes (dashboard)

## Phase 6: Stories 6-7 — Categorias Custom + Busca (P6-P7)
- [ ] CRUD categorias customizadas (FR-032, FR-033, FR-034)
- [ ] Limite 50 categorias por usuário (FR-032)
- [ ] Migração de transações ao excluir categoria (FR-034)
- [ ] Busca textual em descrições (FR-048)
- [ ] Busca por faixa de valor (FR-049)
- [ ] Testes (categories + search)

## Phase 7: Story 8 — Importação Manual (P8)
- [ ] Upload OFX/CSV (FR-027)
- [ ] Parser + Preview (FR-028, FR-030)
- [ ] Deduplicação contra existentes (FR-029)
- [ ] Auto-detect encoding + mapeamento manual (FR-030)
- [ ] Testes (import)

## Phase 8: Story 9 — Tendências (P9)
- [ ] Query: MonthlyTrends — gráfico linha + média móvel (FR-051)
- [ ] Query: MonthComparison — por categoria (FR-052)
- [ ] Angular: Trends page
- [ ] Testes (trends)

## Phase 9: Story 10 — Perfil e Privacidade (P10)
- [ ] Command: UpdateProfile (FR-008, FR-009)
- [ ] Command: ExportData — JSON LGPD (FR-010)
- [ ] Command: DeleteAccount — soft delete 30 dias (FR-011)
- [ ] Command: UpdatePreferences (FR-053, FR-054)
- [ ] Audit logging (FR-055, FR-056)
- [ ] Input sanitization (FR-057)
- [ ] Testes (profile + privacy)

## Completed
- [x] Project enabled for Ralph
- [x] Ralph configured for Motor Financeiro
- [x] Loop 1: Phase 0 scaffolding (entities, DbContext, basic configs)
- [x] Loop 2: Phase 0.5 fixes + Phase 1 auth (28 tests passing)

## Notes
- Implementar na ordem das Phases (0 → 9)
- Cada Phase corresponde a uma User Story (exceto Phase 0 = scaffolding)
- Consultar specs/ para detalhes de cada FR
- Target: ≥90% cobertura de testes no backend
- Commit atômico após cada task concluída
