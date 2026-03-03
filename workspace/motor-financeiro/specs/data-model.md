# Data Model: Motor Financeiro

**Branch**: `001-motor-financeiro` | **Date**: 2026-03-03
**ORM**: Entity Framework Core 10 | **DB**: PostgreSQL 17

---

## Entidades PostgreSQL

### 1. User

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| Email | varchar(255) | UNIQUE, NOT NULL | Email do usuário |
| PasswordHash | varchar(512) | NOT NULL | Hash Argon2id da senha |
| Name | varchar(100) | NOT NULL | Nome de exibição |
| IsEmailConfirmed | boolean | NOT NULL, DEFAULT false | Email verificado? |
| IsActive | boolean | NOT NULL, DEFAULT true | Conta ativa (soft delete) |
| DeactivatedAt | timestamp | NULL | Data de desativação (LGPD 30 dias) |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Has many: RefreshToken, Connection, Account, Transaction, Category, CategorizationRule, SyncHistory, UserPreference

**Índices:**
- `UNIQUE idx_users_email ON (email)`

---

### 2. RefreshToken

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono do token |
| TokenHash | varchar(128) | NOT NULL | SHA-256 do refresh token |
| FamilyId | UUID | NOT NULL | Família de tokens (para detecção de reutilização) |
| Generation | int | NOT NULL, DEFAULT 0 | Número de rotações |
| IsRevoked | boolean | NOT NULL, DEFAULT false | Token revogado? |
| ReplacedById | UUID | FK → RefreshToken, NULL | Token que substituiu este |
| UserAgent | varchar(500) | NULL | Device fingerprint |
| IpAddress | varchar(45) | NULL | IP de origem |
| ExpiresAt | timestamp | NOT NULL | Expiração (7 dias após criação) |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |

**Relacionamentos:**
- Belongs to: User
- Self-reference: ReplacedById → RefreshToken

**Índices:**
- `idx_refresh_tokens_user ON (user_id)`
- `idx_refresh_tokens_token_hash ON (token_hash)`
- `idx_refresh_tokens_family ON (family_id)`

**Regras de negócio:**
- Se `IsRevoked = true` e token é apresentado → revogar toda a família (`WHERE family_id = @familyId`)
- Cleanup automático de tokens expirados via job periódico

---

### 3. Connection

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono da conexão |
| PluggyItemId | varchar(100) | NOT NULL | ID do Item na Pluggy |
| InstitutionName | varchar(200) | NOT NULL | Nome da instituição financeira |
| InstitutionLogo | varchar(500) | NULL | URL do logo |
| Status | varchar(20) | NOT NULL, DEFAULT 'active' | active, expired, revoked, error |
| PluggyAccessToken | text | NOT NULL | Token de acesso Pluggy (encriptado AES-256-GCM) |
| PluggyRefreshToken | text | NULL | Refresh token Pluggy (encriptado AES-256-GCM) |
| ConsentExpiresAt | timestamp | NULL | Data de expiração do consentimento |
| LastSyncAt | timestamp | NULL | Última sincronização bem-sucedida |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User
- Has many: Account, SyncHistory

**Índices:**
- `idx_connections_user ON (user_id)`
- `idx_connections_pluggy_item ON (pluggy_item_id)`

**Transições de estado:**
```
active → expired   (consentimento expirou)
active → revoked   (usuário revogou ou instituição revogou externamente)
active → error     (falha de comunicação persistente)
expired → active   (consentimento renovado)
error → active     (reconexão bem-sucedida)
```

---

### 4. Account

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono da conta |
| ConnectionId | UUID | FK → Connection, NULL | Conexão Open Finance (NULL para import manual) |
| PluggyAccountId | varchar(100) | NULL | ID da conta na Pluggy |
| Name | varchar(200) | NOT NULL | Nome da conta |
| Type | varchar(20) | NOT NULL | checking, savings, credit_card, manual |
| InstitutionName | varchar(200) | NOT NULL | Nome da instituição |
| Balance | decimal(15,2) | NOT NULL, DEFAULT 0 | Saldo atual |
| Currency | varchar(3) | NOT NULL, DEFAULT 'BRL' | Moeda (ISO 4217) |
| LastSyncAt | timestamp | NULL | Última sincronização |
| IsActive | boolean | NOT NULL, DEFAULT true | Conta ativa? |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User, Connection
- Has many: Transaction

**Índices:**
- `idx_accounts_user ON (user_id)`
- `idx_accounts_connection ON (connection_id)`

---

### 5. Transaction

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono da transação |
| AccountId | UUID | FK → Account, NOT NULL | Conta bancária |
| CategoryId | UUID | FK → Category, NULL | Categoria atribuída |
| ExternalIdHash | varchar(128) | NULL | SHA-256 do ID externo (deduplicação) |
| Date | timestamp | NOT NULL | Data da transação |
| Description | varchar(500) | NOT NULL | Descrição da transação |
| Amount | decimal(15,2) | NOT NULL | Valor (negativo = débito, positivo = crédito) |
| Type | varchar(10) | NOT NULL | debit, credit |
| Currency | varchar(3) | NOT NULL, DEFAULT 'BRL' | Moeda original |
| ConfidenceScore | decimal(3,2) | NULL | Confiança da categorização (0.00 a 1.00) |
| IsManualCategory | boolean | NOT NULL, DEFAULT false | Categorizado manualmente? |
| NeedsReview | boolean | NOT NULL, DEFAULT false | Confiança < 0.7? |
| Source | varchar(20) | NOT NULL, DEFAULT 'open_finance' | open_finance, import_ofx, import_csv |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User, Account, Category

**Índices:**
- `idx_transactions_user_date ON (user_id, date DESC)` — queries de listagem
- `idx_transactions_user_category ON (user_id, category_id)` — aggregation por categoria
- `idx_transactions_user_account ON (user_id, account_id)` — filtro por conta
- `idx_transactions_external_hash ON (external_id_hash)` — deduplicação
- `idx_transactions_description_gin ON USING gin (to_tsvector('portuguese', description))` — full-text search
- `idx_transactions_needs_review ON (user_id, needs_review) WHERE needs_review = true` — parcial

**Regras de deduplicação:**
- Na sincronização: `external_id_hash = SHA256(pluggy_transaction_id + account_id)`
- Na importação: `external_id_hash = SHA256(date + description + amount + account_id)`

---

### 6. Category

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NULL | NULL = categoria padrão do sistema |
| Name | varchar(100) | NOT NULL | Nome da categoria |
| Color | varchar(7) | NOT NULL, DEFAULT '#6B7280' | Cor hex (#RRGGBB) |
| Icon | varchar(50) | NOT NULL, DEFAULT 'tag' | Nome do ícone |
| IsDefault | boolean | NOT NULL, DEFAULT false | Categoria padrão do sistema? |
| IsActive | boolean | NOT NULL, DEFAULT true | Categoria ativa? |
| DisplayOrder | int | NOT NULL, DEFAULT 0 | Ordem de exibição |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User (opcional)
- Has many: Transaction, CategorizationRule

**Índices:**
- `idx_categories_user ON (user_id)`

**Categorias padrão do sistema (seed data):**
| Nome | Cor | Ícone |
|------|-----|-------|
| Moradia | #EF4444 | home |
| Alimentação | #F97316 | utensils |
| Transporte | #EAB308 | car |
| Saúde | #22C55E | heart-pulse |
| Educação | #3B82F6 | graduation-cap |
| Lazer | #8B5CF6 | gamepad-2 |
| Utilidades | #06B6D4 | zap |
| Renda | #10B981 | trending-up |
| Transferência | #6B7280 | arrow-left-right |
| Outros | #9CA3AF | tag |

**Regras de negócio:**
- Categorias padrão (`is_default = true`): não podem ser excluídas, apenas desativadas
- Limite de 50 categorias customizadas por usuário
- Ao excluir categoria customizada: transações migram para "Outros" ou categoria escolhida

---

### 7. CategorizationRule

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono da regra |
| CategoryId | UUID | FK → Category, NOT NULL | Categoria alvo |
| Pattern | varchar(500) | NOT NULL | Padrão de descrição (case-insensitive) |
| CorrectionsCount | int | NOT NULL, DEFAULT 3 | Número de correções que geraram a regra |
| IsActive | boolean | NOT NULL, DEFAULT true | Regra ativa? |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User, Category

**Índices:**
- `idx_categorization_rules_user ON (user_id)`

**Regras de negócio:**
- Criada automaticamente após 3 correções consistentes para mesmo padrão de descrição
- Consultada ANTES da IA para economia de custo e personalização
- Matching: `LOWER(description) LIKE LOWER(pattern)`

---

### 8. SyncHistory

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, NOT NULL | Dono |
| ConnectionId | UUID | FK → Connection, NOT NULL | Conexão sincronizada |
| Status | varchar(20) | NOT NULL | success, partial, failed |
| TransactionsCount | int | NOT NULL, DEFAULT 0 | Transações importadas |
| DuplicatesSkipped | int | NOT NULL, DEFAULT 0 | Duplicatas descartadas |
| ErrorMessage | text | NULL | Mensagem de erro (se aplicável) |
| StartedAt | timestamp | NOT NULL | Início da execução |
| CompletedAt | timestamp | NULL | Fim da execução |

**Relacionamentos:**
- Belongs to: User, Connection

**Índices:**
- `idx_sync_history_user ON (user_id)`
- `idx_sync_history_connection ON (connection_id, started_at DESC)`

---

### 9. UserPreference

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| Id | UUID v7 | PK | Identificador único |
| UserId | UUID | FK → User, UNIQUE, NOT NULL | 1:1 com User |
| Currency | varchar(3) | NOT NULL, DEFAULT 'BRL' | Moeda padrão de exibição |
| Timezone | varchar(50) | NOT NULL, DEFAULT 'America/Sao_Paulo' | Fuso horário |
| Locale | varchar(10) | NOT NULL, DEFAULT 'pt-BR' | Idioma |
| CreatedAt | timestamp | NOT NULL, DEFAULT now() | Data de criação |
| UpdatedAt | timestamp | NOT NULL, DEFAULT now() | Última atualização |

**Relacionamentos:**
- Belongs to: User (1:1)

**Índices:**
- `UNIQUE idx_user_preferences_user ON (user_id)`

---

## Entidades MongoDB

### AuditLog (Collection: audit_logs)

```json
{
  "_id": "ObjectId",
  "user_id": "UUID string",
  "action": "LOGIN | LOGOUT | LOGIN_FAILED | REGISTER | PASSWORD_RESET | EMAIL_CONFIRMED | CONNECTION_CREATED | CONNECTION_REVOKED | CATEGORY_CHANGED | TRANSACTION_RECATEGORIZED | DATA_EXPORTED | ACCOUNT_DELETED | PROFILE_UPDATED | TOKEN_REFRESH | TOKEN_REUSE_DETECTED",
  "entity_type": "User | Connection | Transaction | Category | null",
  "entity_id": "UUID string | null",
  "old_value": {},
  "new_value": {},
  "ip_address": "string",
  "user_agent": "string",
  "correlation_id": "UUID string",
  "severity": "info | warn | critical",
  "timestamp": "ISODate"
}
```
- **TTL Index:** `timestamp` com TTL de 365 dias
- **Índice:** `(user_id, timestamp)`, `(action, timestamp)`, `(correlation_id)`
- **Permissões:** Application user tem apenas INSERT (append-only)

### AppLog (Collection: app_logs)

```json
{
  "_id": "ObjectId",
  "level": "Debug | Info | Warning | Error | Critical",
  "message": "string",
  "message_template": "string",
  "exception": "string | null",
  "correlation_id": "UUID string",
  "properties": {},
  "timestamp": "ISODate"
}
```
- **TTL Index:** `timestamp` com TTL de 90 dias

---

## Diagrama de Relacionamentos (ER)

```
User (1) ──── (N) RefreshToken
  │
  ├── (1) ──── (1) UserPreference
  │
  ├── (1) ──── (N) Connection ──── (N) Account ──── (N) Transaction
  │                    │                                    │
  │                    └── (N) SyncHistory                  │
  │                                                         │
  ├── (1) ──── (N) Category ────────────────────────────────┘
  │                    │
  └── (1) ──── (N) CategorizationRule ──────────────────────┘
```

---

## Row-Level Security (PostgreSQL)

```sql
-- Habilitar RLS em todas as tabelas com dados de usuário
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE connections ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorization_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE sync_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_tokens ENABLE ROW LEVEL SECURITY;

-- Política de isolamento por usuário
CREATE POLICY user_isolation ON transactions
    USING (user_id = current_setting('app.current_user_id')::uuid);
-- (repetir para cada tabela)
```

**Nota:** RLS é a segunda camada de proteção. A primeira é o filtro `WHERE user_id = @currentUserId` na Application Layer via `ICurrentUser`.

---

## Migrations Strategy

- EF Core Code-First Migrations
- Cada migration nomeada descritivamente: `AddTransactionNeedsReviewColumn`
- Migration separada para seed data de categorias padrão
- Migration separada para índices (após tabelas criarem)
- Todas as migrations versionadas no Git
