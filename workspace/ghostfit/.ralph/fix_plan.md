# GhostFit MVP — Fix Plan (Ralph Task Tracker)

**Branch:** `001-ghostfit-mvp`
**Spec:** `workspace/ghostfit/specs/001-ghostfit-mvp/spec.md`
**Plan:** `workspace/ghostfit/specs/001-ghostfit-mvp/plan.md`

---

## Phase 0: Project Scaffolding

- [x] Create Android project structure (build.gradle.kts, settings.gradle.kts, AndroidManifest.xml)
- [x] Configure dependencies (Compose, Room, Retrofit, ML Kit, Tink, Billing, AdMob, Coil, Moshi)
- [x] Setup package structure: data/{local,remote,model}, domain, overlay, ui/{onboarding,tryon,subscription,theme}
- [x] Configure BuildConfig for API keys (local.properties)
- [x] Create GhostFitApp.kt (Application class with Tink init)
- [x] Create GhostFitTheme.kt (Material3 theme)
- [x] Create Room database (AppDatabase.kt) with UserProfileDao

---

## Phase 1: Story 1 — Setup Inicial e Onboarding (P1)

- [x] Create UserProfile Room entity + DAO (inclui ReferencePhoto entity + DAO, Converters, GarmentInfo, FeedbackRecord)
- [x] Implement PhotoStorage with Tink AES-256-GCM encryption
- [x] Create WelcomeScreen.kt (onboarding intro)
- [x] Create PermissionScreen.kt (overlay + photos permissions with explanations)
- [x] Create LgpdConsentScreen.kt (explicit consent checkbox, not pre-checked)
- [x] Create PhotoSelectScreen.kt (gallery picker + body validation)
- [x] Wire onboarding navigation (Welcome → Permissions → LGPD → PhotoSelect)
- [x] Unit tests for UserProfile DAO
- [x] Unit tests for PhotoStorage encryption/decryption

---

## Phase 2: Story 5 — Overlay Flutuante (P1)

- [x] Create OverlayService.kt (Foreground Service + TYPE_APPLICATION_OVERLAY)
- [x] Create OverlayComposable.kt (fantasminha FAB via ComposeView in WindowManager)
- [x] Implement drag gesture for overlay positioning
- [x] Persist overlay position across sessions
- [x] Create ScreenCapture.kt (MediaProjection wrapper)
- [x] Handle overlay lifecycle (start/stop from app, notification control)
- [x] Unit tests for OverlayService state management

---

## Phase 3: Story 2 — Try-On Virtual (P1)

- [x] Create GarmentDetector.kt (ML Kit crop via GarmentCropper interface + GPT-4o Vision classification)
- [x] Create FashnApi.kt (Retrofit interface for FASHN.ai VTON)
- [x] Create VertexAiApi.kt (Retrofit interface for Vertex AI VTON fallback)
- [x] Create VisionLlmApi.kt (Retrofit interface for GPT-4o Vision)
- [x] Create GhostFitApi.kt (Backend API: feedback, config, routing, dataset)
- [x] Create ModelRouter.kt (chain-of-responsibility: FASHN → Vertex AI fallback, 2 retries each)
- [x] Create TryOnUseCase.kt (orchestrate: capture → detect → generate → display, daily limit check)
- [x] Create TryOnResultScreen.kt (display generated image + loading/error/no-garment states)
- [x] Handle "no garment detected" case with user-friendly message (no attempt consumed)
- [x] Unit tests for GarmentDetector (6 tests: classification, fallback, markdown JSON, categories)
- [x] Unit tests for ModelRouter fallback logic (8 tests: primary, fallback, both-fail, category mapping)
- [x] Unit tests for TryOnUseCase (6 tests: full pipeline, no-garment, daily limit, no-ref-photo, errors)

---

## Phase 4: Story 3 — Interação com Resultado (P2)

- [x] Implement "Tentar novamente" (regenerate with different seed) — TryOnUseCase.regenerate()
- [x] Implement "Trocar foto" (switch reference photo, regenerate) — TryOnResultScreen onChangePhoto
- [x] Implement thumbs up/down feedback — TryOnUseCase.submitFeedback() + TryOnResultScreen UI
- [x] Implement share via Android share sheet (with GhostFit branding) — ShareUtils.kt
- [x] Unit tests for feedback submission — FeedbackSubmissionTest.kt (8 tests)
- [ ] Unit tests for regenerate/swap photo (FR-010, FR-011)
- [ ] Unit tests for share (FR-013)

---

## Phase 5: Story 4 — Monetização e Limites (P2)

- [x] Create BillingManager.kt (Google Play Billing wrapper)
- [x] Implement daily try-on counter (3 free/day, reset at midnight local) — TryOnUseCase.checkDailyLimit/incrementDailyTries
- [x] Create UpgradeScreen.kt (plan options: pacote avulso, assinatura mensal)
- [x] Create AdManager.kt (interstitial between generations, SHOW_AD_AFTER_GENERATIONS=2)
- [ ] Integrate AdMob interstitial end-to-end (wire AdManager into overlay/result flow)
- [x] Handle subscription state changes (purchase → unlock)
- [x] Unit tests for BillingManager — BillingManagerTest.kt (20+ tests)
- [x] Unit tests for daily limit logic — DailyLimitTest.kt (18+ tests)
- [x] Unit tests for AdManager — AdManagerTest.kt (9 tests)

---

## Phase 6: Migrar Vision LLM — Gemini 2.0 Flash (primario) + GPT-4o (fallback)

**Contexto:** A classificacao de roupa usa GPT-4o Vision (~$0.001/chamada).
Gemini 2.0 Flash faz o mesmo por ~$0.00001/chamada — 100x mais barato.
Queremos Gemini como primario e GPT-4o como fallback automatico caso Gemini falhe.

**Gemini API Key:** Google AI Studio (https://aistudio.google.com) > Get API Key.
Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent`
Docs: https://ai.google.dev/gemini-api/docs/vision

**Arquitetura da deteccao apos migracao:**
```
ML Kit (on-device crop) → Gemini 2.0 Flash (primario, 5s timeout)
                            ↓ falha?
                          GPT-4o Vision (fallback, 5s timeout)
                            ↓ falha?
                          Default: category="top", confidence=0.5
```

**Tasks:**
- [x] Criar `GeminiVisionApi.kt` em `data/remote/` (Retrofit interface)
  - POST `v1beta/models/gemini-2.0-flash:generateContent`
  - Header: `x-goog-api-key: {GEMINI_API_KEY}` (via BuildConfig)
  - Request body:
    ```json
    {
      "contents": [{
        "parts": [
          { "text": "Analise esta imagem de roupa. Retorne JSON: {\"category\": \"top|bottom|dress|outerwear\", \"color\": \"<cor>\", \"description\": \"<1 frase>\", \"confidence\": 0.0-1.0}" },
          { "inline_data": { "mime_type": "image/jpeg", "data": "<base64>" } }
        ]
      }],
      "generationConfig": { "responseMimeType": "application/json" }
    }
    ```
  - Response: `candidates[0].content.parts[0].text` → parse como JSON (mesmo formato GarmentClassification)
  - Timeout: 5 segundos
  - OkHttpClient com connectTimeout=5s, readTimeout=5s
- [x] Adicionar `GEMINI_API_KEY` ao `build.gradle.kts` (BuildConfig, mesmo padrao das outras keys)
- [x] Adicionar `GEMINI_API_KEY=placeholder` ao `local.properties`
- [x] Refatorar `GarmentDetector.kt` para chain de vision providers:
  - Receber AMBOS `GeminiVisionApi` e `VisionLlmApi` no construtor
  - No `classifyWithVisionLlm()`: tentar Gemini primeiro → se Exception, tentar GPT-4o → se Exception, fallback default
  - Manter EXATAMENTE o mesmo output: `GarmentClassification` (category, color, description, confidence)
  - Manter threshold >= 0.6
  - Log.d() indicando qual provider foi usado (para debug)
- [x] NAO deletar `VisionLlmApi.kt` — manter intacto como fallback
  - Se `OPENAI_API_KEY` estiver vazio/placeholder, pular o fallback GPT-4o (ir direto pro default)
- [x] Atualizar testes em `GarmentDetectorTest.kt`:
  - Teste: Gemini sucesso → retorna resultado Gemini
  - Teste: Gemini falha, GPT-4o sucesso → retorna resultado GPT-4o
  - Teste: Ambos falham → retorna default "top"
  - Manter testes existentes de parsing/markdown/categories
- [x] Atualizar docs:
  - `SETUP.md`: GEMINI_API_KEY como obrigatorio, OPENAI_API_KEY como opcional/fallback
  - `local.properties`: adicionar GEMINI_API_KEY
  - `api-contracts.md`: adicionar secao 1.5 Gemini Vision API
  - `research.md`: atualizar decisao de deteccao com nova cadeia

---

## Phase 7: Correcoes da Validacao (/validate report 2026-03-05)

**Contexto:** O `/validate ghostfit` identificou 1 CRITICAL, 6 HIGH, 11 MEDIUM e 5 LOW issues.
Estas tasks endereçam os issues que Ralph pode corrigir autonomamente (codigo + testes + docs).
**Validation report:** `workspace/ghostfit/specs/001-ghostfit-mvp/validation-report.md`

### 7A. CRITICAL — Pipeline end-to-end (overlay → try-on)

O toque no fantasminha exibe Toast stub em vez de acionar o pipeline real.
OverlayService.kt:138-144 tem `Toast.makeText("Captura de tela — em breve!")`.
O pipeline (ScreenCapture → GarmentDetector → ModelRouter → TryOnResultScreen) existe
isoladamente mas NAO esta wired no fluxo do overlay.

**Problema tecnico:** MediaProjection requer consentimento via Activity result antes de
ScreenCapture funcionar. O OverlayService (Foreground Service) nao tem Activity context.
**Solucao:** MainActivity deve obter o MediaProjection intent result e passar para o
OverlayService via Intent extra ou singleton. O OverlayService entao usa ScreenCaptureProvider.

- [ ] Criar fluxo de consentimento MediaProjection em MainActivity (Activity result launcher)
  - Ao iniciar overlay, solicitar MediaProjection consent se nao concedido
  - Armazenar resultCode + data do MediaProjection para uso pelo OverlayService
  - Passar via Intent extra ou ScreenCaptureProvider singleton
- [ ] Substituir Toast stub em OverlayService.kt:138-144 por chamada real ao pipeline:
  - `onTap` deve chamar `screenCapture.capture()` → `garmentDetector.detect()` → `modelRouter.generate()`
  - Exibir resultado via overlay card ou abrir TryOnResultScreen via Intent
  - Se geracao em andamento, mostrar toast "Geracao em andamento..." e ignorar toque (edge case da spec)
  - Se nenhuma roupa detectada, mostrar overlay card com mensagem (nao consumir tentativa)
- [ ] Testes: verificar que onTap aciona pipeline (mock ScreenCaptureProvider + GarmentDetector)

### 7B. HIGH — Violacoes de arquitetura (domain layer)

BillingManager.kt e AdManager.kt estao no package `domain/` mas importam SDKs Android
(BillingClient, AdMob, Activity, Context). Domain nao deve depender de infraestrutura.

- [x] Extrair interface `BillingProvider` em `domain/BillingProvider.kt`:
  ```kotlin
  interface BillingProvider {
      val productDetails: StateFlow<Map<String, Any>>
      val isConnected: StateFlow<Boolean>
      val purchaseEvent: StateFlow<PurchaseEvent?>
      fun connect()
      fun disconnect()
      suspend fun queryProducts()
      fun launchPurchaseFlow(activity: Any, productId: String): Any
      suspend fun restorePurchases()
      fun consumePurchaseEvent()
  }
  ```
  - Mover `BillingManager.kt` para `data/billing/BillingManagerImpl.kt`
  - BillingManagerImpl implementa BillingProvider
  - Atualizar todos os call sites (UpgradeScreen, MainActivity) para usar interface
  - Manter PurchaseEvent e PurchaseType no domain (sao domain models)
- [x] Extrair interface `AdProvider` em `domain/AdProvider.kt`:
  ```kotlin
  interface AdProvider {
      fun initialize(context: Any, adUnitId: String? = null)
      fun onGenerationCompleted()
      suspend fun shouldShowAd(): Boolean
      suspend fun showAdIfNeeded(activity: Any, onComplete: () -> Unit)
      fun resetSessionCount()
  }
  ```
  - Mover `AdManager.kt` para `data/ads/AdManagerImpl.kt`
  - AdManagerImpl implementa AdProvider
  - Atualizar call sites para usar interface
- [x] Testes: atualizar BillingManagerTest e AdManagerTest para testar via interface (imports atualizados para BillingManagerImpl/AdManagerImpl + BillingProvider/AdProvider constants)

### 7C. HIGH — Seguranca: gcpAccessToken vazio

ModelRouter.kt:21 aceita `gcpAccessToken: String = ""` como default.
Bearer auth com string vazia falha silenciosamente no retry loop (catch swallows all).

- [ ] Adicionar validacao no construtor de ModelRouter:
  ```kotlin
  init {
      require(gcpAccessToken.isNotBlank()) { "GCP access token must not be blank" }
  }
  ```
  - Atualizar testes em ModelRouterTest: adicionar teste que verifica IllegalArgumentException com token vazio
  - Verificar que todos os call sites passam token valido (ou BuildConfig value)

### 7D. HIGH — Silent exceptions (logging)

7+ locais com `catch (_: Exception)` que descartam erros sem logging.
Dificulta debug em producao.

- [ ] Adicionar `android.util.Log.w(TAG, "descricao", e)` nos seguintes catches:
  - `TryOnUseCase.kt:218` — submitFeedback fire-and-forget (Log.w, nao crashar)
  - `ModelRouter.kt:81` — tryFashn retry (Log.d para retry, Log.w para ultimo attempt)
  - `ModelRouter.kt:114` — tryVertex retry (mesmo padrao)
  - `ModelRouter.kt:125` — base64ToBitmap decode failure (Log.w)
  - `GarmentDetector.kt:82` — classifyWithVisionLlm fallback (Log.w)
  - `ShareUtils.kt:35` — sharing failed (Log.e, share e acao do usuario)
  - `OverlayService.kt:159` — removeView already removed (Log.d, esperado)
  - Adicionar `companion object { private const val TAG = "ClassName" }` onde nao existir

### 7E. HIGH — Testes faltantes (FR-011, FR-013)

FR-011 (trocar foto) e FR-013 (share) existem no codigo mas nao tem testes dedicados.

- [x] Criar testes para FR-011 (trocar foto de referencia) em TryOnUseCaseTest.kt:
  - Teste: regenerate com referencePhotoId diferente usa foto correta
  - Teste: regenerate sem foto de referencia retorna erro
  - Teste: regenerate conta como tentativa adicional
- [x] Criar testes para FR-013 (share) em novo `ShareUtilsTest.kt`:
  - Teste: shareTryOnImage cria arquivo temporario no cache
  - Teste: shareTryOnImage cria Intent com ACTION_SEND e tipo image/jpeg
  - Teste: shareTryOnImage inclui texto com branding GhostFit
  - Teste: shareTryOnImage com bitmap null/invalido nao crasha (catch)

### 7F. HIGH — Documentar bonusTries no data-model

UserProfile.kt:19 tem campo `bonusTries: Int = 0` usado ativamente por BillingManager
(+10 por pack) e TryOnUseCase (consumido antes de dailyTriesUsed), mas NAO esta
documentado no data-model.md.

- [ ] Adicionar campo `bonusTries` ao data-model.md na tabela UserProfile:
  ```markdown
  | `bonusTries` | Int | >= 0, default 0 | Tentativas bonus de pacote avulso |
  ```
  - Adicionar regra de negocio: "Consumido ANTES de dailyTriesUsed. Incrementado em +10 por compra de pacote (PACK). Usuarios PREMIUM ignoram este campo."
  - Adicionar na transicao de estado: `bonusTries += PACK_TRIES_COUNT (10) via Google Play Billing pack purchase`

### 7G. MEDIUM — Codigo duplicado em TryOnUseCase

Logica de decriptacao de foto duplicada entre `execute()` (linhas 85-95) e
`regenerate()` (linhas 157-163). Mesmo padrao: buscar foto → decrypt → toBase64.

- [ ] Extrair metodo privado em TryOnUseCase.kt:
  ```kotlin
  private suspend fun getActiveReferenceBase64(): Pair<String, String> {
      val profile = userProfileDao.getProfile() ?: throw IllegalStateException("No profile")
      val refPhoto = referencePhotoDao.getByUser(profile.id).firstOrNull()
          ?: throw IllegalStateException("Nenhuma foto de referencia encontrada")
      val bitmap = photoStorage.decrypt(refPhoto.encryptedFilePath)
          ?: throw IllegalStateException("Falha ao decifrar foto de referencia")
      return Pair(refPhoto.id, bitmap.toBase64Jpeg())
  }
  ```
  - Usar em execute() e regenerate() substituindo o codigo duplicado
  - Testes existentes devem continuar passando (refactor puro)

### 7H. MEDIUM — AdManager integracao end-to-end

AdManager existe como classe mas NAO esta wired no fluxo do overlay/resultado.
FR-016 precisa de integracao real.

- [ ] Integrar AdManager no fluxo de try-on:
  - Instanciar AdManager no GhostFitApp.kt ou MainActivity (singleton)
  - Chamar `adManager.initialize(context)` no onCreate da Activity
  - Chamar `adManager.onGenerationCompleted()` apos cada try-on bem-sucedido (status DONE)
  - Chamar `adManager.showAdIfNeeded(activity) { /* proceed */ }` ANTES de iniciar nova geracao
  - Garantir que anuncio aparece ENTRE geracoes, NUNCA antes do resultado (spec FR-016)
- [ ] Teste de integracao: verificar que apos SHOW_AD_AFTER_GENERATIONS (2) geracoes, shouldShowAd() retorna true

### 7I. MEDIUM — Atualizar docs da spec para consistencia

Divergencias menores entre documentos que devem ser alinhadas.

- [ ] Atualizar `data-model.md`: adicionar nota explicitando que TryOnSession e efemera (100% em memoria, sem Room)
- [ ] Atualizar `plan.md`: remover `tasks.md` da estrutura de diretorios (arquivo nao existe) OU gerar via `/speckit.tasks`
- [ ] Atualizar `plan.md` ou `api-contracts.md`: documentar que genTimeout=20000 e o timeout maximo, e 15s e o target de UX

---

## Completed
- Phase 0: Project scaffolding completo (estrutura, dependencies, AppDatabase, entities, theme)
- Phase 1: Onboarding completo — UserProfile, ReferencePhoto, DAOs, PhotoStorage (Tink AES-256-GCM)
- Phase 1: WelcomeScreen + PermissionScreen + LgpdConsentScreen + PhotoSelectScreen
- Phase 1: Onboarding navigation wired (NavHost: Welcome → Permissions → LGPD → PhotoSelect)
- Phase 1: UserProfileDao unit tests (10 testes), PhotoStorage tests (12 testes)
- Phase 2: OverlayService + OverlayComposable + ScreenCapture (drag + posição persistida)
- Phase 2: 7 unit tests para overlay
- Phase 3: GarmentDetector + FashnApi + VertexAiApi + VisionLlmApi + GhostFitApi
- Phase 3: ModelRouter + TryOnUseCase + TryOnResultScreen (pipeline completo)
- Phase 3: 20 unit tests (GarmentDetector 6, ModelRouter 8, TryOnUseCase 6)
- Phase 4: TryOnResultScreen com regenerate, trocar foto, feedback thumbs, share (ShareUtils.kt)
- Phase 4: FeedbackSubmissionTest.kt (8 testes)
- Phase 5: BillingManager + UpgradeScreen + AdManager + daily limit logic
- Phase 5: BillingManagerTest (20+), DailyLimitTest (18+), AdManagerTest (9 testes)

## Notes
- Implementar na ordem das Phases (0 → 5)
- Cada Phase corresponde a uma User Story (exceto Phase 0 = scaffolding)
- Consultar specs/ para detalhes de cada acceptance scenario
- Target: ≥80% cobertura de testes em domain e data
- Commit atômico após cada task concluída
