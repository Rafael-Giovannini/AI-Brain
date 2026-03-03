# Quickstart: Motor Financeiro

**Branch**: `001-motor-financeiro` | **Date**: 2026-03-03

Guia rápido para configurar o ambiente de desenvolvimento e rodar o Motor Financeiro localmente.

---

## Pré-requisitos

| Ferramenta | Versão | Finalidade |
|-----------|--------|-----------|
| .NET SDK | 10.0+ | Backend (ASP.NET Core) |
| Node.js | 20+ LTS | Frontend (Angular CLI) |
| Docker + Docker Compose | 24+ | PostgreSQL, MongoDB, Redis |
| Git | 2.40+ | Controle de versão |

**IDEs recomendadas:** JetBrains Rider ou Visual Studio 2022+

---

## 1. Clonar e configurar

```bash
git clone https://github.com/<repo>/AI-Brain.git
cd AI-Brain
git checkout 001-motor-financeiro
cd workspace/motor-financeiro
```

---

## 2. Subir infraestrutura (Docker)

```bash
docker compose -f docker/docker-compose.dev.yml up -d
```

Isso sobe:
- **PostgreSQL 17** na porta 5432 (user: `motor`, pass: `motor_dev`, db: `motor_financeiro`)
- **MongoDB 8** na porta 27017 (sem auth no dev)
- **Redis 7** na porta 6379 (sem auth no dev)

Verificar que os containers estão rodando:
```bash
docker compose -f docker/docker-compose.dev.yml ps
```

---

## 3. Configurar variáveis de ambiente

Copiar o template:
```bash
cp src/MotorFinanceiro.Api/appsettings.Development.example.json src/MotorFinanceiro.Api/appsettings.Development.json
```

Preencher as chaves obrigatórias:
```json
{
  "ConnectionStrings": {
    "PostgreSQL": "Host=localhost;Port=5432;Database=motor_financeiro;Username=motor;Password=motor_dev",
    "MongoDB": "mongodb://localhost:27017/motor_financeiro_logs",
    "Redis": "localhost:6379"
  },
  "Jwt": {
    "Issuer": "motor-financeiro",
    "Audience": "motor-financeiro-client",
    "AccessTokenExpirationMinutes": 15,
    "RefreshTokenExpirationDays": 7
  },
  "Pluggy": {
    "ClientId": "<PLUGGY_CLIENT_ID>",
    "ClientSecret": "<PLUGGY_CLIENT_SECRET>",
    "BaseUrl": "https://api.pluggy.ai",
    "UseSandbox": true
  },
  "OpenAI": {
    "ApiKey": "<OPENAI_API_KEY>",
    "Model": "gpt-4o-mini",
    "MaxTokens": 150,
    "Temperature": 0.1
  },
  "Resend": {
    "ApiKey": "<RESEND_API_KEY>",
    "FromEmail": "noreply@motor-financeiro.com"
  },
  "Encryption": {
    "MasterKey": "<CHAVE_AES_256_BASE64>"
  }
}
```

**Obter credenciais:**
- **Pluggy:** Criar conta em [dashboard.pluggy.ai](https://dashboard.pluggy.ai) → Sandbox credentials
- **OpenAI:** Criar API key em [platform.openai.com](https://platform.openai.com)
- **Resend:** Criar conta em [resend.com](https://resend.com) → API key
- **Encryption key:** Gerar com `openssl rand -base64 32`

**Gerar par de chaves RSA para JWT:**
```bash
mkdir -p src/MotorFinanceiro.Api/Keys
openssl genrsa -out src/MotorFinanceiro.Api/Keys/rsa-private.pem 2048
openssl rsa -in src/MotorFinanceiro.Api/Keys/rsa-private.pem -pubout -out src/MotorFinanceiro.Api/Keys/rsa-public.pem
```

---

## 4. Rodar o Backend

```bash
cd src/MotorFinanceiro.Api

# Restaurar dependências
dotnet restore

# Aplicar migrations no PostgreSQL
dotnet ef database update --project ../MotorFinanceiro.Infrastructure

# Rodar a API (hot reload)
dotnet watch run
```

A API estará disponível em:
- **Swagger UI:** http://localhost:5000/swagger
- **API Base:** http://localhost:5000/api/v1
- **Health check:** http://localhost:5000/health

---

## 5. Rodar o Frontend

```bash
cd src/MotorFinanceiro.Web

# Instalar dependências
npm install

# Rodar dev server (hot reload)
ng serve
```

O frontend estará disponível em:
- **App:** http://localhost:4200
- Proxy configurado para redirecionar `/api/*` para `localhost:5000`

---

## 6. Rodar Testes

```bash
# Backend - testes unitários
dotnet test tests/MotorFinanceiro.Domain.Tests
dotnet test tests/MotorFinanceiro.Application.Tests

# Backend - testes de integração (requer Docker)
dotnet test tests/MotorFinanceiro.Infrastructure.Tests
dotnet test tests/MotorFinanceiro.Api.Tests

# Todos os testes com cobertura
dotnet test --collect:"XPlat Code Coverage" --results-directory ./coverage

# Frontend - testes unitários
cd src/MotorFinanceiro.Web
ng test --watch=false --code-coverage

# E2E (requer backend + frontend rodando)
npx cypress run
```

---

## 7. Estrutura do Projeto

```
workspace/motor-financeiro/
├── src/
│   ├── MotorFinanceiro.Api/              # ASP.NET Core Web API
│   │   ├── Controllers/                   # API Controllers
│   │   ├── Middleware/                     # Auth, RateLimit, Logging
│   │   └── Program.cs                     # DI + Pipeline setup
│   │
│   ├── MotorFinanceiro.Domain/           # Entidades, Value Objects, Interfaces
│   ├── MotorFinanceiro.Application/      # Use Cases (Commands/Queries via MediatR)
│   ├── MotorFinanceiro.Infrastructure/   # EF Core, Pluggy, OpenAI, Redis, Hangfire
│   │
│   └── MotorFinanceiro.Web/             # Angular SPA
│       └── src/app/features/             # Auth, Dashboard, Transactions, etc.
│
├── tests/
│   ├── MotorFinanceiro.Domain.Tests/
│   ├── MotorFinanceiro.Application.Tests/
│   ├── MotorFinanceiro.Infrastructure.Tests/
│   ├── MotorFinanceiro.Api.Tests/
│   └── MotorFinanceiro.E2E.Tests/
│
├── docker/
│   ├── docker-compose.yml                # Produção
│   ├── docker-compose.dev.yml            # Desenvolvimento
│   ├── Dockerfile.api
│   └── Dockerfile.web
│
├── docs/                                  # Documentação de produto
└── specs/                                 # Especificações técnicas (Spec-Kit)
```

---

## Comandos Úteis

| Comando | Descrição |
|---------|-----------|
| `dotnet watch run` | Rodar API com hot reload |
| `ng serve` | Rodar frontend com hot reload |
| `dotnet ef migrations add <Name>` | Criar nova migration |
| `dotnet ef database update` | Aplicar migrations |
| `dotnet test` | Rodar todos os testes |
| `docker compose -f docker/docker-compose.dev.yml logs -f` | Ver logs da infraestrutura |
| `docker compose -f docker/docker-compose.dev.yml down -v` | Parar e remover volumes |

---

## Troubleshooting

**PostgreSQL não conecta:**
- Verificar se o container está rodando: `docker ps`
- Verificar porta: `docker port motor-postgres`
- Testar conexão: `psql -h localhost -U motor -d motor_financeiro`

**Migration falha:**
- Verificar connection string no `appsettings.Development.json`
- Rodar `dotnet ef database drop` e `dotnet ef database update` para reset

**Pluggy sandbox não responde:**
- Verificar credenciais no dashboard.pluggy.ai
- Confirmar que `UseSandbox: true` no config
- Testar com `curl`: `curl -X POST https://api.pluggy.ai/auth -d '{"clientId":"...","clientSecret":"..."}'`

**Frontend não conecta na API:**
- Verificar proxy em `proxy.conf.json` aponta para `localhost:5000`
- Verificar CORS no backend (dev mode aceita `localhost:4200`)
