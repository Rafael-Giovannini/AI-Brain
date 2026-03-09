# Product Requirements Document: GhostFit

**Date:** 2026-03-04
**Author:** rafael.giovannini
**Version:** 1.0
**Project Type:** Android Native (Kotlin)
**Project Level:** 2
**Status:** Draft

---

## Document Overview

This Product Requirements Document (PRD) defines the functional and non-functional requirements for GhostFit. It serves as the source of truth for what will be built and provides traceability from requirements through implementation.

**Related Documents:**
- Product Brief: `docs/product-brief-ghostfit-2026-03-04.md`

---

## Executive Summary

GhostFit é um aplicativo Android que funciona como um overlay flutuante (fantasminha) sobre apps de e-commerce como Shopee e Shein. Com um único toque, o app captura a tela, detecta a roupa exibida usando IA de visão, e gera uma imagem realista do usuário vestindo aquela roupa — permitindo um "provador virtual" antes da compra.

O público-alvo principal são mulheres de classe média (18-45 anos) que compram moda online e querem eliminar a incerteza de como a roupa ficará nelas. Nenhum marketplace oferece virtual try-on hoje — GhostFit é o primeiro a resolver isso.

O sistema inclui um motor de aprendizado que usa o feedback das usuárias para melhorar continuamente a qualidade das imagens geradas, com roteamento inteligente entre modelos de IA e pipeline de dados para futuro fine-tuning de modelo próprio.

---

## Product Goals

### Business Objectives

1. **Validação de produto (0-3 meses):** Lançar MVP na Play Store e validar que a qualidade das imagens geradas é aprovada pelas usuárias
2. **Crescimento orgânico (3-6 meses):** Atingir base inicial de usuárias ativas através de compartilhamento viral (feature de compartilhar resultado com branding)
3. **Sustentabilidade (6-12 meses):** Receita de anúncios + assinaturas cobre custos de API
4. **B2B (12+ meses):** Usar tração do app como vitrine para vender tecnologia/SDK para marketplaces

### Success Metrics

- Usuárias ativas mensais (MAU) — métrica principal
- Taxa de aprovação das imagens geradas ≥ 70% (feedback thumbs up/down)
- Número de compartilhamentos por usuária (indicador de viralidade)
- Taxa de conversão free → pago
- Avaliação na Play Store (meta: 4.0+)
- Custo por geração de imagem (viabilidade econômica)

---

## Functional Requirements

Functional Requirements (FRs) define **what** the system does - specific features and behaviors.

Each requirement includes:
- **ID**: Unique identifier (FR-001, FR-002, etc.)
- **Priority**: Must Have / Should Have / Could Have / Won't Have (MoSCoW)
- **Description**: What the system should do
- **Acceptance Criteria**: How to verify it's complete

---

### FR-001: Cadastro de Fotos Pessoais via Photo Picker Nativo

**Priority:** Must Have

**Description:**
Usuária seleciona fotos de corpo inteiro a partir do Android Photo Picker nativo (galeria do dispositivo, inclui Google Fotos) para servir como referência visual no try-on. O sistema deve validar que a foto contém uma pessoa de corpo inteiro.

**Acceptance Criteria:**
- [ ] Integração funcional com Android Photo Picker nativo (sem OAuth)
- [ ] Usuária pode selecionar 1 ou mais fotos de corpo inteiro
- [ ] Sistema valida que a foto contém pessoa de corpo inteiro visível
- [ ] Fotos são armazenadas localmente com criptografia

**Dependencies:** FR-003 (consentimento LGPD)

---

### FR-002: Permissão SYSTEM_ALERT_WINDOW com Onboarding Explicativo

**Priority:** Must Have

**Description:**
App solicita permissão de overlay com tela de onboarding que explica claramente por que a permissão é necessária e como será usada, antes de redirecionar para settings do Android.

**Acceptance Criteria:**
- [ ] Tela de onboarding explica a necessidade da permissão com linguagem simples
- [ ] Redireciona para settings do Android para ativação
- [ ] Detecta quando permissão foi concedida e prossegue automaticamente
- [ ] Funciona em Android 8.0+ (API 26+)

**Dependencies:** Nenhuma

---

### FR-003: Consentimento LGPD Explícito

**Priority:** Must Have

**Description:**
Antes de acessar qualquer foto pessoal, o sistema exibe termos de uso e política de privacidade, exigindo consentimento explícito (opt-in) da usuária conforme LGPD.

**Acceptance Criteria:**
- [ ] Termos de uso e política de privacidade exibidos de forma clara
- [ ] Checkbox de consentimento explícito (não pré-marcado)
- [ ] Consentimento registrado com timestamp
- [ ] Opção de revogar consentimento a qualquer momento
- [ ] Sem consentimento, app não acessa fotos pessoais

**Dependencies:** Nenhuma

---

### FR-004: Overlay Flutuante sobre Qualquer App

**Priority:** Must Have

**Description:**
Ícone de fantasma (fantasminha GhostFit) exibido como overlay flutuante persistente sobre qualquer aplicativo aberto no dispositivo.

**Acceptance Criteria:**
- [ ] Overlay visível sobre Shopee, Shein e qualquer outro app
- [ ] Overlay persiste ao navegar entre apps
- [ ] Overlay não bloqueia interação com o app hospedeiro
- [ ] Overlay tem design do fantasminha GhostFit reconhecível
- [ ] Overlay pode ser fechado/minimizado pela usuária

**Dependencies:** FR-002 (permissão SYSTEM_ALERT_WINDOW)

---

### FR-005: Captura de Tela Automática ao Tocar no Overlay

**Priority:** Must Have

**Description:**
Ao tocar no overlay (fantasminha), o sistema captura automaticamente a tela atual do dispositivo para análise de IA.

**Acceptance Criteria:**
- [ ] Um toque no overlay inicia captura de tela
- [ ] Screenshot capturado em < 1 segundo
- [ ] Captura inclui conteúdo completo da tela visível
- [ ] Feedback visual para a usuária (animação ou indicador de loading)
- [ ] Screenshot temporário, deletado após processamento

**Dependencies:** FR-004 (overlay funcional)

---

### FR-006: Overlay Reposicionável (Drag)

**Priority:** Must Have

**Description:**
Usuária pode arrastar o overlay para qualquer posição na tela, evitando que ele bloqueie conteúdo importante.

**Acceptance Criteria:**
- [ ] Overlay responde a gesture de drag/long-press
- [ ] Posição é salva entre sessões
- [ ] Overlay não pode ser arrastado para fora da tela
- [ ] Animação suave durante o reposicionamento

**Dependencies:** FR-004 (overlay funcional)

---

### FR-007: Detecção Automática de Roupa via IA de Visão

**Priority:** Must Have

**Description:**
Pipeline de 2 camadas: ML Kit Object Detection (on-device) localiza a roupa no screenshot via bounding box, seguido de Gemini 2.0 Flash (primário) ou GPT-4o Vision (fallback) para classificação de metadados (tipo, cor, descrição, confiança). Decisão técnica documentada em `specs/001-ghostfit-mvp/research.md`.

**Acceptance Criteria:**
- [ ] Detecta roupa principal na imagem do produto
- [ ] Retorna bounding box ou segmentação da roupa
- [ ] Identifica tipo de roupa (vestido, blusa, calça, etc.)
- [ ] Processamento em < 3 segundos
- [ ] Funciona com layouts típicos de Shopee e Shein

**Dependencies:** FR-005 (captura de tela)

---

### FR-008: Aviso "Nenhuma Roupa Detectada"

**Priority:** Must Have

**Description:**
Quando o modelo de visão não encontra roupa na screenshot (ex: usuária tocou em tela de configurações), exibe mensagem amigável.

**Acceptance Criteria:**
- [ ] Mensagem clara: "Nenhuma roupa detectada. Tente em uma página de produto."
- [ ] Threshold de confiança definido (ex: < 60% = não detectado)
- [ ] Botão para tentar novamente
- [ ] Sem cobrança de tentativa quando roupa não é detectada

**Dependencies:** FR-007 (detecção de roupa)

---

### FR-009: Geração de Imagem Virtual Try-On

**Priority:** Must Have

**Description:**
Modelo generativo (FASHN.ai/Vertex AI) cria imagem realista da usuária vestindo a roupa detectada, mantendo fisionomia, tom de pele e proporções corporais da foto de referência.

**Acceptance Criteria:**
- [ ] Imagem gerada mostra a usuária vestindo a roupa detectada
- [ ] Fisionomia e tom de pele preservados fielmente
- [ ] Caimento da roupa é realista para o biotipo da usuária
- [ ] Resolução mínima aceitável (512x512+)
- [ ] Geração completa em < 15 segundos

**Dependencies:** FR-001 (foto de referência), FR-007 (roupa detectada)

---

### FR-010: Opção "Tentar Novamente"

**Priority:** Must Have

**Description:**
Na tela de resultado, botão permite regerar a imagem com os mesmos inputs (mesma roupa + mesma foto), obtendo variação diferente.

**Acceptance Criteria:**
- [ ] Botão "Tentar novamente" visível na tela de resultado
- [ ] Regenera com variação (seed diferente ou temperatura alterada)
- [ ] Conta como tentativa adicional no limite diário
- [ ] Loading indicator durante regeneração

**Dependencies:** FR-009 (geração de imagem)

---

### FR-011: Opção "Trocar Foto do Usuário"

**Priority:** Must Have

**Description:**
Na tela de resultado, botão permite trocar a foto de referência da usuária e regerar o try-on com a mesma roupa mas foto diferente.

**Acceptance Criteria:**
- [ ] Botão "Trocar foto" visível na tela de resultado
- [ ] Abre seletor de fotos previamente cadastradas
- [ ] Regenera try-on com nova foto + mesma roupa
- [ ] Conta como tentativa adicional no limite diário

**Dependencies:** FR-001 (fotos cadastradas), FR-009 (geração de imagem)

---

### FR-012: Fallback entre Modelos de IA

**Priority:** Must Have

**Description:**
Sistema abstrai a camada de IA com providers intercambiáveis. Se o modelo primário falhar (timeout, erro, indisponibilidade), automaticamente tenta o modelo secundário.

**Acceptance Criteria:**
- [ ] Cadeia de fallback configurável (FASHN.ai primário → Vertex AI fallback)
- [ ] Timeout por provider (FASHN.ai: 10s, Vertex AI: 15s)
- [ ] Usuária não percebe a troca — experiência transparente
- [ ] Log de qual modelo foi usado para cada geração
- [ ] Retry automático até 2x antes de fallback

**Dependencies:** FR-009 (geração de imagem)

---

### FR-013: Compartilhar Resultado com Branding GhostFit

**Priority:** Must Have

**Description:**
Usuária pode compartilhar a imagem gerada via share sheet do Android (WhatsApp, Instagram, etc.) com marca d'água/branding sutil do GhostFit.

**Acceptance Criteria:**
- [ ] Botão "Compartilhar" na tela de resultado
- [ ] Imagem incluí branding GhostFit (marca d'água sutil ou rodapé)
- [ ] Integra com share sheet nativa do Android
- [ ] Funciona com WhatsApp, Instagram, Telegram e outros
- [ ] Branding não compromete a qualidade visual da imagem

**Dependencies:** FR-009 (imagem gerada)

---

### FR-014: Feedback Thumbs Up/Down

**Priority:** Must Have

**Description:**
Na tela de resultado, botões de thumbs up/down permitem que a usuária avalie a qualidade da imagem gerada. Feedback é coletado e usado para melhoria do sistema.

**Acceptance Criteria:**
- [ ] Botões thumbs up/down visíveis na tela de resultado
- [ ] Feedback registrado com: modelo usado, tipo de roupa, resultado
- [ ] Feedback enviado ao backend de forma assíncrona
- [ ] Feedback não bloqueia a experiência da usuária
- [ ] Dados coletados alimentam FR-019 e FR-020

**Dependencies:** FR-009 (imagem gerada)

---

### FR-015: Limite de 3 Tentativas Grátis por Dia

**Priority:** Must Have

**Description:**
Usuárias no plano free têm limite de 3 gerações de try-on por dia. Contador reseta à meia-noite (horário local).

**Acceptance Criteria:**
- [ ] Contador de tentativas visível para a usuária
- [ ] Bloqueia geração após 3 tentativas (exibe oferta de upgrade)
- [ ] Contador reseta à meia-noite horário local
- [ ] Tentativas com "nenhuma roupa detectada" NÃO contam
- [ ] Persistência local do contador (não burlável por reinstalação fácil)

**Dependencies:** Nenhuma

---

### FR-016: Exibição de Anúncios (Ads)

**Priority:** Must Have

**Description:**
Usuárias no plano free veem anúncios em momentos estratégicos (entre gerações, na tela de resultado, etc.) via AdMob ou similar.

**Acceptance Criteria:**
- [ ] Anúncios exibidos para usuárias free
- [ ] Anúncios não interrompem o fluxo core (não antes do resultado)
- [ ] Formatos: banner ou interstitial entre gerações
- [ ] Integração com AdMob ou rede similar
- [ ] Usuárias premium não veem anúncios

**Dependencies:** FR-015 (distinção free/premium)

---

### FR-017: Pacotes Pagos / Assinatura Mensal

**Priority:** Must Have

**Description:**
Planos de monetização: pacotes avulsos (ex: 10 tentativas por R$X) e assinatura mensal para uso ilimitado, sem anúncios.

**Acceptance Criteria:**
- [ ] Pelo menos 2 opções: pacote avulso e assinatura mensal
- [ ] Tela de upgrade clara com comparação de planos
- [ ] Assinatura remove ads e limite de tentativas
- [ ] Pacote avulso adiciona tentativas extras
- [ ] Preços configuráveis remotamente

**Dependencies:** FR-018 (Google Play Billing)

---

### FR-018: In-App Purchase via Google Play Billing

**Priority:** Must Have

**Description:**
Integração com Google Play Billing Library para processar compras dentro do app (pacotes e assinaturas).

**Acceptance Criteria:**
- [ ] Integração com Google Play Billing Library v6+ (implementação atual: v8.3.0)
- [ ] Suporte a compras avulsas (one-time) e assinaturas (recurring)
- [ ] Verificação de compra server-side ou local
- [ ] Restauração de compras em reinstalação
- [ ] Compliance com políticas do Google Play

**Dependencies:** Nenhuma

---

### FR-019: Coleta de Dados para Dataset de Treinamento

**Priority:** Must Have

**Description:**
Cada geração aprovada (thumbs up) é coletada como par de treinamento anonimizado: (roupa detectada + parâmetros de geração + imagem gerada + feedback). Dados são enviados a storage seguro para futuro fine-tuning.

**Acceptance Criteria:**
- [ ] Pares coletados apenas quando usuária dá thumbs up
- [ ] Dados completamente anonimizados — sem fotos pessoais identificáveis
- [ ] Metadados incluem: modelo usado, tipo de roupa, parâmetros, timestamp
- [ ] Armazenamento em cloud storage seguro e criptografado
- [ ] Usuária é informada sobre coleta na política de privacidade
- [ ] Compliance LGPD mantida (dados anonimizados não são dados pessoais)

**Dependencies:** FR-014 (feedback), NFR-015 (anonimização)

---

### FR-020: Roteamento Inteligente entre Modelos de IA

**Priority:** Must Have

**Description:**
Sistema analisa score de aprovação acumulado por modelo e tipo de roupa. Automaticamente prioriza o modelo com melhor taxa de aprovação para cada categoria (ex: Vertex AI melhor para vestidos, FASHN.ai melhor para blusas).

**Acceptance Criteria:**
- [ ] Score de aprovação calculado por (modelo × tipo de roupa)
- [ ] Roteamento automático para modelo com melhor score após N gerações
- [ ] Fallback para distribuição uniforme quando dados insuficientes
- [ ] Dashboard/log de performance por modelo (interno, para o desenvolvedor)
- [ ] Threshold mínimo de dados antes de alterar roteamento (ex: 50+ feedbacks)

**Dependencies:** FR-014 (feedback), FR-019 (dados coletados)

---

### FR-021: Pipeline de Dados para Fine-Tuning de Modelo Próprio

**Priority:** Must Have

**Description:**
Infraestrutura de dados para acumular pares de treinamento aprovados, organizados e prontos para futuro fine-tuning de modelo proprietário GhostFit. Objetivo: reduzir dependência de APIs externas e criar diferencial competitivo.

**Acceptance Criteria:**
- [ ] Storage organizado por categoria de roupa e modelo gerador
- [ ] Formato de dados compatível com fine-tuning (ex: pares imagem-prompt)
- [ ] Volume tracking: dashboard mostrando quantidade de pares por categoria
- [ ] Export capability: dados podem ser extraídos para treinamento offline
- [ ] Política de retenção definida (dados mantidos por X meses)
- [ ] Custo de storage monitorado e alertas configurados

**Dependencies:** FR-019 (coleta de dados)

---

## Non-Functional Requirements

Non-Functional Requirements (NFRs) define **how** the system performs - quality attributes and constraints.

---

### NFR-001: Performance — Detecção de Roupa

**Priority:** Must Have

**Description:**
Detecção de roupa na screenshot deve completar em menos de 3 segundos após captura.

**Acceptance Criteria:**
- [ ] 95% das detecções completam em < 3s em conexão 4G

**Rationale:** Experiência fluida — mais que 3s a usuária perde interesse.

---

### NFR-002: Performance — Geração de Imagem

**Priority:** Must Have

**Description:**
Geração da imagem virtual try-on deve completar em menos de 15 segundos.

**Acceptance Criteria:**
- [ ] 90% das gerações completam em < 15s em conexão 4G
- [ ] Progress indicator visível durante espera

**Rationale:** 15s é tolerável com feedback visual, mais que isso causa abandono.

---

### NFR-003: Performance — Overlay

**Priority:** Must Have

**Description:**
Overlay flutuante não deve causar impacto perceptível na performance do app hospedeiro (Shopee, Shein, etc.).

**Acceptance Criteria:**
- [ ] Uso de memória do overlay < 50MB
- [ ] Sem frame drops ou lag perceptível no app hospedeiro
- [ ] Consumo de bateria incremental < 5% por hora de uso ativo

**Rationale:** Se o overlay travar o Shopee, a usuária desinstala imediatamente.

---

### NFR-004: Segurança — Criptografia em Trânsito e Repouso

**Priority:** Must Have

**Description:**
Todas as fotos pessoais e dados sensíveis devem ser criptografados em trânsito (TLS 1.2+) e em repouso (AES-256 ou Android Keystore).

**Acceptance Criteria:**
- [ ] TLS 1.2+ para todas as comunicações de rede
- [ ] Fotos locais criptografadas com Android Keystore
- [ ] Nenhum dado sensível em plain text no storage local

**Rationale:** Fotos pessoais são dados extremamente sensíveis.

---

### NFR-005: Privacidade — Imagens Efêmeras

**Priority:** Must Have

**Description:**
Imagens geradas pelo try-on NUNCA são armazenadas em servidores. Processamento deve ser stateless — resultado entregue e descartado.

**Acceptance Criteria:**
- [ ] Nenhuma imagem gerada persistida no servidor após entrega
- [ ] Screenshots temporários deletados do dispositivo após processamento
- [ ] Auditável: logs comprovam que nenhuma imagem foi retida

**Rationale:** Privacy by design — diferencial competitivo e compliance LGPD.

---

### NFR-006: Compliance — LGPD

**Priority:** Must Have

**Description:**
App em total conformidade com a Lei Geral de Proteção de Dados (LGPD): consentimento explícito, direito a exclusão, portabilidade, transparência.

**Acceptance Criteria:**
- [ ] Consentimento explícito (opt-in) antes de processar dados pessoais
- [ ] Política de privacidade acessível e em linguagem clara (PT-BR)
- [ ] Funcionalidade de exclusão de dados (direito ao esquecimento)
- [ ] Registro de consentimentos com timestamp
- [ ] DPO ou canal de contato para questões de privacidade

**Rationale:** Obrigação legal + confiança da usuária.

---

### NFR-007: Privacidade — Dados de Terceiros

**Priority:** Must Have

**Description:**
Nenhum dado pessoal da usuária é compartilhado com terceiros sem consentimento explícito e específico.

**Acceptance Criteria:**
- [ ] APIs de IA recebem apenas dados necessários para processamento
- [ ] Fotos pessoais não são enviadas a terceiros para treinamento de modelos
- [ ] Termos de uso das APIs de IA revisados para compliance

**Rationale:** Confiança da usuária e compliance LGPD.

---

### NFR-008: Confiabilidade — Modo Offline Parcial

**Priority:** Should Have

**Description:**
App funcional para navegação de configurações e galeria offline. Try-on requer conexão com internet.

**Acceptance Criteria:**
- [ ] Tela principal e configurações acessíveis offline
- [ ] Mensagem clara quando try-on não disponível offline
- [ ] Cache de fotos de referência disponível localmente

**Rationale:** Experiência não quebra completamente sem internet.

---

### NFR-009: Confiabilidade — Graceful Degradation

**Priority:** Must Have

**Description:**
Quando APIs de IA estão indisponíveis, o sistema exibe mensagem clara e amigável em vez de erro técnico.

**Acceptance Criteria:**
- [ ] Mensagem: "Serviço temporariamente indisponível. Tente novamente em alguns minutos."
- [ ] Retry automático com backoff exponencial
- [ ] Tentativa com falha de API NÃO conta no limite diário
- [ ] Status page ou health check para monitoramento

**Rationale:** APIs externas podem falhar — a experiência não pode quebrar.

---

### NFR-010: Usabilidade — Fluxo Mínimo

**Priority:** Must Have

**Description:**
Fluxo principal de try-on (toque → resultado) deve ser completado em no máximo 2 toques pela usuária.

**Acceptance Criteria:**
- [ ] Toque 1: tap no fantasminha (captura + detecção)
- [ ] Toque 0: resultado aparece automaticamente
- [ ] Nenhuma etapa intermediária obrigatória

**Rationale:** Simplicidade extrema é o diferencial UX. Público com nível técnico baixo.

---

### NFR-011: Compatibilidade — Android Mínimo

**Priority:** Must Have

**Description:**
App suporta Android 8.0 (API 26) e superior.

**Acceptance Criteria:**
- [ ] minSdkVersion = 26
- [ ] Testado em Android 8, 10, 12, 13, 14
- [ ] Funcional em smartphones mid-range (4GB RAM, processador Snapdragon 600+)

**Rationale:** Android 8+ cobre 95%+ dos dispositivos ativos no Brasil.

---

### NFR-012: Usabilidade — Idioma

**Priority:** Must Have

**Description:**
Interface completamente em Português Brasileiro.

**Acceptance Criteria:**
- [ ] Todos os textos, mensagens e labels em PT-BR
- [ ] Sem termos técnicos em inglês na interface
- [ ] Strings externalizadas para futura internacionalização

**Rationale:** Público-alvo brasileiro, nível técnico baixo-médio.

---

### NFR-013: Compatibilidade — Apps de E-commerce

**Priority:** Must Have

**Description:**
Overlay e captura de tela funcionam corretamente sobre Shopee, Shein e qualquer outro app de e-commerce.

**Acceptance Criteria:**
- [ ] Testado e funcional sobre Shopee, Shein, AliExpress, Renner, C&A
- [ ] Overlay não é bloqueado por esses apps
- [ ] Screenshot captura conteúdo correto independente do app

**Rationale:** Shopee e Shein são os marketplaces prioritários do público-alvo.

---

### NFR-014: Compliance — Google Play Store

**Priority:** Must Have

**Description:**
App em total conformidade com políticas da Google Play Store, especialmente para apps com overlay (SYSTEM_ALERT_WINDOW).

**Acceptance Criteria:**
- [ ] Uso de SYSTEM_ALERT_WINDOW justificado e documentado
- [ ] Apenas permissões estritamente necessárias solicitadas
- [ ] Política de privacidade publicada e linkada no listing
- [ ] Content rating adequado
- [ ] Sem violação de intellectual property

**Rationale:** Rejeição na Play Store = app não chega ao público.

---

### NFR-015: Privacidade — Anonimização do Dataset

**Priority:** Must Have

**Description:**
Dataset de treinamento (FR-019/FR-021) deve ser completamente anonimizado — sem dados pessoais identificáveis da usuária.

**Acceptance Criteria:**
- [ ] Fotos pessoais da usuária NUNCA incluídas no dataset
- [ ] Dataset contém apenas: roupa detectada, parâmetros de geração, resultado, feedback
- [ ] Impossível re-identificar usuária a partir dos dados do dataset
- [ ] Auditoria periódica de anonimização

**Rationale:** Compliance LGPD e ética — feedback melhora o sistema sem comprometer privacidade.

---

## Epics

Epics are logical groupings of related functionality that will be broken down into user stories during sprint planning (Phase 4).

Each epic maps to multiple functional requirements and will generate 2-10 stories.

---

### EPIC-001: Onboarding e Permissões

**Description:**
Setup inicial do app — onboarding explicativo, solicitação de permissões (overlay, fotos), consentimento LGPD e cadastro de fotos pessoais de referência via Google Fotos.

**Functional Requirements:**
- FR-001: Cadastro de fotos via Google Fotos
- FR-002: Permissão SYSTEM_ALERT_WINDOW com onboarding
- FR-003: Consentimento LGPD explícito

**Story Count Estimate:** 3-5

**Priority:** Must Have

**Business Value:**
Porta de entrada do produto. Sem onboarding funcional e permissões concedidas, nenhuma outra feature funciona. Onboarding bem feito reduz desinstalação por "não entendi como usar".

---

### EPIC-002: Overlay e Captura

**Description:**
Overlay flutuante (fantasminha) funcional sobre qualquer app, com captura de tela automática ao toque e reposicionamento drag.

**Functional Requirements:**
- FR-004: Overlay flutuante sobre qualquer app
- FR-005: Captura de tela automática ao toque
- FR-006: Overlay reposicionável (drag)

**Story Count Estimate:** 3-5

**Priority:** Must Have

**Business Value:**
Core UX do produto — a interação principal que diferencia o GhostFit de qualquer outra solução. O fantasminha é a identidade do produto.

---

### EPIC-003: Motor de IA (Detecção + Geração + Aprendizado)

**Description:**
Pipeline de IA completo: detecção de roupa na screenshot, geração de imagem virtual try-on, fallback entre modelos, roteamento inteligente baseado em feedback, e pipeline de dados para futuro fine-tuning.

**Functional Requirements:**
- FR-007: Detecção automática de roupa via IA
- FR-008: Aviso "nenhuma roupa detectada"
- FR-009: Geração de imagem virtual try-on
- FR-010: Opção "Tentar novamente"
- FR-011: Opção "Trocar foto do usuário"
- FR-012: Fallback entre modelos de IA
- FR-019: Coleta de dados para dataset de treinamento
- FR-020: Roteamento inteligente entre modelos
- FR-021: Pipeline de dados para fine-tuning

**Story Count Estimate:** 6-10

**Priority:** Must Have

**Business Value:**
Coração tecnológico do produto. A qualidade da IA define se a usuária aprova ou não a imagem, e o motor de aprendizado melhora continuamente essa qualidade — criando moat competitivo.

---

### EPIC-004: Compartilhamento e Feedback

**Description:**
Compartilhar resultado com branding GhostFit para viralidade e coletar feedback de qualidade (thumbs up/down) para alimentar o motor de aprendizado.

**Functional Requirements:**
- FR-013: Compartilhar resultado com branding
- FR-014: Feedback thumbs up/down

**Story Count Estimate:** 2-3

**Priority:** Must Have

**Business Value:**
Motor de crescimento viral (compartilhamento traz novas usuárias) e dados de feedback alimentam melhoria contínua da IA (FR-019/020). Dupla função: growth + qualidade.

---

### EPIC-005: Monetização

**Description:**
Sistema de monetização freemium completo: limite de tentativas grátis, exibição de anúncios, pacotes pagos, assinatura mensal via Google Play Billing.

**Functional Requirements:**
- FR-015: Limite de 3 tentativas grátis/dia
- FR-016: Exibição de anúncios (ads)
- FR-017: Pacotes pagos / assinatura mensal
- FR-018: In-app purchase via Google Play Billing

**Story Count Estimate:** 4-6

**Priority:** Must Have

**Business Value:**
Sustentabilidade econômica — receita de ads + assinaturas precisa cobrir custos de APIs de IA. Sem monetização, o produto não é viável a médio prazo.

---

## User Stories (High-Level)

User stories follow the format: "As a [user type], I want [goal] so that [benefit]."

These are preliminary stories. Detailed stories will be created in Phase 4 (Sprint Planning).

---

### EPIC-001: Onboarding e Permissões
- Como **usuária nova**, quero um onboarding claro que me explique por que cada permissão é necessária, para que eu me sinta segura ao conceder acesso.
- Como **usuária**, quero selecionar fotos de corpo inteiro do meu Google Fotos, para que o app tenha minha referência visual.
- Como **usuária**, quero dar consentimento explícito sobre o uso das minhas fotos (LGPD), para ter controle sobre meus dados.

### EPIC-002: Overlay e Captura
- Como **usuária**, quero ver o fantasminha flutuando sobre o Shopee/Shein, para acessar o try-on sem sair do app de compras.
- Como **usuária**, quero tocar no fantasminha e capturar a tela automaticamente, para provar a roupa com um único gesto.
- Como **usuária**, quero arrastar o fantasminha para onde quiser na tela, para que ele não atrapalhe minha navegação.

### EPIC-003: Motor de IA
- Como **usuária**, quero que o app detecte automaticamente a roupa na foto do produto, para não precisar recortar nada manualmente.
- Como **usuária**, quero ver uma imagem realista de mim vestindo a roupa, para decidir se compro ou não.
- Como **usuária**, quero poder "tentar novamente" se a imagem não ficou boa, para ter mais chances de um bom resultado.
- Como **sistema**, quero rotear entre modelos de IA com base no histórico de aprovação, para melhorar a qualidade ao longo do tempo.

### EPIC-004: Compartilhamento e Feedback
- Como **usuária**, quero compartilhar o resultado com amigas via WhatsApp/Instagram, para pedir opinião antes de comprar.
- Como **usuária**, quero dar thumbs up/down na imagem gerada, para ajudar o app a melhorar.

### EPIC-005: Monetização
- Como **usuária free**, quero usar 3 try-ons grátis por dia, para experimentar o app sem pagar.
- Como **usuária**, quero assinar um plano mensal para uso ilimitado, para provar roupas sem restrição.

---

## User Personas

### Persona 1: Maria — Compradora Frequente

- **Idade:** 28 anos
- **Perfil:** Classe média, trabalha em escritório, compra moda online 2-3x/mês
- **Dispositivo:** Samsung Galaxy A54, Android 13
- **Comportamento:** Navega Shopee e Shein nos intervalos do trabalho e à noite
- **Dor principal:** Já devolveu 4 roupas nos últimos 6 meses porque não ficaram como esperava
- **Nível técnico:** Baixo-médio — usa WhatsApp, Instagram, Shopee com facilidade, mas não entende configurações técnicas
- **Expectativa:** "Quero ver como fica em MIM, não na modelo"

### Persona 2 (Futura): Pedro — PM de Marketplace

- **Idade:** 32 anos
- **Perfil:** Product Manager em marketplace de moda
- **Interesse:** Integrar tecnologia de virtual try-on nativamente no app do marketplace
- **Timeline:** Relevante após tração B2C (12+ meses)

---

## User Flows

### Flow 1: First-Time Setup
```
Instalar GhostFit → Tela de boas-vindas → Explicação do overlay →
Conceder permissão SYSTEM_ALERT_WINDOW → Aceitar termos LGPD →
Selecionar fotos no Google Fotos → Setup completo → Fantasminha ativo
```

### Flow 2: Try-On Diário (Core Flow)
```
Abrir Shopee/Shein → Navegar produtos → Encontrar roupa interessante →
Tocar no fantasminha → [Loading: detectando roupa...] →
[Loading: gerando imagem...] → Ver resultado →
Thumbs up/down → Compartilhar ou Tentar novamente → Voltar a navegar
```

### Flow 3: Upgrade para Premium
```
Atingir limite de 3 tentativas → Tela "Limite atingido" →
Ver planos (avulso / mensal) → Escolher plano →
Google Play Billing checkout → Tentativas liberadas → Sem ads
```

---

## Dependencies

### Internal Dependencies

- Setup de projeto Android (Kotlin, Gradle, Android Studio)
- Backend leve para: receber feedback, armazenar dataset, servir configurações remotas
- Storage cloud para dataset de treinamento (Firebase Storage, S3, ou similar)

### External Dependencies

- **Android Photo Picker** — Seleção nativa de fotos da galeria (sem OAuth)
- **APIs de IA (Detecção):** ML Kit Object Detection (on-device) + Gemini 2.0 Flash (classificação remota, primário) + GPT-4o Vision (fallback)
- **APIs de IA (Geração):** FASHN.ai v1.5 (primário), Google Vertex AI VTON (fallback)
- **Google Play Billing Library v6+ (v8.3.0 em uso)** — Para processar compras e assinaturas
- **AdMob** — Para exibição de anúncios no plano free
- **Android SYSTEM_ALERT_WINDOW API** — Para overlay flutuante
- **Firebase** (ou similar) — Analytics, crash reporting, remote config

---

## Assumptions

1. FASHN.ai e/ou Vertex AI VTON mantêm consistência na geração de imagens sem alterar fisionomia
2. Custo por geração de imagem será viável economicamente com modelo freemium + ads
3. Usuárias possuem smartphones Android com capacidade suficiente para o app (4GB+ RAM)
4. Google Fotos API permite acesso às fotos do usuário com as permissões adequadas
5. A detecção de roupa via modelo de visão econômico terá acurácia suficiente (>80%)
6. Políticas da Play Store permitem overlay com as permissões corretas (SYSTEM_ALERT_WINDOW)
7. Usuárias estão dispostas a fornecer fotos pessoais em troca da funcionalidade
8. Volume de feedbacks será suficiente para roteamento inteligente em 2-3 meses

---

## Out of Scope

Os seguintes itens **não** fazem parte do MVP (v1):

- Versão iOS (requer ambiente Mac para homologação)
- SDK/API B2B para marketplaces
- Histórico de try-ons salvos no app
- Múltiplas roupas na mesma imagem (look completo)
- Visualização lateral / girar 360°
- Login social (Google/Apple)
- Suporte a vídeo
- Detecção automática de tamanho/medidas
- Funcionalidade social (feed de looks, opiniões de amigos)
- Internacionalização (v1 apenas PT-BR)

---

## Open Questions

1. **Qual modelo de IA priorizar para geração?** Decidido: FASHN.ai primário ($0.075/img), Vertex AI fallback (ver research.md)
2. **Google Fotos API é suficiente?** Decidido: usar Android Photo Picker nativo (sem API externa, sem OAuth)
3. **Política da Play Store:** Confirmar que o uso de overlay + screenshot não viola termos atuais
4. **Custo real por geração:** Precisa de spike técnico para estimar custo com volume
5. **Backend próprio ou serverless?** Firebase Functions vs backend Kotlin/Node para receber feedback e servir config

---

## Approval & Sign-off

### Stakeholders

- **Rafael Giovannini (Founder/Developer)** — Influência Alta. Único responsável por produto, desenvolvimento, design e estratégia.

### Approval Status

- [ ] Product Owner (Rafael)
- [ ] Engineering Lead (Rafael)

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-03-04 | rafael.giovannini | Initial PRD |

---

## Next Steps

### Phase 3: Architecture

Run `/bmad:architecture` to create system architecture based on these requirements.

The architecture will address:
- All 21 functional requirements (FRs)
- All 15 non-functional requirements (NFRs)
- Technical stack decisions (Kotlin, APIs de IA, backend)
- Data models and APIs
- System components (overlay, IA pipeline, monetização)

### Phase 4: Sprint Planning

After architecture is complete, run `/bmad:sprint-planning` to:
- Break 5 epics into detailed user stories (~18-27 stories)
- Estimate story complexity
- Plan sprint iterations
- Begin implementation

---

**This document was created using BMAD Method v6 - Phase 2 (Planning)**

*To continue: Run `/bmad:workflow-status` to see your progress and next recommended workflow.*

---

## Appendix A: Requirements Traceability Matrix

| Epic ID | Epic Name | Functional Requirements | Story Count (Est.) |
|---------|-----------|-------------------------|-------------------|
| EPIC-001 | Onboarding e Permissões | FR-001, FR-002, FR-003 | 3-5 |
| EPIC-002 | Overlay e Captura | FR-004, FR-005, FR-006 | 3-5 |
| EPIC-003 | Motor de IA | FR-007, FR-008, FR-009, FR-010, FR-011, FR-012, FR-019, FR-020, FR-021 | 6-10 |
| EPIC-004 | Compartilhamento e Feedback | FR-013, FR-014 | 2-3 |
| EPIC-005 | Monetização | FR-015, FR-016, FR-017, FR-018 | 4-6 |
| **TOTAL** | **5 Epics** | **21 FRs** | **18-29 stories** |

---

## Appendix B: Prioritization Details

### MoSCoW Summary

| Priority | FRs | NFRs | Total |
|----------|-----|------|-------|
| Must Have | 21 | 14 | 35 |
| Should Have | 0 | 1 | 1 |
| Could Have | 0 | 0 | 0 |
| **Total** | **21** | **15** | **36** |

### Requirement → Business Objective Mapping

| Business Objective | Supporting Requirements |
|-------------------|----------------------|
| Validação de produto | FR-001~FR-012, NFR-001~003, NFR-010 |
| Crescimento orgânico | FR-013, FR-014 |
| Sustentabilidade | FR-015~FR-018, FR-016 |
| Futuro B2B / Modelo próprio | FR-019, FR-020, FR-021, NFR-015 |
