# Sprint Plan: GhostFit

**Data:** 2026-03-04
**Scrum Master:** rafael.giovannini
**Nível do Projeto:** 2
**Total de Stories:** 22
**Total de Pontos:** 103 pontos
**Fases Planejadas:** 4
**Capacidade:** 1 dev mid-level (~8 pontos/semana)
**Previsão de Conclusão:** ~13 semanas

---

## Resumo Executivo

Plano de implementação do GhostFit organizado em 4 fases lógicas que seguem a ordem natural de dependências: infraestrutura → core experience → inteligência → monetização. Cada fase entrega valor incremental e pode ser validada independentemente.

**Métricas:**
- Total Stories: 22
- Total Pontos: 103
- Fases: 4
- Capacidade: ~8 pontos/semana (1 dev, 5h/dia, 1pt = 3h)
- Previsão: ~13 semanas

---

## Inventário de Stories

---

### EPIC-001: Onboarding e Permissões

#### STORY-001: Setup do Projeto Android

**Epic:** EPIC-001
**Prioridade:** Must Have

**User Story:**
Como desenvolvedor,
Quero ter o projeto Android configurado com toda a stack tecnológica definida na arquitetura,
Para que eu possa começar a implementar features com a base correta.

**Acceptance Criteria:**
- [ ] Projeto Kotlin com Gradle (Kotlin DSL) e version catalogs
- [ ] Jetpack Compose configurado
- [ ] Hilt (DI) configurado com módulos base
- [ ] Room database configurado
- [ ] Retrofit + OkHttp configurado
- [ ] Coil configurado
- [ ] Ktlint + Detekt configurados
- [ ] JUnit 5 + MockK configurados
- [ ] minSdk = 26, targetSdk = 35
- [ ] Estrutura de pacotes conforme arquitetura

**Notas Técnicas:**
Seguir stack definida na arquitetura (seção 3). Criar módulos base para DI.

**Dependências:** Nenhuma

**Pontos:** 5

---

#### STORY-002: Consentimento LGPD

**Epic:** EPIC-001
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero dar consentimento explícito sobre o uso dos meus dados antes de usar o app,
Para ter controle sobre minha privacidade conforme a LGPD.

**Acceptance Criteria:**
- [ ] Tela de termos de uso e política de privacidade em PT-BR
- [ ] Checkbox de consentimento explícito (não pré-marcado)
- [ ] Consentimento registrado com timestamp no Room (`UserProfile`)
- [ ] Opção de revogar consentimento em Configurações
- [ ] Sem consentimento, app não acessa fotos pessoais
- [ ] ConsentManager implementado conforme interface da arquitetura

**Notas Técnicas:**
Implementar `ConsentManager` interface. Persistir consentimento via Room (`UserProfile.lgpdConsentGranted` + `lgpdConsentTimestamp`).

**Dependências:** STORY-001 (projeto configurado)

**Pontos:** 3

---

#### STORY-003: Permissão SYSTEM_ALERT_WINDOW com Onboarding

**Epic:** EPIC-001
**Prioridade:** Must Have

**User Story:**
Como usuária nova,
Quero entender por que o app precisa da permissão de overlay com uma explicação clara,
Para me sentir segura ao conceder acesso.

**Acceptance Criteria:**
- [ ] Tela de onboarding explicando a permissão com linguagem simples
- [ ] Redireciona para Settings do Android para ativação
- [ ] Detecta quando permissão foi concedida e prossegue automaticamente
- [ ] Funciona em Android 8.0+ (API 26+)
- [ ] Design consistente com UX do app

**Notas Técnicas:**
Usar `Settings.canDrawOverlays()` e `ACTION_MANAGE_OVERLAY_PERMISSION`. Lifecycle-aware para detectar retorno.

**Dependências:** STORY-001, STORY-002

**Pontos:** 3

---

#### STORY-004: Cadastro de Fotos via Google Fotos

**Epic:** EPIC-001
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero selecionar fotos de corpo inteiro do meu Google Fotos,
Para que o app tenha minha referência visual para o try-on.

**Acceptance Criteria:**
- [ ] Integração funcional com Google Photos API (ou picker nativo)
- [ ] Usuária pode selecionar 1 ou mais fotos de corpo inteiro
- [ ] Validação de que a foto contém pessoa de corpo inteiro visível
- [ ] Fotos armazenadas localmente com criptografia (Android Keystore)
- [ ] PhotoManager implementado conforme interface da arquitetura
- [ ] Opção de deletar fotos cadastradas

**Notas Técnicas:**
Implementar `PhotoManager` interface. Validação de corpo inteiro pode usar ML Kit Pose Detection (local). Criptografia via Tink `StreamingAead` (AES-256-GCM) + `filesDir` conforme research.md.

**Dependências:** STORY-002 (consentimento LGPD obrigatório antes)

**Pontos:** 8

---

### EPIC-002: Overlay e Captura

#### STORY-005: Overlay Flutuante (Fantasminha)

**Epic:** EPIC-002
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero ver o fantasminha flutuando sobre o Shopee/Shein,
Para acessar o try-on sem sair do app de compras.

**Acceptance Criteria:**
- [ ] Overlay visível sobre qualquer app
- [ ] Overlay persiste ao navegar entre apps
- [ ] Overlay não bloqueia interação com o app hospedeiro
- [ ] Design do fantasminha GhostFit reconhecível
- [ ] Overlay pode ser fechado/minimizado pela usuária
- [ ] Foreground Service com notificação persistente
- [ ] Uso de memória do overlay < 50MB

**Notas Técnicas:**
Implementar `OverlayService` como Foreground Service. Usar `WindowManager` com `TYPE_APPLICATION_OVERLAY`. Compose não suporta overlay — usar View-based overlay com WindowManager.

**Dependências:** STORY-003 (permissão SYSTEM_ALERT_WINDOW)

**Pontos:** 8

---

#### STORY-006: Overlay Reposicionável (Drag)

**Epic:** EPIC-002
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero arrastar o fantasminha para onde quiser na tela,
Para que ele não atrapalhe minha navegação.

**Acceptance Criteria:**
- [ ] Overlay responde a gesture de drag/long-press
- [ ] Posição salva entre sessões (SharedPreferences)
- [ ] Overlay não pode ser arrastado para fora da tela
- [ ] Animação suave durante reposicionamento

**Notas Técnicas:**
Touch listener no overlay view. Usar `WindowManager.LayoutParams` para atualizar posição. Persistir posição no SharedPreferences.

**Dependências:** STORY-005

**Pontos:** 3

---

#### STORY-007: Captura de Tela ao Tocar no Overlay

**Epic:** EPIC-002
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero tocar no fantasminha e capturar a tela automaticamente,
Para provar a roupa com um único gesto.

**Acceptance Criteria:**
- [ ] Um toque no overlay inicia captura de tela
- [ ] Screenshot capturado em < 1 segundo
- [ ] Captura inclui conteúdo completo da tela visível
- [ ] Feedback visual (animação ou loading)
- [ ] Screenshot temporário — deletado após processamento
- [ ] Permissão MediaProjection solicitada na primeira vez

**Notas Técnicas:**
Usar `MediaProjection` API. Requer user consent flow (uma vez). Bitmap mantido em memória, não salvo em disco.

**Dependências:** STORY-005

**Pontos:** 5

---

### EPIC-003: Motor de IA (Detecção + Geração + Aprendizado)

#### STORY-008: Provider Abstraction Layer (PAL)

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero uma camada de abstração para providers de IA,
Para trocar entre modelos sem alterar o resto do código.

**Acceptance Criteria:**
- [ ] Interface `VisionProvider` implementada
- [ ] Interface `GenerationProvider` implementada
- [ ] Interface `ProviderRouter` implementada
- [ ] Chain-of-responsibility para fallback (Primary → Secondary → Error)
- [ ] Timeout configurável por provider (default 20s)
- [ ] Retry automático (até 2x) antes de fallback
- [ ] Log de qual modelo foi usado por geração

**Notas Técnicas:**
Seguir interfaces definidas na arquitetura (seção 4.3). Injeção via Hilt modules.

**Dependências:** STORY-001

**Pontos:** 5

---

#### STORY-009: Detecção de Roupa via IA de Visão

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero que o app detecte automaticamente a roupa na foto do produto,
Para não precisar recortar nada manualmente.

**Acceptance Criteria:**
- [ ] Detecta roupa principal na imagem do produto
- [ ] Retorna bounding box ou segmentação da roupa
- [ ] Identifica tipo de roupa (vestido, blusa, calça, etc.)
- [ ] Processamento em < 3 segundos (95% dos casos em 4G)
- [ ] Funciona com layouts típicos de Shopee e Shein
- [ ] Implementação ML Kit Object Detection como detecção on-device (bounding box + crop)
- [ ] Implementação GPT-4o Vision como classificação remota (categoria, cor, descrição, confiança)

**Notas Técnicas:**
Pipeline de 2 camadas conforme research.md: ML Kit (on-device, < 100ms) para localizar roupa na screenshot via bounding box, seguido de GPT-4o Vision para extrair metadados estruturados (tipo, cor, descrição, confiança) da imagem recortada. Implementar `MlKitObjectDetector` e `GptVisionClassifier`.

**Dependências:** STORY-008 (PAL)

**Pontos:** 5

---

#### STORY-010: Aviso "Nenhuma Roupa Detectada"

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero ver uma mensagem clara quando o app não encontrar roupa na tela,
Para entender que devo tentar em uma página de produto.

**Acceptance Criteria:**
- [ ] Mensagem: "Nenhuma roupa detectada. Tente em uma página de produto."
- [ ] Threshold de confiança (< 60% = não detectado)
- [ ] Botão para tentar novamente
- [ ] Tentativa com falha NÃO conta no limite diário

**Notas Técnicas:**
Tratar `TryOnResult.NoClothingDetected` no pipeline. UI overlay ou mini-dialog.

**Dependências:** STORY-009

**Pontos:** 2

---

#### STORY-011: Geração de Imagem Virtual Try-On

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero ver uma imagem realista de mim vestindo a roupa,
Para decidir se compro ou não.

**Acceptance Criteria:**
- [ ] Imagem gerada mostra a usuária vestindo a roupa detectada
- [ ] Fisionomia e tom de pele preservados fielmente
- [ ] Caimento realista para o biotipo da usuária
- [ ] Resolução mínima 512x512
- [ ] Geração completa em < 15 segundos (90% em 4G)
- [ ] Implementação FASHN.ai como primary
- [ ] Implementação Vertex AI VTON como fallback

**Notas Técnicas:**
Implementar `FashnProvider` e `VertexAiProvider`. Enviar foto de referência (decriptada em memória) + roupa detectada. Imagem gerada mantida apenas em memória.

**Dependências:** STORY-008 (PAL), STORY-009 (detecção), STORY-004 (fotos de referência)

**Pontos:** 8

---

#### STORY-012: Tela de Resultado do Try-On

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero ver o resultado do try-on em uma tela clara com ações disponíveis,
Para avaliar a imagem e decidir próximos passos.

**Acceptance Criteria:**
- [ ] Imagem gerada exibida em destaque (overlay ou activity)
- [ ] Botão "Tentar novamente" (regenerar com seed diferente)
- [ ] Botão "Trocar foto" (abrir seletor de fotos)
- [ ] Botões thumbs up/down visíveis
- [ ] Botão "Compartilhar"
- [ ] Loading indicator durante regeneração
- [ ] Cada ação (tentar novamente, trocar foto) conta como tentativa

**Notas Técnicas:**
Compose Screen com ViewModel. Orquestrar ações via AI Pipeline. FR-010 e FR-011 resolvidos aqui.

**Dependências:** STORY-011

**Pontos:** 5

---

#### STORY-013: AI Pipeline Orquestrador

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero um pipeline que orquestre captura → detecção → geração → resultado,
Para que o fluxo completo funcione de forma integrada.

**Acceptance Criteria:**
- [ ] Pipeline recebe screenshot do overlay e executa fluxo completo
- [ ] Gerencia estados: detectando → gerando → resultado/erro
- [ ] Integra fallback chain (via PAL)
- [ ] Retorna `TryOnResult` (Success / NoClothingDetected / Error)
- [ ] Graceful degradation — mensagem amigável em falha de API
- [ ] Retry com backoff exponencial em erros de rede
- [ ] Tentativa com falha de API NÃO conta no limite diário

**Notas Técnicas:**
Implementar `AiPipeline` interface da arquitetura. Coroutines para async. Conectar overlay → pipeline → tela de resultado.

**Dependências:** STORY-007 (captura), STORY-009 (detecção), STORY-011 (geração)

**Pontos:** 5

---

### EPIC-004: Compartilhamento e Feedback

#### STORY-014: Compartilhar Resultado com Branding

**Epic:** EPIC-004
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero compartilhar o resultado com amigas via WhatsApp/Instagram,
Para pedir opinião antes de comprar.

**Acceptance Criteria:**
- [ ] Botão "Compartilhar" na tela de resultado
- [ ] Imagem inclui branding GhostFit (marca d'água sutil ou rodapé)
- [ ] Integra com share sheet nativa do Android
- [ ] Funciona com WhatsApp, Instagram, Telegram e outros
- [ ] Branding não compromete qualidade visual

**Notas Técnicas:**
Bitmap manipulation para adicionar branding. `Intent.ACTION_SEND` com FileProvider para compartilhar.

**Dependências:** STORY-012 (tela de resultado)

**Pontos:** 3

---

#### STORY-015: Feedback Thumbs Up/Down

**Epic:** EPIC-004
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero dar thumbs up/down na imagem gerada,
Para ajudar o app a melhorar.

**Acceptance Criteria:**
- [ ] Botões thumbs up/down visíveis na tela de resultado
- [ ] Feedback registrado com: modelo usado, tipo de roupa, timestamp
- [ ] Feedback enviado ao Firebase de forma assíncrona
- [ ] Feedback não bloqueia a experiência da usuária
- [ ] TryOnFeedback entity conforme data model da arquitetura

**Notas Técnicas:**
Implementar `BackendService.submitFeedback()`. Firebase Functions para receber e armazenar no Firestore.

**Dependências:** STORY-012 (tela de resultado)

**Pontos:** 3

---

#### STORY-016: Backend Firebase — Feedback API + Remote Config

**Epic:** EPIC-004
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero um backend leve no Firebase para receber feedback e servir configurações,
Para alimentar o motor de aprendizado e controlar o app remotamente.

**Acceptance Criteria:**
- [ ] Firebase project configurado (Functions + Firestore + Remote Config)
- [ ] Cloud Function para receber feedback (POST)
- [ ] Remote Config com: modelo primário, preços, limites
- [ ] Firestore collection para feedback records
- [ ] FirebaseBackendService implementado conforme arquitetura
- [ ] Analytics + Crashlytics configurados

**Notas Técnicas:**
Implementar `FirebaseBackendService` conforme interface `BackendService`. Cloud Functions em TypeScript/Node.

**Dependências:** STORY-001

**Pontos:** 5

---

### EPIC-005: Monetização

#### STORY-017: Limite de 3 Tentativas Grátis/Dia

**Epic:** EPIC-005
**Prioridade:** Must Have

**User Story:**
Como usuária free,
Quero usar 3 try-ons grátis por dia,
Para experimentar o app sem pagar.

**Acceptance Criteria:**
- [ ] Contador de tentativas visível na interface
- [ ] Bloqueia geração após 3 tentativas (exibe oferta de upgrade)
- [ ] Contador reseta à meia-noite horário local
- [ ] Tentativas com "nenhuma roupa detectada" NÃO contam
- [ ] Tentativas com falha de API NÃO contam
- [ ] Persistência segura do contador via Room (`UserProfile.dailyTriesUsed` + `dailyTriesResetDate`)

**Notas Técnicas:**
Implementar `BillingManager.consumeTrial()`. Usar `UserProfile.dailyTriesUsed` + `dailyTriesResetDate` (Room) conforme data-model.md.

**Dependências:** STORY-013 (pipeline integrado)

**Pontos:** 3

---

#### STORY-018: Exibição de Anúncios (AdMob)

**Epic:** EPIC-005
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero exibir anúncios para usuárias free em momentos estratégicos,
Para gerar receita que cubra custos de API.

**Acceptance Criteria:**
- [ ] Anúncios exibidos para usuárias free
- [ ] Anúncios não interrompem o fluxo core (não antes do resultado)
- [ ] Formatos: banner na tela principal + interstitial entre gerações
- [ ] Integração com AdMob
- [ ] Usuárias premium não veem anúncios

**Notas Técnicas:**
Google AdMob SDK. Interstitial entre a 2ª e 3ª tentativa. Banner na home.

**Dependências:** STORY-017 (distinção free/premium)

**Pontos:** 3

---

#### STORY-019: Google Play Billing (Compras e Assinaturas)

**Epic:** EPIC-005
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero assinar um plano mensal ou comprar pacotes extras,
Para usar o try-on sem restrições.

**Acceptance Criteria:**
- [ ] Google Play Billing Library v6+ integrada
- [ ] Suporte a compras avulsas (10 tentativas por R$X)
- [ ] Suporte a assinatura mensal (uso ilimitado, sem ads)
- [ ] Verificação de compra (local ou server-side)
- [ ] Restauração de compras em reinstalação
- [ ] Compliance com políticas do Google Play

**Notas Técnicas:**
Implementar `BillingManager` interface. BillingClient flow completo (queryProducts → launchBillingFlow → handlePurchase → acknowledge).

**Dependências:** STORY-017

**Pontos:** 8

---

#### STORY-020: Tela de Upgrade / Planos

**Epic:** EPIC-005
**Prioridade:** Must Have

**User Story:**
Como usuária,
Quero ver uma tela clara comparando planos free e premium,
Para decidir se faço upgrade.

**Acceptance Criteria:**
- [ ] Tela de upgrade com comparação de planos
- [ ] Pelo menos 2 opções: pacote avulso e assinatura mensal
- [ ] Preços carregados via Remote Config
- [ ] Exibida quando atinge limite de 3 tentativas
- [ ] Acessível também via menu/configurações

**Notas Técnicas:**
Compose Screen. Preços via Firebase Remote Config para atualizar sem deploy.

**Dependências:** STORY-019

**Pontos:** 3

---

### Stories de Motor de Aprendizado (EPIC-003 — Fase 4)

#### STORY-021: Coleta de Dados para Dataset de Treinamento

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero coletar pares de treinamento anonimizados das gerações aprovadas,
Para construir dataset para futuro fine-tuning de modelo próprio.

**Acceptance Criteria:**
- [ ] Pares coletados apenas quando usuária dá thumbs up
- [ ] Dados completamente anonimizados — sem fotos pessoais
- [ ] Metadados: modelo usado, tipo de roupa, parâmetros, timestamp
- [ ] Armazenamento em Firebase Cloud Storage criptografado
- [ ] Organizado por categoria de roupa e modelo gerador
- [ ] Volume tracking: contagem de pares por categoria

**Notas Técnicas:**
Implementar `BackendService.submitDatasetEntry()`. AnonymizedDataEntry sem PII.

**Dependências:** STORY-015 (feedback), STORY-016 (Firebase backend)

**Pontos:** 5

---

#### STORY-022: Roteamento Inteligente entre Modelos

**Epic:** EPIC-003
**Prioridade:** Must Have

**User Story:**
Como sistema,
Quero rotear entre modelos de IA com base no histórico de aprovação,
Para melhorar a qualidade das imagens ao longo do tempo.

**Acceptance Criteria:**
- [ ] Score de aprovação calculado por (modelo × tipo de roupa)
- [ ] Roteamento automático para modelo com melhor score
- [ ] Fallback para distribuição uniforme quando dados insuficientes
- [ ] Threshold mínimo: 50+ feedbacks antes de alterar roteamento
- [ ] Log de performance por modelo
- [ ] ModelScore entity conforme data model da arquitetura

**Notas Técnicas:**
Implementar `ProviderRouter` com lógica de scoring. Buscar scores do Firestore via `BackendService.getModelScores()`.

**Dependências:** STORY-021 (dataset), STORY-008 (PAL)

**Pontos:** 5

---

## Organização por Fases

---

### Fase 1: Fundação (Semanas 1-3) — 27 pontos

**Objetivo:** App funcional com onboarding completo — usuária consegue se cadastrar, conceder permissões e cadastrar fotos de referência.

| Story | Título | Pontos | FRs |
|-------|--------|--------|-----|
| STORY-001 | Setup do Projeto Android | 5 | Infra |
| STORY-002 | Consentimento LGPD | 3 | FR-003 |
| STORY-003 | Permissão SYSTEM_ALERT_WINDOW | 3 | FR-002 |
| STORY-004 | Cadastro de Fotos via Google Fotos | 8 | FR-001 |
| STORY-005 | Overlay Flutuante (Fantasminha) | 8 | FR-004 |

**Total:** 27 pontos (~3.4 semanas)

**Riscos:**
- Google Photos API pode ter restrições — fallback: picker nativo do Android
- Overlay view-based (não Compose) requer abordagem diferente

---

### Fase 2: Core Experience (Semanas 4-7) — 33 pontos

**Objetivo:** Fluxo completo de try-on funcional — toque no fantasminha → resultado com imagem gerada.

| Story | Título | Pontos | FRs |
|-------|--------|--------|-----|
| STORY-006 | Overlay Reposicionável (Drag) | 3 | FR-006 |
| STORY-007 | Captura de Tela ao Toque | 5 | FR-005 |
| STORY-008 | Provider Abstraction Layer | 5 | FR-012 |
| STORY-009 | Detecção de Roupa via IA | 5 | FR-007 |
| STORY-010 | Aviso "Nenhuma Roupa Detectada" | 2 | FR-008 |
| STORY-011 | Geração de Imagem Try-On | 8 | FR-009 |
| STORY-012 | Tela de Resultado | 5 | FR-010, FR-011 |

**Total:** 33 pontos (~4.1 semanas)

**Riscos:**
- Qualidade da geração de imagem pode não atender expectativas — precisa de benchmark
- MediaProjection requer consent flow específico
- Latência de APIs de IA pode exceder limites definidos

---

### Fase 3: Monetização e Social (Semanas 8-10) — 22 pontos

**Objetivo:** App monetizável — ads, compras, compartilhamento e feedback funcionando.

| Story | Título | Pontos | FRs |
|-------|--------|--------|-----|
| STORY-013 | AI Pipeline Orquestrador | 5 | FR-009, FR-012 |
| STORY-014 | Compartilhar com Branding | 3 | FR-013 |
| STORY-015 | Feedback Thumbs Up/Down | 3 | FR-014 |
| STORY-016 | Backend Firebase | 5 | FR-014, FR-019 |
| STORY-017 | Limite 3 Tentativas/Dia | 3 | FR-015 |
| STORY-020 | Tela de Upgrade/Planos | 3 | FR-017 |

**Total:** 22 pontos (~2.8 semanas)

**Nota:** STORY-013 (pipeline) conecta Fase 2 com o fluxo completo end-to-end.

**Riscos:**
- Firebase setup pode levar mais tempo que estimado se primeiro projeto Firebase

---

### Fase 4: Inteligência e Billing (Semanas 11-13) — 21 pontos

**Objetivo:** Motor de aprendizado ativo + billing completo — app pronto para Play Store.

| Story | Título | Pontos | FRs |
|-------|--------|--------|-----|
| STORY-018 | Anúncios AdMob | 3 | FR-016 |
| STORY-019 | Google Play Billing | 8 | FR-018 |
| STORY-021 | Coleta de Dados para Dataset | 5 | FR-019, FR-021 |
| STORY-022 | Roteamento Inteligente | 5 | FR-020 |

**Total:** 21 pontos (~2.6 semanas)

**Nota:** Header anterior dizia 16 pts — corrigido para 21 pts conforme soma real (3+8+5+5).

**Riscos:**
- Google Play Billing tem complexidade alta (fluxo de compra, verificação, restauração)
- Roteamento inteligente precisa de volume mínimo de feedbacks para funcionar

---

## Rastreabilidade Epic → Story

| Epic ID | Epic | Stories | Total Pontos | Fase |
|---------|------|---------|--------------|------|
| EPIC-001 | Onboarding e Permissões | STORY-001, 002, 003, 004 | 19 | 1 |
| EPIC-002 | Overlay e Captura | STORY-005, 006, 007 | 16 | 1-2 |
| EPIC-003 | Motor de IA | STORY-008, 009, 010, 011, 012, 013, 021, 022 | 40 | 2-4 |
| EPIC-004 | Compartilhamento e Feedback | STORY-014, 015, 016 | 11 | 3 |
| EPIC-005 | Monetização | STORY-017, 018, 019, 020 | 17 | 3-4 |
| **TOTAL** | **5 Epics** | **22 Stories** | **103 pts** | **4 fases** |

---

## Cobertura de Requisitos Funcionais

| FR ID | FR | Story | Fase |
|-------|----|-------|------|
| FR-001 | Cadastro de fotos via Google Fotos | STORY-004 | 1 |
| FR-002 | Permissão SYSTEM_ALERT_WINDOW | STORY-003 | 1 |
| FR-003 | Consentimento LGPD | STORY-002 | 1 |
| FR-004 | Overlay flutuante | STORY-005 | 1 |
| FR-005 | Captura de tela ao toque | STORY-007 | 2 |
| FR-006 | Overlay reposicionável (drag) | STORY-006 | 2 |
| FR-007 | Detecção de roupa via IA | STORY-009 | 2 |
| FR-008 | Aviso "nenhuma roupa detectada" | STORY-010 | 2 |
| FR-009 | Geração de imagem try-on | STORY-011, STORY-013 | 2-3 |
| FR-010 | Opção "tentar novamente" | STORY-012 | 2 |
| FR-011 | Opção "trocar foto" | STORY-012 | 2 |
| FR-012 | Fallback entre modelos de IA | STORY-008, STORY-013 | 2-3 |
| FR-013 | Compartilhar com branding | STORY-014 | 3 |
| FR-014 | Feedback thumbs up/down | STORY-015, STORY-016 | 3 |
| FR-015 | Limite 3 tentativas/dia | STORY-017 | 3 |
| FR-016 | Exibição de anúncios | STORY-018 | 4 |
| FR-017 | Pacotes pagos / assinatura | STORY-019, STORY-020 | 4 |
| FR-018 | Google Play Billing | STORY-019 | 4 |
| FR-019 | Coleta de dados para dataset | STORY-021 | 4 |
| FR-020 | Roteamento inteligente | STORY-022 | 4 |
| FR-021 | Pipeline de dados para fine-tuning | STORY-021 | 4 |

**Cobertura: 21/21 FRs (100%)**

---

## Riscos e Mitigação

**Alto:**
- **Qualidade da geração de imagem** — A imagem pode não parecer realista o suficiente para aprovação das usuárias. Mitigação: benchmark de modelos na Fase 2, fallback entre providers.
- **Google Play Billing complexity** — Fluxo de billing tem muitos edge cases. Mitigação: seguir samples oficiais do Google, testar com sandbox.
- **Políticas da Play Store para overlay** — App pode ser rejeitado. Mitigação: documentar justificativa do overlay, seguir guidelines da Play Store.

**Médio:**
- **Custo por geração de API** — Custo real pode inviabilizar modelo freemium. Mitigação: monitorar custo desde Fase 2, ajustar limites via Remote Config.
- **Google Photos API restrições** — API pode ter limitações. Mitigação: fallback para picker nativo do Android (photo picker).
- **Latência de APIs de IA** — Pode exceder 15s. Mitigação: timeout + fallback + feedback visual de progresso.

**Baixo:**
- **Compatibilidade com apps de e-commerce** — Overlay pode ser bloqueado por alguns apps. Mitigação: testar em Shopee, Shein, AliExpress.
- **Volume de feedbacks insuficiente** — Roteamento inteligente precisa de dados. Mitigação: distribuição uniforme como default, threshold de 50+ feedbacks.

---

## Dependências Externas

- **Android Photo Picker** — Seleção nativa de fotos (sem OAuth)
- **ML Kit Object Detection** — Detecção de roupa on-device (< 100ms)
- **GPT-4o Vision** — Classificação remota de tipo/cor de roupa
- **FASHN.ai v1.5** — Geração de imagem try-on (primário)
- **Google Vertex AI VTON** — Fallback para geração
- **Google Play Billing Library v6+** — Compras e assinaturas
- **Google AdMob** — Anúncios
- **Firebase** — Functions, Firestore, Remote Config, Analytics, Crashlytics, Cloud Storage

---

## Definition of Done

Para uma story ser considerada completa:
- [ ] Código implementado e commitado
- [ ] Testes unitários escritos e passando (≥80% coverage)
- [ ] Testes de integração passando (quando aplicável)
- [ ] UI em Português Brasileiro
- [ ] Sem hardcoded strings (usar string resources)
- [ ] Ktlint + Detekt sem violações
- [ ] Spec atualizada se necessário
- [ ] Funcional em Android 8.0+ (API 26)

---

## Próximos Passos

**Imediato:** Começar Fase 1

Para iniciar a implementação:
1. `/speckit.specify` — Gerar spec técnica para STORY-001
2. `/ralph-loop` — Executar loop TDD para implementar

Ou diretamente:
- `/bmad:dev-story STORY-001` — Implementar setup do projeto

**Ordem recomendada de execução:**
```
STORY-001 → STORY-002 → STORY-003 → STORY-004 → STORY-005
    → STORY-006 + STORY-007 (paralelo) → STORY-008 → STORY-009
    → STORY-010 → STORY-011 → STORY-012 → STORY-013
    → STORY-014 + STORY-015 + STORY-016 (paralelo)
    → STORY-017 → STORY-018 → STORY-019 → STORY-020
    → STORY-021 → STORY-022
```

---

**Este plano foi criado usando BMAD Method v6 — Fase 4 (Sprint Planning)**

*Para continuar: Execute `/bmad:workflow-status` para ver seu progresso e próximo workflow recomendado.*
