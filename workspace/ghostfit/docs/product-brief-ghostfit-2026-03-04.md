# Product Brief: GhostFit

**Date:** 2026-03-04
**Author:** rafael.giovannini
**Version:** 1.0
**Project Type:** Android Native (Kotlin)
**Project Level:** 2

---

## Executive Summary

GhostFit é um aplicativo Android que funciona como um overlay flutuante (fantasminha) sobre apps de e-commerce como Shopee e Shein. Com um único toque, o app captura a tela, detecta a roupa exibida usando IA de visão, e gera uma imagem realista do usuário vestindo aquela roupa — permitindo um "provador virtual" antes da compra. O público-alvo principal são mulheres de classe média que compram moda online e querem eliminar a incerteza de como a roupa ficará nelas.

---

## Problem Statement

### The Problem

Comprar roupas online é um tiro no escuro. As fotos dos produtos nos marketplaces mostram modelos com biotipos específicos (muito magras, muito altas, etc.) que não representam o consumidor real. Isso leva a compras frustradas — a roupa chega e o decote não fica bom, o caimento é diferente do esperado, o tamanho não corresponde. O resultado: devoluções, desperdício de tempo e dinheiro, e insatisfação do cliente.

**Exemplo concreto:** Roupas compradas na Shein e Shopee que pareciam ótimas nas fotos, mas ao chegar não correspondiam à expectativa — decotes que não ficavam bem, caimento diferente do modelo — resultando em devolução.

### Why Now?

O momento é ideal por dois fatores convergentes:
1. **Maturidade tecnológica:** Modelos de virtual try-on como o FASHN.ai e o Google Vertex AI VTON atingiram consistência suficiente para editar roupas em fotos sem alterar a fisionomia das pessoas. Isso não era possível 1-2 anos atrás.
2. **Gap de mercado:** Nenhum marketplace (Shopee, Shein, Renner, C&A, Zara, AliExpress) oferece virtual try-on hoje. O primeiro a resolver isso tem vantagem competitiva massiva.

### Impact if Unsolved

- Consumidores continuam comprando no escuro e devolvendo roupas
- Marketplaces perdem receita com devoluções e insatisfação
- A experiência de compra de moda online permanece inferior à da loja física
- Oportunidade de mercado inexplorada continua aberta para concorrentes

---

## Target Audience

### Primary Users

- **Demografia:** Mulheres (~80% do público), classe média, 18-45 anos
- **Comportamento:** Compradoras frequentes de moda online (Shopee, Shein principalmente)
- **Nível técnico:** Baixo a médio — o app precisa ser extremamente simples (um toque = resultado)
- **Pain point principal:** Incerteza sobre como a roupa vai ficar nelas antes de comprar
- **Dispositivo:** Android (smartphones mid-range a high-end)

### Secondary Users

- **Marketplaces (futuro B2B):** Times de produto de Shopee, Shein e outros que poderiam integrar a tecnologia GhostFit diretamente em seus apps, com o ícone do fantasminha nativo na plataforma
- **Homens (~20%):** Compradores de moda online masculina com a mesma dor

### User Needs

1. **Visualizar como a roupa ficaria nelas** antes de finalizar a compra — com seu próprio corpo e fisionomia
2. **Experiência sem fricção** — um toque e o resultado aparece, sem etapas complexas
3. **Confiança na compra** — reduzir a ansiedade e incerteza da compra online de roupas

---

## Solution Overview

### Proposed Solution

Aplicativo Android com overlay flutuante (ícone de fantasma) que funciona sobre qualquer app de e-commerce. O fluxo é:

1. **Setup (uma vez):** Usuária instala o GhostFit e seleciona fotos de corpo inteiro via Photo Picker nativo
2. **Uso diário:** Abre Shopee/Shein normalmente, navega pelos produtos
3. **Try-on (um toque):** Encontra roupa → toca no fantasminha → app captura tela automaticamente
4. **IA em ação:** ML Kit detecta a roupa on-device (< 100ms), Gemini 2.0 Flash classifica tipo/cor (GPT-4o Vision como fallback). Se não encontrar roupa, exibe aviso "Nenhuma roupa detectada"
5. **Geração:** FASHN.ai (primário) ou Vertex AI (fallback) gera imagem da usuária vestindo a roupa
6. **Resultado:** Exibe a imagem. Opções: "Tentar novamente", "Trocar foto", "Compartilhar"

### Key Features

- Overlay flutuante persistente (fantasminha) sobre qualquer app
- Captura de tela automática com um toque
- Detecção de roupa via IA de visão (modelo econômico)
- Geração de imagem virtual try-on via FASHN.ai/Vertex AI (modelos especializados em VTON)
- Seleção de fotos pessoais via Android Photo Picker nativo
- Compartilhamento do resultado com branding GhostFit (marketing viral)
- Monetização: 3 tentativas grátis/dia com anúncios + pacotes pagos/assinatura mensal
- Imagens efêmeras — não armazenadas no servidor (privacy by design)

### Value Proposition

"Veja como a roupa fica em VOCÊ antes de comprar — com um toque." O GhostFit elimina a principal barreira da compra de moda online: a incerteza. Nenhum marketplace oferece isso hoje.

---

## Business Objectives

### Goals

- **Validação de produto (0-3 meses):** Lançar MVP na Play Store e validar que a qualidade das imagens geradas é aprovada pelas usuárias
- **Crescimento orgânico (3-6 meses):** Atingir base inicial de usuárias ativas através de compartilhamento viral (feature de compartilhar resultado com branding)
- **Sustentabilidade (6-12 meses):** Receita de anúncios + assinaturas cobre custos de API
- **B2B (12+ meses):** Usar tração do app como vitrine para vender tecnologia/SDK para marketplaces

### Success Metrics

- Usuárias ativas mensais (MAU) — métrica principal
- Taxa de aprovação das imagens geradas (feedback thumbs up/down)
- Número de compartilhamentos por usuária (indicador de viralidade)
- Taxa de conversão free → pago
- Avaliação na Play Store (meta: 4.0+)
- Custo por geração de imagem (viabilidade econômica)

### Business Value

Duplo: **B2C** como app independente gerando receita via freemium + ads, e **B2B** como tecnologia proprietária para licenciar a marketplaces que não possuem virtual try-on.

---

## Scope

### In Scope (MVP - v1)

- App Android nativo (Kotlin)
- Overlay flutuante (fantasminha) sobre outros apps
- Cadastro de fotos via Android Photo Picker nativo
- Screenshot automático ao tocar no overlay
- Detecção de roupa via ML Kit (on-device) + GPT-4o Vision (classificação remota)
- Geração de imagem via FASHN.ai (primário) ou Vertex AI VTON (fallback)
- Aviso "nenhuma roupa detectada" quando aplicável
- Opções: "Tentar novamente" e "Trocar foto do usuário"
- Compartilhamento do resultado com branding GhostFit
- Monetização: 3 tentativas grátis/dia com anúncios + pacotes pagos/assinatura
- Conformidade LGPD: imagens efêmeras, consentimento explícito, criptografia
- Segurança: dados criptografados, sem armazenamento de fotos no servidor

### Out of Scope (Não entra na v1)

- Versão iOS (requer ambiente Mac para homologação)
- SDK/API B2B para marketplaces
- Histórico de try-ons salvos no app
- Múltiplas roupas na mesma imagem
- Visualização lateral / girar 360°
- Login social (Google/Apple)
- Suporte a vídeo
- Detecção automática de tamanho/medidas

### Future Considerations

- iOS (quando houver acesso a ambiente Mac)
- SDK B2B para integração nativa em marketplaces (fantasminha dentro do Shopee/Shein)
- Histórico de try-ons com comparação lado a lado
- Recomendação de tamanho baseada nas medidas do usuário
- Suporte a múltiplas roupas (look completo)
- Funcionalidade social (compartilhar looks, pedir opinião de amigos)

---

## Key Stakeholders

- **Rafael Giovannini (Founder/Developer)** - Influência Alta. Único responsável por produto, desenvolvimento, design e estratégia. Trabalha 4-6h/dia no projeto.

---

## Constraints and Assumptions

### Constraints

- **Budget:** Mínimo viável — custo deve ser o suficiente para ter algo entregável. Receita de ads precisa cobrir custo de tokens
- **Plataforma:** Apenas Android (sem acesso a Mac para build iOS)
- **Equipe:** Desenvolvedor solo (Rafael), 4-6h/dia
- **Tecnológica:** Dependência de APIs externas (FASHN.ai, GPT-4o Vision, Vertex AI) para funcionalidade core
- **Regulatória:** LGPD — fotos pessoais exigem consentimento explícito, criptografia e política de privacidade robusta
- **Play Store:** Políticas restritivas para apps com overlay — requer compliance rigoroso

### Assumptions

- FASHN.ai e/ou Vertex AI VTON mantêm consistência na geração de imagens sem alterar fisionomia
- Custo por geração de imagem será viável economicamente com modelo freemium + ads
- Usuárias possuem smartphones Android com capacidade suficiente para o app
- Android Photo Picker nativo permite seleção de fotos do usuário com as permissões adequadas (sem OAuth)
- A detecção de roupa via modelo de visão econômico terá acurácia suficiente
- Políticas da Play Store permitem overlay com as permissões corretas (SYSTEM_ALERT_WINDOW)
- Usuárias estão dispostas a fornecer fotos pessoais em troca da funcionalidade

---

## Success Criteria

- **Qualidade da IA:** Usuárias avaliam positivamente (thumbs up) em pelo menos 70% das imagens geradas
- **Adoção:** App está sendo usado recorrentemente (usuárias voltam para novas compras)
- **Viralidade:** Usuárias compartilham resultados organicamente, trazendo novas usuárias
- **Viabilidade econômica:** Receita de ads + assinaturas cobre custos de API dentro de 6 meses
- **Validação de mercado:** Feedback qualitativo confirma que o conceito resolve a dor de comprar roupa online

---

## Timeline and Milestones

### Target Launch

MVP na Play Store em **4-6 meses** (meta: Julho-Setembro 2026)

### Key Milestones

- **Mês 1:** Arquitetura definida, setup do projeto, protótipo de overlay funcional
- **Mês 2:** Integração com Google Fotos, fluxo de captura de tela funcionando
- **Mês 3:** Integração com modelos de IA (detecção + geração), fluxo completo end-to-end
- **Mês 4:** UI/UX polida, monetização (ads + in-app purchase), testes com usuárias reais
- **Mês 5:** Conformidade LGPD, segurança, políticas Play Store, beta testing
- **Mês 6:** Publicação na Play Store, monitoramento, iteração com feedback

---

## Risks and Mitigation

- **Risk:** Qualidade inconsistente das imagens geradas pela IA
  - **Likelihood:** Medium-High
  - **Mitigation:** Testar múltiplos modelos (FASHN.ai, Vertex AI), implementar fallback entre modelos, e permitir "tentar novamente" para a usuária

- **Risk:** Políticas da Play Store rejeitarem o app por uso de overlay
  - **Likelihood:** Medium
  - **Mitigation:** Estudar e seguir rigorosamente as policies de SYSTEM_ALERT_WINDOW, implementar apenas permissões necessárias, documentar uso legítimo

- **Risk:** LGPD — vazamento ou uso indevido de fotos pessoais
  - **Likelihood:** Low (mitigável)
  - **Mitigation:** Imagens 100% efêmeras (nunca armazenadas no servidor), criptografia end-to-end, consentimento explícito, política de privacidade transparente, auditorias de segurança

- **Risk:** Custo de API inviável com escala
  - **Likelihood:** Medium
  - **Mitigation:** Separar modelos por função (visão barata + geração premium), cache de detecções, limitar tentativas grátis, ads cobrem custo base

- **Risk:** Permissões sensíveis assustam usuárias (overlay + acesso a fotos)
  - **Likelihood:** Medium
  - **Mitigation:** Onboarding claro explicando por que cada permissão é necessária, transparência total sobre uso de dados

- **Risk:** Dependência de APIs externas (FASHN.ai, GPT-4o Vision, Vertex AI)
  - **Likelihood:** Low-Medium
  - **Mitigation:** Abstrair camada de IA para trocar providers facilmente, ter fallback entre modelos

---

## Next Steps

1. Create Product Requirements Document (PRD) - `/bmad:prd`
2. Conduct competitive research (virtual try-on market) - `/bmad:research`
3. Create UX design (fluxo do overlay) - `/bmad:create-ux-design`

---

**This document was created using BMAD Method v6 - Phase 1 (Analysis)**

*To continue: Run `/bmad:workflow-status` to see your progress and next recommended workflow.*
