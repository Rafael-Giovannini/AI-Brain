# UX Design: GhostFit

**Data:** 2026-03-04
**Designer:** rafael.giovannini (assistido por BMAD UX Designer)
**Versão:** 1.0
**Plataforma:** Android Mobile (API 26+)
**Acessibilidade:** WCAG 2.1 Nível AA
**Idioma:** Português Brasileiro (PT-BR)

---

## Visão Geral do Design

**Projeto:** GhostFit — Provador Virtual com Overlay Flutuante
**Telas:** 16
**Fluxos de Usuário:** 5
**Componentes Reutilizáveis:** 12
**Persona Principal:** Maria, 28 anos, compradora frequente de moda online

### Princípios de Design

1. **Simplicidade extrema** — 1 toque = resultado (NFR-010)
2. **Confiança e transparência** — Usuária sempre sabe o que está acontecendo com seus dados
3. **Personalidade divertida** — O fantasminha traz leveza à experiência
4. **Performance percebida** — Feedbacks visuais durante toda a espera
5. **Mobile-first e thumb-friendly** — Áreas de toque ≥ 48dp, ações no bottom half

---

## 1. Fluxos de Usuário

### Fluxo 1: Primeiro Uso (Onboarding)

**Ponto de Entrada:** Usuária abre o app pela primeira vez após instalar

**Caminho Feliz:**
```
[Splash Screen]
   ↓ (2s auto)
[Tela 1: Boas-Vindas]
   ↓ "Começar"
[Tela 2: Explicação do Overlay]
   ↓ "Ativar Overlay"
[Android Settings — SYSTEM_ALERT_WINDOW]
   ↓ (permissão concedida, retorno automático)
[Tela 3: Consentimento LGPD]
   ↓ "Aceitar e Continuar"
[Tela 4: Seleção de Fotos — Google Fotos]
   ↓ (seleciona 1+ fotos de corpo inteiro)
[Validação da foto]
   ↓ (foto aprovada)
[Tela 5: Setup Completo!]
   ↓ "Ativar Fantasminha"
[Overlay ativo — fantasminha flutuando]
```

**Pontos de Decisão:**
- Na permissão de overlay: Se negar → Tela explicando que é obrigatório, botão "Tentar Novamente"
- No consentimento LGPD: Se negar → App funcional limitado (sem try-on), tela informativa
- Na seleção de fotos: Se foto não contém pessoa de corpo inteiro → Aviso "Selecione uma foto de corpo inteiro"

**Saídas:**
- Sucesso: Overlay ativado, pronto para uso
- Cancelamento: Usuária pode sair a qualquer momento, progresso salvo
- Erro: Tela de erro genérica com "Tentar Novamente"

---

### Fluxo 2: Try-On Diário (Core Flow)

**Ponto de Entrada:** Fantasminha flutuando sobre Shopee/Shein

**Caminho Feliz:**
```
[Shopee/Shein — página de produto]
   ↓ (toque no fantasminha)
[Animação: fantasminha pulsa + captura de tela]
   ↓ (< 1s)
[Overlay expandido: "Detectando roupa..."]
   ↓ (< 3s — FR-007)
[Overlay expandido: "Gerando sua imagem..."]
   ↓ (< 15s — FR-009)
[Tela de Resultado: imagem try-on]
   ↓ Opções: 👍/👎, Compartilhar, Tentar Novamente, Trocar Foto
[Ação escolhida]
   ↓
[Voltar a navegar — fantasminha volta ao estado mínimo]
```

**Pontos de Decisão:**
- Detecção: Se nenhuma roupa → [Tela "Nenhuma roupa detectada"] → "Tentar em outra página"
- Resultado: Thumbs up → Feedback enviado + Dados coletados (FR-019)
- Resultado: Thumbs down → Feedback enviado + Opção "Tentar Novamente"
- Resultado: "Tentar Novamente" → Regenera imagem (conta como tentativa)
- Resultado: "Trocar Foto" → Mini-picker de fotos cadastradas → Regenera

**Casos de Erro:**
- API indisponível → "Serviço temporariamente indisponível. Tente em alguns minutos." (NFR-009)
- Sem internet → "Sem conexão. O try-on precisa de internet." (NFR-008)
- Timeout → Retry automático (2x) → Fallback modelo → Erro amigável

---

### Fluxo 3: Limite Atingido → Upgrade

**Ponto de Entrada:** Usuária free tenta 4ª geração do dia

```
[Toque no fantasminha (4ª tentativa)]
   ↓
[Tela: Limite Atingido]
   ↓ "Ver Planos"
[Tela: Comparação de Planos]
   ├─→ "Pacote Avulso" → [Google Play Billing] → Tentativas liberadas
   ├─→ "Assinatura Mensal" → [Google Play Billing] → Ilimitado + sem ads
   └─→ "Voltar" → Aguardar reset à meia-noite
```

---

### Fluxo 4: Gerenciamento de Fotos

**Ponto de Entrada:** Configurações → Minhas Fotos

```
[Configurações]
   ↓ "Minhas Fotos"
[Galeria de Fotos de Referência]
   ├─→ "Adicionar Foto" → Google Fotos picker → Validação → Adicionada
   ├─→ "Remover Foto" → Confirmação → Removida
   └─→ "Definir como Principal" → Foto marcada como padrão
```

---

### Fluxo 5: Configurações e Privacidade

**Ponto de Entrada:** App GhostFit → Tela principal → ⚙️

```
[Tela Principal do App]
   ↓ (ícone engrenagem)
[Configurações]
   ├─→ Minhas Fotos (Fluxo 4)
   ├─→ Meu Plano (status free/premium, tentativas restantes)
   ├─→ Privacidade (política, revogar consentimento, excluir dados)
   ├─→ Sobre o GhostFit
   └─→ Ajuda / FAQ
```

---

## 2. Wireframes

### Tela 1: Splash Screen

```
┌─────────────────────────┐
│                         │
│                         │
│                         │
│         👻              │
│    (Logo GhostFit       │
│     fantasminha         │
│     animado,            │
│     flutuando)          │
│                         │
│      GhostFit           │ ← Logo texto (24sp, bold)
│   Provador Virtual      │ ← Tagline (14sp, light)
│                         │
│                         │
│                         │
│     ● ○ ○ ○ ○          │ ← Progress dots (loading)
│                         │
└─────────────────────────┘

Duração: 2s → auto-navega para Boas-Vindas
Animação: Fantasminha faz bounce suave
Background: Gradiente roxo escuro → roxo claro
```

---

### Tela 2: Boas-Vindas (Onboarding 1/3)

```
┌─────────────────────────┐
│                    Pular │ ← Link discreto (14sp)
│                         │
│                         │
│    ┌───────────────┐    │
│    │               │    │
│    │  Ilustração:  │    │ ← 200dp × 200dp
│    │  Fantasminha  │    │
│    │  sobre tela   │    │
│    │  de celular   │    │
│    │  com roupas   │    │
│    │               │    │
│    └───────────────┘    │
│                         │
│  Veja como a roupa      │ ← H1 (24sp, bold, center)
│  fica em VOCÊ           │
│                         │
│  Com um toque, o        │ ← Body (16sp, center)
│  GhostFit mostra como   │
│  qualquer roupa ficaria  │
│  no seu corpo.          │
│                         │
│  ┌─────────────────┐    │
│  │    Começar →    │    │ ← Primary Button (48dp)
│  └─────────────────┘    │
│                         │
│       ● ○ ○             │ ← Page indicator
│                         │
└─────────────────────────┘
```

---

### Tela 3: Explicação do Overlay (Onboarding 2/3)

```
┌─────────────────────────┐
│  ←                 Pular │
│                         │
│    ┌───────────────┐    │
│    │               │    │
│    │  Ilustração:  │    │ ← 200dp × 200dp
│    │  Fantasminha  │    │
│    │  flutuando    │    │
│    │  sobre app    │    │
│    │  Shopee       │    │
│    │               │    │
│    └───────────────┘    │
│                         │
│  O fantasminha          │ ← H1 (24sp, bold, center)
│  precisa flutuar        │
│                         │
│  Para funcionar sobre   │ ← Body (16sp, center)
│  o Shopee e Shein, o    │
│  GhostFit precisa da    │
│  permissão de overlay.  │
│                         │
│  🔒 Seus dados estão    │ ← Info box (bg: roxo 10%)
│  seguros. Nenhuma foto  │
│  sai do seu celular.    │
│                         │
│  ┌─────────────────┐    │
│  │ Ativar Overlay →│    │ ← Primary Button (48dp)
│  └─────────────────┘    │
│                         │
│       ○ ● ○             │
│                         │
└─────────────────────────┘

Ação do botão: Intent para Settings → Overlay permission
Ao retornar: detecta se permissão foi concedida
- Concedida → auto-navega para LGPD
- Negada → Dialog "Sem essa permissão, o fantasminha não funciona. Ativar?"
```

---

### Tela 4: Consentimento LGPD (Onboarding 3/3)

```
┌─────────────────────────┐
│  ←                      │
│                         │
│  Sua privacidade        │ ← H1 (24sp, bold)
│  é sagrada 🔒           │
│                         │
│  ┌─────────────────────┐│
│  │ O GhostFit:         ││ ← Card (bg: surface)
│  │                     ││
│  │ ✓ Suas fotos ficam  ││
│  │   só no seu celular ││
│  │                     ││
│  │ ✓ Imagens geradas   ││
│  │   são temporárias   ││
│  │                     ││
│  │ ✓ Nenhum dado é     ││
│  │   vendido a         ││
│  │   terceiros         ││
│  │                     ││
│  │ ✓ Você pode excluir ││
│  │   tudo a qualquer   ││
│  │   momento           ││
│  └─────────────────────┘│
│                         │
│  ☐ Li e aceito os       │ ← Checkbox (não pré-marcado)
│    Termos de Uso e a    │    Links clicáveis
│    Política de          │
│    Privacidade          │
│                         │
│  ┌─────────────────┐    │
│  │ Aceitar e       │    │ ← Primary Button (disabled até check)
│  │ Continuar →     │    │
│  └─────────────────┘    │
│                         │
└─────────────────────────┘

Estados do botão:
- Checkbox desmarcado: botão disabled (opacity 50%)
- Checkbox marcado: botão enabled (cor primária)
Links "Termos de Uso" e "Política de Privacidade": abrem WebView
```

---

### Tela 5: Seleção de Fotos

```
┌─────────────────────────┐
│  ←  Selecione suas      │
│      fotos              │ ← H1 (20sp, bold)
│                         │
│  Escolha 1 ou mais      │ ← Body (14sp)
│  fotos de corpo inteiro │
│  para o try-on.         │
│                         │
│  ┌─────────────────────┐│
│  │                     ││
│  │   Ilustração:       ││ ← Guia visual (120dp)
│  │   Exemplo de        ││
│  │   "corpo inteiro"   ││
│  │   ✓ vs ✗            ││
│  │                     ││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────┐    │
│  │  📷 Selecionar  │    │ ← Primary Button
│  │  do Google Fotos│    │
│  └─────────────────┘    │
│                         │
│  Fotos selecionadas:    │ ← Label (14sp, bold)
│                         │
│  ┌────┐ ┌────┐ ┌────┐  │
│  │    │ │    │ │ +  │  │ ← Thumbnails (80dp × 80dp)
│  │foto│ │foto│ │add │  │    + = adicionar mais
│  │ ✓  │ │ ✓  │ │    │  │    ✓ = validada
│  └────┘ └────┘ └────┘  │
│                         │
│  ┌─────────────────┐    │
│  │   Continuar →   │    │ ← Primary (enabled com 1+ foto)
│  └─────────────────┘    │
│                         │
└─────────────────────────┘

Validação (FR-001):
- Foto sem pessoa de corpo inteiro → Toast: "Selecione uma foto de corpo inteiro"
- Foto aprovada → Thumbnail com ✓ verde
- Fotos criptografadas localmente (AES-256)
```

---

### Tela 6: Setup Completo

```
┌─────────────────────────┐
│                         │
│                         │
│                         │
│         👻              │
│    (fantasminha         │
│     com confete         │
│     animação)           │
│                         │
│  Tudo pronto! 🎉       │ ← H1 (28sp, bold, center)
│                         │
│  O fantasminha vai      │ ← Body (16sp, center)
│  aparecer sobre seus    │
│  apps. Quando encontrar │
│  uma roupa, é só        │
│  tocar nele!            │
│                         │
│  ┌─────────────────┐    │
│  │   Ativar        │    │ ← Primary Button
│  │   Fantasminha →  │    │
│  └─────────────────┘    │
│                         │
│  3 try-ons grátis/dia   │ ← Caption (12sp, center)
│                         │
└─────────────────────────┘

Ação: Inicia FloatingService → overlay aparece
```

---

### Tela 7: Overlay Flutuante (Fantasminha)

```
Estado Normal (sobre qualquer app):

┌──────────────────────────────┐
│                              │
│  [Conteúdo do app            │
│   hospedeiro                 │
│   (Shopee, Shein, etc.)]    │
│                              │
│                    ┌────┐   │
│                    │ 👻 │   │ ← Overlay (56dp × 56dp)
│                    │    │   │    Draggable (FR-006)
│                    └────┘   │    Posição salva
│                              │
│                              │
└──────────────────────────────┘

Interações:
- Toque simples → Inicia captura (FR-005)
- Long press + drag → Reposiciona (FR-006)
- Drag para bottom-center → Fecha overlay (zona de "fechar")

Estado durante captura:
┌────┐
│ 👻 │ ← Pulsa/brilha (animação lottie)
│ ⏳ │ ← Indicador de loading sutil
└────┘

Specs do Overlay:
- Tamanho: 56dp × 56dp (touch target ≥ 48dp — WCAG AA ✓)
- Elevação: 8dp shadow
- Forma: Círculo com ícone fantasminha
- Cor: Roxo primário (#7C3AED) com borda branca 2dp
- Memória: < 50MB (NFR-003)
- Posição default: bottom-right, 16dp do edge
```

---

### Tela 8: Loading — Detecção de Roupa

```
Overlay expande para mini-card:

┌──────────────────────────────┐
│                              │
│  [App hospedeiro]            │
│                              │
│  ┌──────────────────────┐   │
│  │ 👻 Detectando        │   │ ← Mini-card (240dp × 80dp)
│  │    roupa...           │   │    Animação: shimmer
│  │ ████████░░░░░  60%   │   │ ← Progress bar
│  └──────────────────────┘   │
│                              │
└──────────────────────────────┘

Tempo máximo: 3s (NFR-001)
Se sucesso → Transição para Loading Geração
Se falha → Tela "Nenhuma Roupa Detectada"
```

---

### Tela 9: Aviso "Nenhuma Roupa Detectada" (FR-008)

```
Overlay mostra card de erro:

┌──────────────────────────────┐
│                              │
│  [App hospedeiro]            │
│                              │
│  ┌──────────────────────┐   │
│  │                      │   │
│  │   👻 😕              │   │ ← Fantasminha triste
│  │                      │   │
│  │  Nenhuma roupa       │   │ ← Title (16sp, bold)
│  │  detectada           │   │
│  │                      │   │
│  │  Tente em uma página │   │ ← Body (14sp)
│  │  de produto com      │   │
│  │  foto de roupa.      │   │
│  │                      │   │
│  │  [Tentar Novamente]  │   │ ← Secondary Button
│  │  [Fechar]            │   │ ← Text Button
│  │                      │   │
│  └──────────────────────┘   │
│                              │
└──────────────────────────────┘

- NÃO conta como tentativa (FR-015 AC)
- Card some após 5s ou toque em "Fechar"
```

---

### Tela 10: Loading — Geração de Imagem

```
Card expande para tela de resultado (bottom sheet):

┌─────────────────────────┐
│  [App hospedeiro dim]   │ ← Dim overlay (50% opacity)
│                         │
├─────────────────────────┤ ← Bottom sheet (drag handle)
│  ─────                  │
│                         │
│  ┌─────────────────┐   │
│  │                 │   │
│  │   👻            │   │ ← Fantasminha animado
│  │   Gerando sua   │   │    (gira, flutua)
│  │   imagem...     │   │
│  │                 │   │ ← Placeholder (aspect 3:4)
│  │  ░░░░░░░░░░░░░  │   │
│  │  ░░░░░░░░░░░░░  │   │    Shimmer effect
│  │  ░░░░░░░░░░░░░  │   │
│  │                 │   │
│  └─────────────────┘   │
│                         │
│  Isso pode levar até    │ ← Caption (12sp, center)
│  15 segundos ⏳         │
│                         │
│  ████████████░░░  80%   │ ← Progress bar (animated)
│                         │
│  Tentativa 1 de 3 hoje  │ ← Counter (12sp)
│                         │
└─────────────────────────┘

Tempo máximo: 15s (NFR-002)
Progress bar: etapas (detecção 0-30%, geração 30-100%)
```

---

### Tela 11: Resultado do Try-On (Tela Principal de Resultado)

```
┌─────────────────────────┐
│  ✕                      │ ← Close button (48dp)
│                         │
│  ┌─────────────────┐   │
│  │                 │   │
│  │                 │   │
│  │   IMAGEM        │   │ ← Resultado try-on
│  │   GERADA        │   │    (aspect 3:4, max width)
│  │   (Usuária      │   │
│  │   vestindo a    │   │
│  │   roupa)        │   │
│  │                 │   │
│  │                 │   │
│  └─────────────────┘   │
│                         │
│  O que achou?           │ ← Label (14sp)
│                         │
│  ┌──────┐  ┌──────┐    │
│  │  👍  │  │  👎  │    │ ← Feedback (48dp × 48dp each)
│  │Gostei│  │Não   │    │   FR-014
│  └──────┘  └──────┘    │
│                         │
│  ┌─────────────────┐   │
│  │ 📤 Compartilhar │   │ ← Primary Button (FR-013)
│  └─────────────────┘   │
│                         │
│  ┌────────┐ ┌────────┐ │
│  │🔄Tentar│ │📷Trocar│ │ ← Secondary Buttons
│  │novament│ │  foto  │ │    FR-010 / FR-011
│  └────────┘ └────────┘ │
│                         │
│  Tentativa 1 de 3 hoje  │ ← Counter (12sp, muted)
│                         │
└─────────────────────────┘

Interações:
- 👍 → Animação de check, feedback enviado async (FR-014)
- 👎 → Animação, feedback enviado, sugere "Tentar Novamente"
- Compartilhar → Android share sheet, imagem com branding (FR-013)
- Tentar Novamente → Regenera com seed diferente (FR-010)
- Trocar Foto → Mini-picker inline das fotos cadastradas (FR-011)
- ✕ → Fecha resultado, volta ao overlay mínimo
```

---

### Tela 12: Mini-Picker de Foto (Inline)

```
(Aparece como bottom sheet sobre tela de resultado)

┌─────────────────────────┐
│  ─────                  │ ← Drag handle
│                         │
│  Escolha outra foto:    │ ← Label (16sp, bold)
│                         │
│  ┌────┐ ┌────┐ ┌────┐  │
│  │    │ │    │ │    │  │ ← Thumbnails (96dp × 96dp)
│  │foto│ │foto│ │foto│  │    Borda roxa = selecionada
│  │ 1  │ │ 2★ │ │ 3  │  │    ★ = atual
│  └────┘ └────┘ └────┘  │
│                         │
│  ┌─────────────────┐   │
│  │   Gerar com     │   │ ← Primary Button
│  │   esta foto →   │   │
│  └─────────────────┘   │
│                         │
└─────────────────────────┘
```

---

### Tela 13: Limite Atingido (FR-015)

```
┌─────────────────────────┐
│  ✕                      │
│                         │
│         👻 😴           │ ← Fantasminha dormindo
│                         │
│  Suas 3 tentativas      │ ← H1 (22sp, bold, center)
│  de hoje acabaram       │
│                         │
│  Volte amanhã para      │ ← Body (16sp, center)
│  mais 3 grátis, ou      │
│  desbloqueie agora!     │
│                         │
│  ┌─────────────────┐   │
│  │  Ver Planos →   │   │ ← Primary Button
│  └─────────────────┘   │
│                         │
│  ┌─────────────────┐   │
│  │  Assistir Ad    │   │ ← Secondary Button
│  │  = +1 Tentativa │   │    (opcional, rewarded ad)
│  └─────────────────┘   │
│                         │
│  Reseta à meia-noite    │ ← Caption (12sp, muted)
│  🕛 Faltam 4h 23min    │
│                         │
└─────────────────────────┘
```

---

### Tela 14: Planos e Upgrade (FR-017)

```
┌─────────────────────────┐
│  ←  Escolha seu plano   │ ← H1 (20sp, bold)
│                         │
│  ┌─────────────────────┐│
│  │  ⭐ MENSAL          ││ ← Card Premium (destaque)
│  │                     ││    Borda dourada
│  │  R$ 9,90/mês       ││ ← Preço (24sp, bold)
│  │                     ││
│  │  ✓ Try-ons          ││
│  │    ilimitados       ││
│  │  ✓ Sem anúncios     ││
│  │  ✓ Prioridade na    ││
│  │    geração          ││
│  │                     ││
│  │  [Assinar Agora]    ││ ← Primary Button
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │  PACOTE AVULSO      ││ ← Card Avulso
│  │                     ││
│  │  10 tentativas      ││
│  │  R$ 4,90            ││ ← Preço (20sp, bold)
│  │                     ││
│  │  [Comprar]          ││ ← Secondary Button
│  └─────────────────────┘│
│                         │
│  Pagamento seguro via   │ ← Caption (12sp)
│  Google Play            │
│                         │
│  Cancelar a qualquer    │ ← Caption (12sp)
│  momento                │
│                         │
└─────────────────────────┘

Ação: Google Play Billing (FR-018)
```

---

### Tela 15: Tela Principal do App (Home)

```
┌─────────────────────────┐
│                    ⚙️   │ ← Settings icon (48dp)
│                         │
│      👻                 │ ← Logo GhostFit grande
│    GhostFit             │
│                         │
│  ┌─────────────────────┐│
│  │ Status do           ││ ← Status Card
│  │ Fantasminha:        ││
│  │                     ││
│  │ 🟢 Ativo            ││ ← ou 🔴 Inativo
│  │                     ││
│  │ Tentativas hoje:    ││
│  │ ██░░░  1/3          ││ ← Progress (visual)
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ Minhas Fotos (2)    ││ ← Fotos Card
│  │ ┌────┐ ┌────┐ ┌──┐ ││
│  │ │foto│ │foto│ │+ │ ││
│  │ │ 1★ │ │ 2  │ │  │ ││
│  │ └────┘ └────┘ └──┘ ││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────┐   │
│  │ Ativar/Desativar│   │ ← Toggle Button
│  │ Fantasminha     │   │
│  └─────────────────┘   │
│                         │
│  ───── Banner Ad ───── │ ← Ad banner (FR-016)
│                         │    (só para free)
└─────────────────────────┘
```

---

### Tela 16: Configurações

```
┌─────────────────────────┐
│  ←  Configurações       │ ← H1 (20sp, bold)
│                         │
│  ┌─────────────────────┐│
│  │ 📷 Minhas Fotos     ││ ← List item (56dp height)
│  │    2 fotos   →      ││
│  ├─────────────────────┤│
│  │ ⭐ Meu Plano         ││
│  │    Free (1/3)  →    ││
│  ├─────────────────────┤│
│  │ 👻 Overlay           ││
│  │    Ativo      🔘    ││ ← Toggle switch
│  ├─────────────────────┤│
│  │ 🔒 Privacidade       ││
│  │                →    ││
│  ├─────────────────────┤│
│  │ ❓ Ajuda / FAQ       ││
│  │                →    ││
│  ├─────────────────────┤│
│  │ ℹ️ Sobre             ││
│  │    v1.0.0     →    ││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ 🗑️ Excluir meus     ││ ← Danger zone
│  │   dados             ││    (text: vermelho)
│  └─────────────────────┘│
│                         │
└─────────────────────────┘

Privacidade:
- Política de Privacidade (WebView)
- Termos de Uso (WebView)
- Revogar consentimento
- Excluir todos os dados (LGPD)
```

---

### Tela 17: Estado de Erro — Sem Internet / API Indisponível

```
┌──────────────────────────────┐
│                              │
│  [App hospedeiro]            │
│                              │
│  ┌──────────────────────┐   │
│  │                      │   │
│  │   👻 📡              │   │ ← Fantasminha sem sinal
│  │                      │   │
│  │  Sem conexão         │   │ ← ou "Serviço indisponível"
│  │                      │   │
│  │  O try-on precisa    │   │
│  │  de internet para    │   │
│  │  funcionar.          │   │
│  │                      │   │
│  │  [Tentar Novamente]  │   │
│  │  [Fechar]            │   │
│  │                      │   │
│  └──────────────────────┘   │
│                              │
└──────────────────────────────┘

- Não conta como tentativa (NFR-009)
- Retry com backoff exponencial
```

---

## 3. Acessibilidade (WCAG 2.1 Nível AA)

### Geral — Todas as Telas

**Perceptível:**
- Todas as imagens e ícones possuem `contentDescription` (equivalente a alt text)
- Contraste de cores verificado (mínimo 4.5:1 para texto, 3:1 para componentes UI)
- Informação nunca transmitida apenas por cor (ícones + texto + cor)
- Texto redimensionável até 200% sem quebra de layout (sp units)
- Nenhum scroll horizontal forçado

**Operável:**
- Todas as áreas de toque ≥ 48dp × 48dp (Material Design guideline)
- Ordem de foco (`android:importantForAccessibility`) segue ordem visual
- Sem armadilhas de foco (keyboard traps)
- Animações respeitam `Settings > Accessibility > Remove animations`
- Overlay: acessível via TalkBack (`AccessibilityNodeInfo`)

**Compreensível:**
- Idioma definido: `pt-BR`
- Labels em todos os inputs (campos de formulário)
- Mensagens de erro claras e acionáveis
- Navegação consistente entre telas
- Interações previsíveis (sem navegação surpresa)

**Robusto:**
- HTML semântico via componentes Material Design 3
- `contentDescription` onde necessário
- `android:labelFor` em campos de formulário
- LiveRegion para conteúdo dinâmico (loading, resultados)

### Acessibilidade por Tela

**Overlay (Fantasminha):**
- `contentDescription = "GhostFit. Toque para provar a roupa na tela"`
- Ao expandir: `announceForAccessibility("Detectando roupa...")`
- Resultado: `announceForAccessibility("Imagem gerada. Avalie o resultado.")`

**Tela de Resultado:**
- Imagem gerada: `contentDescription = "Imagem de você vestindo [tipo de roupa]"`
- Botões: `contentDescription` explícito ("Gostei", "Não gostei", "Compartilhar resultado", "Tentar novamente", "Trocar foto de referência")
- Feedback enviado: `announceForAccessibility("Obrigada pelo feedback!")`

**Loading States:**
- `android:accessibilityLiveRegion = "polite"`
- Progress: `contentDescription = "Carregando, X por cento"`

**Navegação por TalkBack:**
```
Tab → Próximo elemento interativo
Swipe left/right → Navegar entre elementos
Double tap → Ativar elemento
Swipe up-then-down → Abrir opções do elemento
```

### Checklist de Contraste de Cores

| Elemento | Foreground | Background | Ratio | Status |
|----------|-----------|------------|-------|--------|
| Texto corpo | #1F1F1F | #FFFFFF | 16.5:1 | ✓ AAA |
| Texto secundário | #6B6B6B | #FFFFFF | 5.3:1 | ✓ AA |
| Botão primário texto | #FFFFFF | #7C3AED | 5.8:1 | ✓ AA |
| Botão secundário texto | #7C3AED | #F3E8FF | 4.6:1 | ✓ AA |
| Erro texto | #DC2626 | #FFFFFF | 4.6:1 | ✓ AA |
| Link texto | #7C3AED | #FFFFFF | 5.8:1 | ✓ AA |
| Caption/muted | #9CA3AF | #FFFFFF | 2.9:1 | ⚠️ (decorativo) |

---

## 4. Biblioteca de Componentes

### Componente: Botão (Button)

**Variantes:**

| Variante | Background | Texto | Borda | Uso |
|----------|-----------|-------|-------|-----|
| Primary | #7C3AED | #FFFFFF | — | Ações principais (Começar, Gerar, Compartilhar) |
| Secondary | #F3E8FF | #7C3AED | — | Ações secundárias (Tentar Novamente, Trocar) |
| Tertiary | transparent | #7C3AED | — | Links/ações terciárias (Pular, Fechar) |
| Danger | transparent | #DC2626 | — | Ações destrutivas (Excluir dados) |

**Specs:**
- Altura mínima: 48dp
- Padding horizontal: 24dp
- Border radius: 12dp
- Font: 16sp, SemiBold (600)
- Full-width em contextos de ação principal

**Estados:**
- Default → cores da variante
- Pressed → darken 15%
- Focused → outline 2dp, cor primária, offset 2dp
- Disabled → opacity 50%, cursor blocked
- Loading → spinner + texto "Aguarde..."

---

### Componente: Card

**Variantes:**
- **Surface:** Background branco, elevation 1dp (cards de informação)
- **Outlined:** Borda 1dp neutral-200 (cards de seleção)
- **Highlighted:** Borda 2dp primary, background primary-50 (card selecionado/destaque)

**Specs:**
- Padding interno: 16dp
- Border radius: 16dp
- Gap entre cards: 12dp
- Elevation: 1dp (surface), 0dp (outlined)

---

### Componente: Overlay Card

**Para estados expandidos do fantasminha (loading, erro, resultado).**

**Specs:**
- Max width: 280dp
- Border radius: 20dp
- Background: #FFFFFF
- Elevation: 8dp
- Padding: 20dp
- Animação de entrada: scale 0→1 + fade in (200ms, ease-out)
- Animação de saída: scale 1→0.9 + fade out (150ms, ease-in)

---

### Componente: Feedback Buttons (Thumbs)

**Layout:** Row, 2 botões lado a lado

**Specs:**
- Tamanho: 64dp × 64dp cada
- Ícone: 24dp (outlined)
- Background default: neutral-50
- Background selected 👍: #DCFCE7 (verde claro)
- Background selected 👎: #FEE2E2 (vermelho claro)
- Animação ao selecionar: scale 1→1.2→1 (bounce, 300ms)

---

### Componente: Photo Thumbnail

**Specs:**
- Tamanho: 80dp × 80dp (galeria), 96dp × 96dp (picker)
- Border radius: 12dp
- Borda default: 2dp transparent
- Borda selecionada: 2dp primary (#7C3AED)
- Badge ✓: 20dp círculo verde, canto bottom-right
- Badge ★: 20dp círculo dourado (foto principal)

---

### Componente: Progress Bar

**Specs:**
- Altura: 4dp
- Border radius: 2dp
- Track: neutral-200
- Fill: gradiente primary (#7C3AED → #A78BFA)
- Animação: ease-in-out, atualiza a cada etapa

---

### Componente: Toast / Snackbar

**Para mensagens temporárias (feedback enviado, erro leve).**

**Specs:**
- Posição: bottom, 16dp do edge
- Height: 48dp
- Background: neutral-900 (#1F1F1F)
- Texto: #FFFFFF, 14sp
- Border radius: 8dp
- Duração: 3s
- Animação: slide up + fade in

---

### Componente: Bottom Sheet

**Para tela de resultado e pickers.**

**Specs:**
- Background: #FFFFFF
- Border radius top: 24dp
- Drag handle: 40dp × 4dp, neutral-300, center
- Scrim: #000000 50% opacity
- Animação: slide up (300ms, ease-out)
- Dismissível via drag down ou toque no scrim

---

### Componente: Onboarding Page

**Specs:**
- Ilustração: 200dp × 200dp, center
- Título: H1, 24sp, bold, center
- Descrição: Body, 16sp, regular, center, max 3 linhas
- CTA: Primary Button, full-width, bottom
- Page indicator: dots, 8dp, gap 8dp
- Navegação: swipe horizontal ou botão

---

### Componente: Banner Ad

**Specs (FR-016):**
- Posição: bottom da tela principal (não do overlay)
- Altura: 50dp (AdMob standard banner)
- Padding: 0dp
- Só visível para usuárias free
- Nunca aparece durante o fluxo core (loading, resultado)

---

## 5. Design Tokens

### Cores

**Paleta Primária (Roxo — identidade GhostFit):**
```
primary-50:   #F5F3FF   (backgrounds sutis)
primary-100:  #EDE9FE   (hover states)
primary-200:  #DDD6FE   (borders)
primary-400:  #A78BFA   (ícones secundários)
primary-500:  #8B5CF6   (ícones)
primary-600:  #7C3AED   (COR PRIMÁRIA — botões, links)
primary-700:  #6D28D9   (hover de botões)
primary-800:  #5B21B6   (pressed)
primary-900:  #4C1D95   (texto sobre fundo claro)
```

**Cores Semânticas:**
```
success:      #16A34A   (feedback positivo, validação ✓)
success-bg:   #DCFCE7   (background de sucesso)
warning:      #F59E0B   (avisos)
warning-bg:   #FEF3C7
error:        #DC2626   (erros, ações destrutivas)
error-bg:     #FEE2E2
info:         #2563EB   (informações)
info-bg:      #DBEAFE
```

**Neutros:**
```
neutral-50:   #FAFAFA   (background de cards)
neutral-100:  #F4F4F5   (background alternativo)
neutral-200:  #E4E4E7   (borders, dividers)
neutral-300:  #D4D4D8   (borders mais visíveis)
neutral-400:  #A1A1AA   (placeholder text)
neutral-500:  #71717A   (texto secundário)
neutral-600:  #52525B   (texto regular)
neutral-700:  #3F3F46   (texto forte)
neutral-800:  #27272A   (títulos)
neutral-900:  #1F1F1F   (texto máximo contraste)
white:        #FFFFFF
black:        #000000
```

**Contraste verificado (sobre branco):**
- primary-600 (#7C3AED): 5.8:1 ✓ AA
- neutral-900 (#1F1F1F): 16.5:1 ✓ AAA
- neutral-600 (#52525B): 7.2:1 ✓ AAA
- error (#DC2626): 4.6:1 ✓ AA
- success (#16A34A): 3.6:1 ✓ AA (large text only, usar sobre success-bg)

### Tipografia

**Font Family:** Roboto (sistema Android)

| Token | Size | Weight | Line Height | Uso |
|-------|------|--------|-------------|-----|
| display | 28sp | Bold (700) | 36sp | Splash, telas de destaque |
| h1 | 24sp | Bold (700) | 32sp | Títulos de tela |
| h2 | 20sp | SemiBold (600) | 28sp | Subtítulos, cards |
| h3 | 18sp | SemiBold (600) | 24sp | Seções |
| body-lg | 16sp | Regular (400) | 24sp | Texto principal |
| body | 14sp | Regular (400) | 20sp | Texto secundário |
| caption | 12sp | Regular (400) | 16sp | Legendas, contadores |
| button | 16sp | SemiBold (600) | 24sp | Labels de botões |
| overline | 11sp | Medium (500) | 16sp | Tags, badges |

### Espaçamento (base 4dp)

```
space-xs:     4dp
space-sm:     8dp
space-md:     12dp
space-lg:     16dp
space-xl:     20dp
space-2xl:    24dp
space-3xl:    32dp
space-4xl:    48dp
space-5xl:    64dp
```

**Padrões de Layout:**
- Padding de tela: 16dp horizontal
- Gap entre cards: 12dp
- Gap entre seções: 24dp
- Margin do overlay: 16dp dos edges

### Elevação (Shadows)

```
elevation-0:  nenhum (flat)
elevation-1:  0dp 1dp 3dp rgba(0,0,0,0.12)    (cards)
elevation-2:  0dp 2dp 6dp rgba(0,0,0,0.16)    (bottom sheet)
elevation-3:  0dp 4dp 12dp rgba(0,0,0,0.20)   (overlay, modals)
elevation-4:  0dp 8dp 24dp rgba(0,0,0,0.24)   (fantasminha flutuante)
```

### Border Radius

```
radius-sm:    4dp   (badges, tags)
radius-md:    8dp   (inputs, toasts)
radius-lg:    12dp  (botões, thumbnails)
radius-xl:    16dp  (cards)
radius-2xl:   20dp  (overlay cards)
radius-3xl:   24dp  (bottom sheets)
radius-full:  50%   (círculos, fantasminha)
```

### Animações

```
duration-fast:    150ms   (hover, press)
duration-normal:  200ms   (transições de estado)
duration-slow:    300ms   (bottom sheets, modals)
duration-loading: 2000ms  (shimmer loop)

easing-in:        cubic-bezier(0.4, 0, 1, 1)
easing-out:       cubic-bezier(0, 0, 0.2, 1)
easing-standard:  cubic-bezier(0.4, 0, 0.2, 1)
easing-bounce:    cubic-bezier(0.34, 1.56, 0.64, 1)
```

---

## 6. Handoff para Desenvolvedor

### Prioridades de Implementação

**Fase 1 — Fundação (Semana 1-2):**
1. Configurar design tokens (colors.xml, dimens.xml, type.xml)
2. Implementar componentes base (Button, Card, Toast)
3. Configurar tema Material Design 3 com paleta roxa
4. Implementar navegação (NavHost) entre telas

**Fase 2 — Onboarding (Semana 3-4):**
1. Tela de Splash (animação Lottie do fantasminha)
2. Onboarding 3 telas (ViewPager2)
3. Permissão SYSTEM_ALERT_WINDOW
4. Consentimento LGPD
5. Seleção de fotos + validação

**Fase 3 — Core (Semana 5-8):**
1. FloatingService (overlay fantasminha)
2. MediaProjection (captura de tela)
3. Drag & drop do overlay
4. Loading states (detecção + geração)
5. Tela de resultado com ações

**Fase 4 — Monetização (Semana 9-10):**
1. Contador de tentativas
2. Tela de limite atingido
3. Tela de planos
4. Google Play Billing
5. AdMob integration

### Notas de Implementação

**Overlay (FloatingService):**
```kotlin
// WindowManager.LayoutParams para o overlay
val params = WindowManager.LayoutParams(
    56.dp, 56.dp,  // tamanho do fantasminha
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    PixelFormat.TRANSLUCENT
)
params.gravity = Gravity.END or Gravity.BOTTOM
params.x = 16.dp  // margin
params.y = 100.dp // posição default
```

**Drag do Overlay:**
```kotlin
// Diferenciar tap vs drag:
// - Move < 10dp = TAP (captura tela)
// - Move >= 10dp = DRAG (reposiciona)
// Salvar posição em SharedPreferences
```

**Bottom Sheet (Resultado):**
```kotlin
// BottomSheetDialogFragment ou BottomSheetBehavior
// peekHeight: 60% da tela
// fullExpand: 90% da tela
// Dismissível via swipe down
```

**Acessibilidade obrigatória:**
```xml
<!-- Todos os ícones interativos -->
android:contentDescription="@string/desc_fantasminha"
android:importantForAccessibility="yes"
android:minWidth="48dp"
android:minHeight="48dp"

<!-- Live regions para loading -->
android:accessibilityLiveRegion="polite"
```

### Assets Necessários

**Ícones (SVG/Vector Drawable):**
- Fantasminha (logo) — 56dp, 24dp, 120dp variantes
- Fantasminha triste (erro)
- Fantasminha dormindo (limite atingido)
- Fantasminha com confete (sucesso)
- Thumbs up / down (24dp)
- Share (24dp)
- Refresh (24dp)
- Camera/photo (24dp)
- Settings gear (24dp)
- Close X (24dp)
- Back arrow (24dp)
- Lock (24dp)
- Check (20dp)

**Animações Lottie:**
- Fantasminha flutuando (idle loop)
- Fantasminha pulsando (captura)
- Shimmer loading (placeholder)
- Confete (setup completo)
- Bounce feedback (thumbs)

**Ilustrações (Onboarding):**
- Fantasminha sobre celular com roupas
- Fantasminha sobre Shopee
- Cadeado com escudo (privacidade)
- Exemplo corpo inteiro (guia de fotos)

---

## 7. Validação — Cobertura de Requisitos

### Mapeamento FR → Tela

| FR | Descrição | Tela(s) |
|----|-----------|---------|
| FR-001 | Cadastro fotos Google Fotos | Seleção de Fotos, Home (galeria) |
| FR-002 | Permissão overlay | Explicação Overlay (onboarding) |
| FR-003 | Consentimento LGPD | Consentimento LGPD |
| FR-004 | Overlay flutuante | Overlay (fantasminha) |
| FR-005 | Captura de tela | Overlay (toque → animação) |
| FR-006 | Overlay reposicionável | Overlay (drag) |
| FR-007 | Detecção de roupa | Loading Detecção |
| FR-008 | Aviso "sem roupa" | Erro "Nenhuma Roupa Detectada" |
| FR-009 | Geração try-on | Loading Geração + Resultado |
| FR-010 | Tentar novamente | Resultado (botão) |
| FR-011 | Trocar foto | Resultado → Mini-Picker |
| FR-012 | Fallback modelos | Transparente (sem tela dedicada) |
| FR-013 | Compartilhar com branding | Resultado (botão Compartilhar) |
| FR-014 | Feedback thumbs | Resultado (botões 👍👎) |
| FR-015 | Limite 3/dia | Home (contador), Limite Atingido |
| FR-016 | Anúncios | Home (banner), entre gerações |
| FR-017 | Planos pagos | Planos e Upgrade |
| FR-018 | Google Play Billing | Planos e Upgrade (ação) |
| FR-019 | Coleta dataset | Transparente (backend) |
| FR-020 | Roteamento IA | Transparente (backend) |
| FR-021 | Pipeline fine-tuning | Transparente (backend) |

**Cobertura: 21/21 FRs mapeados ✓**

### Checklist de Acessibilidade

- [x] WCAG 2.1 Nível AA compliance definido
- [x] Todas as áreas de toque ≥ 48dp
- [x] Contraste de cores verificado (4.5:1 texto, 3:1 UI)
- [x] ContentDescriptions definidos
- [x] LiveRegions para conteúdo dinâmico
- [x] Ordem de foco documentada
- [x] Animações respeitam preferências do sistema
- [x] Textos em sp (escaláveis)
- [x] Navegação consistente

### Sign-off

- [ ] Product Owner (Rafael) aprovou fluxos
- [ ] Arquitetura revisada (pós `/bmad:architecture`)
- [ ] Pronto para implementação

---

*Gerado pelo BMAD Method v6 — UX Designer*
*Data do Design: 2026-03-04*
