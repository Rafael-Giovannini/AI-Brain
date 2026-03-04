# Research: GhostFit MVP — Provador Virtual com Overlay

**Feature Branch**: `001-ghostfit-mvp`
**Data**: 2026-03-04
**Status**: Completo

---

## 1. Android Overlay (SYSTEM_ALERT_WINDOW)

**Decisão**: Foreground Service + `TYPE_APPLICATION_OVERLAY` via `WindowManager`

**Racional**:
- `TYPE_APPLICATION_OVERLAY` é o único tipo não-deprecado no API 26+ (Android 8.0+)
- Requer Foreground Service — Android 15 exige janela overlay visível para manter o serviço
- Permissão via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` com redirect para configurações
- Play Store aceita overlay para assistentes de compras/provador virtual, desde que claramente justificado na listagem

**Alternativas descartadas**:
- Accessibility Service: permissão mais intrusiva, escrutínio maior da Play Store
- Picture-in-Picture (PiP): controle de layout muito limitado, não permite posicionamento livre

---

## 2. Captura de Tela (Screenshot)

**Decisão**: MediaProjection API + `ImageReader` / `VirtualDisplay`

**Racional**:
- Única API legítima para capturar conteúdo de outros apps
- Requer `foregroundServiceType="mediaProjection"` no manifest (obrigatório Android 14+)
- Consentimento do usuário necessário a cada sessão (sem "lembrar escolha")
- Apps com `FLAG_SECURE` renderizam como retângulo preto (DRM)
- Captura sob demanda (toque no overlay), não contínua — minimiza bateria/memória

**Alternativa MVP**: Share sheet nativa (usuária compartilha imagem do produto direto com GhostFit). Elimina fricção do MediaProjection mas perde o "toque mágico no fantasminha". Considerar como fallback se Play Store rejeitar combinação overlay+MediaProjection.

**Alternativas descartadas**:
- Accessibility Service: retorna metadados, não pixels
- Root-based: inviável para Play Store

---

## 3. Modelos de IA para Virtual Try-On (VTON)

**Decisão**: FASHN.ai API (primário) + Google Vertex AI Virtual Try-On (secundário/fallback)

**Racional**:
- **NanoBanana** é o nome informal do Gemini 2.5 Flash na feature de shopping do Google — NÃO é uma API standalone de VTON. Não construir dependência nesse nome.
- **Grok** (xAI) é gerador de imagens genérico, não possui pipeline de warping de roupas. Não recomendado para VTON.
- **FASHN.ai v1.5**: API REST hospedada, $0.075/imagem, 576x864px, maskless pixel-space. Melhor API de produção disponível.
- **Google Vertex AI VTON** (`virtual-try-on-001`): Atualizado Set/2025 com melhor preservação de formato corporal. Boa opção para GCP.

**Input pattern**: imagem da pessoa + imagem da roupa → imagem gerada

**Alternativas descartadas**:
- IDM-VTON (open source): qualidade muito alta mas requer GPU A100 para self-host — inviável para MVP
- OOTDiffusion: não suporta roupas de parte inferior do corpo
- Kolors VTON: disponível via Pixazo, menor documentação

---

## 4. Detecção de Roupas em Screenshots

**Decisão**: Pipeline de 2 camadas: ML Kit Object Detection (on-device) → GPT-4o Vision / Claude Vision (cloud)

**Racional**:
- **Camada 1 — ML Kit**: Detecção on-device com categoria "Fashion good". Retorna bounding box em milissegundos, sem custo, sem rede. Localiza a roupa na screenshot.
- **Camada 2 — Vision LLM**: Imagem recortada enviada para GPT-4o ou Claude para extrair metadados estruturados (categoria: top/bottom/dress, cor, descrição, confiança).
- ML Kit sozinho retorna apenas "Fashion good" sem subcategoria — insuficiente para a API de VTON que precisa de `category: "tops"`.
- Vision LLM sozinho na screenshot inteira (1080x2400px) é lento e caro. ML Kit primeiro reduz payload em ~90%.

**Alternativas descartadas**:
- Google Vision API Product Search: requer setup de catálogo, overkill para MVP
- Custom TFLite/AutoML: requer dados de treino rotulados, inviável para MVP
- Full-image LLM sem crop: viável para MVP ultra-simplificado mas mais caro e lento

---

## 5. Framework de UI: Kotlin + Jetpack Compose

**Decisão**: Jetpack Compose para UI padrão + `ComposeView` com lifecycle manual para o overlay

**Racional**:
- Compose é o toolkit oficial recomendado pelo Google para novos projetos Android
- `WindowManager.addView()` exige `View` — usar `ComposeView` como wrapper
- Lifecycle manual via `ViewTreeLifecycleOwner` e `ViewTreeSavedStateRegistryOwner` no Service
- Padrão bem documentado com exemplos de produção funcionais (2025+)

**Alternativas descartadas**:
- XML Views para overlay: funciona sem concerns de lifecycle mas perde benefícios do Compose
- Flutter: suporte instável para overlays via `WindowManager`

---

## 6. Google Play Billing Library

**Decisão**: GPBL 8.3.0 + Kotlin extensions (`billing-ktx`)

**Racional**:
- Desde 31/Ago/2025, apps novos DEVEM usar GPBL 7.0.0+. Para novos projetos, começar em 8.x.
- GPBL 8.0 renomeou "in-app items" para "one-time products" e adicionou múltiplas opções de compra.
- **Regra crítica**: acknowledge toda compra em até 3 dias ou Google faz refund automático.
- Verificação server-side obrigatória para produção (Google Play Developer API).
- Real-time Developer Notifications (RTDNs) via Google Cloud Pub/Sub para lifecycle de assinatura.

**Alternativa recomendada para simplificar**: RevenueCat SDK (grátis até $2.500 MRR). Abstrai toda complexidade de billing, webhooks e analytics. Fortemente recomendado se equipe não tem experiência com billing backend.

---

## 7. Armazenamento Local de Imagens com Criptografia

**Decisão**: Android Keystore + Google Tink `StreamingAead` (AES-256-GCM) + `filesDir` (internal storage)

**Racional**:
- `EncryptedSharedPreferences` e `EncryptedFile` do `androidx.security:security-crypto` foram **deprecados em Abril/2025**. NÃO usar.
- Tink Android 1.12.0 é a alternativa oficial recomendada pelo Google.
- Chaves armazenadas no Android Keystore (hardware-backed em dispositivos com TEE/StrongBox).
- `StreamingAead` previne ataques de reordenamento de ciphertext.
- `filesDir` é privado ao app, deletado na desinstalação — ideal para fotos de referência LGPD-compliant.

**Alternativas descartadas**:
- `EncryptedFile` (deprecado): ainda funciona mas sem manutenção
- SQLCipher: overkill para 3 arquivos de imagem
- Community fork `encrypted-shared-preferences`: stopgap, não solução de longo prazo

---

## Tabela Resumo de Decisões

| Tópico | Decisão | Versão/Ferramenta | Risco Principal |
|--------|---------|-------------------|-----------------|
| Overlay | Foreground Service + `TYPE_APPLICATION_OVERLAY` | API 26+ | Android 15 enforcement de visibilidade |
| Captura de Tela | MediaProjection API | Android 14+ FGS type | Fricção de consentimento; `FLAG_SECURE` |
| Modelo VTON | FASHN.ai primário, Vertex AI secundário | FASHN v1.5 / `virtual-try-on-001` | Custo $0.075/imagem em escala |
| Detecção de Roupa | ML Kit ODT + GPT-4o/Claude Vision | ML Kit 17.x / `gpt-4o` | Latência LLM cloud (~1-2s) |
| UI Framework | Jetpack Compose + `ComposeView` no overlay | Compose 1.7+ | Lifecycle manual no overlay |
| Billing | GPBL 8.3.0 + Kotlin extensions | `billing-ktx:8.3.0` | Acknowledge em 3 dias |
| Storage | Android Keystore + Tink `StreamingAead` | Tink Android 1.12.0 | `EncryptedFile` deprecado |
