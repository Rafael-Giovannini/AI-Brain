# Validation Report: ghostfit / 001-ghostfit-mvp

**Generated**: 2026-03-04 — Validacao #3 (pos-correcoes)
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp (Provador Virtual com Overlay)
**Status do Codigo**: Phase 1 completa (Onboarding). Phases 2-5 pendentes.

---

## Executive Summary

| Metric | Value |
|--------|-------|
| Spec Doc Consistency Issues | 2 (de 4 — 2 corrigidos) |
| BMAD Docs vs Spec Issues | 1 (de 4 — 3 corrigidos) |
| Ralph Config Issues | 1 (de 2 — 1 corrigido) |
| Entities: Definidas / Implementadas | 7 / 4 |
| API Endpoints: Definidos / Implementados | 9 / 0 |
| FRs Traced to Code | 3 / 21 (14%) |
| Acceptance Scenarios with Tests | 0 / 28 (0%) |
| Code Quality Issues | 3 (de 7 — 4 corrigidos) |
| **Critical Issues** | **0** |
| **High Issues** | **0** (2 corrigidos) |
| **Medium Issues** | **4** (de 10 — 6 corrigidos) |
| **Low Issues** | **3** (de 4 — 1 corrigido) |

**Nota**: O projeto esta em estagio inicial (Phase 1 de 5 concluida). A maioria dos endpoints e FRs MISSING sao esperados neste momento.

**Correcoes aplicadas nesta sessao**:
- H-1/H-2: Business logic extraida para `OnboardingViewModel.kt`, eliminando `CoroutineScope` nao-gerenciado
- M-1: Quickstart atualizado — Hilt comentado com explicacao de singletons no MVP
- M-3: PRD FR-001 corrigido: "Google Photos API" → "Photo Picker Nativo"
- M-4: PROMPT.md corrigido: "ComposeView (overlay)" → "View + WindowManager (overlay)"
- M-5: Sprint-plan STORY-006 corrigido: "SharedPreferences" → "Room (UserProfile)"
- M-6: AndroidManifest AdMob ID substituido por test ID oficial com TODO para BuildConfig
- M-7: PhotoSelectScreen contentDescription adicionado para acessibilidade WCAG

---

## 1. Spec Doc Consistency (Pass A)

| ID | Severity | Location | Finding | Recommendation |
|----|----------|----------|---------|----------------|
| A-01 | MEDIUM | plan.md vs codigo | Plan e architecture especificam **Hilt** para DI, mas o codigo usa singletons manuais (`AppDatabase.getInstance()`, `PhotoStorage.getInstance()`) | Implementar Hilt conforme definido na arquitetura, ou atualizar docs para refletir a decisao de nao usar Hilt no MVP |
| ~~A-02~~ | ~~MEDIUM~~ | ~~sprint-plan vs data-model~~ | ~~SharedPreferences vs Room~~ | **CORRIGIDO** — sprint-plan atualizado para Room |
| ~~A-03~~ | ~~LOW~~ | ~~quickstart.md vs plan.md~~ | ~~Hilt dependency sem uso~~ | **CORRIGIDO** — Hilt comentado no quickstart com explicacao |
| A-04 | LOW | api-contracts.md vs plan.md | Contratos especificam `genTimeout: 20000` no endpoint `/v1/config`, mas spec e plan definem timeout de geracao como 15s (FR-009) | Alinhar — se 20s e o timeout do config e 15s e o SLA, documentar a diferenca |

---

## 2. Entity Coverage Matrix (Pass B)

| Entity | Code File | Status | Missing Fields | Extra Fields |
|--------|-----------|--------|----------------|--------------|
| UserProfile | `data/model/UserProfile.kt` | **COMPLETE** | — | — |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | **COMPLETE** | — | — |
| TryOnSession | — | **MISSING** | Todos (9 campos) | — |
| GarmentInfo | `data/model/GarmentInfo.kt` | **COMPLETE** | — | — |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | **COMPLETE** | — | — |
| SubscriptionState | — | **MISSING** | Todos (7 campos) | — |
| ModelScore | — | **MISSING** | Todos (6 campos) | — |

**Notas**:
- UserProfile: 10/10 campos, PlanType enum com FREE/PREMIUM conforme spec
- ReferencePhoto: 6/6 campos, FK para UserProfile com CASCADE
- GarmentInfo: 5/5 campos, GarmentCategory enum (TOP/BOTTOM/DRESS/OUTERWEAR)
- FeedbackRecord: 5/5 campos
- TryOnSession: Esperado MISSING — entidade efemera, Phase 3
- SubscriptionState: Esperado MISSING — Phase 5
- ModelScore: Backend-only, nao precisa existir no app

---

## 3. API Contract Coverage (Pass C)

| Endpoint | Code File | Status | Notes |
|----------|-----------|--------|-------|
| POST /v1/feedback | — | **MISSING** | Phase 4 — esperado |
| GET /v1/models/route | — | **MISSING** | Phase 5 — esperado |
| GET /v1/config | — | **MISSING** | Phase 4 — esperado |
| GET /v1/health | — | **MISSING** | Phase 4 — esperado |
| POST /v1/dataset | — | **MISSING** | Phase 4 — esperado |
| POST fashn.ai/v1/run | — | **MISSING** | Phase 3 — esperado |
| POST vertex-ai predict | — | **MISSING** | Phase 3 — esperado |
| POST openai/v1/chat/completions | — | **MISSING** | Phase 3 — esperado |
| ML Kit Object Detection | — | **MISSING** | Phase 3 — esperado |

**Nota**: Todos os endpoints sao Phase 3+. Nenhuma implementacao de API remota e esperada neste estagio.

---

## 4. FR Traceability (Pass D)

| FR | Description | In Code? | In Tests? | Status |
|----|-------------|----------|-----------|--------|
| FR-001 | Cadastro fotos corpo inteiro (ate 3, criptografia) | PhotoSelectScreen.kt, PhotoStorage.kt | PhotoStorageTest.kt | **PARTIAL** |
| FR-002 | Permissao SYSTEM_ALERT_WINDOW com onboarding | PermissionScreen.kt, AndroidManifest.xml | — | **PARTIAL** |
| FR-003 | Consentimento LGPD (checkbox nao pre-marcado, timestamp) | LgpdConsentScreen.kt, MainActivity.kt | UserProfileDaoTest.kt (lgpd consent flow) | **TRACED** |
| FR-004 | Overlay flutuante sobre qualquer app | — | — | **UNTRACED** |
| FR-005 | Captura de tela ao toque (< 1s) | — | — | **UNTRACED** |
| FR-006 | Reposicionamento overlay (drag, posicao salva) | DAO pronto (updateOverlayPosition) | UserProfileDaoTest (overlay position) | **PARTIAL** |
| FR-007 | Deteccao roupa via IA (< 3s) | — | — | **UNTRACED** |
| FR-008 | Mensagem "Nenhuma roupa detectada" | — | — | **UNTRACED** |
| FR-009 | Geracao imagem try-on (< 15s) | — | — | **UNTRACED** |
| FR-010 | Tentar novamente (variacao diferente) | — | — | **UNTRACED** |
| FR-011 | Trocar foto de referencia | — | — | **UNTRACED** |
| FR-012 | Fallback FASHN.ai → Vertex AI | — | — | **UNTRACED** |
| FR-013 | Compartilhar com branding GhostFit | — | — | **UNTRACED** |
| FR-014 | Feedback thumbs up/down | — | — | **UNTRACED** |
| FR-015 | Limite 3 tentativas/dia | DAO pronto (updateDailyTries) | UserProfileDaoTest (daily tries) | **PARTIAL** |
| FR-016 | Exibicao de anuncios | — | — | **UNTRACED** |
| FR-017 | Pacotes pagos / assinatura mensal | — | — | **UNTRACED** |
| FR-018 | Google Play Billing | — | — | **UNTRACED** |
| FR-019 | Coleta dados dataset anonimizado | — | — | **UNTRACED** |
| FR-020 | Roteamento inteligente modelos | — | — | **UNTRACED** |
| FR-021 | Pipeline fine-tuning | — | — | **UNTRACED** |

**Resumo**: 1 TRACED, 4 PARTIAL, 16 UNTRACED — condizente com Phase 1 completa (FR-001, FR-002, FR-003).

---

## 5. Acceptance Scenario Coverage (Pass E)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| US1 | S1: Tela boas-vindas ao abrir app | WelcomeScreen renderiza | No | — |
| US1 | S2: Explicacao permissao overlay | PermissionScreen com texto explicativo | No | — |
| US1 | S3: Auto-deteccao permissao concedida | lifecycle observer em MainActivity | No | — |
| US1 | S4: Checkbox LGPD nao pre-marcado | `mutableStateOf(false)` em LgpdConsentScreen | No | — |
| US1 | S5: Seletor fotos corpo inteiro | PhotoSelectScreen com picker | No | — |
| US1 | S6: Validacao corpo inteiro | `isBodyFullVisible` hardcoded `true` | No | — |
| US2 | S1-S6 | Captura + deteccao + geracao | No | — (Phase 3) |
| US3 | S1-S5 | Retry, trocar foto, feedback, share | No | — (Phase 4) |
| US4 | S1-S6 | Limite, ads, billing | No | — (Phase 5) |
| US5 | S1-S5 | Overlay, drag, persist position | No | — (Phase 2) |

**Resumo**: 0/28 cenarios com testes de UI. Ha testes unitarios para DAOs mas nenhum teste Compose UI.

---

## 6. Code Quality (Pass F)

| File | Category | Severity | Finding | Recommendation |
|------|----------|----------|---------|----------------|
| ~~MainActivity.kt~~ | ~~Architecture~~ | ~~HIGH~~ | ~~Business logic na Activity com CoroutineScope nao-gerenciado~~ | **CORRIGIDO** — Extraido para `OnboardingViewModel.kt` com `viewModelScope` |
| ~~MainActivity.kt~~ | ~~Code Smell~~ | ~~HIGH~~ | ~~CoroutineScope(Dispatchers.IO).launch nao-gerenciado~~ | **CORRIGIDO** — ViewModel com `viewModelScope.launch(Dispatchers.IO)` |
| MainActivity.kt | Code Smell | **MEDIUM** | `isBodyFullVisible = true` hardcoded — sem validacao real de corpo inteiro (FR-001 acceptance criteria incompleto) | Implementar validacao via ML Kit Pose Detection conforme sprint-plan STORY-004 notas tecnicas |
| ~~AndroidManifest.xml~~ | ~~Security~~ | ~~MEDIUM~~ | ~~AdMob App ID placeholder hardcoded~~ | **CORRIGIDO** — Substituido por test ID oficial do AdMob com TODO para BuildConfig |
| ~~PhotoSelectScreen.kt~~ | ~~Accessibility~~ | ~~MEDIUM~~ | ~~Image thumbnail sem contentDescription~~ | **CORRIGIDO** — `contentDescription` adicionado (cd_photo_reference, cd_remove_photo) |
| WelcomeScreen.kt:40 | Code Smell | **LOW** | Emoji do fantasminha como placeholder (unicode) ao inves de asset vetorial | Substituir por icone vetorial `ic_ghost.xml` quando disponivel |
| AppDatabase.kt | Architecture | **MEDIUM** | Singleton manual com `@Volatile` + `synchronized` — correto mas Hilt com `@Singleton` seria mais limpo conforme arquitetura | Considerar migrar para Hilt quando implementar Phase 2+ |

---

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severity | Docs File | Spec File | Finding | Recommendation |
|----|----------|-----------|-----------|---------|----------------|
| ~~G-01~~ | ~~MEDIUM~~ | ~~prd (FR-001)~~ | ~~spec.md / research.md~~ | ~~PRD diz "Google Photos API"~~ | **CORRIGIDO** — PRD atualizado para "Photo Picker Nativo" |
| G-02 | MEDIUM | architecture.md | codigo-fonte | Arquitetura define Hilt para injecao de dependencia (secoes 3, 4), mas codigo implementa singletons manuais | Implementar Hilt ou atualizar architecture.md para refletir decisao de nao usar DI no MVP |
| G-03 | LOW | prd (FR-018) | quickstart.md | PRD especifica "Google Play Billing Library v6+" mas quickstart/architecture usam v8.3.0 | Atualizar PRD para "v8.3.0" para consistencia |
| ~~G-04~~ | ~~MEDIUM~~ | ~~sprint-plan (STORY-005)~~ | ~~PROMPT.md~~ | ~~ComposeView vs View-based overlay~~ | **CORRIGIDO** — PROMPT.md atualizado para "View + WindowManager (overlay)" |

---

## 8. Ralph Config (Pass H)

| ID | Severity | File | Finding | Recommendation |
|----|----------|------|---------|----------------|
| ~~H-01~~ | ~~MEDIUM~~ | ~~.ralph/PROMPT.md~~ | ~~ComposeView vs View + WindowManager~~ | **CORRIGIDO** — PROMPT.md atualizado |
| H-02 | LOW | .ralph/PROMPT.md | Lista "Moshi 1.15" na tech stack mas nenhum adapter Moshi configurado no codigo | Aceitavel — sera implementado em Phase 3 (APIs remotas). Sem acao necessaria agora |

**Config geral**:
- `.ralphrc`: PROJECT_NAME, PROJECT_TYPE, PROJECT_ROOT — todos corretos
- `AGENT.md`: Build/test commands corretos, prerequisites corretos
- `fix_plan.md`: Phases alinhadas com spec stories, tasks completadas correspondem a codigo existente

---

## Next Actions

### Critical (must fix before continuing)
- (nenhum)

### High (fix soon)
- ~~**[H-1]** Extrair business logic para ViewModel~~ — **CORRIGIDO**
- ~~**[H-2]** Migrar para viewModelScope~~ — **CORRIGIDO**

### Medium (address during implementation)
- **[M-1]** ~~Atualizar docs sobre Hilt~~ — **CORRIGIDO** (quickstart). Pendente: atualizar `architecture.md` se decidir nao usar Hilt
- **[M-2]** Implementar validacao real de corpo inteiro na foto (FR-001 S6) — atualmente hardcoded `true`. Requer ML Kit Pose Detection (Phase 3+)
- ~~**[M-3]** Atualizar PRD FR-001~~ — **CORRIGIDO**
- ~~**[M-4]** Corrigir PROMPT.md~~ — **CORRIGIDO**
- ~~**[M-5]** Corrigir sprint-plan STORY-006~~ — **CORRIGIDO**
- ~~**[M-6]** AdMob App ID~~ — **CORRIGIDO** (test ID + TODO para BuildConfig)
- ~~**[M-7]** contentDescription WCAG~~ — **CORRIGIDO**
- **[M-8]** Adicionar testes Compose UI para telas de onboarding (US1 S1-S6)

### Low (address when convenient)
- Alinhar versao do Play Billing entre PRD (v6+) e quickstart (v8.3.0)
- ~~Remover dependency Hilt do quickstart~~ — **CORRIGIDO** (comentado com explicacao)
- Substituir emoji placeholder por icone vetorial
- Alinhar timeout genTimeout (20s) no config endpoint vs SLA (15s)
