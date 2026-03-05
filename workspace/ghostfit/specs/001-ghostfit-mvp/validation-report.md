# Relatório de Validação: ghostfit / 001-ghostfit-mvp

**Gerado**: 2026-03-05 — Validação completa (8 passes)
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp (GhostFit MVP — Provador Virtual com Overlay)

---

## Resumo Executivo

| Métrica | Valor |
|---------|-------|
| Problemas de Consistência entre Docs (Pass A) | 4 |
| Problemas BMAD Docs vs Spec (Pass G) | 4 |
| Problemas Ralph Config (Pass H) | 3 |
| Entidades: Definidas / Implementadas | 7 / 5 COMPLETE + 1 PARTIAL + 1 MISSING |
| API Endpoints: Definidos / Implementados | 9 / 8 (89%) |
| FRs Rastreados no Código | 6 TRACED / 13 PARTIAL / 2 UNTRACED |
| Cenários de Aceitação com Testes | 5 / 28 (18%) |
| Problemas de Qualidade de Código | 24 |
| Issues CRITICAL | 0 |
| Issues HIGH | 4 |
| Issues MEDIUM | 16 |
| Issues LOW | 12 |

**Estado Geral**: Projeto na **Phase 3 completa** (Onboarding + Overlay + Try-On Core). Code quality sólida no geral. Phases 4-5 (Story 3: Interações, Story 4: Monetização) ainda não implementadas — SubscriptionState e Google Play Billing são os maiores gaps. 1 endpoint MISSING (GET /v1/health). Nenhum issue CRITICAL.

---

## 1. Consistência entre Docs de Spec (Pass A)

| ID | Severidade | Localização | Achado | Recomendação |
|----|-----------|-------------|--------|--------------|
| A-01 | MEDIUM | plan.md vs sprint-plan | plan.md diz "Singletons manuais no MVP (Hilt planejado para refactor futuro)", mas sprint-plan STORY-001 AC lista "Hilt (DI) configurado com módulos base" | Alinhar: remover Hilt do AC do STORY-001 no sprint-plan ou atualizar plan.md |
| A-02 | LOW | quickstart.md vs plan.md | quickstart.md lista Compose BOM `2024.12.01`, plan.md diz "Compose 1.7+" — versões implicitamente compatíveis mas não explicitamente alinhadas | Atualizar plan.md com versão exata do BOM |
| A-03 | MEDIUM | data-model.md vs api-contracts.md | Entidade `SubscriptionState` no data-model define `purchaseType` como Enum (NONE/PACK/MONTHLY), mas api-contracts.md não define endpoint para gerenciar assinatura | Documentar no api-contracts.md que SubscriptionState é gerenciado localmente via Play Billing |
| A-04 | MEDIUM | data-model.md vs code | TryOnSession no data-model define 5 estados (CAPTURING/DETECTING/GENERATING/DONE/ERROR), mas código implementa 7 (adiciona IDLE e NO_GARMENT). Extra field `errorMessage` no código | Atualizar data-model.md para incluir estados IDLE e NO_GARMENT, e campo errorMessage |

---

## 2. Matriz de Cobertura de Entidades (Pass B)

| Entidade | Arquivo de Código | Status | Campos Faltantes | Campos Extras |
|----------|-------------------|--------|-------------------|---------------|
| UserProfile | `data/model/UserProfile.kt` | COMPLETE | — | — (lgpdConsentTimestamp e dailyTriesResetDate nullable vs spec non-null — aceitável) |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | COMPLETE | — | — (displayName nullable vs spec String — aceitável) |
| TryOnSession | `domain/TryOnUseCase.kt` | PARTIAL | — | `errorMessage: String?` extra; enums IDLE e NO_GARMENT extras |
| GarmentInfo | `data/model/GarmentInfo.kt` | COMPLETE | — | — (enum GarmentCategory = TOP/BOTTOM/DRESS/OUTERWEAR — match exato) |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | COMPLETE | — | — (5 campos match exato) |
| SubscriptionState | — | MISSING | Todos os 7 campos + Enum PurchaseType | — (Phase 5 — Monetização) |
| ModelScore | — | N/A | Backend-only; não requerido no app Android | — |

---

## 3. Cobertura de Contratos de API (Pass C)

| Endpoint | Arquivo de Código | Status | Notas |
|----------|-------------------|--------|-------|
| POST fashn.ai/v1/run | `data/remote/FashnApi.kt` | IMPLEMENTED | Match completo; category mapping correto |
| POST vertex-ai/.../predict | `data/remote/VertexAiApi.kt` | IMPLEMENTED | Region hardcoded "us-central1"; match completo |
| POST openai/chat/completions | `data/remote/VisionLlmApi.kt` | IMPLEMENTED | Falta campo `response_format: json_object` no VisionRequest |
| ML Kit Object Detection | `domain/GarmentCropper.kt` | IMPLEMENTED | Usa `.enableMultipleObjects()` ao invés de `.enableClassification()` + filtro "Fashion good" |
| POST /v1/feedback | `data/remote/GhostFitApi.kt` | IMPLEMENTED | Wired em TryOnUseCase.submitFeedback() |
| GET /v1/models/route | `data/remote/GhostFitApi.kt` | IMPLEMENTED | Declarado mas NÃO chamado — ModelRouter usa chain hardcoded |
| GET /v1/config | `data/remote/GhostFitApi.kt` | IMPLEMENTED | Declarado mas NÃO chamado — app usa defaults hardcoded |
| GET /v1/health | — | MISSING | Nenhuma declaração no codebase |
| POST /v1/dataset | `data/remote/GhostFitApi.kt` | IMPLEMENTED | Wired em TryOnUseCase (apenas quando thumbsUp=true) |

---

## 4. Rastreabilidade de FRs (Pass D)

| FR | Descrição | No Código? | Nos Testes? | Status |
|----|-----------|-----------|-------------|--------|
| FR-001 | Cadastro de fotos via Photo Picker | ✅ PhotoSelectScreen + OnboardingViewModel (TODO: body validation) | ❌ | PARTIAL |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | ✅ PermissionScreen.kt | ❌ | PARTIAL |
| FR-003 | Consentimento LGPD | ✅ LgpdConsentScreen + UserProfile | ✅ UserProfileDaoTest (consent flow) | TRACED |
| FR-004 | Overlay flutuante | ✅ OverlayService.kt | ✅ OverlayServiceTest (parcial) | PARTIAL |
| FR-005 | Captura de tela | ✅ ScreenCapture.kt + TryOnUseCase | ✅ TryOnUseCaseTest (CAPTURING status) | PARTIAL |
| FR-006 | Overlay reposicionável | ✅ OverlayComposable + OverlayService | ✅ OverlayServiceTest (position) | TRACED |
| FR-007 | Detecção de roupa via IA | ✅ GarmentDetector + GarmentCropper | ✅ GarmentDetectorTest (6 testes) | TRACED |
| FR-008 | Aviso "nenhuma roupa detectada" | ✅ TryOnUseCase (NO_GARMENT) + TryOnResultScreen | ✅ TryOnUseCaseTest (no garment) | TRACED |
| FR-009 | Geração de imagem try-on | ✅ ModelRouter + FashnApi + VertexAiApi | ✅ ModelRouterTest (primary model) | PARTIAL |
| FR-010 | Regenerar try-on | ✅ TryOnUseCase.regenerate() | ❌ | PARTIAL |
| FR-011 | Trocar foto de referência | ✅ TryOnResultScreen (onChangePhoto) | ❌ | PARTIAL |
| FR-012 | Fallback entre modelos de IA | ✅ ModelRouter (FASHN→Vertex 2x retry cada) | ✅ ModelRouterTest (fallback + both-fail) | TRACED |
| FR-013 | Compartilhar com branding | ✅ TryOnResultScreen.shareTryOnImage() | ❌ | PARTIAL |
| FR-014 | Feedback thumbs up/down | ✅ TryOnUseCase.submitFeedback() + GhostFitApi | ❌ | PARTIAL |
| FR-015 | Limite 3 tentativas/dia | ✅ TryOnUseCase.checkDailyLimit() | ✅ TryOnUseCaseTest (daily limit) + UserProfileDaoTest | TRACED |
| FR-016 | Exibição de anúncios | ❌ | ❌ | UNTRACED |
| FR-017 | Pacotes pagos / assinatura | ✅ PlanType enum + RemoteConfig.subscriptionPrices (parcial) | ❌ | PARTIAL |
| FR-018 | Google Play Billing | ❌ | ❌ | UNTRACED |
| FR-019 | Coleta de dados dataset | ✅ TryOnUseCase.submitFeedback() → uploadDataset() | ❌ | PARTIAL |
| FR-020 | Roteamento inteligente | ✅ GhostFitApi.getModelRoute() declarado (não chamado) | ❌ | PARTIAL |
| FR-021 | Pipeline de dados fine-tuning | ✅ DatasetEntry com clothingType/modelUsed | ❌ | PARTIAL |

**Resumo**: 6 TRACED (29%) / 13 PARTIAL (62%) / 2 UNTRACED (10%)

---

## 5. Cobertura de Cenários de Aceitação (Pass E)

### Story 1 — Setup Inicial e Onboarding (P1)

| Cenário | Assertiva Chave | Teste? | Arquivo de Teste |
|---------|-----------------|--------|------------------|
| US1-1: Tela de boas-vindas | WelcomeScreen exibida ao primeiro uso | ❌ UNCOVERED | — |
| US1-2: Explicação overlay | PermissionScreen explica SYSTEM_ALERT_WINDOW | ❌ UNCOVERED | — |
| US1-3: Detecta permissão auto | Lifecycle-aware check ao retornar | ❌ UNCOVERED | — |
| US1-4: Checkbox LGPD não pré-marcado | `isChecked = false` default | PARTIAL | UserProfileDaoTest (DB layer only) |
| US1-5: Seleção de fotos | Photo Picker nativo funcional | ❌ UNCOVERED | — |
| US1-6: Validação corpo inteiro | isBodyFullVisible check real | ❌ UNCOVERED | TODO no código |

### Story 2 — Try-On Virtual (P1)

| Cenário | Assertiva Chave | Teste? | Arquivo de Teste |
|---------|-----------------|--------|------------------|
| US2-1: Captura < 1s ao tocar fantasminha | Screenshot em tempo | PARTIAL | TryOnUseCaseTest (status CAPTURING) |
| US2-2: Detecção roupa < 3s | ML Kit + GPT-4o Vision | PARTIAL | GarmentDetectorTest (logic, não timing) |
| US2-3: Geração imagem < 15s | FASHN/Vertex AI gera resultado | PARTIAL | ModelRouterTest (logic, não timing) |
| US2-4: Fisionomia preservada | Qualidade visual | ❌ UNCOVERED | Não testável unitariamente |
| US2-5: Sem roupa → mensagem, sem consumir tentativa | NO_GARMENT status | ✅ COVERED | TryOnUseCaseTest |
| US2-6: Fallback automático primário → secundário | FASHN falha → Vertex | ✅ COVERED | ModelRouterTest |

### Story 3 — Interação com Resultado (P2)

| Cenário | Assertiva Chave | Teste? | Arquivo de Teste |
|---------|-----------------|--------|------------------|
| US3-1: "Tentar novamente" gera variação | regenerate() chamado | PARTIAL | Método existe, sem teste |
| US3-2: "Trocar foto" regenera com mesma roupa | onChangePhoto callback | ❌ UNCOVERED | — |
| US3-3: Feedback thumbs registrado async | submitFeedback() fire-and-forget | PARTIAL | Método existe, sem teste |
| US3-4: Compartilhar via share sheet | shareTryOnImage() | ❌ UNCOVERED | — |
| US3-5: Thumbs up → dataset upload anonimizado | uploadDataset() só em thumbsUp | PARTIAL | Lógica existe, sem teste |

### Story 4 — Monetização e Limites (P2)

| Cenário | Assertiva Chave | Teste? | Arquivo de Teste |
|---------|-----------------|--------|------------------|
| US4-1: Contador "3 tentativas restantes" | UI mostra contador | ❌ UNCOVERED | — |
| US4-2: Limite 3 → bloqueio + upgrade | DailyLimitExceededException | ✅ COVERED | TryOnUseCaseTest |
| US4-3: Reset à meia-noite | Date-based reset | ✅ COVERED | UserProfileDaoTest + TryOnUseCaseTest |
| US4-4: Anúncios entre gerações | AdMob integration | ❌ UNCOVERED | FR-016 não implementado |
| US4-5: Assinatura via Play Billing | BillingClient flow | ❌ UNCOVERED | FR-018 não implementado |
| US4-6: Restaurar compras em reinstalação | Restore flow | ❌ UNCOVERED | FR-018 não implementado |

### Story 5 — Overlay Flutuante (P1)

| Cenário | Assertiva Chave | Teste? | Arquivo de Teste |
|---------|-----------------|--------|------------------|
| US5-1: Fantasminha visível sobre qualquer app | TYPE_APPLICATION_OVERLAY | PARTIAL | OverlayServiceTest (constants) |
| US5-2: Overlay persiste entre apps | Foreground Service | ❌ UNCOVERED | — |
| US5-3: Drag move suave | detectDragGestures | ❌ UNCOVERED | — |
| US5-4: Posição salva entre sessões | updateOverlayPosition | ✅ COVERED | OverlayServiceTest |
| US5-5: Não bloqueia app hospedeiro | FLAG_NOT_FOCUSABLE | PARTIAL | Mecanismo correto, sem teste |

**Total**: 5 COVERED (18%) / 9 PARTIAL (32%) / 14 UNCOVERED (50%)

---

## 6. Qualidade de Código (Pass F)

| Arquivo | Categoria | Severidade | Achado | Recomendação |
|---------|-----------|-----------|--------|--------------|
| `domain/TryOnUseCase.kt`, `ModelRouter.kt`, `GarmentDetector.kt` | Arquitetura | HIGH | Classes domain importam tipos de infraestrutura diretamente (Retrofit interfaces, Room DAOs, DTOs de API) — viola Dependency Rule | Introduzir interfaces de repositório no domain; data layer implementa |
| `data/model/UserProfile.kt`, `ReferencePhoto.kt` | Arquitetura | MEDIUM | Entities de modelo anotadas com Room `@Entity` — funde schema de persistência com modelo de domínio | Separar Room entities de domain models com mappers |
| `ui/tryon/TryOnResultScreen.kt` | Arquitetura | MEDIUM | Contém lógica de I/O (`shareTryOnImage`: FileOutputStream, FileProvider) em arquivo de UI composable | Mover para use case ou helper dedicado |
| `ui/tryon/TryOnResultScreen.kt` | Arquitetura | LOW | Arquivo com 365 linhas (> 300 threshold) | Separar composables de estado em arquivo próprio |
| `overlay/OverlayService.kt` | Arquitetura | LOW | DAO instanciado diretamente via singleton, sem DI | Injetar via DI ou abstração |
| `domain/TryOnUseCase.kt:226,243` | Segurança | MEDIUM | `planType.name == "PREMIUM"` — comparação string frágil em gate de segurança | Trocar por `planType == PlanType.PREMIUM` |
| `ui/onboarding/OnboardingViewModel.kt:56` | Segurança | MEDIUM | `isBodyFullVisible = true` hardcoded — validação de corpo inteiro é placeholder | Implementar ML Kit Pose Detection ou documentar gap |
| `FashnApi.kt`, `GhostFitApi.kt`, `VertexAiApi.kt`, `VisionLlmApi.kt` | Segurança | MEDIUM | Sem certificate pinning em nenhum OkHttpClient — fotos biométricas transmitidas | Adicionar CertificatePinner antes de release produção |
| `ui/tryon/TryOnResultScreen.kt:340-362` | Segurança | LOW | Share grava JPEG não criptografado em cacheDir sem cleanup | Deletar arquivo após share; excluir de backup |
| `data/local/AppDatabase.kt:14` | Segurança | LOW | `exportSchema = false` impede auditoria de migrações | Habilitar `exportSchema = true` |
| `domain/TryOnUseCase.kt:234` | Code Smell | MEDIUM | Magic number `3` para limite diário; ignora `RemoteConfig.maxFreeTrials` | Usar maxFreeTrials de RemoteConfig ou constante nomeada |
| `overlay/OverlayService.kt:137`, `OnboardingViewModel.kt:50` | Code Smell | MEDIUM | 2 TODOs de features incompletas (tap handler no-op, body validation) | Rastrear como backlog formal |
| `TryOnUseCase.kt`, `ModelRouter.kt`, `GarmentDetector.kt` | Code Smell | LOW | `bitmapToBase64` duplicado em 3 classes | Extrair para `Bitmap.toBase64Jpeg()` extension |
| `ModelRouter.kt`, `GarmentDetector.kt`, `TryOnResultScreen.kt` | Code Smell | LOW | `catch (_: Exception)` silencioso em 4 locais — sem logging | Adicionar Log.w ou crash reporting |
| `domain/GarmentDetector.kt` | Code Smell | LOW | Instância Moshi criada por classe em vez de singleton | Compartilhar Moshi via companion ou DI |
| `domain/GarmentCropper.kt` | Testes | MEDIUM | MlKitGarmentCropper sem testes (crop logic, bounding box, edge cases) | Criar GarmentCropperTest.kt com Robolectric |
| `ui/onboarding/OnboardingViewModel.kt` | Testes | MEDIUM | ViewModel com lógica LGPD (consent + timestamp) sem testes | Criar OnboardingViewModelTest.kt |
| `data/local/ReferencePhotoDao.kt` | Testes | LOW | DAO sem testes (FK cascade, ordering, count) | Criar ReferencePhotoDaoTest.kt |
| `overlay/OverlayServiceTest.kt` | Testes | LOW | Teste nomeado "OverlayService" mas testa apenas DAO; imports Robolectric não usados | Renomear ou adicionar testes reais de lifecycle |
| UI screens (todas) | Testes | LOW | Nenhum teste Compose UI para telas, incluindo LgpdConsentScreen | Adicionar teste mínimo para checkbox LGPD |
| `ui/tryon/TryOnResultScreen.kt:338-363` | Performance | MEDIUM | `shareTryOnImage` faz I/O bloqueante, provavelmente na Main thread | Executar em Dispatchers.IO via coroutine |
| `domain/ModelRouter.kt` | Performance | LOW | Retries sem backoff (repeat 2x consecutivo) | Adicionar delay com backoff exponencial |

**Pontos Positivos**:
- Nenhum segredo hardcoded; API keys via BuildConfig ✓
- Criptografia Tink AES-256-GCM com Android Keystore — implementação correta ✓
- Nenhum `runBlocking` na Main thread ✓
- I/O de rede e DB corretamente despachado para `Dispatchers.IO` ✓
- Test naming convention consistente (backtick sentence style) ✓
- 48+ testes unitários (DAO 11 + PhotoStorage 10 + Overlay 7 + GarmentDetector 6 + ModelRouter 8 + TryOnUseCase 6)

---

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severidade | Arquivo Docs | Arquivo Spec | Achado | Recomendação |
|----|-----------|-------------|-------------|--------|--------------|
| G-01 | HIGH | sprint-plan STORY-001 | plan.md | Sprint-plan define "Hilt (DI) configurado com módulos base" como AC, mas plan.md e código usam singletons manuais | Atualizar STORY-001 no sprint-plan: trocar AC de Hilt para "Singletons manuais" |
| G-02 | HIGH | sprint-plan STORY-001 | plan.md | Sprint-plan lista "Ktlint + Detekt configurados" como AC, mas build.gradle.kts não contém esses plugins | Adicionar Ktlint/Detekt ao build.gradle ou remover AC do sprint-plan |
| G-03 | MEDIUM | ux-design (Tela 13) | spec.md | UX inclui botão "Assistir Ad = +1 Tentativa" (rewarded ad), mas spec/PRD não mencionam rewarded ads | Alinhar: adicionar rewarded ads à spec ou remover da UX |
| G-04 | MEDIUM | ux-design (Tela 15) | spec.md | UX define Tela Home com toggle overlay e galeria inline — não existe na spec ou plan | Adicionar tela Home ao plan.md ou spec |

---

## 8. Ralph Config (Pass H)

| ID | Severidade | Arquivo | Achado | Recomendação |
|----|-----------|---------|--------|--------------|
| H-01 | HIGH | AGENT.md:8 | AGENT.md diz `kotlin -version # Kotlin 1.9+` como pré-requisito, mas projeto usa Kotlin 2.2.0 | Atualizar AGENT.md: `Kotlin 2.2+` |
| H-02 | MEDIUM | AGENT.md | Não menciona Android SDK como pré-requisito; quickstart.md exige "Android SDK API 26+" | Adicionar Android SDK ao bloco de pré-requisitos |
| H-03 | MEDIUM | fix_plan.md + AGENT.md | Sprint-plan e código usam Robolectric, AGENT.md não menciona | Adicionar menção a Robolectric |

**Pontos Positivos do Ralph Config**:
- `.ralphrc`: PROJECT_NAME, PROJECT_TYPE, PROJECT_ROOT todos corretos ✓
- `.ralphrc`: ALLOWED_TOOLS sem comandos perigosos ✓
- `PROMPT.md`: Nome do projeto, tech stack, branch, paths todos corretos ✓
- `fix_plan.md`: Phases 0-3 marcadas complete — coerente com estado do código ✓
- `fix_plan.md`: Phases 4-5 com tasks pendentes — alinhado com stories restantes ✓

---

## Próximas Ações

### HIGH (corrigir em breve)

1. **[G-01]** Atualizar STORY-001 no sprint-plan: trocar "Hilt configurado" por "Singletons manuais"
2. **[G-02]** Decidir sobre Ktlint/Detekt: configurar no projeto ou remover do AC do STORY-001
3. **[H-01]** Atualizar AGENT.md: Kotlin 1.9+ → Kotlin 2.2+
4. **[F1-1]** Introduzir interfaces de repositório no domain para desacoplar de infraestrutura (Retrofit/Room)

### MEDIUM (resolver durante implementação)

1. **[A-03]** Documentar no api-contracts.md que SubscriptionState é gerenciado localmente
2. **[A-04]** Atualizar data-model.md com estados IDLE/NO_GARMENT e campo errorMessage
3. **[F2-3]** Corrigir `planType.name == "PREMIUM"` → `planType == PlanType.PREMIUM` (one-liner)
4. **[F3-1]** Wiring RemoteConfig.maxFreeTrials no TryOnUseCase (remover magic number 3)
5. **[F2-5]** Adicionar certificate pinning antes de release produção
6. **[F4-1]** Criar testes para GarmentCropper e OnboardingViewModel
7. **[C-VisionLlm]** Adicionar `response_format: json_object` ao VisionRequest
8. **[C-MlKit]** Usar `.enableClassification()` + filtro "Fashion good" no GarmentCropper
9. **[C-Route/Config]** Wiring getModelRoute() e getConfig() ou marcar como deferred
10. **[C-Health]** Implementar GET /v1/health no GhostFitApi
11. **[G-03/G-04]** Alinhar UX design com spec (rewarded ads, tela Home)
12. **[H-02/H-03]** Atualizar AGENT.md (Android SDK, Robolectric)
13. **[F5-3]** Mover shareTryOnImage para Dispatchers.IO

### LOW (informativo)

1. **[A-02]** Alinhar versão exata do Compose BOM entre plan.md e quickstart.md
2. **[F3-3]** Extrair bitmapToBase64 duplicado para extension function
3. **[F3-4]** Adicionar logging nos catch-all silenciosos
4. **[F4-3/F4-4]** Criar ReferencePhotoDaoTest; renomear/melhorar OverlayServiceTest

---

*Relatório gerado pela skill `/validate`. Execute `/validate ghostfit` novamente após corrigir os issues para verificar.*
