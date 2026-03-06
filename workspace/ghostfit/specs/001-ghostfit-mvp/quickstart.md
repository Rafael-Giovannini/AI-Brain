# Quickstart: GhostFit MVP

**Feature Branch**: `001-ghostfit-mvp`
**Data**: 2026-03-04

---

## Pré-requisitos

- Android Studio Ladybug (2024.2+) ou superior
- JDK 17+
- Android SDK API 26+ (target API 35)
- Dispositivo físico Android 8.0+ (overlay e MediaProjection não funcionam em emulador)
- Contas de API:
  - FASHN.ai (chave API)
  - Google Cloud Platform (Vertex AI habilitado, service account)
  - Google AI Studio / Gemini API (chave API para Gemini 2.0 Flash — primário para detecção de roupa)
  - OpenAI (chave API para GPT-4o Vision — fallback opcional)

---

## Setup do Projeto

### 1. Criar projeto Android

```bash
# Na raiz do workspace ghostfit
mkdir -p android/app
cd android
```

Criar projeto via Android Studio:
- **Nome**: GhostFit
- **Package**: `app.ghostfit`
- **Linguagem**: Kotlin
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35
- **Template**: Empty Compose Activity

### 2. Dependências principais (build.gradle.kts)

```kotlin
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Hilt (DI — conforme architecture doc, planejado para Phase 2+)
    // MVP Phase 1 usa singletons manuais (AppDatabase.getInstance, PhotoStorage.getInstance)
    // Descomentar quando migrar para Hilt:
    // implementation("com.google.dagger:hilt-android:2.51.1")
    // ksp("com.google.dagger:hilt-compiler:2.51.1")
    // implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room (UserProfile, SubscriptionState)
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    // ML Kit Object Detection
    implementation("com.google.mlkit:object-detection:17.0.2")

    // Google Play Billing
    implementation("com.android.billingclient:billing:8.3.0")
    implementation("com.android.billingclient:billing-ktx:8.3.0")

    // Tink (criptografia de fotos)
    implementation("com.google.crypto.tink:tink-android:1.12.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### 3. Permissões (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application ...>
    <service
        android:name=".overlay.OverlayService"
        android:foregroundServiceType="mediaProjection"
        android:exported="false" />
</application>
```

### 4. Configuração de API Keys

Criar `local.properties` (NÃO commitar):
```properties
FASHN_API_KEY=your_fashn_key
GEMINI_API_KEY=your_gemini_key
OPENAI_API_KEY=your_openai_key  # opcional (fallback para detecção de roupa)
GCP_PROJECT_ID=your_gcp_project
```

Acessar via BuildConfig:
```kotlin
// build.gradle.kts
android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        val props = gradleLocalProperties(rootDir, providers)
        buildConfigField("String", "FASHN_API_KEY", "\"${props["FASHN_API_KEY"]}\"")
        buildConfigField("String", "OPENAI_API_KEY", "\"${props["OPENAI_API_KEY"]}\"")
    }
}
```

---

## Estrutura de Diretórios

```
android/app/src/main/
├── java/app/ghostfit/
│   ├── GhostFitApp.kt              # Application class (Tink init, DI)
│   ├── MainActivity.kt             # Entry point + onboarding
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt      # Room DB
│   │   │   ├── UserProfileDao.kt
│   │   │   └── PhotoStorage.kt     # Tink encrypted file I/O
│   │   ├── remote/
│   │   │   ├── FashnApi.kt         # FASHN.ai Retrofit interface
│   │   │   ├── VertexAiApi.kt      # Vertex AI Retrofit interface
│   │   │   ├── GeminiVisionApi.kt  # Gemini 2.0 Flash Vision (primário)
│   │   │   ├── VisionLlmApi.kt     # GPT-4o Vision (fallback)
│   │   │   └── GhostFitApi.kt      # Backend próprio (feedback, routing)
│   │   └── model/
│   │       ├── UserProfile.kt      # Room Entity
│   │       ├── GarmentInfo.kt      # Value Object
│   │       └── FeedbackRecord.kt   # DTO
│   ├── domain/
│   │   ├── TryOnUseCase.kt         # Orquestra o fluxo completo
│   │   ├── GarmentDetector.kt      # ML Kit + Vision LLM pipeline
│   │   ├── ModelRouter.kt          # Roteamento FASHN/Vertex
│   │   └── BillingManager.kt       # Google Play Billing wrapper
│   ├── overlay/
│   │   ├── OverlayService.kt       # Foreground Service + WindowManager
│   │   ├── OverlayComposable.kt    # UI do fantasminha (Compose)
│   │   └── ScreenCapture.kt        # MediaProjection wrapper
│   └── ui/
│       ├── onboarding/
│       │   ├── WelcomeScreen.kt
│       │   ├── PermissionScreen.kt
│       │   ├── LgpdConsentScreen.kt
│       │   └── PhotoSelectScreen.kt
│       ├── tryon/
│       │   └── TryOnResultScreen.kt # Resultado + ações (retry, share, feedback)
│       ├── subscription/
│       │   └── UpgradeScreen.kt
│       └── theme/
│           └── GhostFitTheme.kt
├── res/
│   ├── drawable/
│   │   └── ic_ghost.xml            # Ícone do fantasminha
│   └── values/
│       └── strings.xml             # Textos em PT-BR
└── AndroidManifest.xml
```

---

## Fluxo de Desenvolvimento Recomendado

1. **Story 1 — Onboarding**: Setup do projeto, Room DB, Tink, telas de onboarding
2. **Story 5 — Overlay**: OverlayService, fantasminha flutuante, drag & persist position
3. **Story 2 — Try-On Core**: MediaProjection, ML Kit, APIs de VTON, tela de resultado
4. **Story 3 — Interação**: Retry, trocar foto, feedback, compartilhar
5. **Story 4 — Monetização**: Billing, limites, ads, upgrade screen

---

## Comandos Úteis

```bash
# Build
./gradlew assembleDebug

# Testes unitários
./gradlew testDebugUnitTest

# Testes instrumentados (requer dispositivo)
./gradlew connectedDebugAndroidTest

# Install no dispositivo
./gradlew installDebug

# Lint
./gradlew lintDebug
```
