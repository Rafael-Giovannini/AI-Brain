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

- [x] Create UserProfile Room entity + DAO
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

- [x] Create GarmentDetector.kt (ML Kit crop + GPT-4o Vision classification)
- [x] Create FashnApi.kt (Retrofit interface for FASHN.ai VTON)
- [x] Create VertexAiApi.kt (Retrofit interface for Vertex AI VTON fallback)
- [x] Create VisionLlmApi.kt (Retrofit interface for GPT-4o Vision)
- [x] Create ModelRouter.kt (chain-of-responsibility: FASHN → Vertex AI fallback)
- [x] Create TryOnUseCase.kt (orchestrate: capture → detect → generate → display)
- [x] Create TryOnResultScreen.kt (display generated image)
- [x] Handle "no garment detected" case with user-friendly message
- [x] Unit tests for GarmentDetector
- [x] Unit tests for ModelRouter fallback logic
- [x] Unit tests for TryOnUseCase

---

## Phase 4: Story 3 — Interação com Resultado (P2)

- [x] Implement "Tentar novamente" (regenerate with different seed)
- [x] Implement "Trocar foto" (switch reference photo, regenerate)
- [x] Implement thumbs up/down feedback
- [x] Create GhostFitApi.kt (backend API for feedback)
- [x] Create FeedbackRecord DTO
- [x] Implement share via Android share sheet (with GhostFit branding)
- [x] Unit tests for feedback submission

---

## Phase 5: Story 4 — Monetização e Limites (P2)

- [x] Create BillingManager.kt (Google Play Billing wrapper)
- [x] Implement daily try-on counter (3 free/day, reset at midnight local)
- [x] Create UpgradeScreen.kt (plan options: pacote avulso, assinatura mensal)
- [x] Integrate AdMob interstitial (between generations only)
- [x] Handle subscription state changes (purchase → unlock)
- [x] Unit tests for BillingManager
- [x] Unit tests for daily limit logic

---

## Phase 6: Integration — Wire Screen Capture to Overlay (FR-005)

- [x] Create MediaProjectionHolder singleton (bridge Activity↔Service)
- [x] Create TryOnActivity (fullscreen pipeline: capture → detect → generate → display)
- [x] Create TryOnSessionHolder (share TryOnSession state between components)
- [x] Update MainActivity — request MediaProjection after onboarding
- [x] Update OverlayService — wire tap to launch TryOnActivity
- [x] Register TryOnActivity in AndroidManifest.xml
- [x] Unit tests for MediaProjectionHolder

---

## Phase 7: Uncovered FRs — Smart Routing & Data Pipeline

- [x] FR-020: Implement smart model routing in ModelRouter — query backend `GET /v1/models/route` for recommended model order based on approval scores, fallback to default FASHN→Vertex if backend unavailable
- [x] FR-020/FR-021: Wire GhostFitApi into ModelRouter construction in TryOnActivity/MainActivity (DI integration)

---

## Phase 7A: End-to-End Pipeline Wiring (CRITICAL)

- [x] Wire end-to-end pipeline in OverlayService onTap — replace activity-launch-only with full pipeline: ScreenCapture.capture → GarmentDetector.detect → ModelRouter.generate → launch TryOnActivity with result via TryOnSessionHolder
- [x] Handle edge case: generation in progress → toast "Geracao em andamento..." (generationInProgress flag)
- [x] Handle edge case: no garment detected → show toast without consuming daily attempt
- [x] Handle edge case: pipeline error → show toast with error message
- [x] Add TryOnActivity display-only mode (EXTRA_DISPLAY_ONLY) — reads pre-computed session from TryOnSessionHolder, regenerate/feedback still functional
- [x] Unit tests for onTap pipeline trigger (OverlayPipelineTest — 13 tests covering guards, session state, status transitions)

---

## Completed
- Phase 0: Project Scaffolding (all 7 tasks — Loop 1)
- Phase 1 Task 1: UserProfile entity + DAO (done in Phase 0)
- Phase 1 Task 2: PhotoStorage with Tink AES-256-GCM encryption (Loop 2)
- Phase 1 Task 9: PhotoStorage unit tests (Loop 2)
- Phase 1 Task 3: WelcomeScreen.kt onboarding intro (Loop 3)
- Phase 1 Tasks 4-8: PermissionScreen, LgpdConsentScreen, PhotoSelectScreen, navigation, UserProfileDaoTest (prior loops)
- Phase 2: All overlay tasks (OverlayService, OverlayComposable, ScreenCapture, tests — prior loops)
- Phase 3: All try-on tasks (GarmentDetector, APIs, ModelRouter, TryOnUseCase, TryOnResultScreen, tests — prior loops)
- Phase 4 Tasks 1-6: Result interaction (regenerate, swap photo, feedback, GhostFitApi, FeedbackRecord, share — prior loops)
- Phase 4 Task 7: Feedback submission unit tests (this loop)
- Phase 5 Task 2: Daily try-on counter (implemented in TryOnUseCase — prior loops)
- Phase 5 Task 1: BillingManager.kt + SubscriptionState entity + DAO + tests (this loop)
- Phase 5 Task 3: UpgradeScreen.kt with monthly subscription + pack options, wired into navigation (this loop)
- Fix: ModelRouter.kt missing Base64 import (prior loop)
- Fix: GarmentDetectorTest.kt bitmapToBase64 → toBase64Jpeg (prior loop)
- Phase 5 Task 7: DailyLimitTest.kt — 15 tests covering free limit enforcement, premium bypass, midnight reset, counter increment, regenerate limits (this loop)
- Phase 5 Task 4: AdManager.kt — AdMob interstitial integration (FR-016). Shows ads between generations after 2nd try-on, never before result. Premium users exempt. AdManagerTest.kt with 9 tests. BuildConfig.ADMOB_INTERSTITIAL_ID added (this loop)
- Phase 5 Task 5: Handle subscription state changes (purchase → unlock). Added PurchaseEvent sealed class (Success/Error/Cancelled) + purchaseEvent StateFlow to BillingManager. UpgradeScreen observes events and shows PurchaseSuccessScreen on successful purchase. MainActivity wired. 6 new tests in BillingManagerTest (this loop)
- Fix: TryOnUseCaseTest + DailyLimitTest — changed ScreenCapture (final class) → ScreenCaptureProvider (fun interface) to fix Mockito mocking failure. Removed unused imports in FeedbackSubmissionTest and TryOnResultScreen (this loop)
- Phase 6: Wire screen capture to overlay tap (FR-005). Created MediaProjectionHolder, TryOnActivity, TryOnSessionHolder. Updated MainActivity (MediaProjection request after onboarding), OverlayService (tap launches TryOnActivity). Registered TryOnActivity in AndroidManifest. 6 tests in MediaProjectionHolderTest (this loop)
- Phase 7 Task 1: FR-020 smart model routing — ModelRouter now accepts optional GhostFitApi, queries `getModelRoute(category)` for backend-recommended model ordering based on approval scores. Falls back to FASHN→Vertex default if backend unavailable or returns null. 5 new tests in ModelRouterTest (this loop)
- Phase 7 Task 2: FR-020/FR-021 DI integration — Wired GhostFitApi.create() into TryOnActivity, passed to both ModelRouter (smart routing) and TryOnUseCase (feedback/dataset). Fixed pre-existing compilation errors: BillingManager (onBillingSetupFinished, Billing 8.x queryProductDetails ktx, PendingPurchasesParams), ScreenCapture (override modifier), BillingManagerTest (stub dispatch fix). All 109 tests pass (this loop)
- Phase 7A: End-to-end pipeline wiring — OverlayService.launchTryOn() now runs full pipeline (capture→detect→generate) in service coroutine while e-commerce app is visible, then launches TryOnActivity in display-only mode. Edge cases: generationInProgress guard, no garment toast, error toast. TryOnActivity supports EXTRA_DISPLAY_ONLY with no-op ScreenCaptureProvider for regenerate/feedback. 13 new tests in OverlayPipelineTest (this loop)

## Notes
- Implementar na ordem das Phases (0 → 5)
- Cada Phase corresponde a uma User Story (exceto Phase 0 = scaffolding)
- Consultar specs/ para detalhes de cada acceptance scenario
- Target: ≥80% cobertura de testes em domain e data
- Commit atômico após cada task concluída
