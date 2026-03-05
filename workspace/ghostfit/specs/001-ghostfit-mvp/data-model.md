# Data Model: GhostFit MVP

**Feature Branch**: `001-ghostfit-mvp`
**Data**: 2026-03-04

---

## Entidades

### 1. UserProfile (Local — Room DB)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `id` | String (UUID) | auto-gerado | Identificador único local |
| `lgpdConsentGranted` | Boolean | obrigatório `true` para operar | Consentimento LGPD explícito |
| `lgpdConsentTimestamp` | Long (epoch ms) | não-nulo se consent=true | Registro do momento do consentimento |
| `onboardingCompleted` | Boolean | — | Setup inicial finalizado |
| `planType` | Enum: FREE, PREMIUM | default FREE | Plano ativo da usuária |
| `dailyTriesUsed` | Int | 0..3 para FREE | Tentativas usadas no dia |
| `dailyTriesResetDate` | String (yyyy-MM-dd) | — | Data do último reset de tentativas |
| `overlayPositionX` | Float | — | Posição X salva do overlay |
| `overlayPositionY` | Float | — | Posição Y salva do overlay |
| `createdAt` | Long (epoch ms) | auto | Data de criação |

**Transições de estado — planType**:
```
FREE → PREMIUM  (via Google Play Billing purchase)
PREMIUM → FREE  (assinatura expirada/cancelada)
```

**Regra de negócio — dailyTriesUsed**:
- Reset para 0 quando `LocalDate.now() > dailyTriesResetDate`
- Incrementa SOMENTE em geração bem-sucedida (detecção falha ou erro de API NÃO consome tentativa)
- Usuárias PREMIUM: campo ignorado (sem limite)

---

### 2. ReferencePhoto (Local — filesDir criptografado)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `id` | String (UUID) | auto-gerado | Identificador único |
| `userId` | String (UUID) | FK → UserProfile | Dona da foto |
| `encryptedFilePath` | String | path em filesDir | Caminho do arquivo .enc |
| `displayName` | String | opcional | Nome amigável ("Foto 1") |
| `isBodyFullVisible` | Boolean | validado no cadastro | Validação de corpo inteiro |
| `createdAt` | Long (epoch ms) | auto | Data de cadastro |

**Constraints**:
- Máximo 3 fotos por `userId`
- Arquivo criptografado com Tink `StreamingAead` (AES-256-GCM)
- Deletado junto com app (internal storage) e sob revogação LGPD

---

### 3. TryOnSession (Efêmera — em memória apenas)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `sessionId` | String (UUID) | auto-gerado | Identificador da sessão |
| `referencePhotoId` | String (UUID) | FK → ReferencePhoto | Foto usada |
| `screenshotBitmap` | Bitmap | não-persistido | Screenshot capturado |
| `detectedGarment` | GarmentInfo? | nullable | Roupa detectada (ou null) |
| `generatedImage` | Bitmap? | não-persistido | Resultado do try-on |
| `modelUsed` | String | "fashn" \| "vertex" | Modelo de IA utilizado |
| `status` | Enum | IDLE → CAPTURING → DETECTING → GENERATING → DONE / ERROR / NO_GARMENT | Estado atual |
| `errorMessage` | String? | nullable | Mensagem de erro ou "nenhuma roupa detectada" |
| `startedAt` | Long (epoch ms) | auto | Início da sessão |
| `durationMs` | Long | calculado | Duração total |

**IMPORTANTE**: Esta entidade é 100% efêmera. Nenhum dado persiste após fechar a tela de resultado. Conformidade LGPD — sem persistência de imagens geradas.

**Transições de estado — status**:
```
IDLE → CAPTURING          (pipeline iniciado)
CAPTURING → DETECTING     (screenshot OK)
DETECTING → GENERATING    (roupa detectada com confiança ≥ 60%)
DETECTING → NO_GARMENT    (nenhuma roupa detectada — "Nenhuma roupa detectada")
DETECTING → ERROR         (falha na detecção)
GENERATING → DONE         (imagem gerada com sucesso)
GENERATING → ERROR        (timeout/falha em ambos os modelos)
```

---

### 4. GarmentInfo (Value Object — efêmero)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `category` | Enum: TOP, BOTTOM, DRESS, OUTERWEAR | obrigatório | Tipo da roupa |
| `color` | String | opcional | Cor principal |
| `description` | String | opcional | Descrição curta |
| `confidence` | Float | 0.0..1.0, threshold ≥ 0.6 | Confiança da detecção |
| `croppedImage` | Bitmap | não-persistido | Imagem recortada da roupa |

---

### 5. FeedbackRecord (Enviado ao backend — não persiste localmente)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `sessionId` | String (UUID) | referência à sessão | Sessão avaliada |
| `thumbsUp` | Boolean | true=up, false=down | Avaliação da usuária |
| `modelUsed` | String | "fashn" \| "vertex" | Modelo que gerou |
| `garmentCategory` | String | categoria da roupa | Tipo de roupa |
| `timestamp` | Long (epoch ms) | auto | Momento do feedback |

**Comportamento**: Enviado ao backend via POST assíncrono. Se falhar, descartado (best-effort para MVP). Não contém fotos pessoais nem imagens geradas — apenas metadados anonimizados.

---

### 6. SubscriptionState (Local — sincronizado com Play Billing)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `userId` | String (UUID) | FK → UserProfile | Usuária |
| `purchaseToken` | String? | nullable | Token da compra ativa |
| `productId` | String? | nullable | ID do produto/assinatura |
| `purchaseType` | Enum: NONE, PACK, MONTHLY | default NONE | Tipo de compra |
| `isActive` | Boolean | derivado | Se tem acesso premium |
| `expiresAt` | Long? (epoch ms) | nullable | Expiração (se assinatura) |
| `acknowledgedAt` | Long? (epoch ms) | nullable | Quando foi acknowledged |

**Regra crítica**: Acknowledge toda compra em até 3 dias via `BillingClient.acknowledgePurchase()`.

---

### 7. ModelScore (Backend — para roteamento inteligente)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `modelId` | String | "fashn" \| "vertex" | Modelo de IA |
| `garmentCategory` | String | categoria da roupa | Tipo de roupa |
| `thumbsUpCount` | Int | ≥ 0 | Total de aprovações |
| `thumbsDownCount` | Int | ≥ 0 | Total de reprovações |
| `approvalRate` | Float | calculado | thumbsUp / total |
| `totalFeedbacks` | Int | calculado | Total de feedbacks |

**Regra de roteamento**: Só usar score para roteamento quando `totalFeedbacks ≥ 50` para a combinação (modelo x categoria). Abaixo disso, usar modelo primário (FASHN.ai).

---

## Diagrama de Relacionamentos

```
UserProfile 1──→ 0..3 ReferencePhoto
     │
     └──→ 1 SubscriptionState

TryOnSession (efêmera, em memória)
     ├──→ 1 ReferencePhoto (referência)
     ├──→ 1 GarmentInfo (value object)
     └──→ 0..1 FeedbackRecord (enviado ao backend)

ModelScore (backend, agregado de FeedbackRecords)
```

## Armazenamento por Camada

| Entidade | Onde | Criptografia | Persistência |
|----------|------|--------------|--------------|
| UserProfile | Room DB local | Não (sem PII sensível) | Permanente |
| ReferencePhoto | filesDir/.enc | Tink AES-256-GCM | Até desinstalação/revogação LGPD |
| TryOnSession | Memória (ViewModel) | N/A | Efêmera — descartada ao fechar |
| GarmentInfo | Memória | N/A | Efêmera |
| FeedbackRecord | Backend (POST) | TLS em trânsito | Anonimizado no backend |
| SubscriptionState | Room DB local | Não | Sync com Play Billing |
| ModelScore | Backend DB | N/A | Permanente (agregado) |
