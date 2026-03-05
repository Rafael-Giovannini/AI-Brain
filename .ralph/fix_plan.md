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

- [ ] Create BillingManager.kt (Google Play Billing wrapper)
- [x] Implement daily try-on counter (3 free/day, reset at midnight local)
- [ ] Create UpgradeScreen.kt (plan options: pacote avulso, assinatura mensal)
- [ ] Integrate AdMob interstitial (between generations only)
- [ ] Handle subscription state changes (purchase → unlock)
- [ ] Unit tests for BillingManager
- [ ] Unit tests for daily limit logic

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
- Fix: ModelRouter.kt missing Base64 import (this loop)
- Fix: GarmentDetectorTest.kt bitmapToBase64 → toBase64Jpeg (this loop)

## Notes
- Implementar na ordem das Phases (0 → 5)
- Cada Phase corresponde a uma User Story (exceto Phase 0 = scaffolding)
- Consultar specs/ para detalhes de cada acceptance scenario
- Target: ≥80% cobertura de testes em domain e data
- Commit atômico após cada task concluída
