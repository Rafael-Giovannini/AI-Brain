# Validation Report: ghostfit / 001-ghostfit-mvp

**Generated**: 2026-03-05 14:45
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp

---

## Executive Summary

| Metric | Value |
|--------|-------|
| Spec Doc Consistency Issues | 8 |
| BMAD Docs vs Spec Issues | 8 |
| Ralph Config Issues | 10 |
| Entities: Defined / Implemented | 7 / 6 (86%) |
| API Endpoints: Defined / Implemented | 8 / 8 (100%) |
| FRs Traced to Code | 12 / 21 (57%) |
| Acceptance Scenarios with Tests | 12 / 28 (43%) |
| Code Quality Issues | 22 |
| Critical Issues | 0 |
| High Issues | 7 |
| Medium Issues | 12 |
| Low Issues | 29 |

---

## 1. Spec Doc Consistency (Pass A)

| ID | Severity | Location | Finding | Recommendation |
|----|----------|----------|---------|----------------|
| A-01 | LOW | `data-model.md` vs `spec.md` | 7 entidades no data-model mapeiam corretamente para as 7 entidades-chave da spec (nomes diferentes mas semanticamente iguais). | Nenhuma acao necessaria. |
| A-02 | LOW | `api-contracts.md` vs `plan.md` | 5 interfaces de API (FASHN, Vertex, VisionLLM, ML Kit, Backend) consistentes entre contracts e plan. | Nenhuma acao necessaria. |
| A-03 | LOW | Todos os artefatos | FR-001 a FR-021 referenciados consistentemente em spec.md, plan.md e api-contracts.md. | Nenhuma acao necessaria. |
| A-04 | LOW | `data-model.md` vs `api-contracts.md` | Enums consistentes: GarmentInfo.category (4 valores), SubscriptionState.purchaseType (3 valores), UserProfile.planType (2 valores). | Nenhuma acao necessaria. |
| A-05 | LOW | `api-contracts.md` vs `spec.md` | 7 mensagens de erro nos contracts mapeiam para os edge cases da spec. | Nenhuma acao necessaria. |
| A-06 | MEDIUM | `plan.md` vs `AGENT.md` | Plan menciona "JUnit 4, Compose UI Test, Coroutines Test". AGENT.md lista "JUnit 4, MockK, Robolectric, kotlinx-coroutines-test, mockito-kotlin". Plan nao menciona MockK/Robolectric. | Adicionar Robolectric e MockK ao plan.md para completude. |
| A-07 | HIGH | `architecture-ghostfit-2026-03-04.md` secao 11.3 vs `plan.md` | Architecture doc diz **"JUnit 5 + MockK"** na secao 11.3 e CI pipeline diz **"Unit Tests (JUnit 5)"**. Porem plan.md, sprint-plan e AGENT.md dizem **JUnit 4**. Inconsistencia interna no architecture doc (secao 3.5 diz JUnit 4, secao 11.3 diz JUnit 5). | Corrigir architecture doc secoes 11.3 e 11.4: trocar "JUnit 5" para "JUnit 4". |
| A-08 | MEDIUM | `architecture-ghostfit-2026-03-04.md` secoes 3.5 vs 11.3 | Secao 3.5 diz "Mockito", secao 11.3 diz "MockK". AGENT.md lista ambos. Framework de mocking inconsistente entre secoes. | Decidir framework de mocking e atualizar todos os docs. |

## 2. Entity Coverage Matrix (Pass B)

| Entity | Code File | Status | Missing Fields | Extra Fields |
|--------|-----------|--------|----------------|--------------|
| UserProfile | `data/model/UserProfile.kt` | COMPLETE | Nenhum | Nenhum |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | COMPLETE | Nenhum | Nenhum |
| GarmentInfo | `data/model/GarmentInfo.kt` | COMPLETE | Nenhum | Nenhum |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | COMPLETE | Nenhum | Nenhum |
| SubscriptionState | `data/model/SubscriptionState.kt` | COMPLETE | Nenhum | Nenhum |
| TryOnSession | Em memoria (TryOnUseCase) | COMPLETE | Nenhum (efemera conforme spec) | Nenhum |
| ModelScore | N/A | **MISSING** | Todos (modelId, garmentCategory, thumbsUpCount, thumbsDownCount, approvalRate, totalFeedbacks) | N/A |

## 3. API Contract Coverage (Pass C)

| Endpoint | Code File | Status | Notes |
|----------|-----------|--------|-------|
| FASHN.ai POST /v1/run | `data/remote/FashnApi.kt` | IMPLEMENTED | Request/response DTOs completos, base URL correta |
| Vertex AI POST .../predict | `data/remote/VertexAiApi.kt` | IMPLEMENTED | Path com project/region params, modelo virtual-try-on-001 |
| Vision LLM POST /v1/chat/completions | `data/remote/VisionLlmApi.kt` | IMPLEMENTED | DTOs completos, base URL OpenAI |
| POST /v1/feedback | `data/remote/GhostFitApi.kt` | IMPLEMENTED | FeedbackRecord body + X-Device-Id header |
| GET /v1/models/route | `data/remote/GhostFitApi.kt` | IMPLEMENTED | Query param garmentCategory, ModelRouteResponse |
| GET /v1/config | `data/remote/GhostFitApi.kt` | IMPLEMENTED | RemoteConfig com todos os campos esperados |
| GET /v1/health | `data/remote/GhostFitApi.kt` | IMPLEMENTED | HealthResponse com status e timestamp |
| POST /v1/dataset | `data/remote/GhostFitApi.kt` | IMPLEMENTED | DatasetEntry body |

**Category Mapping**: Todos os 4 mapeamentos (TOP, BOTTOM, DRESS, OUTERWEAR) consistentes entre FASHN, Vertex AI e Vision LLM.

## 4. FR Traceability (Pass D)

| FR | Description | In Code? | In Tests? | Status |
|----|-------------|----------|-----------|--------|
| FR-001 | Cadastro de ate 3 fotos via Photo Picker | SIM | NAO | PARTIAL |
| FR-002 | Permissao SYSTEM_ALERT_WINDOW com onboarding | SIM | NAO | PARTIAL |
| FR-003 | Consentimento LGPD explicito | SIM | SIM | TRACED |
| FR-004 | Overlay flutuante persistente | SIM | SIM | TRACED |
| FR-005 | Captura de tela automatica ao toque | SIM | PARCIAL | PARTIAL |
| FR-006 | Reposicionamento do overlay via drag | SIM | SIM | TRACED |
| FR-007 | Deteccao automatica de roupa via IA | SIM | SIM | TRACED |
| FR-008 | Mensagem "Nenhuma roupa detectada" | SIM | SIM | TRACED |
| FR-009 | Geracao de imagem realista | SIM | SIM | TRACED |
| FR-010 | Regeneracao do try-on (variacao diferente) | SIM | NAO | PARTIAL |
| FR-011 | Troca da foto de referencia | SIM | NAO | PARTIAL |
| FR-012 | Fallback automatico FASHN -> Vertex AI | SIM | SIM | TRACED |
| FR-013 | Compartilhamento via share sheet | SIM | NAO | PARTIAL |
| FR-014 | Feedback thumbs up/down | SIM | SIM | TRACED |
| FR-015 | Limite de 3 geracoes/dia | SIM | SIM | TRACED |
| FR-016 | Anuncios AdMob entre geracoes | **NAO** | NAO | **UNTRACED** |
| FR-017 | Pacotes pagos e assinatura mensal | SIM | NAO | PARTIAL |
| FR-018 | Google Play Billing integration | SIM | SIM | TRACED |
| FR-019 | Coleta de pares de treinamento | SIM | SIM | TRACED |
| FR-020 | Roteamento inteligente entre modelos | PARCIAL | NAO | PARTIAL |
| FR-021 | Pipeline de dados por categoria | SIM | SIM | TRACED |

**Resumo**: 12 TRACED (57%), 8 PARTIAL (38%), 1 UNTRACED (5%)

## 5. Acceptance Scenario Coverage (Pass E)

### Story 1: Onboarding (6 cenarios)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| S1-1 | Welcome screen no primeiro uso | NavHost startDestination="welcome" | UNCOVERED | — |
| S1-2 | Explicacao da permissao de overlay | PermissionScreen com texto explicativo | UNCOVERED | — |
| S1-3 | Auto-detecta permissao concedida | `canDrawOverlays()` re-checked ON_RESUME | UNCOVERED | — |
| S1-4 | LGPD checkbox nao pre-marcado | `mutableStateOf(false)` | UNCOVERED | — |
| S1-5 | Photo picker da galeria | PickVisualMedia launcher | UNCOVERED | — |
| S1-6 | Validacao de corpo inteiro | TODO placeholder — `isBodyFullVisible = true` | UNCOVERED | — |

### Story 2: Try-On Virtual Core (6 cenarios)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| S2-1 | Captura < 1s ao tocar overlay | ScreenCapture.capture() | UNCOVERED | — |
| S2-2 | Deteccao + identificacao < 3s | GarmentDetector.detect() | COVERED | GarmentDetectorTest.kt |
| S2-3 | Geracao de imagem < 15s | ModelRouter.generate() | COVERED | ModelRouterTest.kt |
| S2-4 | Fisionomia preservada | Depende da API externa | UNCOVERED | — |
| S2-5 | "Nenhuma roupa detectada", sem consumir tentativa | NO_GARMENT antes de incrementDailyTries | COVERED | TryOnUseCaseTest.kt |
| S2-6 | Fallback automatico entre modelos | tryFashn -> tryVertex chain | COVERED | ModelRouterTest.kt |

### Story 3: Interacao com Resultado (5 cenarios)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| S3-1 | "Tentar novamente" com seed diferente | TryOnUseCase.regenerate() | UNCOVERED | — |
| S3-2 | "Trocar foto" com picker | onChangePhoto callback | UNCOVERED | — |
| S3-3 | Feedback thumbs async | submitFeedback() fire-and-forget | COVERED | FeedbackSubmissionTest.kt |
| S3-4 | Compartilhar com branding | ShareUtils.shareTryOnImage() | UNCOVERED | — |
| S3-5 | Coleta anonimizada no thumbs up | uploadDataset() apenas se thumbsUp | COVERED | FeedbackSubmissionTest.kt |

### Story 4: Monetizacao (6 cenarios)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| S4-1 | Contador mostra "3 tentativas restantes" | MAX_FREE_DAILY_TRIES=3 | UNCOVERED | — |
| S4-2 | Geracao bloqueada apos 3 + tela upgrade | DailyLimitExceededException | COVERED | TryOnUseCaseTest.kt |
| S4-3 | Reset a meia-noite | checkDailyLimit() compara LocalDate | UNCOVERED | — |
| S4-4 | Ads entre geracoes | NAO IMPLEMENTADO (AdMob ausente) | UNCOVERED | — |
| S4-5 | Assinatura desbloqueia limites | planType == PREMIUM | COVERED | BillingManagerTest.kt |
| S4-6 | Restaurar compras na reinstalacao | BillingManager.restorePurchases() | COVERED | BillingManagerTest.kt |

### Story 5: Overlay UX (5 cenarios)

| Story | Scenario | Key Assertion | Test Found? | Test File |
|-------|----------|---------------|-------------|-----------|
| S5-1 | Overlay visivel sobre qualquer app | TYPE_APPLICATION_OVERLAY | UNCOVERED | — |
| S5-2 | Overlay persiste entre apps | Foreground service | UNCOVERED | — |
| S5-3 | Drag suave | detectDragGestures | UNCOVERED | — |
| S5-4 | Overlay lembra posicao | saveOverlayPosition() | COVERED | OverlayServiceTest.kt |
| S5-5 | Sem lag no app hospedeiro | FLAG_NOT_FOCUSABLE | UNCOVERED | — |

**Resumo**: 12 COVERED (43%), 16 UNCOVERED (57%)

## 6. Code Quality (Pass F)

| File | Category | Severity | Finding | Recommendation |
|------|----------|----------|---------|----------------|
| `domain/ModelRouter.kt` | Architecture | HIGH | Classe domain importa tipos Retrofit (`FashnApi`, `VertexAiApi`) de `data.remote`. Viola clean architecture. | Criar interfaces `TryOnProvider` na camada domain. ModelRouter depende de abstracoes. |
| `domain/GarmentDetector.kt` | Architecture | HIGH | Classe domain importa `VisionLlmApi` de `data.remote` e `MlKitGarmentCropper` de `data.local`. | Extrair interface `GarmentClassifier` no domain. Mover orquestracao da API para data layer. |
| `domain/BillingManager.kt` | Architecture | HIGH | Classe domain depende diretamente de `BillingClient`, `Activity` do Android framework. | Mover para `data/billing/` ou criar `BillingRepository` interface no domain. |
| `domain/TryOnUseCase.kt` | Architecture | MEDIUM | Importa `DatasetEntry`, `GhostFitApi` de data.remote e DAOs de data.local diretamente. | Criar interfaces repository (UserRepository, FeedbackRepository) no domain. |
| `overlay/OverlayService.kt` | Architecture | MEDIUM | Service instancia diretamente `AppDatabase.getInstance(this)` para obter UserProfileDao. | Injetar dependencias via ServiceLocator ou DI (Hilt/Koin). |
| `data/remote/FashnApi.kt` | Security | MEDIUM | API key via `BuildConfig.FASHN_API_KEY` no parametro default da funcao. Key embutida na assinatura do metodo. | Mover injecao de API key para OkHttp interceptor. |
| `data/remote/VisionLlmApi.kt` | Security | MEDIUM | Mesmo padrao: `BuildConfig.OPENAI_API_KEY` no parametro default. | Mover para OkHttp interceptor. |
| `data/remote/VertexAiApi.kt` | Security | MEDIUM | `BuildConfig.GCP_PROJECT_ID` usado em path default. Menos sensivel que API keys. | Aceitavel para project ID. Token de auth passado como parametro (correto). |
| `ui/onboarding/OnboardingViewModel.kt` | CodeSmell | MEDIUM | TODO na linha 50: `"substituir por validacao real de corpo inteiro"`. `isBodyFullVisible` hardcoded como `true` — gap funcional. | Criar issue no tracker e implementar validacao real (FR-001 AC-3). |
| `ui/tryon/TryOnResultScreen.kt` | CodeSmell | LOW | 332 linhas, excede threshold de 300. 5 composables no mesmo arquivo. | Extrair `ErrorState` e `NoGarmentDetected` para arquivos separados. |
| `ui/subscription/UpgradeScreen.kt` | CodeSmell | LOW | 331 linhas. Contem screen + PlanCard + extension functions. | Extrair `PlanCard` para arquivo separado. |
| `domain/TryOnUseCase.kt` | CodeSmell | LOW | Magic number `3` para `MAX_FREE_DAILY_TRIES`. Constante local pode divergir do RemoteConfig do servidor. | Buscar limite de `RemoteConfig` em runtime via `GhostFitApi.getConfig()`. |
| `overlay/OverlayService.kt` | CodeSmell | LOW | Handler `onTap` mostra Toast "em breve!" placeholder. Integracao com pipeline incompleta. | Completar integracao com TryOnUseCase pipeline. |
| `ui/onboarding/OnboardingViewModel.kt` | TestQuality | HIGH | Nenhum arquivo de teste. Fluxo de consentimento LGPD e salvamento de fotos nao testados a nivel de unidade. | Criar `OnboardingViewModelTest.kt`. |
| `data/model/ModelScore` | TestQuality | HIGH | Entidade `ModelScore` do data-model completamente ausente do codebase. | Implementar `ModelScore` data class ou remover da spec. |
| `data/local/MlKitGarmentCropper.kt` | TestQuality | MEDIUM | Nenhum teste. Logica de ML Kit Object Detection nao testada (mock passthrough usado em GarmentDetectorTest). | Adicionar testes com Robolectric ou mock ML Kit. |
| `overlay/ScreenCapture.kt` | TestQuality | MEDIUM | Apenas 1 teste trivial (`isReady`). Metodo `capture()` com MediaProjection nao testado. | Adicionar testes para error cases (null projection, cancellation). |
| `data/local/ReferencePhotoDao` | TestQuality | MEDIUM | Nenhum teste dedicado. Exercitado indiretamente. | Criar `ReferencePhotoDaoTest.kt`. |
| `data/local/SubscriptionStateDao` | TestQuality | MEDIUM | Nenhum teste dedicado. Exercitado via BillingManagerTest. | Criar `SubscriptionStateDaoTest.kt`. |
| `data/remote/FashnApi.kt` | Performance | LOW | Timeout HTTP 10s read. Para geracao que pode demorar mais, timeout prematuro possivel. | Considerar aumentar readTimeout para 30s+ nas APIs de geracao. |
| `data/remote/VisionLlmApi.kt` | Performance | LOW | Timeout HTTP 5s read. Vision API pode demorar mais sob carga. | Considerar 10s read timeout ou usar RemoteConfig.visionTimeout. |
| `domain/TryOnUseCase.kt` | Performance | LOW | Uso correto de `withContext(Dispatchers.IO)`. Sem `runBlocking` no main thread. | Nenhum problema. Padrao adequado. |

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severity | Docs File | Spec File | Finding | Recommendation |
|----|----------|-----------|-----------|---------|----------------|
| G-01 | LOW | sprint-plan | spec.md | 5 User Stories decompostas em 22 stories via 5 epics. Rastreabilidade correta. | Nenhuma acao. |
| G-02 | LOW | architecture | plan.md | Tech stack consistente: Kotlin 2.2.0, Compose, Room, Retrofit. Arch omite versoes menores. | Nenhuma acao. |
| G-03 | HIGH | architecture secao 3.4 | sprint-plan STORY-019, PROMPT.md | Architecture diz **"Google Play Billing v6+"**. Sprint-plan e PROMPT.md dizem **v8.3.0**. | Atualizar architecture doc para "Google Play Billing Library 8.3.0". |
| G-04 | LOW | ux-design | plan.md | 17 telas no UX design mapeiam corretamente para a estrutura ui/ do plan. | Nenhuma acao. |
| G-05 | LOW | prd NFRs | spec.md | 15 NFRs do PRD refletidos nas constraints da spec. | Nenhuma acao. |
| G-06 | LOW | prd FRs | spec.md | 21/21 FRs consistentes entre PRD e spec. | Nenhuma acao. |
| G-07 | LOW | sprint-plan fases | spec.md priorities | P1 stories executam primeiro (Fases 1-2), P2 depois (Fases 3-4). Ordenacao consistente. | Nenhuma acao. |
| G-08 | LOW | architecture patterns | plan.md structure | Camadas data/domain/overlay/ui consistentes entre architecture doc e plan. | Nenhuma acao. |

## 8. Ralph Config (Pass H)

| ID | Severity | File | Finding | Recommendation |
|----|----------|------|---------|----------------|
| H-01 | LOW | `.ralphrc` | PROJECT_NAME, PROJECT_TYPE, PROJECT_ROOT todos consistentes com docs. | OK. |
| H-02 | LOW | `.ralphrc` | ALLOWED_TOOLS nao contem comandos perigosos (rm, git clean, git reset). Seguro. | OK. |
| H-03 | LOW | `PROMPT.md` | Nome do projeto, tech stack, branch, caminhos de spec/docs — todos verificados e existentes. | OK. |
| H-04 | LOW | `PROMPT.md` | Descricao da arquitetura e guidelines de teste consistentes com architecture doc e plan.md. | OK. |
| H-05 | MEDIUM | `AGENT.md` | Test dependencies listam "MockK + mockito-kotlin" mas architecture doc secao 3.5 diz "Mockito". Inconsistencia no framework de mocking. | Alinhar AGENT.md com build.gradle.kts real. |
| H-06 | LOW | `AGENT.md` | Build commands (gradlew), prerequisites (JDK 17), file paths — todos corretos. | OK. |
| H-07 | LOW | `fix_plan.md` | Fases 0-5 mapeiam para 5 stories + scaffolding. Todas as tasks `[x]` verificadas contra filesystem. | OK. |
| H-08 | MEDIUM | `fix_plan.md` | Ordenacao de fases difere do sprint-plan (fix_plan separa Story 5 em Phase 2 propria). Nao e blocker mas vale notar. | Aceitavel — fix_plan e a ordem de execucao do Ralph, mais granular que sprint-plan. |
| H-09 | LOW | `fix_plan.md` | Task duplicada: `GhostFitApi.kt` marcada `[x]` em Phase 3 e Phase 4. Artefato de documentacao. | Remover entrada duplicada da Phase 4 para clareza. |
| H-10 | LOW | `.ralphrc` | Circuit breaker thresholds (3 no-progress, 5 same-error) sao razoaveis para o projeto. | OK. |

---

## Next Actions

### Critical (corrigir antes de continuar)
- Nenhum issue CRITICAL encontrado.

### High (corrigir em breve)
- **A-07**: Corrigir architecture doc secoes 11.3/11.4 — trocar "JUnit 5" para "JUnit 4"
- **G-03**: Atualizar architecture doc secao 3.4 — "Google Play Billing v6+" para "v8.3.0"
- **F-Arch**: Domain layer (`ModelRouter`, `GarmentDetector`, `BillingManager`) viola clean architecture importando tipos de data/remote. Criar interfaces repository/provider no domain.
- **F-Test**: `OnboardingViewModel` sem testes unitarios (fluxo LGPD + fotos)
- **F-Test**: `ModelScore` entity do data-model completamente ausente do codebase
- **FR-016**: AdMob completamente nao implementado (unico FR UNTRACED)
- **FR-020**: Roteamento inteligente parcialmente implementado (endpoint existe mas ModelRouter nao usa scores)

### Medium (abordar durante implementacao)
- **A-06/A-08/H-05**: Harmonizar naming de frameworks de teste (MockK vs Mockito) em todos os docs
- **F-Sec**: API keys embutidas em parametros default de Retrofit — mover para OkHttp interceptors
- **F-Smell**: `isBodyFullVisible` hardcoded como `true` — implementar validacao real (FR-001)
- **F-Test**: Testes ausentes para: `MlKitGarmentCropper`, `ScreenCapture.capture()`, `ReferencePhotoDao`, `SubscriptionStateDao`
- **Story 1**: 0/6 cenarios de aceitacao cobertos por testes (onboarding)
- **Story 3 Phase 4**: 3 tasks restantes — "Tentar novamente", "Trocar foto", feedback submission
- **Story 4 Phase 5**: 4 tasks restantes — daily counter, AdMob, subscription handling, daily limit tests

---

*Gerado por /validate ghostfit — BMAD+SpecKit+Ralph Validation Engine*
*Para re-validar apos correcoes: `/validate ghostfit`*
