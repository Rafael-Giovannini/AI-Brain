# API Contract: Motor Financeiro v1

**Base URL**: `/api/v1`
**Format**: JSON (request/response)
**Auth**: Bearer JWT (header `Authorization: Bearer <access_token>`)
**Errors**: RFC 7807 ProblemDetails
**Pagination**: Offset-based (`?page=1&pageSize=20`)

---

## Authentication

### POST /auth/register
Registrar novo usuário.

**Auth**: Não requer

**Request:**
```json
{
  "email": "usuario@exemplo.com",
  "password": "SenhaForte123!",
  "name": "João Silva"
}
```

**Response 201:**
```json
{
  "message": "Email de confirmação enviado para usuario@exemplo.com"
}
```

**Errors:**
- `400`: Email inválido, senha fraca, nome vazio
- `409`: Email já cadastrado (mensagem genérica por segurança)
- `429`: Rate limit (3/10min/IP)

---

### POST /auth/login
Autenticar usuário.

**Auth**: Não requer

**Request:**
```json
{
  "email": "usuario@exemplo.com",
  "password": "SenhaForte123!"
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "expiresIn": 900
}
```
*Refresh token enviado como httpOnly cookie `refresh_token`*

**Errors:**
- `401`: Credenciais inválidas (mensagem genérica)
- `403`: Email não confirmado
- `429`: Rate limit (5/1min/IP)

---

### POST /auth/refresh
Renovar access token.

**Auth**: Cookie `refresh_token`

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "expiresIn": 900
}
```
*Novo refresh token no cookie (rotation)*

**Errors:**
- `401`: Token inválido, expirado ou reutilizado (revoga família)

---

### POST /auth/logout
Invalidar sessão.

**Auth**: JWT

**Response 204**: (sem corpo)

---

### POST /auth/forgot-password
Solicitar reset de senha.

**Auth**: Não requer

**Request:**
```json
{
  "email": "usuario@exemplo.com"
}
```

**Response 200:**
```json
{
  "message": "Se o email estiver cadastrado, enviaremos instruções de recuperação"
}
```
*Sempre retorna 200 (não revela se email existe)*

---

### POST /auth/reset-password
Definir nova senha.

**Auth**: Reset token (query param)

**Request:**
```json
{
  "token": "abc123...",
  "newPassword": "NovaSenha456!"
}
```

**Response 200:**
```json
{
  "message": "Senha alterada com sucesso"
}
```

**Errors:**
- `400`: Senha fraca, token inválido/expirado

---

### GET /auth/confirm-email?token={token}
Confirmar email.

**Auth**: Email token (query param)

**Response 200:**
```json
{
  "message": "Email confirmado com sucesso"
}
```

---

## User Management

### GET /users/me
Dados do perfil.

**Auth**: JWT

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@exemplo.com",
  "name": "João Silva",
  "isEmailConfirmed": true,
  "createdAt": "2026-01-15T10:30:00Z"
}
```

---

### PATCH /users/me
Atualizar perfil.

**Auth**: JWT

**Request:**
```json
{
  "name": "João da Silva"
}
```

**Response 200:** (objeto User atualizado)

*Alteração de email requer confirmação separada.*

---

### DELETE /users/me
Excluir conta (LGPD).

**Auth**: JWT

**Request:**
```json
{
  "confirmPassword": "SenhaForte123!"
}
```

**Response 200:**
```json
{
  "message": "Conta desativada. Dados serão removidos permanentemente em 30 dias"
}
```

---

### GET /users/me/export
Exportar dados pessoais (LGPD).

**Auth**: JWT

**Response 200:**
```json
{
  "user": { ... },
  "preferences": { ... },
  "connections": [ ... ],
  "accounts": [ ... ],
  "transactions": [ ... ],
  "categories": [ ... ],
  "categorizationRules": [ ... ],
  "exportedAt": "2026-03-03T15:00:00Z"
}
```

---

### GET /users/me/preferences
Preferências do usuário.

**Auth**: JWT

**Response 200:**
```json
{
  "currency": "BRL",
  "timezone": "America/Sao_Paulo",
  "locale": "pt-BR"
}
```

---

### PATCH /users/me/preferences
Atualizar preferências.

**Auth**: JWT

**Request:**
```json
{
  "currency": "BRL",
  "timezone": "America/Sao_Paulo",
  "locale": "pt-BR"
}
```

**Response 200:** (objeto Preferences atualizado)

---

## Open Finance

### GET /institutions
Listar instituições financeiras suportadas.

**Auth**: JWT

**Response 200:**
```json
{
  "data": [
    {
      "id": "connector-123",
      "name": "Nubank",
      "type": "PERSONAL_BANK",
      "imageUrl": "https://cdn.pluggy.ai/connectors/nubank.png",
      "country": "BR"
    }
  ],
  "total": 42
}
```

---

### POST /connections
Iniciar conexão com instituição.

**Auth**: JWT

**Request:**
```json
{
  "institutionId": "connector-123"
}
```

**Response 201:**
```json
{
  "connectionId": "550e8400-e29b-41d4-a716-446655440001",
  "connectUrl": "https://connect.pluggy.ai/...",
  "connectToken": "eyJ..."
}
```
*Frontend abre `connectUrl` para fluxo OAuth*

---

### GET /connections
Listar conexões do usuário.

**Auth**: JWT

**Response 200:**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "institutionName": "Nubank",
      "institutionLogo": "https://cdn.pluggy.ai/...",
      "status": "active",
      "consentExpiresAt": "2026-09-03T00:00:00Z",
      "lastSyncAt": "2026-03-03T12:00:00Z",
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ],
  "total": 2
}
```

---

### DELETE /connections/{id}
Revogar consentimento.

**Auth**: JWT

**Response 204**: (sem corpo)

*Dados já importados são mantidos. Consentimento revogado na Pluggy.*

---

### POST /connections/{id}/renew
Renovar consentimento.

**Auth**: JWT

**Response 200:**
```json
{
  "connectUrl": "https://connect.pluggy.ai/...",
  "connectToken": "eyJ..."
}
```

---

### GET /accounts
Listar contas conectadas.

**Auth**: JWT

**Query params:** `?connectionId={uuid}` (opcional)

**Response 200:**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "connectionId": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Conta Corrente",
      "type": "checking",
      "institutionName": "Nubank",
      "balance": 5432.10,
      "currency": "BRL",
      "lastSyncAt": "2026-03-03T12:00:00Z"
    }
  ],
  "total": 3
}
```

---

### POST /sync
Sincronização manual.

**Auth**: JWT

**Request:**
```json
{
  "connectionId": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response 202:**
```json
{
  "message": "Sincronização iniciada",
  "syncId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Errors:**
- `409`: Sincronização já em andamento para esta conexão
- `404`: Conexão não encontrada

---

### GET /sync/history
Histórico de sincronizações.

**Auth**: JWT

**Query params:** `?connectionId={uuid}&page=1&pageSize=20`

**Response 200:**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "connectionId": "550e8400-e29b-41d4-a716-446655440001",
      "status": "success",
      "transactionsCount": 47,
      "duplicatesSkipped": 3,
      "startedAt": "2026-03-03T12:00:00Z",
      "completedAt": "2026-03-03T12:00:15Z"
    }
  ],
  "total": 10,
  "page": 1,
  "pageSize": 20
}
```

---

### POST /import
Upload de arquivo OFX/CSV.

**Auth**: JWT

**Request:** `multipart/form-data`
- `file`: arquivo OFX ou CSV (max 10MB)
- `accountId`: UUID da conta de destino

**Response 200:**
```json
{
  "preview": [
    {
      "date": "2026-02-15",
      "description": "SUPERMERCADO XYZ",
      "amount": -156.80,
      "type": "debit"
    }
  ],
  "total": 42,
  "importId": "temp-import-123"
}
```

---

### POST /import/confirm
Confirmar importação após preview.

**Auth**: JWT

**Request:**
```json
{
  "importId": "temp-import-123"
}
```

**Response 201:**
```json
{
  "imported": 40,
  "duplicatesSkipped": 2,
  "message": "40 transações importadas com sucesso"
}
```

---

## Categories

### GET /categories
Listar categorias (padrão + customizadas).

**Auth**: JWT

**Response 200:**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440020",
      "name": "Moradia",
      "color": "#EF4444",
      "icon": "home",
      "isDefault": true,
      "isActive": true,
      "transactionCount": 23
    }
  ],
  "total": 14
}
```

---

### POST /categories
Criar categoria customizada.

**Auth**: JWT

**Request:**
```json
{
  "name": "Pet Shop",
  "color": "#F59E0B",
  "icon": "paw-print"
}
```

**Response 201:** (objeto Category criado)

**Errors:**
- `400`: Nome vazio, cor inválida
- `409`: Limite de 50 categorias atingido

---

### PATCH /categories/{id}
Editar categoria.

**Auth**: JWT

**Request:**
```json
{
  "name": "Pet & Veterinário",
  "color": "#F59E0B"
}
```

**Response 200:** (objeto Category atualizado)

---

### DELETE /categories/{id}
Excluir categoria customizada.

**Auth**: JWT

**Query params:** `?migrateTo={categoryId}` (opcional, default: "Outros")

**Response 204**: (sem corpo)

**Errors:**
- `403`: Não é possível excluir categoria padrão

---

### PATCH /transactions/{id}/category
Corrigir categoria de uma transação.

**Auth**: JWT

**Request:**
```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440020"
}
```

**Response 200:** (objeto Transaction atualizado)

---

### POST /transactions/{id}/apply-similar
Aplicar categoria a transações similares.

**Auth**: JWT

**Response 200:**
```json
{
  "updated": 5,
  "message": "Categoria aplicada a 5 transações similares"
}
```

---

## Dashboard

### GET /dashboard/summary
Saldos consolidados.

**Auth**: JWT

**Query params:** `?accountId={uuid}&from={date}&to={date}`

**Response 200:**
```json
{
  "totalBalance": 15432.50,
  "totalBalanceVariation": 1250.00,
  "totalBalanceVariationPercent": 8.82,
  "accounts": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "Conta Corrente - Nubank",
      "balance": 5432.10,
      "currency": "BRL"
    }
  ],
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-03"
  }
}
```

---

### GET /transactions
Lista paginada com filtros.

**Auth**: JWT

**Query params:**
- `page` (int, default: 1)
- `pageSize` (int, default: 20, max: 50)
- `from` (date, ISO 8601)
- `to` (date, ISO 8601)
- `accountId` (UUID)
- `categoryId` (UUID)
- `search` (string, full-text search em descrição)
- `minAmount` (decimal)
- `maxAmount` (decimal)
- `sortBy` (date | amount | category, default: date)
- `sortOrder` (asc | desc, default: desc)

**Response 200:**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440030",
      "accountId": "550e8400-e29b-41d4-a716-446655440002",
      "accountName": "Conta Corrente - Nubank",
      "date": "2026-03-02T14:30:00Z",
      "description": "SUPERMERCADO XYZ",
      "amount": -156.80,
      "type": "debit",
      "currency": "BRL",
      "category": {
        "id": "550e8400-e29b-41d4-a716-446655440020",
        "name": "Alimentação",
        "color": "#F97316",
        "icon": "utensils"
      },
      "confidenceScore": 0.92,
      "isManualCategory": false,
      "needsReview": false
    }
  ],
  "total": 247,
  "page": 1,
  "pageSize": 20,
  "totalPages": 13
}
```

---

### GET /transactions/{id}
Detalhe de transação.

**Auth**: JWT

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440030",
  "accountId": "550e8400-e29b-41d4-a716-446655440002",
  "accountName": "Conta Corrente - Nubank",
  "institutionName": "Nubank",
  "date": "2026-03-02T14:30:00Z",
  "description": "SUPERMERCADO XYZ",
  "amount": -156.80,
  "type": "debit",
  "currency": "BRL",
  "category": {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "name": "Alimentação",
    "color": "#F97316",
    "icon": "utensils"
  },
  "confidenceScore": 0.92,
  "isManualCategory": false,
  "needsReview": false,
  "source": "open_finance",
  "createdAt": "2026-03-02T15:00:00Z"
}
```

---

### GET /dashboard/spending-by-category
Gastos por categoria (para gráficos pizza/barras).

**Auth**: JWT

**Query params:** `?from={date}&to={date}&accountId={uuid}`

**Response 200:**
```json
{
  "data": [
    {
      "categoryId": "550e8400-e29b-41d4-a716-446655440020",
      "categoryName": "Alimentação",
      "categoryColor": "#F97316",
      "categoryIcon": "utensils",
      "total": -2340.50,
      "count": 23,
      "percentage": 28.5
    }
  ],
  "totalSpending": -8212.28,
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-31"
  }
}
```

---

### GET /dashboard/trends
Tendência mensal (últimos 6-12 meses).

**Auth**: JWT

**Query params:** `?months=12&accountId={uuid}`

**Response 200:**
```json
{
  "data": [
    {
      "month": "2026-03",
      "totalSpending": -8212.28,
      "totalIncome": 12500.00,
      "balance": 4287.72
    }
  ],
  "movingAverage": [
    {
      "month": "2026-03",
      "average": -7850.00
    }
  ]
}
```

---

### GET /dashboard/comparison
Comparativo mês atual vs anterior.

**Auth**: JWT

**Query params:** `?accountId={uuid}`

**Response 200:**
```json
{
  "currentMonth": "2026-03",
  "previousMonth": "2026-02",
  "categories": [
    {
      "categoryId": "550e8400-e29b-41d4-a716-446655440020",
      "categoryName": "Alimentação",
      "categoryColor": "#F97316",
      "currentTotal": -2340.50,
      "previousTotal": -2100.00,
      "variation": -240.50,
      "variationPercent": -11.45,
      "direction": "increase"
    }
  ],
  "totals": {
    "currentTotal": -8212.28,
    "previousTotal": -7500.00,
    "variation": -712.28,
    "variationPercent": -9.50
  }
}
```

---

## Formato de Erro (RFC 7807)

```json
{
  "type": "https://motor-financeiro.com/errors/validation",
  "title": "Erro de Validação",
  "status": 400,
  "detail": "Um ou mais campos são inválidos",
  "instance": "/api/v1/auth/register",
  "errors": {
    "email": ["Email inválido"],
    "password": ["Senha deve ter no mínimo 8 caracteres"]
  }
}
```

---

## Paginação Padrão

Todas as listas seguem o formato:
```json
{
  "data": [...],
  "total": 247,
  "page": 1,
  "pageSize": 20,
  "totalPages": 13
}
```

**Limites:**
- `pageSize` mínimo: 1, máximo: 50, default: 20
- `page` mínimo: 1

---

## Headers de Segurança (todas as respostas)

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

---

**Total: 31 endpoints** | **Documentação interativa: Swagger UI em `/swagger`**
