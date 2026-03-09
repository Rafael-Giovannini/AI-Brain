# Implementation Plan: GhostFit MVP — Provador Virtual com Overlay

**Branch**: `001-ghostfit-mvp` | **Data**: 2026-03-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `workspace/ghostfit/specs/001-ghostfit-mvp/spec.md`

## Summary

App Android nativo (Kotlin + Jetpack Compose) que funciona como provador virtual sobre apps de e-commerce. Um overlay flutuante ("fantasminha") permite à usuária capturar a tela, detectar roupas via IA de visão (ML Kit + GPT-4o), e gerar imagens realistas de virtual try-on via FASHN.ai (primário) e Google Vertex AI (fallback). Monetização freemium com Google Play Billing (3 tentativas/dia grátis, ads, assinatura premium).

## Technical Context

**Language/Version**: Kotlin 2.2.0 / JDK 17
**Primary Dependencies**: Jetpack Compose 1.7+, Room 2.7.1, Retrofit 2.11, ML Kit 17.x, Tink 1.12, Coil 2.7, AdMob
**DI**: Singletons manuais no MVP (Hilt planejado para refactor futuro)
**Storage**: Room DB (perfil/assinatura) + filesDir criptografado com Tink AES-256-GCM (fotos de referência)
**Testing**: JUnit 4, MockK, mockito-kotlin, Robolectric, Compose UI Test, kotlinx-coroutines-test
**Target Platform**: Android 8.0+ (API 26+), target API 35
**Project Type**: mobile-app (Android nativo)
**Performance Goals**: Captura de tela < 1s, detecção de roupa < 3s, geração VTON < 15s, overlay sem frame drops
**Constraints**: < 50MB memória do overlay, < 5% bateria/hora adicional, LGPD compliance
**Scale/Scope**: MVP — 16+ telas (onboarding 5 + overlay states 4 + resultado 2 + monetização 2 + home/settings 2 + erros 1), 1 overlay service, 3 APIs externas + 1 backend próprio

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

A constitution do projeto está em template (não configurada). Nenhum gate específico definido. Aplicando as **Skinner Laws** do CLAUDE.md como gates:

| Gate | Status | Notas |
|------|--------|-------|
| No Spec, No Code | ✅ PASS | Spec completa com 5 user stories, 21 FRs, edge cases |
| Atomicidade | ✅ PASS | Plano decomposto em stories independentes |
| Modularidade | ✅ PASS | Estrutura modular: data/domain/overlay/ui |
| Verificabilidade | ✅ PASS | Cada story tem acceptance scenarios testáveis |

**Pós-design re-check**: ✅ PASS — data-model, contracts e quickstart consistentes com a spec.

## Project Structure

### Documentation (this feature)

```text
workspace/ghostfit/specs/001-ghostfit-mvp/
├── spec.md              # Especificação da feature
├── plan.md              # Este arquivo
├── research.md          # Fase 0 — pesquisa e decisões técnicas
├── data-model.md        # Fase 1 — modelo de dados
├── quickstart.md        # Fase 1 — setup do projeto
└── contracts/
    └── api-contracts.md # Fase 1 — contratos de API
```

### Source Code (repository root)

```text
workspace/ghostfit/android/app/src/main/
├── java/app/ghostfit/
│   ├── GhostFitApp.kt
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── local/           # Room DB, Tink encrypted storage
│   │   ├── remote/          # Retrofit APIs (FASHN, Vertex, Vision, Backend)
│   │   └── model/           # Entities e DTOs
│   ├── domain/              # Use cases, detector, router, billing
│   ├── overlay/             # Foreground Service, Compose overlay, MediaProjection
│   └── ui/
│       ├── onboarding/      # Welcome, Permissions, LGPD, PhotoSelect
│       ├── tryon/           # Resultado do try-on
│       ├── subscription/    # Upgrade screen
│       └── theme/
├── res/
└── AndroidManifest.xml

workspace/ghostfit/android/app/src/test/        # Unit tests
workspace/ghostfit/android/app/src/androidTest/  # Instrumented tests
```

**Structure Decision**: Mobile app Android nativo com arquitetura em camadas (data/domain/ui) + módulo de overlay separado. Sem backend neste repo — backend será serviço separado (API mínima para feedback e roteamento).

## Complexity Tracking

Nenhuma violação de constitution identificada. Sem complexidade excessiva justificada.

## Decisões Técnicas Chave (de research.md)

| Decisão | Escolha | Alternativa Descartada |
|---------|---------|----------------------|
| Overlay Android | Foreground Service + TYPE_APPLICATION_OVERLAY | Accessibility Service, PiP |
| Captura de tela | MediaProjection API | Share sheet (considerar como fallback) |
| VTON primário | FASHN.ai API ($0.075/img) | NanoBanana (não é API standalone), Grok (não é VTON) |
| VTON fallback | Google Vertex AI virtual-try-on-001 | IDM-VTON self-hosted (requer GPU A100) |
| Detecção roupa | ML Kit (on-device crop) + Gemini 2.0 Flash (primário) + GPT-4o Vision (fallback) | Custom TFLite, Vision LLM sem crop |
| UI | Jetpack Compose + ComposeView no overlay | XML Views, Flutter |
| Billing | GPBL 8.3.0 (ou RevenueCat) | Manual sem backend |
| Criptografia fotos | Tink StreamingAead + Android Keystore | EncryptedFile (deprecado Abr/2025) |

## Próximos Passos

Executar `/speckit.tasks` para gerar o backlog de tasks decompostas a partir deste plano e da spec.
