# Ralph Agent Configuration — GhostFit MVP

## Prerequisites

```bash
# Verificar ferramentas instaladas
java --version      # Requer JDK 17+
kotlin -version     # Kotlin 2.2+
```

**Nota:** Este projeto Android pode ser buildado via Gradle wrapper (`./gradlew`), versão Gradle 8.11.1.
Não requer Android Studio para builds CLI, mas requer Android SDK instalado (API 26+ mínimo, target API 35).

**Dependências de teste:** JUnit 4, MockK, Robolectric, kotlinx-coroutines-test, mockito-kotlin.

## Build Instructions

```bash
cd workspace/ghostfit/android

# Build debug APK
./gradlew assembleDebug

# Build com lint check
./gradlew assembleDebug lintDebug
```

## Test Instructions

```bash
cd workspace/ghostfit/android

# Unit tests (JVM — não requer dispositivo)
./gradlew testDebugUnitTest

# Instrumented tests (requer dispositivo/emulador conectado)
./gradlew connectedDebugAndroidTest

# Apenas testes de um módulo
./gradlew :app:testDebugUnitTest
```

## Install & Run

```bash
cd workspace/ghostfit/android

# Install no dispositivo conectado
./gradlew installDebug

# Verificar dispositivos conectados
adb devices
```

## Project Structure

```
workspace/ghostfit/android/app/src/main/
├── java/app/ghostfit/
│   ├── GhostFitApp.kt              # Application class (Tink init)
│   ├── MainActivity.kt             # Entry point + onboarding nav
│   ├── data/
│   │   ├── local/                   # Room DB, Tink encrypted storage
│   │   ├── remote/                  # Retrofit APIs
│   │   └── model/                   # Entities e DTOs
│   ├── domain/                      # Use cases, detector, router, billing
│   ├── overlay/                     # Foreground Service, Compose overlay
│   └── ui/                          # Jetpack Compose screens
├── res/
└── AndroidManifest.xml

workspace/ghostfit/android/app/src/test/         # Unit tests (JVM)
workspace/ghostfit/android/app/src/androidTest/   # Instrumented tests
```

## API Keys Configuration

Criar `workspace/ghostfit/android/local.properties` (NÃO commitar):
```properties
FASHN_API_KEY=your_fashn_key
OPENAI_API_KEY=your_openai_key
GCP_PROJECT_ID=your_gcp_project
```

## Notes
- Min SDK: API 26 (Android 8.0)
- Target SDK: API 35
- Package: `app.ghostfit`
- Overlay requer dispositivo físico (MediaProjection não funciona em emulador)
- Fotos criptografadas com Tink AES-256-GCM no filesDir
- Update this file when build process changes
