# Validation Report: ghostfit / 001-ghostfit-mvp

**Gerado**: 2026-03-04 18:00
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp

---

## Sumário Executivo

| Métrica | Valor |
|---------|-------|
| Inconsistências em Docs (Pass A) | 1 |
| BMAD Docs vs Spec (Pass G) | 3 |
| Ralph Config Issues (Pass H) | 0 |
| Entidades: Definidas / Implementadas | 7 / 4 |
| API Endpoints: Definidos / Implementados | 8 / 0 |
| FRs Rastreados no Código | 5 / 21 (24%) |
| Cenários de Aceitação com Testes | 0 / 28 (0%) |
| Issues de Qualidade de Código | 2 |
| Issues CRITICAL | 0 |
| Issues HIGH | 1 |
| Issues MEDIUM | 7 |
| Issues LOW | 3 |

**Nota**: Projeto em estágio inicial de implementação (Phase 1 parcial). A maioria dos MISSING é esperada — entidades de backend, endpoints de API e telas ainda não implementadas. O foco atual está na fundação (onboarding).

---

## 1. Consistência de Docs (Pass A)

| ID | Severity | Localização | Finding | Recomendação |
|----|----------|-------------|---------|--------------|
| A-01 | MEDIUM | sprint-plan STORY-001 vs plan.md | sprint-plan AC diz "JUnit 5 + MockK" mas plan.md e PROMPT.md dizem "JUnit 4". Código usa JUnit 4 (`@org.junit.Test`). | Corrigir sprint-plan STORY-001 para "JUnit 4 + MockK" para alinhar com o código existente e PROMPT.md. |

---

## 2. Matriz de Cobertura de Entidades (Pass B)

| Entidade | Arquivo no Código | Status | Campos Faltantes | Campos Extras |
|----------|-------------------|--------|------------------|---------------|
| UserProfile | `data/model/UserProfile.kt` | COMPLETE | — | — |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | COMPLETE | — | — |
| TryOnSession | — | MISSING (esperado) | Entidade efêmera, implementar em Phase 3 (Story 2) | — |
| GarmentInfo | `data/model/GarmentInfo.kt` | COMPLETE | — | — |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | COMPLETE | — | — |
| SubscriptionState | — | MISSING | Entidade local (Room), implementar em Phase 5 (Story 4) | — |
| ModelScore | — | MISSING (esperado) | Entidade de backend, não faz parte do app Android | — |
| PlanType (enum) | `data/model/UserProfile.kt` | COMPLETE | — | — |
| GarmentCategory (enum) | `data/model/GarmentInfo.kt` | COMPLETE | — | — |

**Resumo**: 4/7 entidades implementadas. 3 MISSING são esperadas para o estágio atual (TryOnSession será in-memory, SubscriptionState em Phase 5, ModelScore é backend).

---

## 3. Cobertura de Contratos de API (Pass C)

| Endpoint | Arquivo no Código | Status | Notas |
|----------|-------------------|--------|-------|
| FASHN.ai POST /v1/run | — | MISSING | Phase 3 (STORY-011) |
| Vertex AI POST predict | — | MISSING | Phase 3 (STORY-011) |
| Vision LLM POST /v1/chat/completions | — | MISSING | Phase 3 (STORY-009) |
| ML Kit Object Detection (on-device) | — | MISSING | Phase 3 (STORY-009) |
| Backend POST /v1/feedback | — | MISSING | Phase 4 (STORY-016) |
| Backend GET /v1/models/route | — | MISSING | Phase 4 (STORY-022) |
| Backend GET /v1/config | — | MISSING | Phase 4 (STORY-016) |
| Backend POST /v1/dataset | — | MISSING | Phase 4 (STORY-021) |

**Resumo**: 0/8 endpoints implementados. Diretório `data/remote/` contém apenas `.gitkeep`. Esperado — APIs são Phase 3-4.

---

## 4. Rastreabilidade de FRs (Pass D)

| FR | Descrição | No Código? | Em Testes? | Status |
|----|-----------|-----------|------------|--------|
| FR-001 | Cadastro de fotos com criptografia | ✅ PhotoStorage, ReferencePhoto, ReferencePhotoDao | ✅ PhotoStorageTest | PARTIAL |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | ✅ PermissionScreen.kt | ❌ | PARTIAL |
| FR-003 | Consentimento LGPD explícito | ✅ UserProfile.lgpdConsentGranted/Timestamp | ❌ | PARTIAL |
| FR-004 | Overlay flutuante | ❌ | ❌ | UNTRACED |
| FR-005 | Captura de tela ao toque | ❌ | ❌ | UNTRACED |
| FR-006 | Overlay reposicionável | ✅ UserProfile.overlayPosition, UserProfileDao.updateOverlayPosition | ❌ | PARTIAL |
| FR-007 | Detecção de roupa via IA | ❌ | ❌ | UNTRACED |
| FR-008 | Aviso "Nenhuma roupa detectada" | ❌ | ❌ | UNTRACED |
| FR-009 | Geração de imagem try-on | ❌ | ❌ | UNTRACED |
| FR-010 | Opção "Tentar novamente" | ❌ | ❌ | UNTRACED |
| FR-011 | Opção "Trocar foto" | ❌ | ❌ | UNTRACED |
| FR-012 | Fallback entre modelos de IA | ❌ | ❌ | UNTRACED |
| FR-013 | Compartilhar com branding | ❌ | ❌ | UNTRACED |
| FR-014 | Feedback thumbs up/down | ❌ | ❌ | UNTRACED |
| FR-015 | Limite 3 tentativas/dia | ✅ UserProfile.dailyTriesUsed/ResetDate, UserProfileDao.updateDailyTries | ❌ | PARTIAL |
| FR-016 | Exibição de anúncios | ❌ | ❌ | UNTRACED |
| FR-017 | Pacotes pagos / assinatura | ❌ | ❌ | UNTRACED |
| FR-018 | Google Play Billing | ❌ | ❌ | UNTRACED |
| FR-019 | Coleta de dados para dataset | ❌ | ❌ | UNTRACED |
| FR-020 | Roteamento inteligente | ❌ | ❌ | UNTRACED |
| FR-021 | Pipeline de dados fine-tuning | ❌ | ❌ | UNTRACED |

**Resumo**: 5/21 FRs com implementação parcial no código (campos/DAOs de suporte). 0 FRs com tags `FR-XXX` nos comentários do código. Nenhum FR com cobertura completa (código + testes).

---

## 5. Cobertura de Cenários de Aceitação (Pass E)

### Story 1 — Setup Inicial e Onboarding (6 cenários)

| Cenário | Asserção Chave | Teste? | Arquivo de Teste |
|---------|----------------|--------|------------------|
| US1-1: Tela de boas-vindas | WelcomeScreen exibe conceito do provador | ❌ | — |
| US1-2: Permissão overlay explicada | PermissionScreen explica overlay em linguagem simples | ❌ | — |
| US1-3: Detecção automática de permissão | PermissionScreen auto-avança ao retornar | ❌ | — |
| US1-4: Checkbox LGPD não pré-marcado | LgpdConsentScreen checkbox desmarcado | ❌ | Tela não implementada |
| US1-5: Seleção de fotos | PhotoSelectScreen com picker | ❌ | Tela não implementada |
| US1-6: Validação corpo inteiro | Validação de foto de corpo inteiro | ❌ | — |

### Story 2 — Try-On Virtual (6 cenários) → UNCOVERED (Phase 3)
### Story 3 — Interação com Resultado (5 cenários) → UNCOVERED (Phase 4)
### Story 4 — Monetização e Limites (6 cenários) → UNCOVERED (Phase 5)
### Story 5 — Overlay Flutuante (5 cenários) → UNCOVERED (Phase 2)

**Resumo**: 0/28 cenários de aceitação com testes dedicados. PhotoStorageTest cobre criptografia (infraestrutura do FR-001) mas não é um teste de cenário de aceitação completo.

---

## 6. Qualidade de Código (Pass F)

| Arquivo | Categoria | Severity | Finding | Recomendação |
|---------|----------|----------|---------|--------------|
| `MainActivity.kt:17,20` | Code Smell | LOW | 2 TODOs sem referência a issue/story: "Replace with NavHost" e "Navigate to PermissionScreen" | Adicionar referência (ex: `// TODO(STORY-001): Wire NavHost`) |
| Projeto inteiro | Test Coverage | HIGH | Apenas 1 arquivo de teste (PhotoStorageTest.kt) para 14 arquivos de código-fonte. Cobertura estimada < 10%. Definition of Done exige ≥80% em domain/data. | Priorizar testes para UserProfileDao e telas de onboarding (Compose UI Test). |

### Qualidade Positiva Identificada

- ✅ **Segurança**: PhotoStorage usa Tink AES-256-GCM + Android Keystore. Padrão exemplar de criptografia.
- ✅ **Testabilidade**: PhotoStorage expõe `internal constructor` para injeção de AEAD em testes.
- ✅ **Separação de camadas**: FeedbackRecord e GarmentInfo são POJOs sem imports de framework.
- ✅ **Strings externalizadas**: WelcomeScreen e PermissionScreen usam `R.string.*` (NFR-012 compliance PT-BR).
- ✅ **Sem segredos hardcoded**: Nenhuma API key, senha ou token no código-fonte.
- ✅ **Código conciso**: Todos os arquivos ≤ 200 linhas, responsabilidade única.
- ✅ **Room bem configurado**: FK com CASCADE delete, índices em `userId`, TypeConverters para enums.
- ✅ **Permissões Android**: PermissionScreen trata corretamente API 33+ (`READ_MEDIA_IMAGES`) vs API < 33 (`READ_EXTERNAL_STORAGE`).

---

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severity | Doc BMAD | Doc Spec | Finding | Recomendação |
|----|----------|----------|----------|---------|--------------|
| G-01 | MEDIUM | sprint-plan STORY-001 | plan.md | sprint-plan diz "JUnit 5 + MockK" nos AC de STORY-001, mas plan.md, PROMPT.md e código real usam JUnit 4 + Robolectric. | Atualizar sprint-plan STORY-001 AC para "JUnit 4 + MockK + Robolectric". |
| G-02 | LOW | product-brief | plan.md / research.md | Product brief ainda menciona "Google Fotos API" como solução, mas research.md e plan.md decidiram usar "Android Photo Picker nativo (sem OAuth)". | Atualizar product-brief para refletir decisão do Photo Picker nativo. |
| G-03 | MEDIUM | plan.md | ux-design | plan.md diz "5 telas (onboarding 4 + resultado 1)" na seção Scale/Scope, mas UX design documenta 16+ telas incluindo splash, loadings, erros, limite, planos, home e configurações. | Atualizar plan.md Scale/Scope para refletir contagem real de telas. |

---

## 8. Ralph Config (Pass H)

| ID | Severity | Arquivo | Finding | Recomendação |
|----|----------|---------|---------|--------------|
| — | — | — | Nenhum issue encontrado. | — |

### Detalhes da Validação Ralph

- ✅ `.ralphrc`: PROJECT_NAME, PROJECT_TYPE, PROJECT_ROOT corretos. ALLOWED_TOOLS adequadas (gradlew, git safe). Sem comandos perigosos.
- ✅ `PROMPT.md`: Tech stack, branch (`001-ghostfit-mvp`), paths de specs/docs corretos. Testing: JUnit 4 (alinhado com código).
- ✅ `AGENT.md`: Build/test commands (gradlew), package (app.ghostfit), SDK versions (26/35) corretos.
- ✅ `fix_plan.md`: 6 fases (0-5) mapeando scaffolding + 5 User Stories. Tasks `[x]` referem código existente. Tasks `[ ]` referem entidades/telas presentes na spec. Ordem de prioridade (P1→P2) coerente com PROMPT.md.

---

## Próximas Ações

### Critical (corrigir antes de continuar)
- Nenhuma issue crítica. Projeto alinhado para estágio atual de implementação.

### HIGH (corrigir em breve)
- Aumentar cobertura de testes: criar testes para UserProfileDao e telas de onboarding. Target ≥80% em domain/data conforme Definition of Done.

### MEDIUM (abordar durante implementação)
- Corrigir "JUnit 5" → "JUnit 4" no sprint-plan STORY-001 (A-01/G-01).
- Atualizar plan.md para refletir 16+ telas do UX design (G-03).
- Adicionar tags `FR-XXX` nos comentários do código para rastreabilidade.
- Implementar `LgpdConsentScreen.kt` e `PhotoSelectScreen.kt` (próximos no fix_plan).
- Conectar navegação (substituir TODOs no MainActivity.kt por NavHost).
- Criar testes de UI (Compose UI Test) para WelcomeScreen e PermissionScreen.

### LOW (melhorias incrementais)
- Atualizar product-brief para refletir decisão do Photo Picker nativo (G-02).
- Adicionar referências a STORY/issue nos TODOs do código.
- Considerar adicionar `SubscriptionState` entity ao Room quando iniciar Phase 5.

---

*Relatório gerado por `/validate ghostfit` em 2026-03-04.*
*Execute `/validate ghostfit` novamente após corrigir os issues para verificar progresso.*
