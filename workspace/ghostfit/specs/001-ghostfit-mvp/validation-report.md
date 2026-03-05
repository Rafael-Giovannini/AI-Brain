# Validation Report: ghostfit / 001-ghostfit-mvp

**Gerado**: 2026-03-05 — Validação Completa (8 passes)
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp (GhostFit MVP — Provador Virtual com Overlay)

---

## Sumário Executivo

| Métrica | Valor |
|---------|-------|
| Problemas de Consistência entre Docs | 3 |
| Problemas BMAD Docs vs Spec | 4 |
| Problemas Ralph Config | 3 |
| Entidades: Definidas / Implementadas | 7 / 4 (57%) |
| API Endpoints: Definidos / Implementados | 9 / 0 (0%) |
| FRs Rastreados no Código | 6 / 21 (29%) |
| Cenários de Aceitação com Testes | 7 / 27 (26%) |
| Problemas de Qualidade de Código | 6 |
| Issues CRITICAL | 0 |
| Issues HIGH | 7 |
| Issues MEDIUM | 10 |
| Issues LOW | 6 |

**Nota:** O projeto está no final da Fase 2 (Story 5 — Overlay). Apenas Phases 0-2 estão implementadas. É esperado que entidades, endpoints e FRs das Fases 3-5 estejam MISSING.

---

## 1. Consistência entre Docs de Spec (Pass A)

| ID | Severidade | Localização | Achado | Recomendação |
|----|-----------|-------------|--------|--------------|
| A-01 | MEDIUM | plan.md vs quickstart.md | plan.md diz "Room 2.6" mas quickstart.md especifica `room-runtime:2.6.1`. Versão minor inconsistente. Além disso, commit `b8f4c6c` atualizou para Room 2.7.1, mas nenhum doc foi atualizado. | Alinhar todos os docs para "Room 2.7.1". |
| A-02 | LOW | data-model.md vs api-contracts.md | data-model define `SubscriptionState.purchaseType` com enum `NONE, PACK, MONTHLY`, mas api-contracts.md não menciona nenhum endpoint de subscription state. O estado é sincronizado com Play Billing sem endpoint backend. | Documentar explicitamente que SubscriptionState é local-only (sem sync via API). |
| A-03 | MEDIUM | plan.md vs código | plan.md diz "Hilt (DI)" como dependência primária, mas quickstart.md mostra Hilt comentado com nota "MVP Phase 1 usa singletons manuais". Decisão deliberada mas cria ambiguidade para quem lê o plan.md isoladamente. | Adicionar nota em plan.md: "Hilt planejado para Phase 2+. Phase 1 usa DI manual." |

---

## 2. Cobertura de Entidades (Pass B)

| Entidade | Arquivo no Código | Status | Campos Faltantes | Campos Extras |
|----------|-------------------|--------|------------------|---------------|
| UserProfile | `data/model/UserProfile.kt` | **COMPLETE** | — | — |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | **COMPLETE** | — | — |
| TryOnSession | — | **MISSING** | Todos (efêmera, esperada na Phase 3) | — |
| GarmentInfo | `data/model/GarmentInfo.kt` | **COMPLETE** | — | — |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | **COMPLETE** | — | — |
| SubscriptionState | — | **MISSING** | Todos (esperada na Phase 5) | — |
| ModelScore | — | **MISSING** | Todos (backend, esperada na Phase 5) | — |

**Detalhes de validação de campos:**

- **UserProfile**: 10/10 campos implementados. `@Entity`, `PlanType` enum com FREE/PREMIUM ✓. Todas as validações (default values, types) corretas.
- **ReferencePhoto**: 6/6 campos. ForeignKey para UserProfile com CASCADE ✓. Index em `userId` ✓.
- **GarmentInfo**: 5/5 campos. `GarmentCategory` enum com TOP/BOTTOM/DRESS/OUTERWEAR ✓. Tipo `Bitmap` para `croppedImage` ✓.
- **FeedbackRecord**: 5/5 campos. Não é `@Entity` (correto — enviado ao backend, não persistido) ✓.

---

## 3. Cobertura de Contratos de API (Pass C)

| Endpoint | Arquivo no Código | Status | Notas |
|----------|-------------------|--------|-------|
| POST api.fashn.ai/v1/run | — | **MISSING** | Esperado na Phase 3 (FashnApi.kt) |
| POST Vertex AI predict | — | **MISSING** | Esperado na Phase 3 (VertexAiApi.kt) |
| POST api.openai.com/v1/chat/completions | — | **MISSING** | Esperado na Phase 3 (VisionLlmApi.kt) |
| ML Kit Object Detection | — | **MISSING** | On-device, esperado Phase 3 (GarmentDetector.kt) |
| POST /v1/feedback | — | **MISSING** | Esperado na Phase 4 (GhostFitApi.kt) |
| GET /v1/models/route | — | **MISSING** | Esperado na Phase 5 (GhostFitApi.kt) |
| GET /v1/config | — | **MISSING** | Esperado na Phase 5 (GhostFitApi.kt) |
| GET /v1/health | — | **MISSING** | Esperado na Phase 5 |
| POST /v1/dataset | — | **MISSING** | Esperado na Phase 5 |

**Nota:** Nenhum endpoint implementado. Esperado — implementação planejada para Phases 3-5.

---

## 4. Rastreabilidade de FRs (Pass D)

| FR | Descrição | No Código? | Nos Testes? | Status |
|----|-----------|-----------|-------------|--------|
| FR-001 | Cadastro de fotos via galeria | ✅ PhotoSelectScreen, PhotoStorage, ReferencePhotoDao | ✅ PhotoStorageTest (10 testes) | **TRACED** |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | ✅ PermissionScreen, AndroidManifest | ❌ | **PARTIAL** |
| FR-003 | Consentimento LGPD explícito | ✅ LgpdConsentScreen, OnboardingViewModel | ✅ UserProfileDaoTest (menciona FR-003) | **TRACED** |
| FR-004 | Overlay flutuante | ✅ OverlayService, OverlayComposable | ✅ OverlayServiceTest | **TRACED** |
| FR-005 | Captura de tela ao toque | ✅ ScreenCapture.kt | ⚠️ Apenas `isReady` testado | **PARTIAL** |
| FR-006 | Overlay reposicionável (drag) | ✅ OverlayComposable (drag), OverlayService (save) | ✅ OverlayServiceTest (posição) | **TRACED** |
| FR-007 | Detecção de roupa via IA | ❌ | ❌ | **UNTRACED** |
| FR-008 | Aviso "nenhuma roupa detectada" | ❌ | ❌ | **UNTRACED** |
| FR-009 | Geração de imagem try-on | ❌ | ❌ | **UNTRACED** |
| FR-010 | Regeneração do try-on | ❌ | ❌ | **UNTRACED** |
| FR-011 | Troca de foto de referência | ❌ | ❌ | **UNTRACED** |
| FR-012 | Fallback entre modelos de IA | ❌ | ❌ | **UNTRACED** |
| FR-013 | Compartilhamento com branding | ❌ | ❌ | **UNTRACED** |
| FR-014 | Feedback thumbs up/down | ❌ | ❌ | **UNTRACED** |
| FR-015 | Limite 3 tentativas/dia | ❌ | ⚠️ UserProfileDaoTest testa dailyTries | **PARTIAL** |
| FR-016 | Anúncios para free | ❌ | ❌ | **UNTRACED** |
| FR-017 | Pacotes pagos / assinatura | ❌ | ❌ | **UNTRACED** |
| FR-018 | Google Play Billing | ❌ | ❌ | **UNTRACED** |
| FR-019 | Coleta de dados para dataset | ❌ | ❌ | **UNTRACED** |
| FR-020 | Roteamento inteligente | ❌ | ❌ | **UNTRACED** |
| FR-021 | Pipeline de dados | ❌ | ❌ | **UNTRACED** |

**Resumo:** 4 TRACED, 3 PARTIAL, 14 UNTRACED (esperado — Phases 3-5 não iniciadas)

---

## 5. Cobertura de Cenários de Aceitação (Pass E)

### User Story 1 — Setup Inicial e Onboarding (P1)

| Cenário | Asserção Chave | Teste? | Arquivo |
|---------|----------------|--------|---------|
| US1-1: Tela de boas-vindas | WelcomeScreen exibe e guia para setup | ❌ | — |
| US1-2: Permissão overlay com explicação | PermissionScreen explica e redireciona | ❌ | — |
| US1-3: Detecção automática de permissão | `allGranted` auto-avança | ❌ | — |
| US1-4: Checkbox LGPD não pré-marcado | `consentChecked = false` | ✅ | UserProfileDaoTest.kt |
| US1-5: Seleção de fotos | PhotoSelectScreen funcional | ❌ | — |
| US1-6: Validação foto corpo inteiro | `isBodyFullVisible` validado | ⚠️ Hardcoded `true` | — |

### User Story 2 — Try-On Virtual (P1) — NÃO IMPLEMENTADA

| Cenário | Status |
|---------|--------|
| US2-1 a US2-6 | **UNCOVERED** (Phase 3) |

### User Story 3 — Interação com Resultado (P2) — NÃO IMPLEMENTADA

| Cenário | Status |
|---------|--------|
| US3-1 a US3-5 | **UNCOVERED** (Phase 4) |

### User Story 4 — Monetização e Limites (P2) — NÃO IMPLEMENTADA

| Cenário | Status |
|---------|--------|
| US4-1 a US4-6 | **UNCOVERED** (Phase 5) |

### User Story 5 — Overlay Flutuante (P1)

| Cenário | Asserção Chave | Teste? | Arquivo |
|---------|----------------|--------|---------|
| US5-1: Overlay visível | TYPE_APPLICATION_OVERLAY | ✅ | OverlayServiceTest.kt |
| US5-2: Persiste entre apps | START_STICKY + Foreground | ✅ | Implícito |
| US5-3: Drag suave | detectDragGestures | ✅ | OverlayServiceTest.kt |
| US5-4: Posição salva | updateOverlayPosition | ✅ | OverlayServiceTest.kt |
| US5-5: Sem blocking/lag | Overlay < 50MB | ❌ | — |

**Resumo:** 7/27 cenários COVERED, 1 PARTIAL, 19 UNCOVERED

---

## 6. Qualidade de Código (Pass F)

| Arquivo | Categoria | Severidade | Achado | Recomendação |
|---------|-----------|-----------|--------|--------------|
| `overlay/OverlayComposable.kt:20` | Code Smell | LOW | `GhostPurple` (Color 0xFF7C3AED) duplicado — mesmo valor em `ui/theme/GhostFitTheme.kt:14`. | Extrair para arquivo de constantes compartilhado ou importar de GhostFitTheme. |
| `overlay/OverlayService.kt:74` | Arquitetura | MEDIUM | OverlayService acessa `AppDatabase.getInstance()` diretamente (camada Service importando data/local). Deveria usar um repositório ou use case. | Introduzir OverlayRepository ou injetar UserProfileDao via construtor/factory ao migrar para Hilt. |
| `ui/onboarding/OnboardingViewModel.kt:54` | Lógica de Negócio | HIGH | `isBodyFullVisible = true` hardcoded — a validação de corpo inteiro da spec (FR-001, US1-6) não está implementada. Todas as fotos são aceitas sem validação. | Implementar validação real via ML Kit Pose Detection ou placeholder que rejeita fotos claramente inadequadas. |
| `ui/onboarding/PhotoSelectScreen.kt:244` | Segurança | LOW | `uriToBitmap` captura `Exception` genérica sem logging. Erros silenciosos dificultam debugging. | Adicionar log de erro (pelo menos em Debug) para facilitar diagnóstico. |
| `AndroidManifest.xml:53` | Segurança | LOW | AdMob test App ID hardcoded (`ca-app-pub-3940256099942544~...`). Aceitável em desenvolvimento, mas deve ser migrado para BuildConfig antes da produção. | Mover para `BuildConfig.ADMOB_APP_ID` via local.properties. |
| `overlay/ScreenCapture.kt:71` | Performance | LOW | Usa API deprecada `wm.defaultDisplay.getRealMetrics()`. Funcional mas gera warning. | Migrar para `WindowMetrics` API (Android R+) com fallback para API antiga. |

### Cobertura de Testes

| Camada | Arquivos | Testes | Cobertura Estimada |
|--------|----------|--------|--------------------|
| data/local | AppDatabase, UserProfileDao, ReferencePhotoDao, PhotoStorage, Converters | UserProfileDaoTest (10), PhotoStorageTest (10) | ~70% |
| data/model | UserProfile, ReferencePhoto, GarmentInfo, FeedbackRecord | Testados indiretamente via DAO tests | ~60% |
| overlay | OverlayService, OverlayComposable, ScreenCapture | OverlayServiceTest (7) | ~40% |
| ui/onboarding | WelcomeScreen, PermissionScreen, LgpdConsentScreen, PhotoSelectScreen, OnboardingViewModel | ❌ Nenhum teste | 0% |
| domain | (não existe ainda) | — | N/A |

---

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severidade | Doc BMAD | Doc Spec | Achado | Recomendação |
|----|-----------|----------|----------|--------|--------------|
| G-01 | HIGH | architecture-ghostfit.md | plan.md | Arquitetura define Hilt como DI framework obrigatório, mas código usa singletons manuais (`AppDatabase.getInstance`, `PhotoStorage.getInstance`). plan.md lista Hilt como dependência primária. | Atualizar architecture/plan para refletir DI manual no MVP, ou migrar para Hilt na próxima phase. |
| G-02 | MEDIUM | ux-design-ghostfit.md (Tela 13) | spec.md | UX design inclui botão "Assistir Ad = +1 Tentativa" (rewarded ad) na tela de Limite Atingido. Este recurso NÃO está nos FRs da spec (FR-016 só menciona banner/interstitial). | Remover da UX ou adicionar FR correspondente na spec. |
| G-03 | MEDIUM | sprint-plan-ghostfit.md | spec.md | Sprint plan atribui STORY-013 (AI Pipeline Orquestrador) à Fase 3 enquanto é dependência do fluxo core (Fase 2). | Considerar mover STORY-013 para o final da Fase 2. |
| G-04 | HIGH | product-brief-ghostfit.md | spec.md | Product brief diz "Photo Picker nativo", spec (FR-001) diz "Google Fotos ou galeria do dispositivo". São equivalentes na prática mas terminologia inconsistente. | Alinhar para "Photo Picker nativo" (que inclui Google Fotos) em todos os docs. |

---

## 8. Ralph Config (Pass H)

| ID | Severidade | Arquivo | Achado | Recomendação |
|----|-----------|---------|--------|--------------|
| H-01 | HIGH | `.ralph/PROMPT.md` | Tech stack lista "Room 2.6" mas build real usa Room 2.7.1 (atualizado no commit `b8f4c6c`). | Atualizar PROMPT.md: Room 2.6 → 2.7.1. |
| H-02 | MEDIUM | `.ralph/PROMPT.md` | Tech stack lista "Kotlin 1.9+" mas build real usa Kotlin 2.2.0 (atualizado no commit `b8f4c6c`). | Atualizar PROMPT.md: Kotlin 1.9+ → 2.2.0. |
| H-03 | MEDIUM | `.ralph/fix_plan.md` | Phase 4 inclui task "Create FeedbackRecord DTO" mas FeedbackRecord já foi criado na Phase 1 (marcada como [x]). Task duplicada. | Remover ou marcar como [x] na Phase 4 com nota "já criado na Phase 1". |

### .ralphrc — Validação ✅

| Campo | Valor | Status |
|-------|-------|--------|
| PROJECT_NAME | "GhostFit MVP" | ✅ |
| PROJECT_TYPE | "android-kotlin" | ✅ |
| PROJECT_ROOT | "workspace/ghostfit" | ✅ |
| ALLOWED_TOOLS | gradlew, git (safe only) | ✅ Sem comandos perigosos |

### AGENT.md — Validação ✅

| Item | Status |
|------|--------|
| Build: `./gradlew assembleDebug` | ✅ |
| Tests: `./gradlew testDebugUnitTest` | ✅ |
| Package: `app.ghostfit` | ✅ |
| Min SDK 26, Target 35 | ✅ |

### fix_plan.md — Validação ✅

| Item | Status |
|------|--------|
| Phase 0-2 marcados [x] | ✅ Matches código |
| Phase 3-5 marcados [ ] | ✅ Não implementados |
| Cobertura 21 FRs | ✅ |

---

## Próximas Ações

### HIGH (corrigir em breve)

1. **H-01**: Atualizar `.ralph/PROMPT.md` tech stack: Room 2.6 → 2.7.1
2. **H-02**: Atualizar `.ralph/PROMPT.md` tech stack: Kotlin 1.9+ → 2.2.0
3. **G-01/A-03**: Alinhar documentação sobre Hilt vs DI manual em plan.md e architecture
4. **G-04**: Alinhar terminologia "Google Fotos" vs "Photo Picker nativo" em spec.md (FR-001)
5. **F-03**: Implementar validação real de "corpo inteiro" na foto (`isBodyFullVisible` hardcoded `true`)
6. **FR-002**: Adicionar testes para PermissionScreen
7. **FR-005**: Expandir testes de ScreenCapture

### MEDIUM (endereçar durante implementação)

1. **G-02**: Decidir se "rewarded ad" (UX Tela 13) entra no MVP
2. **G-03**: Considerar mover STORY-013 para final da Fase 2
3. **H-03**: Limpar task duplicada FeedbackRecord no fix_plan.md
4. **A-01**: Alinhar versão Room em plan.md e quickstart.md → 2.7.1
5. **OverlayService**: Refatorar acesso direto ao DB — usar repositório
6. **UI Tests**: Adicionar testes para OnboardingViewModel e telas de onboarding

### LOW (melhorias pontuais)

1. Extrair `GhostPurple` color para arquivo compartilhado
2. Adicionar logging em `PhotoSelectScreen.uriToBitmap`
3. Migrar AdMob App ID para BuildConfig antes de produção
4. Migrar `ScreenCapture.getRealMetrics()` para `WindowMetrics` API
5. Documentar SubscriptionState como local-only em data-model.md
6. Alinhar versão Room minor em plan.md

---

*Execute `/validate ghostfit` novamente após corrigir os issues para verificar progresso.*
