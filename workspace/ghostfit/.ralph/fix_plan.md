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
- [ ] Unit tests for UserProfile DAO
- [x] Unit tests for PhotoStorage encryption/decryption

---

## Phase 2: Story 5 — Overlay Flutuante (P1)

- [ ] Create OverlayService.kt (Foreground Service + TYPE_APPLICATION_OVERLAY)
- [ ] Create OverlayComposable.kt (fantasminha FAB via ComposeView in WindowManager)
- [ ] Implement drag gesture for overlay positioning
- [ ] Persist overlay position across sessions
- [ ] Create ScreenCapture.kt (MediaProjection wrapper)
- [ ] Handle overlay lifecycle (start/stop from app, notification control)
- [ ] Unit tests for OverlayService state management

---

## Phase 3: Story 2 — Try-On Virtual (P1)

- [ ] Create GarmentDetector.kt (ML Kit crop + GPT-4o Vision classification)
- [ ] Create FashnApi.kt (Retrofit interface for FASHN.ai VTON)
- [ ] Create VertexAiApi.kt (Retrofit interface for Vertex AI VTON fallback)
- [ ] Create VisionLlmApi.kt (Retrofit interface for GPT-4o Vision)
- [ ] Create ModelRouter.kt (chain-of-responsibility: FASHN → Vertex AI fallback)
- [ ] Create TryOnUseCase.kt (orchestrate: capture → detect → generate → display)
- [ ] Create TryOnResultScreen.kt (display generated image)
- [ ] Handle "no garment detected" case with user-friendly message
- [ ] Unit tests for GarmentDetector
- [ ] Unit tests for ModelRouter fallback logic
- [ ] Unit tests for TryOnUseCase

---

## Phase 4: Story 3 — Interação com Resultado (P2)

- [ ] Implement "Tentar novamente" (regenerate with different seed)
- [ ] Implement "Trocar foto" (switch reference photo, regenerate)
- [ ] Implement thumbs up/down feedback
- [ ] Create GhostFitApi.kt (backend API for feedback)
- [ ] Create FeedbackRecord DTO
- [ ] Implement share via Android share sheet (with GhostFit branding)
- [ ] Unit tests for feedback submission

---

## Phase 5: Story 4 — Monetização e Limites (P2)

- [ ] Create BillingManager.kt (Google Play Billing wrapper)
- [ ] Implement daily try-on counter (3 free/day, reset at midnight local)
- [ ] Create UpgradeScreen.kt (plan options: pacote avulso, assinatura mensal)
- [ ] Integrate AdMob interstitial (between generations only)
- [ ] Handle subscription state changes (purchase → unlock)
- [ ] Unit tests for BillingManager
- [ ] Unit tests for daily limit logic

---

## Completed
- Phase 0: Project scaffolding completo (estrutura, dependencies, AppDatabase, entities, theme)
- Phase 1 parcial: UserProfile + ReferencePhoto entities e DAOs implementados
- Phase 1: WelcomeScreen + PermissionScreen implementados (overlay SYSTEM_ALERT_WINDOW + fotos)
- Phase 1: LgpdConsentScreen implementado (checkbox explícito não pré-marcado, strings PT-BR)
- Phase 1: PhotoSelectScreen implementado (Photo Picker nativo, máx 3 fotos, thumbnails)
- Phase 1: Onboarding navigation wired (NavHost: Welcome → Permissions → LGPD → PhotoSelect) com lifecycle-aware overlay permission check, LGPD consent persistence, e encrypted photo storage

## Notes
- Implementar na ordem das Phases (0 → 5)
- Cada Phase corresponde a uma User Story (exceto Phase 0 = scaffolding)
- Consultar specs/ para detalhes de cada acceptance scenario
- Target: ≥80% cobertura de testes em domain e data
- Commit atômico após cada task concluída
