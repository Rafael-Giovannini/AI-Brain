# Validation Report: ghostfit / 001-ghostfit-mvp

**Generated**: 2026-03-04
**Workspace**: ghostfit
**Feature**: 001-ghostfit-mvp
**Validator**: `/validate ghostfit`

---

## Executive Summary

| Metric | Value |
|--------|-------|
| Spec Doc Consistency Issues | 3 |
| BMAD Docs vs Spec Issues | 6 |
| Ralph Config Issues | 1 |
| Entities: Defined / Implemented | 7 / 4 |
| API Endpoints: Defined / Implemented | 7 / 0 |
| FRs Traced to Code | 0 / 21 (0%) |
| Acceptance Scenarios with Tests | 0 / 28 (0%) |
| Code Quality Issues | 2 |
| Critical Issues | 0 |
| High Issues | 5 |
| Medium Issues | 4 |
| Low Issues | 3 |

**Stage**: Early implementation (Phase 0 scaffolding complete). No source code beyond data model entities and project setup. No tests exist. This is expected — focus should be on fixing doc inconsistencies before proceeding with implementation.

---

## 1. Spec Doc Consistency (Pass A)

| ID | Severity | Location | Finding | Recommendation |
|----|----------|----------|---------|----------------|
| A-01 | **HIGH** | spec.md (FR-012, Clarifications) vs api-contracts.md | Spec references "NanoBanana primário (10s) → Grok fallback (15s)" as VTON models. api-contracts.md uses FASHN.ai (10s) → Vertex AI (15s). Model names are inconsistent — the technical docs evolved after research phase but spec was not updated. | Update spec.md FR-012 and Clarifications section to reflect FASHN.ai / Vertex AI as the chosen VTON providers per research.md decisions. |
| A-02 | **MEDIUM** | data-model.md vs api-contracts.md | data-model.md defines `TryOnSession.modelUsed` as `"fashn" \| "vertex"` and `FeedbackRecord.modelUsed` as `"fashn" \| "vertex"`. api-contracts backend also uses `"fashn" \| "vertex"`. Internally consistent ✓, but inconsistent with spec.md which says NanoBanana/Grok. | Resolve by updating spec.md to use "fashn"/"vertex" terminology. |
| A-03 | **LOW** | plan.md vs data-model.md | plan.md lists `tasks.md` in project structure (`workspace/ghostfit/specs/001-ghostfit-mvp/tasks.md`) but this file does not exist yet. | Generate tasks.md via `/speckit.tasks` or remove from planned structure until ready. |

---

## 2. Entity Coverage Matrix (Pass B)

| Entity | Code File | Status | Missing Fields | Extra Fields |
|--------|-----------|--------|----------------|--------------|
| UserProfile | `data/model/UserProfile.kt` | **COMPLETE** | — | — |
| ReferencePhoto | `data/model/ReferencePhoto.kt` | **COMPLETE** | — | — |
| TryOnSession | — | **MISSING** | All (ephemeral, ViewModel-based — expected for later phase) | — |
| GarmentInfo | `data/model/GarmentInfo.kt` | **COMPLETE** | — | — |
| FeedbackRecord | `data/model/FeedbackRecord.kt` | **COMPLETE** | — | — |
| SubscriptionState | — | **MISSING** | All (Phase 5 — monetização) | — |
| ModelScore | — | **MISSING** | All (backend entity — expected) | — |

**Notes**: 4/7 entities implemented. Missing entities correspond to later implementation phases (TryOnSession = Phase 3, SubscriptionState = Phase 5, ModelScore = backend). Room DB includes UserProfile + ReferencePhoto with proper DAO implementations.

---

## 3. API Contract Coverage (Pass C)

| Endpoint | Code File | Status | Notes |
|----------|-----------|--------|-------|
| POST FASHN.ai /v1/run | — | **MISSING** | Phase 3 — Story 2 |
| POST Vertex AI predict | — | **MISSING** | Phase 3 — Story 2 |
| POST OpenAI /v1/chat/completions (Vision) | — | **MISSING** | Phase 3 — Story 2 |
| ML Kit Object Detection (on-device) | — | **MISSING** | Phase 3 — Story 2 |
| POST /v1/feedback | — | **MISSING** | Phase 4 — Story 3 |
| GET /v1/models/route | — | **MISSING** | Phase 4 — Story 3 |
| GET /v1/health | — | **MISSING** | Phase 4 — Story 3 |

**Notes**: 0/7 endpoints implemented. Expected — project is at Phase 0 scaffolding stage.

---

## 4. FR Traceability (Pass D)

| FR | Description | In Code? | In Tests? | Status |
|----|-------------|----------|-----------|--------|
| FR-001 | Cadastro de fotos de corpo inteiro | No | No | **UNTRACED** |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | No | No | **UNTRACED** |
| FR-003 | Consentimento LGPD explícito | No | No | **UNTRACED** |
| FR-004 | Overlay flutuante persistente | No | No | **UNTRACED** |
| FR-005 | Captura de tela ao toque | No | No | **UNTRACED** |
| FR-006 | Reposicionamento do overlay | No | No | **UNTRACED** |
| FR-007 | Detecção de roupa via IA | No | No | **UNTRACED** |
| FR-008 | Mensagem "Nenhuma roupa detectada" | No | No | **UNTRACED** |
| FR-009 | Geração de imagem try-on | No | No | **UNTRACED** |
| FR-010 | Regeneração (tentar novamente) | No | No | **UNTRACED** |
| FR-011 | Trocar foto de referência | No | No | **UNTRACED** |
| FR-012 | Fallback entre modelos de IA | No | No | **UNTRACED** |
| FR-013 | Compartilhamento com branding | No | No | **UNTRACED** |
| FR-014 | Feedback thumbs up/down | No | No | **UNTRACED** |
| FR-015 | Limite de 3 tentativas/dia | No | No | **UNTRACED** |
| FR-016 | Exibição de anúncios | No | No | **UNTRACED** |
| FR-017 | Pacotes pagos / assinatura | No | No | **UNTRACED** |
| FR-018 | Google Play Billing | No | No | **UNTRACED** |
| FR-019 | Coleta de dados para dataset | No | No | **UNTRACED** |
| FR-020 | Roteamento inteligente | No | No | **UNTRACED** |
| FR-021 | Pipeline de dados fine-tuning | No | No | **UNTRACED** |

**Notes**: 0/21 FRs traced. Expected — only scaffolding exists. Data model entities support future FR implementation.

---

## 5. Acceptance Scenario Coverage (Pass E)

| Story | Scenarios | Covered | Status |
|-------|-----------|---------|--------|
| Story 1 — Setup Inicial e Onboarding | 6 | 0 | **UNCOVERED** |
| Story 2 — Try-On Virtual | 6 | 0 | **UNCOVERED** |
| Story 3 — Interação com Resultado | 5 | 0 | **UNCOVERED** |
| Story 4 — Monetização e Limites | 6 | 0 | **UNCOVERED** |
| Story 5 — Overlay Flutuante | 5 | 0 | **UNCOVERED** |
| **Total** | **28** | **0** | **0%** |

**Notes**: No test files exist in `workspace/ghostfit/tests/` or `workspace/ghostfit/android/app/src/test/`. Expected at Phase 0.

---

## 6. Code Quality (Pass F)

| File | Category | Severity | Finding | Recommendation |
|------|----------|----------|---------|----------------|
| `ui/theme/GhostFitTheme.kt` | Naming/Design | **MEDIUM** | Primary color `GhostPurple = Color(0xFF7C4DFF)` does not match UX design token `primary-600: #7C3AED`. The theme will render a different purple than specified. | Change to `Color(0xFF7C3AED)` to match UX design spec. Also update `GhostPurpleLight` and `GhostPurpleDark` to match the palette (`#A78BFA` and `#4C1D95` respectively). |
| `GhostFitApp.kt` | Architecture | **LOW** | Uses `TinkConfig.register()` (general Tink init) but plan.md specifies `StreamingAead` specifically and architecture doc says `EncryptedFile` API. These are compatible but `TinkConfig.register()` registers ALL primitives which is heavier than needed. | Consider using `StreamingAeadConfig.register()` for lighter footprint, or keep as-is if other Tink primitives may be needed. |

**Notes**: Only 11 source files exist. Code quality is clean — proper Room annotations, ForeignKey constraints, type converters, and singleton pattern for database. No security issues, no hardcoded secrets, no code smells detected. Architecture layers (data/model, data/local) follow the planned structure.

---

## 7. BMAD Docs vs Specs (Pass G)

| ID | Severity | Docs File | Spec File | Finding | Recommendation |
|----|----------|-----------|-----------|---------|----------------|
| G-01 | **HIGH** | `prd-ghostfit` (FR-009, FR-012) | `spec.md` (FR-009, FR-012) | PRD references "NanoBanana/Grok" and "Stable Diffusion" as VTON models with "20s timeout per provider". Spec also says NanoBanana/Grok. But plan.md and api-contracts.md settled on FASHN.ai/Vertex AI with 10s/15s timeouts after research phase. | Update PRD FR-009 and FR-012 descriptions to reference FASHN.ai/Vertex AI. Update timeouts to 10s/15s. Remove Stable Diffusion reference. |
| G-02 | **HIGH** | `architecture-ghostfit` (Section 5.1) | `data-model.md` | Architecture defines different entity names: `UserConsent`, `UserPhoto`, `TrialCounter`, `OverlayPosition`, `TryOnFeedback`. data-model.md (and code) uses: `UserProfile`, `ReferencePhoto`, `TryOnSession`, `FeedbackRecord`, `SubscriptionState`. Completely different schema structure. | Align architecture doc entities with data-model.md. The data-model.md is more recent and already implemented in code — architecture should be updated to match. |
| G-03 | **HIGH** | `architecture-ghostfit` (Section 6.2) | `api-contracts.md` | Architecture backend API has different endpoints: `POST /feedback`, `GET /config`, `GET /model-scores`, `POST /dataset`. api-contracts.md defines: `POST /v1/feedback`, `GET /v1/models/route`, `GET /v1/health`. Different paths, different endpoints (no /config or /dataset in contracts, no /health in architecture). | Reconcile backend API definitions. api-contracts.md should be the source of truth — update architecture to match, or add missing endpoints to api-contracts.md. |
| G-04 | **HIGH** | `architecture-ghostfit` (Section 3) | `plan.md` | Architecture specifies Kotlin 2.0+, JUnit 5, Kotlinx Serialization, EncryptedFile+EncryptedSharedPrefs. plan.md specifies Kotlin 1.9+, JUnit 4, Moshi (implied by Retrofit), Tink StreamingAead. Different versions and libraries. | Decide on authoritative tech stack. plan.md + PROMPT.md + AGENT.md all agree on Kotlin 1.9+/JUnit 4/Tink — update architecture to match, or vice versa. |
| G-05 | **MEDIUM** | `architecture-ghostfit` (Section 6.1) | `api-contracts.md` | Architecture vision detection uses Gemini Flash + OpenAI GPT-4o Vision (both remote APIs). api-contracts.md and plan.md use ML Kit (on-device) + GPT-4o Vision (two-stage: local crop then remote classification). Architecture misses the ML Kit on-device step. | Update architecture Section 6.1 to include ML Kit on-device detection as first stage before Vision LLM classification. |
| G-06 | **LOW** | `product-brief-ghostfit` | `plan.md` | Product brief and PRD mention "Google Fotos API" for photo selection. plan.md research notes that Android Photo Picker (native) is simpler and doesn't require Google Photos API OAuth. Decision not fully reflected in PRD. | Update product-brief and PRD to note that native Android Photo Picker will be used (per research.md decision), not Google Photos API. |

---

## 8. Ralph Config (Pass H)

| ID | Severity | File | Finding | Recommendation |
|----|----------|------|---------|----------------|
| H-01 | **MEDIUM** | `.ralph/PROMPT.md` | PROMPT.md line 43 says priority order "Story 1 → Story 5 → Story 2 → Story 3 → Story 4" but fix_plan.md phases are ordered "Phase 0 (scaffolding) → Phase 1 (Story 1) → Phase 2 (Story 5) → Phase 3 (Story 2) → Phase 4 (Story 3) → Phase 5 (Story 4)". The priority order is consistent between them. PROMPT.md also says "JUnit 4" which matches plan.md but not architecture doc (JUnit 5). | No action needed for priority order (they match). For JUnit version, ensure architecture doc is updated to match JUnit 4 (per plan.md consensus). |
| H-02 | **LOW** | `.ralphrc` | ALLOWED_TOOLS includes `Bash(cat *)` which is redundant since the Read tool is available and preferred. | Remove `Bash(cat *)` from ALLOWED_TOOLS to encourage use of the Read tool. |

**Notes**: Ralph config is well-structured and internally consistent. .ralphrc, PROMPT.md, AGENT.md, and fix_plan.md all align with each other and with plan.md/data-model.md. The primary inconsistencies are inherited from the architecture doc divergence (G-04).

---

## Next Actions

### Critical (must fix before continuing)

*(none)*

### High (fix soon — before Phase 1 implementation)

- **[G-01, A-01, A-02]** Reconcile VTON model names across ALL docs: spec.md, PRD, product-brief still say "NanoBanana/Grok" but technical docs (plan.md, api-contracts.md, research.md) chose FASHN.ai/Vertex AI. **Action**: Update spec.md FR-012, Clarifications section, and PRD FR-009/FR-012 to use FASHN.ai/Vertex AI.
- **[G-02]** Align architecture entity names with data-model.md (UserConsent→UserProfile, UserPhoto→ReferencePhoto, etc.). Architecture doc Section 5.1 needs rewrite to match implemented data model.
- **[G-03]** Reconcile architecture backend API endpoints with api-contracts.md. Add missing endpoints (/config, /dataset) to api-contracts.md or remove from architecture.
- **[G-04]** Settle tech stack versions: Kotlin 1.9+ vs 2.0+, JUnit 4 vs 5, Moshi vs Kotlinx Serialization, Tink vs EncryptedFile. Update the out-of-date doc to match the consensus.

### Medium (address during implementation)

- **[F-01]** Fix GhostFitTheme primary color from `0xFF7C4DFF` to `0xFF7C3AED` to match UX design spec.
- **[G-05]** Add ML Kit on-device detection to architecture doc.
- **[H-01]** Ensure JUnit version is consistent across all docs.
- **[A-03]** Generate tasks.md via `/speckit.tasks`.

### Low (address when convenient)

- **[G-06]** Update product-brief/PRD to note native Photo Picker instead of Google Photos API.
- **[H-02]** Remove `Bash(cat *)` from .ralphrc ALLOWED_TOOLS.
- **[F-02]** Consider lighter Tink init (`StreamingAeadConfig.register()`).

---

*Run `/validate ghostfit` again after fixing issues to verify.*
