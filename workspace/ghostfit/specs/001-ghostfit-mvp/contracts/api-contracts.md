# API Contracts: GhostFit MVP

**Feature Branch**: `001-ghostfit-mvp`
**Data**: 2026-03-04

---

## Visão Geral

O GhostFit MVP possui duas categorias de interfaces externas:

1. **APIs de terceiros consumidas** pelo app (FASHN.ai, Vertex AI, ML Kit, Vision LLM)
2. **API backend própria** para feedback, roteamento de modelos e analytics

---

## 1. APIs de Terceiros Consumidas

### 1.1 FASHN.ai — Virtual Try-On (Primário)

```
POST https://api.fashn.ai/v1/run
Authorization: Bearer {FASHN_API_KEY}
Content-Type: application/json

Request:
{
  "model_image": "<base64 da foto de referência>",
  "garment_image": "<base64 da roupa recortada>",
  "category": "tops" | "bottoms" | "one-pieces"
}

Response 200:
{
  "id": "run_abc123",
  "status": "completed",
  "output": {
    "image_url": "https://...",
    "image_base64": "<base64 da imagem gerada>"
  }
}

Response 4xx/5xx:
{
  "error": { "message": "...", "code": "..." }
}

Timeout: 10 segundos (depois → fallback para Vertex AI)
Retry: até 2x com backoff antes de acionar fallback
```

### 1.2 Google Vertex AI — Virtual Try-On (Fallback)

```
POST https://{REGION}-aiplatform.googleapis.com/v1/projects/{PROJECT}/locations/{REGION}/publishers/google/models/virtual-try-on-001:predict
Authorization: Bearer {GCP_ACCESS_TOKEN}
Content-Type: application/json

Request:
{
  "instances": [{
    "person_image": { "bytesBase64Encoded": "<base64>" },
    "garment_image": { "bytesBase64Encoded": "<base64>" },
    "garment_type": "TOP" | "BOTTOM" | "FULL"
  }],
  "parameters": { "sampleCount": 1 }
}

Response 200:
{
  "predictions": [{
    "bytesBase64Encoded": "<base64 da imagem gerada>"
  }]
}

Timeout: 15 segundos
Retry: até 2x com backoff
```

### 1.3 Vision LLM — Detecção de Metadados de Roupa

```
POST https://api.openai.com/v1/chat/completions
Authorization: Bearer {OPENAI_API_KEY}
Content-Type: application/json

Request:
{
  "model": "gpt-4o",
  "messages": [{
    "role": "user",
    "content": [
      { "type": "text", "text": "Analise esta imagem de roupa. Retorne JSON: {\"category\": \"top|bottom|dress|outerwear\", \"color\": \"<cor>\", \"description\": \"<1 frase>\", \"confidence\": 0.0-1.0}" },
      { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,<cropped_garment>" } }
    ]
  }],
  "max_tokens": 150,
  "response_format": { "type": "json_object" }
}

Response 200:
{
  "choices": [{
    "message": {
      "content": "{\"category\": \"top\", \"color\": \"vermelho\", \"description\": \"Blusa de manga curta\", \"confidence\": 0.92}"
    }
  }]
}

Timeout: 5 segundos
Fallback: se falhar, usar apenas categoria "tops" como default
```

### 1.4 ML Kit Object Detection (On-Device)

```kotlin
// Interface interna — sem chamada de rede
// Input: InputImage (do screenshot Bitmap)
// Output: List<DetectedObject> com boundingBox e labels

val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(SINGLE_IMAGE_MODE)
    .enableClassification()
    .build()

val detector = ObjectDetection.getClient(options)
detector.process(inputImage)
    .addOnSuccessListener { objects ->
        // Filtrar por label "Fashion good"
        // Retornar boundingBox para crop
    }

Latência: < 100ms (on-device)
```

---

## 2. API Backend GhostFit (Própria)

### Base URL
```
https://api.ghostfit.app/v1  (produção)
http://localhost:8080/v1      (desenvolvimento)
```

### 2.1 POST /feedback — Enviar Feedback de Try-On

```
POST /v1/feedback
Content-Type: application/json
X-Device-Id: {UUID do dispositivo}

Request:
{
  "sessionId": "uuid-da-sessao",
  "thumbsUp": true,
  "modelUsed": "fashn" | "vertex",
  "garmentCategory": "top" | "bottom" | "dress" | "outerwear",
  "timestamp": 1709568000000
}

Response 201:
{ "status": "received" }

Response 429:
{ "error": "rate_limit", "retryAfterMs": 5000 }
```

**Notas**:
- Fire-and-forget do lado do app (não bloquear UX)
- Sem dados pessoais (fotos, userId real) — apenas metadados anonimizados
- Rate limit: 10 req/min por device

### 2.2 GET /models/route — Obter Modelo Recomendado

```
GET /v1/models/route?garmentCategory={category}
X-Device-Id: {UUID do dispositivo}

Response 200:
{
  "recommendedModel": "fashn" | "vertex",
  "fallbackModel": "vertex" | "fashn",
  "reason": "approval_rate" | "default"
}

Response 200 (dados insuficientes):
{
  "recommendedModel": "fashn",
  "fallbackModel": "vertex",
  "reason": "default"
}
```

**Regra**: Só retorna roteamento baseado em score quando há ≥ 50 feedbacks para a combinação (modelo × categoria). Caso contrário, retorna FASHN como default.

### 2.3 GET /v1/config — Configuração Remota

```
GET /v1/config

Response 200:
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

### 2.4 GET /v1/health — Health Check

```
GET /v1/health

Response 200:
{ "status": "ok", "timestamp": 1709568000000 }
```

### 2.5 POST /v1/dataset — Entrada de Dataset Anonimizado

Envia dados anonimizados para dataset de treinamento (apenas quando thumbs up).

```
POST /v1/dataset

Request:
{
  "clothingType": "dress",
  "clothingDescription": "Red floral summer dress",
  "modelUsed": "fashn",
  "generationTimeMs": 8500,
  "approved": true,
  "timestamp": 1709568000000
}

Response 201:
{ "id": "ds_abc123" }
```

---

## 3. Mapeamento de Categorias

O app precisa mapear entre diferentes formatos de categoria nos diversos serviços:

| App Interno | FASHN.ai | Vertex AI | Vision LLM |
|-------------|----------|-----------|------------|
| `TOP` | `"tops"` | `"TOP"` | `"top"` |
| `BOTTOM` | `"bottoms"` | `"BOTTOM"` | `"bottom"` |
| `DRESS` | `"one-pieces"` | `"FULL"` | `"dress"` |
| `OUTERWEAR` | `"tops"` | `"TOP"` | `"outerwear"` |

---

## 4. Tratamento de Erros (Padrão)

Todos os erros de API seguem o padrão:

```json
{
  "error": {
    "code": "string",
    "message": "string human-readable"
  }
}
```

### Códigos de erro do app para a usuária:

| Cenário | Mensagem para Usuária |
|---------|----------------------|
| Sem internet | "Sem conexão. Verifique sua internet e tente novamente." |
| Timeout ambos modelos | "Serviço temporariamente indisponível. Tente novamente em instantes." |
| Nenhuma roupa detectada | "Nenhuma roupa detectada. Tente em uma página de produto." |
| Foto sem corpo inteiro | "Essa foto não parece conter uma pessoa de corpo inteiro. Tente outra." |
| Limite diário atingido | "Você usou suas 3 tentativas de hoje! Faça upgrade para continuar." |
| Play Store indisponível | "Não foi possível processar a compra. Tente novamente mais tarde." |
| Geração em andamento | "Geração em andamento..." (toast) |
