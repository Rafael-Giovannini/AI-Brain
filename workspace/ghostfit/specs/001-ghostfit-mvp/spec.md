# Feature Specification: GhostFit MVP — Provador Virtual com Overlay

**Feature Branch**: `001-ghostfit-mvp`
**Created**: 2026-03-04
**Status**: Draft
**Input**: User description: "GhostFit - Provador virtual com overlay flutuante sobre apps de e-commerce. App Android que permite visualizar como a roupa ficaria no corpo da usuária antes da compra."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Setup Inicial e Onboarding (Priority: P1)

A usuária instala o GhostFit pela primeira vez. O app exibe uma tela de boas-vindas explicando o conceito do "provador virtual". Em seguida, solicita as permissões necessárias (overlay e acesso a fotos) com explicações claras em linguagem simples sobre por que cada permissão é necessária. A usuária aceita os termos LGPD e seleciona fotos de corpo inteiro do Google Fotos como referência para o try-on.

**Why this priority**: Sem onboarding funcional, nenhuma outra funcionalidade opera. É a porta de entrada obrigatória do produto. Se o onboarding for confuso ou gerar desconfiança, a usuária desinstala imediatamente.

**Independent Test**: Pode ser testado instalando o app em um dispositivo Android e verificando que a usuária consegue completar todo o fluxo de setup — desde a concessão de permissões até a seleção de fotos — sem abandonar o processo.

**Acceptance Scenarios**:

1. **Given** a usuária instalou o GhostFit pela primeira vez, **When** ela abre o app, **Then** uma tela de boas-vindas explica o conceito do provador virtual e guia ela pelo setup.
2. **Given** a usuária está no fluxo de onboarding, **When** o app solicita permissão de overlay (SYSTEM_ALERT_WINDOW), **Then** uma tela explica em linguagem simples por que a permissão é necessária antes de redirecionar para as configurações do Android.
3. **Given** a usuária concedeu a permissão de overlay, **When** ela retorna ao app, **Then** o app detecta automaticamente que a permissão foi concedida e avança para o próximo passo.
4. **Given** a usuária está no passo de consentimento LGPD, **When** os termos são exibidos, **Then** o checkbox de consentimento NÃO está pré-marcado e a usuária deve marcar explicitamente para prosseguir.
5. **Given** a usuária aceitou os termos LGPD, **When** ela acessa o seletor de fotos, **Then** ela pode selecionar uma ou mais fotos de corpo inteiro do Google Fotos ou galeria do dispositivo.
6. **Given** a usuária selecionou uma foto, **When** o sistema valida a foto, **Then** confirma que contém uma pessoa de corpo inteiro visível ou informa que a foto não é adequada.

---

### User Story 2 - Try-On Virtual (Fluxo Core) (Priority: P1)

A usuária está navegando no Shopee ou Shein e encontra uma roupa interessante. Com um único toque no fantasminha flutuante, o app captura a tela, detecta a roupa na imagem do produto via IA de visão, e gera uma imagem realista da usuária vestindo aquela roupa — mantendo sua fisionomia, tom de pele e proporções corporais.

**Why this priority**: Este é o core value proposition do produto. Sem o try-on funcional, o app não tem razão de existir. A qualidade e velocidade deste fluxo determinam se a usuária adota ou abandona o produto.

**Independent Test**: Pode ser testado tocando no fantasminha sobre uma página de produto de roupa no Shopee/Shein e verificando que uma imagem da usuária vestindo a roupa é gerada em tempo aceitável, com fisionomia preservada.

**Acceptance Scenarios**:

1. **Given** a usuária tem o overlay ativo e está em uma página de produto de roupa, **When** ela toca no fantasminha, **Then** o app captura a tela automaticamente em menos de 1 segundo.
2. **Given** o app capturou a tela de uma página de produto, **When** a IA de visão analisa a imagem, **Then** a roupa principal é detectada e identificada por tipo (vestido, blusa, calça, etc.) em menos de 3 segundos.
3. **Given** a roupa foi detectada com sucesso, **When** o modelo generativo processa a imagem, **Then** uma imagem realista da usuária vestindo a roupa é exibida em menos de 15 segundos.
4. **Given** a imagem foi gerada, **When** a usuária visualiza o resultado, **Then** a fisionomia, tom de pele e proporções corporais estão preservados fielmente.
5. **Given** a usuária está em uma tela sem roupa (configurações, chat, etc.), **When** ela toca no fantasminha, **Then** o app exibe a mensagem "Nenhuma roupa detectada. Tente em uma página de produto." e NÃO consome uma tentativa do limite diário.
6. **Given** o modelo primário de IA falha (timeout ou erro), **When** o sistema detecta a falha, **Then** automaticamente tenta o modelo secundário sem que a usuária perceba a troca.

---

### User Story 3 - Interação com Resultado (Priority: P2)

Após ver o resultado do try-on, a usuária pode interagir com a imagem: tentar novamente para obter uma variação diferente, trocar a foto de referência para ver a roupa em outra pose, avaliar a qualidade com thumbs up/down, ou compartilhar o resultado com amigas via WhatsApp/Instagram com branding GhostFit.

**Why this priority**: Estas interações aumentam o engajamento, alimentam o motor de aprendizado (feedback) e impulsionam o crescimento viral (compartilhamento). São essenciais para retenção e crescimento, mas o produto funciona sem elas no primeiro uso.

**Independent Test**: Pode ser testado gerando um resultado de try-on e verificando que todos os botões de ação (tentar novamente, trocar foto, thumbs, compartilhar) funcionam corretamente e produzem os efeitos esperados.

**Acceptance Scenarios**:

1. **Given** a usuária está vendo o resultado do try-on, **When** ela toca em "Tentar novamente", **Then** uma nova imagem é gerada com variação diferente (seed/parâmetros distintos) e conta como tentativa adicional.
2. **Given** a usuária está vendo o resultado do try-on, **When** ela toca em "Trocar foto", **Then** o seletor de fotos cadastradas abre e, ao selecionar outra foto, o try-on é regenerado com a mesma roupa.
3. **Given** a usuária está vendo o resultado, **When** ela toca em thumbs up ou thumbs down, **Then** o feedback é registrado com modelo usado, tipo de roupa e timestamp, e enviado ao backend sem bloquear a experiência.
4. **Given** a usuária está vendo o resultado, **When** ela toca em "Compartilhar", **Then** a imagem com branding sutil do GhostFit é compartilhada via share sheet nativa do Android (WhatsApp, Instagram, Telegram, etc.).
5. **Given** a usuária deu thumbs up em uma geração, **When** o feedback é processado, **Then** os dados anonimizados (roupa detectada, parâmetros, resultado) são coletados para o dataset de treinamento sem incluir fotos pessoais.

---

### User Story 4 - Monetização e Limites (Priority: P2)

A usuária no plano gratuito tem direito a 3 tentativas de try-on por dia. Anúncios são exibidos em momentos estratégicos (entre gerações). Quando atinge o limite, uma tela de upgrade apresenta as opções de pacotes avulsos ou assinatura mensal para uso ilimitado e sem anúncios. A compra é processada via Google Play Billing.

**Why this priority**: A monetização é essencial para a sustentabilidade econômica do produto (custos de API de IA são significativos), mas a funcionalidade core pode ser validada antes da implementação completa de billing.

**Independent Test**: Pode ser testado usando 3 tentativas de try-on, verificando que o limite é respeitado, que a tela de upgrade é exibida, e que a compra via Google Play Billing desbloqueia tentativas adicionais.

**Acceptance Scenarios**:

1. **Given** a usuária é free e usou 0 tentativas, **When** ela visualiza a interface, **Then** o contador mostra "3 tentativas restantes" de forma visível.
2. **Given** a usuária free usou 3 tentativas no dia, **When** ela tenta gerar um novo try-on, **Then** a geração é bloqueada e a tela de upgrade é exibida com opções de planos.
3. **Given** a meia-noite (horário local) passa, **When** a usuária abre o app no dia seguinte, **Then** o contador de tentativas é resetado para 3.
4. **Given** a usuária free está usando o app, **When** um anúncio é exibido, **Then** ele aparece entre gerações (nunca antes de mostrar o resultado) e não interrompe o fluxo core.
5. **Given** a usuária escolhe assinar o plano mensal, **When** a compra é processada via Google Play Billing, **Then** o limite de tentativas é removido e anúncios deixam de ser exibidos.
6. **Given** a usuária reinstala o app, **When** ela restaura compras, **Then** suas assinaturas e pacotes ativos são restaurados corretamente.

---

### User Story 5 - Overlay Flutuante e Reposicionamento (Priority: P1)

O fantasminha GhostFit aparece como overlay flutuante persistente sobre qualquer app aberto no dispositivo. A usuária pode arrastar o fantasminha para qualquer posição na tela para que não atrapalhe sua navegação. A posição é salva entre sessões.

**Why this priority**: O overlay é o mecanismo de acesso ao produto — sem ele funcionar corretamente sobre os apps de e-commerce, não há como iniciar o try-on. É uma dependência técnica fundamental.

**Independent Test**: Pode ser testado abrindo o Shopee/Shein com o overlay ativo e verificando que o fantasminha aparece, pode ser arrastado, persiste entre apps, e não causa lag no app hospedeiro.

**Acceptance Scenarios**:

1. **Given** a usuária completou o onboarding e concedeu permissão de overlay, **When** ela abre qualquer app, **Then** o fantasminha GhostFit é visível sobre o app hospedeiro.
2. **Given** o overlay está ativo sobre o Shopee, **When** a usuária navega entre Shopee e Shein, **Then** o overlay persiste e continua visível.
3. **Given** o overlay está em uma posição que atrapalha, **When** a usuária arrasta o fantasminha, **Then** ele se move suavemente para a nova posição.
4. **Given** a usuária reposicionou o overlay, **When** ela fecha e reabre o app, **Then** o overlay aparece na última posição salva.
5. **Given** o overlay está ativo, **When** a usuária interage com o app hospedeiro (scroll, toque em botões), **Then** o overlay não bloqueia a interação e não causa lag perceptível ou frame drops.

---

### Edge Cases

- O que acontece quando a foto selecionada não contém uma pessoa de corpo inteiro? Sistema informa que a foto não é adequada e solicita outra.
- O que acontece quando a conexão de internet cai durante a geração? Mensagem amigável "Serviço temporariamente indisponível" com opção de tentar novamente, sem consumir tentativa.
- O que acontece quando todos os modelos de IA (primário e fallback) estão indisponíveis? Retry com backoff exponencial e mensagem de indisponibilidade temporária, sem consumir tentativa.
- O que acontece quando a usuária toca no fantasminha em um app que bloqueia overlay ou screenshot? Sistema trata o erro graciosamente e informa a usuária.
- O que acontece quando o dispositivo tem pouca memória disponível (<100MB)? Overlay minimiza consumo e prioriza estabilidade do app hospedeiro.
- O que acontece quando a usuária revoga o consentimento LGPD? Todas as fotos pessoais e dados são deletados, e o app retorna ao estado de onboarding.
- O que acontece quando a usuária tenta comprar mas a Play Store está indisponível? Mensagem de erro amigável e sugestão de tentar novamente mais tarde.
- O que acontece quando a usuária toca no overlay enquanto uma geração anterior está em andamento? O toque é ignorado e um toast "Geração em andamento..." é exibido brevemente.
- O que acontece com as imagens geradas pelo try-on ao fechar a tela? São descartadas imediatamente — não há persistência de resultados, em conformidade com LGPD.

## Clarifications

### Session 2026-03-04

- Q: O que acontece se a usuária tocar no overlay enquanto uma geração anterior está em andamento? → A: Ignorar toque e mostrar toast "Geração em andamento..."
- Q: A usuária pode salvar e visualizar histórico de try-ons anteriores? → A: Não — cada resultado é descartado ao fechar a tela, em conformidade com LGPD (sem persistência de imagens geradas).
- Q: Qual a ordem de prioridade dos modelos de IA e timeout antes de fallback? → A: FASHN.ai primário (10s timeout), Vertex AI fallback (15s timeout).
- Q: Quantas fotos de referência a usuária pode cadastrar? → A: Até 3 fotos, selecionável no momento do try-on.
- Q: Qual o nível de disponibilidade esperado para o backend? → A: 99% uptime (~7h downtime/mês) — adequado para MVP.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema DEVE permitir cadastro de até 3 fotos de corpo inteiro via Photo Picker nativo do Android (inclui Google Fotos e galeria do dispositivo), com validação automática de corpo inteiro visível e seleção da foto ativa no momento do try-on
- **FR-002**: Sistema DEVE solicitar permissão SYSTEM_ALERT_WINDOW com tela de onboarding explicativa em linguagem simples antes de redirecionar para configurações do Android
- **FR-003**: Sistema DEVE exigir consentimento LGPD explícito (opt-in, checkbox não pré-marcado) antes de acessar qualquer foto pessoal, com registro de timestamp
- **FR-004**: Sistema DEVE exibir overlay flutuante (fantasminha) persistente sobre qualquer app, sem bloquear interação com o app hospedeiro
- **FR-005**: Sistema DEVE capturar a tela automaticamente ao toque no overlay, em menos de 1 segundo, com feedback visual
- **FR-006**: Sistema DEVE permitir reposicionamento do overlay via drag, com posição salva entre sessões
- **FR-007**: Sistema DEVE detectar automaticamente a roupa principal na screenshot via IA de visão, identificando tipo de roupa, em menos de 3 segundos
- **FR-008**: Sistema DEVE exibir mensagem "Nenhuma roupa detectada" quando a confiança da detecção for inferior a 60%, sem consumir tentativa
- **FR-009**: Sistema DEVE gerar imagem realista da usuária vestindo a roupa detectada, preservando fisionomia, tom de pele e proporções, em menos de 15 segundos
- **FR-010**: Sistema DEVE permitir regeneração do try-on (variação diferente) com mesmos inputs, contando como tentativa adicional
- **FR-011**: Sistema DEVE permitir troca da foto de referência e regeneração do try-on com a mesma roupa
- **FR-012**: Sistema DEVE implementar fallback automático entre modelos de IA: FASHN.ai primário (timeout 10s) → Vertex AI fallback (timeout 15s), com retry até 2x por provider antes de acionar fallback
- **FR-013**: Sistema DEVE permitir compartilhamento do resultado via share sheet nativa com branding sutil GhostFit
- **FR-014**: Sistema DEVE coletar feedback thumbs up/down com metadados (modelo usado, tipo de roupa, timestamp) enviados ao backend de forma assíncrona
- **FR-015**: Usuárias free DEVEM ter limite de 3 gerações por dia, com contador visível, reset à meia-noite (horário local), sem cobrar tentativas com detecção falha ou erro de API
- **FR-016**: Sistema DEVE exibir anúncios para usuárias free em momentos estratégicos (entre gerações), sem interromper o fluxo core
- **FR-017**: Sistema DEVE oferecer pacotes pagos (avulsos) e assinatura mensal para uso ilimitado sem anúncios
- **FR-018**: Sistema DEVE integrar Google Play Billing para processar compras e assinaturas, com restauração de compras em reinstalação
- **FR-019**: Sistema DEVE coletar pares de treinamento anonimizados (roupa + parâmetros + resultado + feedback) apenas quando usuária dá thumbs up, sem incluir fotos pessoais
- **FR-020**: Sistema DEVE rotear entre modelos de IA com base no score de aprovação acumulado por (modelo x tipo de roupa), com threshold mínimo de 50 feedbacks
- **FR-021**: Sistema DEVE manter pipeline de dados organizado por categoria de roupa e modelo gerador, com tracking de volume e capacidade de export

### Key Entities

- **Usuária**: Perfil da usuária com fotos de referência cadastradas, preferências, status de consentimento LGPD, e plano (free/premium)
- **Foto de Referência**: Imagem de corpo inteiro da usuária (máximo 3 por usuária), armazenada localmente com criptografia, selecionável no momento do try-on
- **Sessão de Try-On**: Evento de uso: screenshot capturado, roupa detectada, imagem gerada, feedback dado, modelo utilizado
- **Feedback**: Avaliação thumbs up/down associada a uma sessão de try-on, com metadados do modelo e tipo de roupa
- **Plano/Assinatura**: Estado de monetização da usuária (free com limite diário, pacote avulso com tentativas extras, assinatura mensal ilimitada)
- **Dataset de Treinamento**: Pares anonimizados de (roupa + parâmetros + resultado aprovado) organizados por categoria para futuro fine-tuning
- **Model Score**: Score de aprovação acumulado por combinação (modelo de IA x tipo de roupa) para roteamento inteligente

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 70% ou mais das imagens geradas recebem thumbs up das usuárias
- **SC-002**: Usuárias completam o fluxo de try-on (toque no fantasminha até ver resultado) em menos de 20 segundos no total
- **SC-003**: 90% das usuárias completam o onboarding sem abandonar o processo
- **SC-004**: Usuárias ativas retornam para usar o app em pelo menos 3 dias diferentes no primeiro mês (retenção)
- **SC-005**: Pelo menos 30% dos resultados de try-on são compartilhados via redes sociais (viralidade)
- **SC-006**: Receita de anúncios e assinaturas cobre 100% dos custos operacionais de APIs de IA dentro de 6 meses após lançamento
- **SC-007**: Avaliação média na Play Store de 4.0 ou superior
- **SC-008**: O overlay não causa degradação perceptível no desempenho dos apps hospedeiros (menos de 50MB de memória, menos de 5% de consumo adicional de bateria por hora)
- **SC-009**: Backend (API de roteamento e feedback) mantém 99% de uptime (~7h downtime/mês máximo)

## Assumptions

- Google Fotos API ou picker nativo do Android permite seleção de fotos com as permissões adequadas
- Modelos de IA generativa (FASHN.ai, Vertex AI) mantêm consistência na preservação de fisionomia ao trocar roupas
- Custo por geração de imagem é viável economicamente com modelo freemium + ads
- Usuárias possuem smartphones Android com capacidade suficiente (4GB+ RAM, Android 8.0+)
- Políticas da Play Store permitem overlay com SYSTEM_ALERT_WINDOW quando devidamente justificado
- Usuárias estão dispostas a fornecer fotos pessoais em troca da funcionalidade de provador virtual
- Detecção de roupa via IA de visão terá acurácia suficiente (>80%) em screenshots de Shopee e Shein
- Interface em Português Brasileiro atende o público-alvo principal
