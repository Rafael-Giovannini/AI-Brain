# Ralph Agent Configuration — Motor Financeiro

## Prerequisites

```bash
# Verificar ferramentas instaladas
dotnet --version    # Requer .NET 10+
node --version      # Requer Node.js 20+
docker --version    # Requer Docker 24+
```

## Infrastructure (Docker)

```bash
# Subir PostgreSQL 17 + MongoDB 8 + Redis 7
cd workspace/motor-financeiro
docker compose -f docker/docker-compose.dev.yml up -d

# Verificar containers
docker compose -f docker/docker-compose.dev.yml ps

# Parar infraestrutura
docker compose -f docker/docker-compose.dev.yml down

# Reset completo (apaga volumes)
docker compose -f docker/docker-compose.dev.yml down -v
```

## Build Instructions

```bash
cd workspace/motor-financeiro

# Backend — Restore + Build
dotnet restore src/MotorFinanceiro.Api/MotorFinanceiro.Api.csproj
dotnet build src/MotorFinanceiro.Api/MotorFinanceiro.Api.csproj --no-restore

# Frontend — Install + Build
cd src/MotorFinanceiro.Web
npm install
ng build
```

## Test Instructions

```bash
cd workspace/motor-financeiro

# Backend — Unit tests (Domain + Application)
dotnet test tests/MotorFinanceiro.Domain.Tests
dotnet test tests/MotorFinanceiro.Application.Tests

# Backend — Integration tests (requer Docker)
dotnet test tests/MotorFinanceiro.Infrastructure.Tests
dotnet test tests/MotorFinanceiro.Api.Tests

# Backend — Todos com cobertura
dotnet test --collect:"XPlat Code Coverage" --results-directory ./coverage

# Frontend — Unit tests
cd src/MotorFinanceiro.Web
ng test --watch=false --code-coverage

# E2E (requer backend + frontend rodando)
npx cypress run
```

## Run Instructions

```bash
cd workspace/motor-financeiro

# 1. Subir infra
docker compose -f docker/docker-compose.dev.yml up -d

# 2. Aplicar migrations
cd src/MotorFinanceiro.Api
dotnet ef database update --project ../MotorFinanceiro.Infrastructure

# 3. Rodar API (hot reload)
dotnet watch run
# API disponível em: http://localhost:5000/swagger

# 4. Rodar Frontend (em outro terminal)
cd src/MotorFinanceiro.Web
ng serve
# Frontend disponível em: http://localhost:4200
```

## Database Commands

```bash
cd workspace/motor-financeiro/src/MotorFinanceiro.Api

# Criar nova migration
dotnet ef migrations add <MigrationName> --project ../MotorFinanceiro.Infrastructure

# Aplicar migrations
dotnet ef database update --project ../MotorFinanceiro.Infrastructure

# Reverter última migration
dotnet ef migrations remove --project ../MotorFinanceiro.Infrastructure

# Reset database
dotnet ef database drop --project ../MotorFinanceiro.Infrastructure --force
dotnet ef database update --project ../MotorFinanceiro.Infrastructure
```

## Notes
- Conexões de dev: PostgreSQL (localhost:5432, motor/motor_dev), MongoDB (localhost:27017), Redis (localhost:6379)
- Config em: `src/MotorFinanceiro.Api/appsettings.Development.json`
- Chaves RSA JWT em: `src/MotorFinanceiro.Api/Keys/` (gitignored)
- Update this file when build process changes
