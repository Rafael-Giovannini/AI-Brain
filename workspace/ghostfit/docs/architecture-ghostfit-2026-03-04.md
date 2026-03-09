# Architecture Document: GhostFit

**Date:** 2026-03-04
**Author:** System Architect (BMAD Method)
**Version:** 1.0
**Project Level:** 2
**Status:** Draft

---

## Related Documents

- Product Brief: `docs/product-brief-ghostfit-2026-03-04.md`
- PRD: `docs/prd-ghostfit-2026-03-04.md`
- UX Design: `docs/ux-design-ghostfit-2026-03-04.md`

---

## 1. Architectural Drivers

Os drivers arquiteturais foram priorizados com **Privacy-First** como princípio dominante.

| Priority | NFR | Requirement | Impact on Architecture |
|----------|-----|-------------|----------------------|
| **#1** | NFR-004/005 | Criptografia + Imagens Efêmeras | Client-heavy: fotos pessoais nunca saem do device. Backend stateless. |
| **#2** | NFR-006/007 | LGPD Compliance + Dados de Terceiros | Consent management, data minimization, anonimização obrigatória |
| **#3** | NFR-015 | Anonimização do Dataset | Pipeline de dados sem PII |
| **#4** | NFR-001/002 | Performance (detecção < 3s, geração < 15s) | Chamadas diretas às APIs de IA, sem proxy intermediário |
| **#5** | NFR-003 | Overlay < 50MB RAM | Foreground Service leve, UI mínima no overlay |
| **#6** | NFR-009 + FR-012 | Graceful Degradation + Fallback | Provider abstraction layer com chain-of-responsibility |
| **#7** | NFR-011 | Android 8.0+ (API 26) | Limita APIs disponíveis, garante compatibilidade ampla |

---

## 2. High-Level Architecture

### Pattern: Client-Heavy + Lightweight Serverless Backend

```
┌─────────────────────────────────────────────────────────────────┐
│                      ANDROID APP (Kotlin)                        │
│                                                                   │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐ │
│  │  Overlay    │  │    AI      │  │  Billing   │  │   Photo   │ │
│  │  Service    │→ │  Pipeline  │  │  Manager   │  │  Manager  │ │
│  │(Foreground) │  │            │  │            │  │(Encrypted)│ │
│  └─────┬──────┘  └─────┬──────┘  └────────────┘  └───────────┘ │
│        │               │                                         │
│        │    ┌──────────┴──────────┐                              │
│        │    │ Provider Abstraction │  (fallback chain)            │
│        │    │    Layer (PAL)       │                              │
│        │    └──────────┬──────────┘                              │
│        │               │                                         │
│  ┌─────┴──────┐  ┌─────┴──────┐  ┌────────────┐                │
│  │ Screenshot  │  │  Backend   │  │  Consent   │                │
│  │  Capture    │  │  Service   │  │  Manager   │                │
│  │(MediaProj.) │  │ (Abstract) │  │  (LGPD)    │                │
│  └────────────┘  └─────┬──────┘  └────────────┘                │
└────────────────────────┼────────────────────────────────────────┘
                         │
            HTTPS (TLS 1.2+) — All traffic encrypted
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
  ┌────────────┐  ┌────────────┐  ┌────────────┐
  │  ML Kit     │  │  FASHN.ai  │  │ Vertex AI  │
  │ (On-Device) │  │   (VTON)   │  │   (VTON    │
  │ + GPT-4o    │  │ (Primary)  │  │ Fallback)  │
  └────────────┘  └────────────┘  └────────────┘

┌─────────────────────────────────────────────────────────────────┐
│               FIREBASE BACKEND (Serverless)                      │
│                                                                   │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐ │
│  │  Feedback   │  │  Remote    │  │  Dataset   │  │ Analytics │ │
│  │  API        │  │  Config    │  │  Storage   │  │ + Crash   │ │
│  │ (Functions) │  │            │  │ (Anon.only)│  │ Reporting │ │
│  └────────────┘  └────────────┘  └────────────┘  └───────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Rationale

- **Client-Heavy:** Fotos pessoais NUNCA saem do dispositivo. App faz chamadas diretas às APIs de IA — privacy by design.
- **Serverless (Firebase):** Para MVP, Firebase oferece free tier generoso e zero infra para manter. Backend abstrato permite migração futura para Ktor/.NET.
- **Provider Abstraction Layer:** No app, interface `AiProvider` com implementações intercambiáveis. Fallback chain: Primary → Secondary → Error.

### Migration Path (v2+)

O app se comunica com o backend através de uma interface `BackendService`. No MVP, a implementação é `FirebaseBackendService`. Para migrar:
1. Criar `KtorBackendService` ou `DotNetBackendService` implementando a mesma interface
2. Trocar o singleton no Application class
3. Zero mudanças no resto do app

---

## 3. Technology Stack

### 3.1 Android App

| Category | Technology | Rationale | Trade-offs |
|----------|-----------|-----------|------------|
| **Language** | Kotlin 2.2.0 | Linguagem oficial Android, null safety, coroutines | — |
| **UI** | Jetpack Compose | Declarativo, moderno, menos boilerplate | Curva de aprendizado vs XML |
| **DI** | Singletons manuais (Hilt planejado pós-MVP) | Simplicidade para MVP, sem overhead de DI framework | Migrar para Hilt quando escalar |
| **Networking** | Retrofit + OkHttp | Padrão de mercado, interceptors, logging | — |
| **Image Loading** | Coil | Kotlin-first, Compose-native, leve | Menos features que Glide |
| **Local DB** | Room | ORM oficial Android, compile-time queries | — |
| **Encryption** | Tink AES-256-GCM (StreamingAead) | Crypto moderna, EncryptedFile deprecado Abr/2025 | Tink 1.12+ |
| **Coroutines** | Kotlin Coroutines + Flow | Async nativo Kotlin, compose-friendly | — |
| **Navigation** | Compose Navigation | Navegação declarativa, type-safe args | — |
| **Serialization** | Moshi 1.15 | Integração nativa com Retrofit, JSON adapters | — |

### 3.2 AI Services

| Category | Primary | Fallback | Rationale |
|----------|---------|----------|-----------|
| **Vision (detecção)** | ML Kit Object Detection (on-device) + Gemini 2.0 Flash (classificação) | GPT-4o Vision (fallback classificação) | ML Kit < 100ms local. Gemini 2.0 Flash primário (~100x mais barato que GPT-4o). GPT-4o como fallback. |
| **Generation (try-on)** | FASHN.ai v1.5 | Google Vertex AI VTON | FASHN.ai $0.075/img, maskless pixel-space. Vertex AI como fallback GCP |

### 3.3 Backend (Firebase)

| Service | Use Case | Rationale |
|---------|----------|-----------|
| **Cloud Functions** | Feedback API, dataset ingestion | Serverless, auto-scale, free tier |
| **Firestore** | Feedback records, user config, model scores | NoSQL flexível, real-time sync |
| **Cloud Storage** | Dataset anonimizado (roupas + parâmetros) | Barato, seguro, integrado |
| **Remote Config** | Feature flags, preços, modelo primário | Zero-deploy para mudar config |
| **Analytics** | Eventos de uso, funnel tracking | Gratuito, integrado |
| **Crashlytics** | Crash reporting, ANR tracking | Essencial para produção |

### 3.4 Monetização

| Service | Use Case | Rationale |
|---------|----------|-----------|
| **Google Play Billing Library 8.3.0** | Assinaturas + compras avulsas | Único permitido na Play Store |
| **Google AdMob** | Banners + interstitials | Maior rede de ads Android |

### 3.5 Development & Deployment

| Category | Technology | Rationale |
|----------|-----------|-----------|
| **Version Control** | Git (GitHub) | Padrão de mercado |
| **CI/CD** | GitHub Actions | Integrado ao repo, free tier generoso |
| **Testing** | JUnit 4 + mockito-kotlin + MockK + Robolectric + Compose UI Test | Stack validada no projeto |
| **Code Quality** | Ktlint + Detekt | Linting + static analysis |
| **Build** | Gradle (Kotlin DSL) | Padrão Android, version catalogs |
| **Min SDK** | API 26 (Android 8.0) | Cobre 95%+ dispositivos Brasil |
| **Target SDK** | API 35 (Android 15) | Compliance Play Store |

---

## 4. System Components

### 4.1 Overlay Service

**Purpose:** Exibir fantasminha flutuante sobre qualquer app e capturar telas.

**Responsibilities:**
- Renderizar overlay flutuante (fantasminha) via WindowManager
- Gerenciar drag/reposicionamento do overlay
- Iniciar captura de tela via MediaProjection ao toque
- Gerenciar lifecycle do foreground service
- Persistir posição do overlay entre sessões

**Interfaces:**
- `OverlayService` (Android Foreground Service)
- `WindowManager` API para posicionamento
- `MediaProjection` API para screenshot

**Dependencies:**
- SYSTEM_ALERT_WINDOW permission
- MediaProjection permission
- AI Pipeline (para enviar screenshot)

**FRs Addressed:** FR-002, FR-004, FR-005, FR-006

---

### 4.2 AI Pipeline

**Purpose:** Orquestrar detecção de roupa e geração de imagem virtual try-on.

**Responsibilities:**
- Receber screenshot do Overlay Service
- Enviar para Vision API (detecção de roupa)
- Se roupa detectada: enviar para Gen AI (geração de try-on)
- Gerenciar fallback chain entre providers
- Implementar roteamento inteligente (baseado em scores)
- Retornar resultado ou erro amigável

**Interfaces:**
```kotlin
interface AiPipeline {
    suspend fun processScreenshot(
        screenshot: Bitmap,
        userPhoto: EncryptedPhoto
    ): TryOnResult
}

sealed class TryOnResult {
    data class Success(val image: Bitmap, val modelUsed: String) : TryOnResult()
    data class NoClothingDetected(val confidence: Float) : TryOnResult()
    data class Error(val message: String, val canRetry: Boolean) : TryOnResult()
}
```

**Dependencies:**
- Provider Abstraction Layer
- Photo Manager (foto de referência)
- Backend Service (scores de roteamento)

**FRs Addressed:** FR-007, FR-008, FR-009, FR-010, FR-011, FR-012, FR-020

---

### 4.3 Provider Abstraction Layer (PAL)

**Purpose:** Abstrair APIs de IA com fallback chain e roteamento inteligente.

**Responsibilities:**
- Definir interface única para vision e generation providers
- Implementar chain-of-responsibility (primary → fallback → error)
- Gerenciar timeouts por provider (20s default)
- Retry automático (até 2x) antes de fallback
- Logging de qual modelo foi usado
- Roteamento por score (tipo de roupa × modelo)

**Interfaces:**
```kotlin
interface VisionProvider {
    suspend fun detectClothing(image: Bitmap): ClothingDetection
    val name: String
}

interface GenerationProvider {
    suspend fun generateTryOn(
        clothing: ClothingDetection,
        userPhoto: Bitmap,
        seed: Long? = null
    ): GeneratedImage
    val name: String
}

interface ProviderRouter {
    fun selectProvider(clothingType: ClothingType): GenerationProvider
}
```

**Dependencies:**
- ML Kit Object Detection (vision on-device)
- OpenAI GPT-4o Vision SDK (classificação remota)
- FASHN.ai API (VTON generation primary)
- Vertex AI VTON API (VTON generation fallback)
- Model Score Repository (roteamento)

**FRs Addressed:** FR-007, FR-009, FR-012, FR-020

---

### 4.4 Photo Manager

**Purpose:** Gerenciar fotos pessoais da usuária com criptografia.

**Responsibilities:**
- Integrar com Android Photo Picker nativo para seleção de fotos
- Validar que foto contém pessoa de corpo inteiro
- Criptografar fotos localmente com Tink AES-256-GCM
- Gerenciar CRUD de fotos de referência
- Nunca enviar fotos para storage externo (privacy-first)

**Interfaces:**
```kotlin
interface PhotoManager {
    suspend fun selectFromPhotoPicker(): List<ReferencePhoto>
    suspend fun validateFullBody(photo: Bitmap): Boolean
    fun getEncryptedPhotos(): Flow<List<EncryptedPhoto>>
    suspend fun deletePhoto(id: String)
    suspend fun deleteAllPhotos() // LGPD: direito ao esquecimento
}
```

**Dependencies:**
- Android Photo Picker
- Tink StreamingAead
- Room Database (metadata)

**FRs Addressed:** FR-001, FR-003

---

### 4.5 Consent Manager (LGPD)

**Purpose:** Gerenciar consentimento LGPD e permissões do app.

**Responsibilities:**
- Exibir termos de uso e política de privacidade
- Registrar consentimento explícito com timestamp
- Permitir revogação de consentimento a qualquer momento
- Bloquear acesso a fotos sem consentimento
- Gerenciar exclusão de dados (direito ao esquecimento)
- Gerenciar permissão SYSTEM_ALERT_WINDOW

**Interfaces:**
```kotlin
interface ConsentManager {
    fun hasConsent(): Boolean
    suspend fun requestConsent(): ConsentResult
    suspend fun revokeConsent()
    fun getConsentTimestamp(): Instant?
    suspend fun deleteAllUserData() // LGPD Art. 18
}
```

**Dependencies:**
- Room Database (consent records)
- Photo Manager (para exclusão)

**FRs Addressed:** FR-002, FR-003

---

### 4.6 Billing Manager

**Purpose:** Gerenciar monetização — limites grátis, ads e pagamentos.

**Responsibilities:**
- Controlar limite de 3 tentativas grátis/dia
- Integrar Google Play Billing para compras e assinaturas
- Integrar AdMob para anúncios
- Verificar status premium da usuária
- Restaurar compras em reinstalação

**Interfaces:**
```kotlin
interface BillingManager {
    fun getRemainingFreeTrials(): Flow<Int>
    fun isPremium(): Flow<Boolean>
    suspend fun consumeTrial(): Boolean // false se limite atingido
    suspend fun purchasePackage(sku: String): PurchaseResult
    suspend fun subscribe(planId: String): PurchaseResult
    suspend fun restorePurchases(): List<Purchase>
}
```

**Dependencies:**
- Google Play Billing Library v6+
- Google AdMob SDK
- Room Database (trial counter)

**FRs Addressed:** FR-015, FR-016, FR-017, FR-018

---

### 4.7 Backend Service (Abstract)

**Purpose:** Interface para comunicação com backend. Implementação Firebase no MVP.

**Responsibilities:**
- Enviar feedback (thumbs up/down)
- Buscar remote config (modelo primário, preços, limites)
- Enviar dados anonimizados para dataset
- Buscar scores de modelo para roteamento

**Interfaces:**
```kotlin
interface BackendService {
    suspend fun submitFeedback(feedback: TryOnFeedback)
    suspend fun getRemoteConfig(): RemoteConfig
    suspend fun submitDatasetEntry(entry: AnonymizedDataEntry)
    suspend fun getModelScores(): Map<ModelKey, Float>
}

// MVP implementation
class FirebaseBackendService @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig
) : BackendService { ... }
```

**Dependencies:**
- Firebase Functions
- Firestore
- Firebase Remote Config
- Firebase Cloud Storage

**FRs Addressed:** FR-014, FR-019, FR-020, FR-021

---

### 4.8 Result Screen (Composable)

**Purpose:** Exibir resultado do try-on com ações disponíveis.

**Responsibilities:**
- Exibir imagem gerada em tela cheia/overlay
- Botões: Tentar Novamente, Trocar Foto, Compartilhar, Thumbs Up/Down
- Compartilhar com branding GhostFit via share sheet
- Exibir ad entre gerações (usuárias free)

**Interfaces:**
- Compose Screen com ViewModel
- Share Intent para compartilhamento
- Bitmap manipulation para branding

**Dependencies:**
- AI Pipeline (regeneração)
- Photo Manager (trocar foto)
- Billing Manager (trial counter, ads)
- Backend Service (feedback)

**FRs Addressed:** FR-009, FR-010, FR-011, FR-013, FR-014

---

## 5. Data Architecture

### 5.1 Data Model

```
┌─────────────────────────────────┐
│         UserProfile             │
│ ─────────────────────────────── │
│ id: Long (PK, autoGenerate)     │
│ displayName: String             │
│ lgpdConsentGiven: Boolean       │
│ lgpdConsentTimestamp: Long?     │
│ overlayPositionX: Float         │
│ overlayPositionY: Float         │
│ createdAt: Long                 │
│ updatedAt: Long                 │
│ ─────────────────────────────── │
│ Has many: ReferencePhoto        │
└─────────────────────────────────┘
         │
         │ 1:N (max 3)
         ▼
┌─────────────────────────────────┐
│       ReferencePhoto            │
│ ─────────────────────────────── │
│ id: Long (PK, autoGenerate)     │
│ userProfileId: Long (FK)        │
│ encryptedPath: String           │
│ isFullBody: Boolean             │
│ isActive: Boolean               │
│ createdAt: Long                 │
│ ─────────────────────────────── │
│ Encrypted via Tink AES-256-GCM  │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│     SubscriptionState           │
│ ─────────────────────────────── │
│ plan: "free" | "monthly" |      │
│       "pack_10" | "pack_50"     │
│ dailyTryOnsUsed: Int            │
│ dailyResetDate: String          │
│ purchaseToken: String?          │
│ expiresAt: Long?                │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│       FeedbackRecord            │  ← Enviado ao backend (anonimizado)
│ ─────────────────────────────── │
│ sessionId: String (UUID)        │
│ modelUsed: "fashn" | "vertex"   │
│ clothingType: String            │
│ thumbsUp: Boolean               │
│ generationTimeMs: Long          │
│ timestamp: Long                 │
│ deviceHash: String (anon)       │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│     ModelScore (Remote)         │  ← Firestore
│ ─────────────────────────────── │
│ modelId: "fashn" | "vertex"     │
│ clothingType: String            │
│ approvalRate: Float             │
│ totalFeedbacks: Int             │
│ updatedAt: Timestamp            │
└─────────────────────────────────┘
```

### 5.2 Storage Strategy

| Data | Storage | Encryption | Rationale |
|------|---------|------------|-----------|
| Fotos pessoais | Android Internal Storage | Tink AES-256-GCM (StreamingAead) | Nunca saem do device |
| UserProfile + consent | Room (SQLite) | — (dados não sensíveis) | Auditoria LGPD via timestamps |
| SubscriptionState | Room (SQLite) | — | Validação server-side via Play Billing |
| Overlay position | Room (UserProfile.overlayPositionX/Y) | Não (dado não sensível) | Consolidado no UserProfile entity |
| Screenshots | Memória (Bitmap) | N/A — efêmero | Deletado após processamento |
| Imagens geradas | Memória (Bitmap) | N/A — efêmero | Nunca persistido em disco |
| Feedback | Firestore | TLS em trânsito | Anonimizado, sem PII |
| Dataset | Cloud Storage | At-rest encryption | Anonimizado, sem PII |
| Model scores | Firestore | TLS em trânsito | Dados agregados públicos |

### 5.3 Data Flow

```
[Toque no fantasminha]
        │
        ▼
[MediaProjection captura tela] → Bitmap in memory (EFÊMERO)
        │
        ▼
[ML Kit: detecta objeto roupa] → bounding box + crop
        │                         (tipo, bounding box, confiança)
        │
        ├── confiança < 60% → "Nenhuma roupa detectada" → FIM
        │
        ▼
[Foto da usuária descriptografada] → Bitmap in memory (EFÊMERO)
        │
        ▼
[GPT-4o Vision: classifica tipo/cor] → GarmentInfo
        │
        ▼
[FASHN.ai/Vertex AI: gera try-on] → Bitmap resultado in memory (EFÊMERO)
        │
        ▼
[Result Screen exibe imagem]
        │
        ├── Thumbs up/down → Firebase (anonimizado)
        ├── Compartilhar → Share Intent (com branding)
        ├── Tentar novamente → Volta ao generation
        └── Fechar → Bitmaps liberados da memória
```

**Privacy checkpoint:** Em NENHUM ponto do fluxo a foto pessoal ou a imagem gerada é persistida em disco ou enviada a um servidor. Tudo é processado in-memory e descartado.

---

## 6. API Design

### 6.1 APIs Consumidas pelo App (Externas)

#### Android Photo Picker (On-Device)
```
Usa ActivityResultContracts.PickMultipleVisualMedia() — nativo Android, sem OAuth.
→ Lista de URIs de fotos selecionadas pela usuária
```

#### ML Kit Object Detection (On-Device — Detecção)
```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
    .enableMultipleObjects()
    .enableClassification()
    .build()
val detector = ObjectDetection.getClient(options)
→ List<DetectedObject> com bounding boxes (< 100ms, sem internet)
```

#### GPT-4o Vision (Classificação Remota)
```
POST https://api.openai.com/v1/chat/completions
{
  "model": "gpt-4o",
  "messages": [{"role": "user", "content": [
    {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,{cropped_garment}"}},
    {"type": "text", "text": "Classify: type (top/bottom/dress/outerwear), color, description. JSON only."}
  ]}],
  "max_tokens": 150
}
→ GarmentInfo { type, color, description }
```

#### FASHN.ai v1.5 (VTON — Primary)
```
POST https://api.fashn.ai/v1/run
{
  "model_image": "{base64_user_photo}",
  "garment_image": "{base64_clothing_crop}",
  "category": "tops" | "bottoms" | "one-pieces"
}
→ Generated try-on image (URL), timeout: 10s
```

#### Google Vertex AI VTON (Fallback)
```
POST https://{region}-aiplatform.googleapis.com/v1/projects/{project}/locations/{region}/publishers/google/models/virtual-try-on-001:predict
{
  "instances": [{
    "person_image": {"bytesBase64Encoded": "{base64_user_photo}"},
    "garment_image": {"bytesBase64Encoded": "{base64_clothing_crop}"},
    "garment_type": "TOP" | "BOTTOM" | "FULL"
  }]
}
→ Generated try-on image (base64), timeout: 15s
```

### 6.2 Backend API (Firebase Functions)

#### POST /v1/feedback
Envia feedback de try-on (anonimizado).
```json
Request:
{
  "modelUsed": "fashn",
  "clothingType": "dress",
  "thumbsUp": true,
  "generationTimeMs": 8500,
  "deviceHash": "sha256_anon_id",
  "timestamp": 1709568000000
}

Response: 200 OK
```

#### GET /v1/config
Busca configuração remota.
```json
Response:
{
  "primaryGenModel": "fashn",
  "fallbackGenModel": "vertex",
  "maxFreeTrials": 3,
  "visionTimeout": 5000,
  "genTimeout": 20000,
  "minConfidence": 0.6,
  "subscriptionPrices": {
    "monthly": 9.90,
    "pack10": 4.90
  }
}
```

#### GET /v1/models/route
Busca recomendação de modelo por tipo de roupa (roteamento inteligente).
```json
GET /v1/models/route?clothingType=dress

Response:
{
  "recommendedModel": "fashn",
  "fallbackModel": "vertex",
  "scores": {
    "fashn": {"approvalRate": 0.78, "count": 234},
    "vertex": {"approvalRate": 0.65, "count": 189}
  }
}
```

#### GET /v1/health
Health check do backend.
```json
Response:
{ "status": "ok", "timestamp": 1709568000000 }
```

#### POST /v1/dataset
Envia entrada anonimizada para dataset de treinamento.
```json
Request:
{
  "clothingType": "dress",
  "clothingDescription": "Red floral summer dress",
  "modelUsed": "fashn",
  "generationTimeMs": 8500,
  "approved": true,
  "timestamp": 1709568000000
}

Response: 200 OK
```

### 6.3 Authentication

- **Photo Picker:** Nativo Android — sem OAuth necessário
- **AI APIs:** API Keys armazenadas em:
  - **MVP:** Compiladas no app (obfuscação via ProGuard + BuildConfig)
  - **v2:** Migrar para backend proxy que adiciona API key server-side
- **Firebase:** Firebase Auth anonymous (sem login) + App Check (anti-abuse)
- **Play Billing:** Google Play Services SDK (assinatura verificada pelo Google)

**Nota de segurança:** API keys no client-side é aceitável para MVP com rate limiting e monitoramento. Para produção em escala, migrar para backend proxy.

---

## 7. NFR Coverage

### NFR-001: Performance — Detecção de Roupa (< 3s)

**Requirement:** 95% das detecções completam em < 3s em conexão 4G

**Solution:**
- ML Kit Object Detection: detecção on-device (< 100ms) + GPT-4o Vision para classificação remota
- Imagem comprimida antes do envio (JPEG quality 80, max 1024px)
- OkHttp connection pooling + HTTP/2
- Timeout de 5s antes de fallback para GPT-4o Vision

**Validation:** Monitorar p95 latência via Firebase Analytics custom event

---

### NFR-002: Performance — Geração de Imagem (< 15s)

**Requirement:** 90% das gerações completam em < 15s em conexão 4G

**Solution:**
- Timeout de 20s por provider, fallback após timeout
- Progress indicator com animação durante espera
- Imagens de referência otimizadas (resolução mínima necessária)
- Retry 1x no mesmo provider antes de fallback

**Validation:** Monitorar p90 latência via Firebase Analytics

---

### NFR-003: Performance — Overlay (< 50MB RAM)

**Requirement:** Overlay sem impacto perceptível no app hospedeiro

**Solution:**
- Foreground Service com prioridade baixa
- Overlay UI: single Compose view (fantasminha + ripple)
- Bitmap processing: streams, não manter múltiplos bitmaps em memória
- Recycle bitmaps agressivamente após uso
- Service notification com prioridade MIN

**Validation:** Android Profiler — verificar memory footprint < 50MB

---

### NFR-004: Segurança — Criptografia em Trânsito e Repouso

**Requirement:** TLS 1.2+ para rede, AES-256/Keystore para dados locais

**Solution:**
- OkHttp com TLS 1.2+ enforced (network security config)
- Certificate pinning para APIs críticas (AI providers)
- Fotos pessoais: Tink StreamingAead (AES-256-GCM) com Android Keystore
- Dados sensíveis locais: Room DB (sem dados PII sensíveis, não requer criptografia adicional)

**Implementation Notes:**
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**Validation:** Teste com proxy (Charles/mitmproxy) — verificar que tudo é HTTPS

---

### NFR-005: Privacidade — Imagens Efêmeras

**Requirement:** Imagens geradas NUNCA persistidas em servidores

**Solution:**
- Arquitetura client-heavy: app chama APIs diretamente
- APIs de IA usadas em modo stateless (sem retention)
- Screenshots: Bitmap in-memory, nunca `File`
- Imagens geradas: Bitmap in-memory, recycled após fechar resultado
- Compartilhamento: salva temporariamente em cache dir, deleta após share

**Implementation Notes:**
```kotlin
// Imagem temporária para share
val tempFile = File(context.cacheDir, "share_temp.jpg")
tempFile.deleteOnExit()
// Após share intent completar:
tempFile.delete()
```

**Validation:** Auditoria de código: grep por qualquer File.write com bitmap de resultado

---

### NFR-006: Compliance — LGPD

**Requirement:** Conformidade total com LGPD

**Solution:**
- `ConsentManager` com opt-in explícito antes de acessar fotos
- Consent records com timestamp em storage criptografado
- "Excluir meus dados" — deleta fotos, consent, trials, tudo
- Política de privacidade em PT-BR, linguagem simples
- Dados enviados a Firebase são 100% anonimizados
- Nenhum identificador pessoal no dataset

**Validation:** Checklist LGPD manual antes do lançamento

---

### NFR-007: Privacidade — Dados de Terceiros

**Requirement:** Sem compartilhamento de PII com terceiros

**Solution:**
- APIs de IA recebem: screenshot (temporário) + foto da usuária (temporário)
- Verificar termos das APIs: confirmar que não retêm dados para treinamento
- Preferir APIs com modo "no-training" ou "no-retention"
- Firebase recebe apenas dados anonimizados (hash de device, tipo de roupa, score)

**Validation:** Revisão de ToS das APIs antes do lançamento

---

### NFR-008: Confiabilidade — Modo Offline Parcial

**Requirement:** App funcional offline para navegação básica

**Solution:**
- Room database com fotos de referência acessíveis offline
- Tela principal, configurações, galeria de fotos: offline OK
- Try-on: exibe mensagem "Sem conexão" com retry
- Remote config: cache local com TTL de 24h

**Validation:** Testar em modo avião

---

### NFR-009: Confiabilidade — Graceful Degradation

**Requirement:** Mensagem amigável quando APIs indisponíveis

**Solution:**
- Provider Abstraction Layer com chain: Primary → Fallback → Friendly Error
- Retry com exponential backoff (1s, 2s, 4s)
- Falha de API NÃO conta no trial counter
- Mensagem: "Serviço temporariamente indisponível. Tente em alguns minutos."
- Firebase Remote Config: flag para desabilitar providers com problema

**Validation:** Simular API failure com mock interceptor

---

### NFR-010: Usabilidade — Fluxo Mínimo (2 toques)

**Requirement:** Toque → Resultado em máximo 2 toques

**Solution:**
- Toque 1: tap no fantasminha → captura + detecção + geração (pipeline automático)
- Resultado aparece automaticamente (zero toques adicionais)
- Nenhuma tela intermediária obrigatória
- Loading animations durante processamento

**Validation:** UX testing com personas (Maria)

---

### NFR-011: Compatibilidade — Android 8.0+ (API 26)

**Requirement:** minSdkVersion = 26

**Solution:**
- Gradle: `minSdk = 26`, `targetSdk = 35`
- Verificar que todas as APIs usadas estão disponíveis em API 26
- MediaProjection: disponível desde API 21 ✓
- SYSTEM_ALERT_WINDOW: disponível desde API 1 ✓
- Tink: disponível como dependência Maven ✓

**Validation:** Testar em emuladores API 26, 29, 33, 35

---

### NFR-012: Usabilidade — Idioma PT-BR

**Requirement:** Interface completamente em Português Brasileiro

**Solution:**
- Strings em `res/values-pt-rBR/strings.xml` (default)
- Preparar `res/values/strings.xml` (EN) para futura internacionalização
- Sem termos técnicos em inglês na UI
- Mensagens de erro em linguagem simples

**Validation:** Revisão manual de todas as strings

---

### NFR-013: Compatibilidade — Apps de E-commerce

**Requirement:** Overlay funcional sobre Shopee, Shein, etc.

**Solution:**
- SYSTEM_ALERT_WINDOW é independente do app hospedeiro
- MediaProjection captura qualquer tela (não depende do app)
- Testar overlay z-order com apps específicos
- Overlay usa `TYPE_APPLICATION_OVERLAY` (API 26+)

**Validation:** Testar sobre: Shopee, Shein, AliExpress, Renner, C&A

---

### NFR-014: Compliance — Google Play Store

**Requirement:** Conformidade com políticas da Play Store

**Solution:**
- SYSTEM_ALERT_WINDOW: documentar justificativa (provador virtual)
- Solicitar apenas permissões necessárias: overlay, internet, photos
- Política de privacidade publicada e linkada
- Content rating: "Everyone"
- ProGuard/R8 para ofuscação
- App Bundle (AAB) para distribuição

**Validation:** Checklist Play Store pre-submission

---

### NFR-015: Privacidade — Anonimização do Dataset

**Requirement:** Dataset sem dados pessoais identificáveis

**Solution:**
- Dataset contém APENAS: tipo de roupa, parâmetros de geração, modelo usado, feedback
- Fotos pessoais NUNCA incluídas
- Device ID: SHA-256 hash (não reversível)
- Sem timestamp preciso (arredondado para dia)
- Impossível re-identificar usuária

**Validation:** Auditoria do schema de `TryOnFeedback` e `AnonymizedDataEntry`

---

## 8. Security Architecture

### 8.1 Authentication

| Component | Method | Details |
|-----------|--------|---------|
| Google Photos | OAuth 2.0 | Google Sign-In SDK, scope `photoslibrary.readonly` |
| Firebase | Anonymous Auth | Sem login obrigatório, App Check para anti-abuse |
| AI APIs | API Keys | BuildConfig (MVP), backend proxy (v2) |
| Play Billing | Google Play Services | Managed by SDK |

### 8.2 Authorization

- **Modelo simples:** Free vs Premium (boolean)
- **Free:** 3 trials/dia, com ads
- **Premium:** ilimitado, sem ads
- **Verificação:** Local (Room) + Play Billing status
- **Anti-tampering:** EncryptedSharedPrefs para trial counter, verificação de compra via Play Billing

### 8.3 Data Encryption

| Data | At Rest | In Transit |
|------|---------|------------|
| Fotos pessoais | AES-256 (Android Keystore) | N/A (local only) |
| Consent records | EncryptedSharedPrefs | N/A (local only) |
| Screenshots | In-memory only | TLS 1.2+ to AI APIs |
| Imagens geradas | In-memory only | N/A |
| Feedback | Firestore encryption | TLS 1.2+ |
| API Keys | BuildConfig (obfuscated) | TLS 1.2+ |

### 8.4 Security Best Practices

- **Network:** `cleartextTrafficPermitted="false"` — zero HTTP, apenas HTTPS
- **Certificate Pinning:** Para AI API endpoints (OkHttp CertificatePinner)
- **ProGuard/R8:** Ofuscação de código + remoção de logs em release
- **No Logging PII:** Logs nunca contêm fotos, tokens, ou dados pessoais
- **Secure Bitmap Handling:** Bitmaps reciclados com `bitmap.recycle()` + referências nullificadas
- **Root Detection:** Opcional — avisar (não bloquear) em dispositivos rooted
- **Screenshot Prevention:** `FLAG_SECURE` na Result Screen (proteger a imagem gerada)

---

## 9. Scalability & Performance

### 9.1 Scaling Strategy

**MVP (0-10K MAU):**
- Firebase auto-scales automaticamente
- AI APIs: pay-per-use, escala com demanda
- Sem necessidade de infra adicional

**Growth (10K-100K MAU):**
- Monitorar custos de APIs de IA
- Considerar migrar para backend próprio (Ktor/.NET) para adicionar caching e rate limiting
- Implementar queue para gerações em pico

**Scale (100K+ MAU):**
- Backend próprio com load balancer
- Cache de Remote Config com CDN
- Modelo próprio (fine-tuned) para reduzir custos de API
- Cloud Run auto-scaling

### 9.2 Performance Optimization

- **Image compression:** JPEG quality 80, max 1024px antes de enviar para APIs
- **Connection pooling:** OkHttp mantém conexões abertas para reuso
- **Lazy loading:** Fotos de referência carregadas sob demanda
- **Memory management:** Bitmap recycling agressivo, max 2 bitmaps em memória
- **Background processing:** Coroutines com Dispatchers.IO para API calls

### 9.3 Caching Strategy

| Cache | TTL | Strategy |
|-------|-----|----------|
| Remote Config | 24h | Firebase Remote Config built-in cache |
| Model Scores | 1h | Room database com timestamp check |
| User Photos (thumbnails) | Persistent | Coil disk cache (encrypted) |
| AI responses | None | Stateless, sem cache (cada geração é única) |

---

## 10. Reliability & Availability

### 10.1 Availability

- **AI APIs:** Dependência de terceiros. Mitigação: fallback chain (2+ providers)
- **Firebase:** 99.95% SLA do Google. Mitigação: cache local para config
- **App:** Funcional offline para navegação básica

### 10.2 Error Handling Strategy

```
AI API Call
    │
    ├── Success → Show result
    │
    ├── Timeout (>20s) → Retry 1x
    │   ├── Success → Show result
    │   └── Fail → Try fallback provider
    │       ├── Success → Show result
    │       └── Fail → Friendly error message
    │
    ├── Rate Limited (429) → Wait + retry with backoff
    │
    └── Server Error (5xx) → Try fallback provider
```

### 10.3 Monitoring & Alerting

| Metric | Tool | Alert Threshold |
|--------|------|-----------------|
| Crash rate | Crashlytics | > 1% |
| API latency (vision) | Firebase Analytics | p95 > 5s |
| API latency (generation) | Firebase Analytics | p90 > 20s |
| Generation success rate | Firebase Analytics | < 85% |
| Approval rate (thumbs up) | Firestore aggregation | < 60% |
| Daily active users | Firebase Analytics | Trend monitoring |

---

## 11. Development & Deployment

### 11.1 Project Structure

```
ghostfit/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/ghostfit/
│   │   │   │   ├── di/                    # Hilt modules
│   │   │   │   ├── data/                  # Data layer
│   │   │   │   │   ├── local/             # Room DAOs, encrypted storage
│   │   │   │   │   ├── remote/            # Retrofit services, Firebase
│   │   │   │   │   └── repository/        # Repository implementations
│   │   │   │   ├── domain/                # Domain layer
│   │   │   │   │   ├── model/             # Domain models
│   │   │   │   │   ├── repository/        # Repository interfaces
│   │   │   │   │   └── usecase/           # Use cases
│   │   │   │   ├── ai/                    # AI Pipeline
│   │   │   │   │   ├── provider/          # Vision + Generation providers
│   │   │   │   │   ├── router/            # Intelligent routing
│   │   │   │   │   └── pipeline/          # Orchestration
│   │   │   │   ├── overlay/               # Overlay service + UI
│   │   │   │   ├── billing/               # Play Billing integration
│   │   │   │   ├── consent/               # LGPD consent management
│   │   │   │   ├── photo/                 # Photo management (encrypted)
│   │   │   │   └── ui/                    # Compose screens
│   │   │   │       ├── onboarding/
│   │   │   │       ├── result/
│   │   │   │       ├── settings/
│   │   │   │       └── theme/
│   │   │   ├── res/
│   │   │   │   ├── values-pt-rBR/         # Portuguese strings (default)
│   │   │   │   └── values/                # English strings (fallback)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                          # Unit tests
│   └── build.gradle.kts
├── firebase/
│   ├── functions/                         # Cloud Functions
│   │   ├── src/
│   │   │   ├── feedback.ts
│   │   │   ├── config.ts
│   │   │   └── dataset.ts
│   │   └── package.json
│   ├── firestore.rules
│   └── storage.rules
├── build.gradle.kts                       # Root build file
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml                 # Version catalog
```

### 11.2 Architecture Layers

```
┌─────────────────────────────────────┐
│          UI Layer (Compose)          │
│   Screens, ViewModels, Navigation    │
├─────────────────────────────────────┤
│         Domain Layer                 │
│   Use Cases, Models, Interfaces      │
├─────────────────────────────────────┤
│          Data Layer                  │
│   Repositories, DAOs, APIs, Firebase │
├─────────────────────────────────────┤
│       AI Layer (specialized)         │
│   Providers, Router, Pipeline        │
└─────────────────────────────────────┘
```

**Dependency rule:** UI → Domain ← Data/AI. Domain não depende de nada externo.

### 11.3 Testing Strategy

| Type | Framework | Coverage Target | Scope |
|------|-----------|-----------------|-------|
| Unit | JUnit 4 + mockito-kotlin + MockK | 80%+ | Use cases, providers, router, pipeline |
| Integration | AndroidX Test | Key flows | AI pipeline end-to-end (mocked APIs) |
| UI | Compose Testing | Critical screens | Onboarding, Result, Settings |
| E2E | Manual + Firebase Test Lab | Core flow | Tap → Result sobre Shopee |

### 11.4 CI/CD Pipeline

```
Push to branch
    │
    ▼
[GitHub Actions]
    │
    ├── Lint (ktlint + detekt)
    ├── Unit Tests (JUnit 4)
    ├── Build Debug APK
    │
    ▼ (on merge to main)
    │
    ├── Build Release AAB
    ├── Sign with release keystore
    ├── Upload to Play Console (internal track)
    │
    ▼ (manual promotion)
    │
    └── Promote to Production
```

### 11.5 Environments

| Environment | Purpose | AI APIs | Firebase |
|-------------|---------|---------|----------|
| **Debug** | Development | Sandbox/mock | Firebase emulator |
| **Staging** | QA testing | Real APIs (limited) | Staging Firebase project |
| **Production** | Live users | Real APIs | Production Firebase project |

---

## 12. Traceability & Trade-offs

### 12.1 FR → Component Traceability

| FR ID | FR Name | Components | Notes |
|-------|---------|------------|-------|
| FR-001 | Cadastro fotos Photo Picker | Photo Manager, Consent Manager | Photo Picker nativo, sem OAuth |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | Consent Manager, Overlay Service | Onboarding explicativo |
| FR-003 | Consentimento LGPD | Consent Manager | Opt-in explícito antes de fotos |
| FR-004 | Overlay flutuante | Overlay Service | Foreground Service + WindowManager |
| FR-005 | Captura de tela | Overlay Service | MediaProjection API |
| FR-006 | Overlay reposicionável | Overlay Service | Touch listener + position persistence |
| FR-007 | Detecção roupa IA | AI Pipeline, PAL | ML Kit on-device + GPT-4o Vision |
| FR-008 | Aviso nenhuma roupa | AI Pipeline, Result Screen | Threshold confiança < 60% |
| FR-009 | Geração try-on | AI Pipeline, PAL | FASHN.ai primary |
| FR-010 | Tentar novamente | Result Screen, AI Pipeline | Seed diferente, conta como trial |
| FR-011 | Trocar foto | Result Screen, Photo Manager | Seletor de fotos cadastradas |
| FR-012 | Fallback modelos IA | PAL | Chain-of-responsibility |
| FR-013 | Compartilhar com branding | Result Screen | Share Intent + bitmap overlay |
| FR-014 | Feedback thumbs up/down | Result Screen, Backend Service | Async, não bloqueia UX |
| FR-015 | Limite 3 grátis/dia | Billing Manager | SubscriptionState in Room |
| FR-016 | Exibição de ads | Billing Manager, Result Screen | AdMob integration |
| FR-017 | Pacotes/assinatura | Billing Manager | Play Billing v6+ |
| FR-018 | In-app purchase | Billing Manager | Google Play Billing Library |
| FR-019 | Coleta dataset | Backend Service | Apenas thumbs up, anonimizado |
| FR-020 | Roteamento inteligente | PAL, Backend Service | Score por (modelo × roupa) |
| FR-021 | Pipeline fine-tuning | Backend Service | Cloud Storage organizado |

### 12.2 NFR → Solution Traceability

| NFR ID | NFR Name | Solution | Validation |
|--------|----------|----------|------------|
| NFR-001 | Detecção < 3s | ML Kit (< 100ms) + GPT-4o Vision | p95 latency monitoring |
| NFR-002 | Geração < 15s | Timeout + fallback chain | p90 latency monitoring |
| NFR-003 | Overlay < 50MB | Lightweight service, bitmap recycling | Android Profiler |
| NFR-004 | Criptografia | Tink AES-256-GCM + TLS 1.2+ | Security audit |
| NFR-005 | Imagens efêmeras | In-memory only, no persistence | Code audit |
| NFR-006 | LGPD | ConsentManager, opt-in, delete data | Legal review |
| NFR-007 | Dados terceiros | Data minimization, no PII to APIs | ToS review |
| NFR-008 | Offline parcial | Room cache, offline-first UI | Airplane mode test |
| NFR-009 | Graceful degradation | Fallback chain, friendly errors | API failure simulation |
| NFR-010 | Fluxo 2 toques | Auto-pipeline on tap | UX testing |
| NFR-011 | Android 8.0+ | minSdk 26 | Multi-API testing |
| NFR-012 | PT-BR | Externalized strings | Manual review |
| NFR-013 | Apps e-commerce | SYSTEM_ALERT_WINDOW + MediaProjection | Test on Shopee/Shein |
| NFR-014 | Play Store | Justified permissions, privacy policy | Pre-submission checklist |
| NFR-015 | Anonimização | No PII in dataset, hashed device ID | Schema audit |

### 12.3 Trade-offs

#### Trade-off 1: Client-Heavy vs Backend Proxy

**Decision:** App chama APIs de IA diretamente (client-heavy)

| | Gain | Lose |
|-|------|------|
| ✓ | Fotos pessoais nunca saem do device (privacy-first) | |
| ✓ | Menor latência (sem hop intermediário) | |
| ✗ | | API keys expostas no client (mitigação: obfuscação + rate limiting) |
| ✗ | | Menos controle de custos (cada device faz chamadas independentes) |

**Rationale:** Privacy é o driver #1. API keys no client é risco aceitável para MVP com monitoramento.

**Migration path:** v2 — backend proxy que adiciona API key server-side, mantendo fotos no client.

---

#### Trade-off 2: Firebase vs Backend Próprio

**Decision:** Firebase para MVP, com interface abstrata para migração futura

| | Gain | Lose |
|-|------|------|
| ✓ | Zero infra para manter, free tier generoso | |
| ✓ | Velocidade de desenvolvimento (SDK pronto) | |
| ✗ | | Vendor lock-in (mitigação: BackendService interface) |
| ✗ | | Cold starts em Cloud Functions (~500ms) |

**Rationale:** Para MVP, velocidade de entrega > controle total. Interface abstrata mantém porta aberta.

---

#### Trade-off 3: Gemini Flash vs On-Device YOLO

**Decision:** Gemini Flash (cloud) para detecção de roupa

| | Gain | Lose |
|-|------|------|
| ✓ | Alta qualidade sem treinamento custom | |
| ✓ | Entendimento semântico (tipo de roupa, descrição) | |
| ✗ | | Requer internet para detecção |
| ✗ | | Custo por chamada (mitigação: free tier + eficiência) |

**Rationale:** YOLO detecta objetos mas não entende moda. Gemini Flash retorna descrição semântica necessária para o prompt de geração.

---

#### Trade-off 4: Imagens Efêmeras vs Cache de Resultados

**Decision:** Zero persistência de imagens geradas

| | Gain | Lose |
|-|------|------|
| ✓ | Privacidade máxima (privacy-first) | |
| ✓ | Simplicidade (sem gerenciar cache) | |
| ✗ | | Usuária não pode revisitar resultados antigos |
| ✗ | | Cada "tentar novamente" requer nova chamada de API |

**Rationale:** Decisão deliberada — privacy > conveniência. Histórico de resultados está no "Out of Scope" do PRD.

---

## Validation Checklist

- [x] Todos os 21 FRs têm component assignments
- [x] Todos os 15 NFRs têm soluções arquiteturais
- [x] Escolhas tecnológicas justificadas com trade-offs
- [x] Trade-offs documentados (4 decisões principais)
- [x] Segurança endereçada (criptografia, auth, LGPD, anonimização)
- [x] Path de escalabilidade definido (MVP → Growth → Scale)
- [x] Data model definido (6 entidades + storage strategy)
- [x] API contracts especificados (4 APIs externas + 4 endpoints backend)
- [x] Testing strategy definida (Unit, Integration, UI, E2E)
- [x] Deployment approach definido (CI/CD com GitHub Actions)

---

## Summary

| Metric | Value |
|--------|-------|
| **Pattern** | Client-Heavy + Firebase Serverless |
| **Components** | 8 major components |
| **Tech Stack** | Kotlin 1.9+, Compose, Hilt, ML Kit, FASHN.ai/Vertex AI, Firebase |
| **FRs Addressed** | 21/21 |
| **NFRs Addressed** | 15/15 |
| **Primary Driver** | Privacy-First (LGPD, imagens efêmeras, criptografia) |

---

## Next Steps

### Phase 4: Sprint Planning

Run `/bmad:sprint-planning` to:
- Break 5 epics into detailed user stories (~18-27 stories)
- Estimate story complexity
- Plan sprint iterations
- Begin implementation

You now have complete planning documentation:
- ✓ Product Brief
- ✓ PRD (21 FRs, 15 NFRs, 5 Epics)
- ✓ Architecture

Implementation teams have everything needed to build GhostFit!

---

**This document was created using BMAD Method v6 - Phase 3 (Architecture)**

*To continue: Run `/bmad:sprint-planning` to begin Phase 4.*
