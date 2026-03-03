# Product Brief: Motor Financeiro

**Date:** 2026-03-03
**Author:** rafael.giovannini
**Version:** 1.0
**Project Type:** SaaS Web App (backend preparado para separação futura)
**Project Level:** 2 (Medium)

---

## Executive Summary

Motor financeiro SaaS que se conecta diretamente às instituições financeiras brasileiras via Open Finance (APIs regulamentadas pelo Banco Central) para capturar movimentações em tempo real e categorizá-las automaticamente usando inteligência artificial. O sistema gera uma visão consolidada de todas as contas bancárias em um dashboard unificado, eliminando o trabalho manual de conciliação financeira.

Inicialmente desenvolvido para uso pessoal do autor, o projeto tem visão de se tornar um produto SaaS para pequenas e médias empresas, evoluindo para incluir gestão de contas a pagar e a receber.

---

## Problem Statement

### The Problem

A gestão financeira pessoal e de PMEs sofre de três problemas fundamentais:

1. **Controle excessivamente manual:** É necessário acessar cada banco individualmente, baixar extratos, importar em planilhas e categorizar transações manualmente. Esse processo é trabalhoso, repetitivo e propenso a erros.

2. **Falta de visão consolidada:** Com contas em múltiplas instituições financeiras, não existe uma forma simples de ver o quadro geral da saúde financeira — saldos, movimentações, compromissos — em um único lugar.

3. **Esquecimento de compromissos:** Sem um sistema automatizado de rastreamento, contas são esquecidas, pagamentos atrasam, juros e multas se acumulam desnecessariamente.

### Why Now?

- O **Open Finance Brasil** está maduro: regulamentado pelo Banco Central desde 2021, com cobertura ampla das principais instituições financeiras, APIs padronizadas e ecossistema em expansão.
- **IA generativa e modelos de classificação** estão acessíveis e performáticos, viabilizando categorização automática inteligente a baixo custo.
- O **.NET 8+** oferece um ecossistema robusto para aplicações financeiras com excelente performance e segurança.

### Impact if Unsolved

- **Perda financeira direta:** Juros por atraso, multas, perda de descontos por pagamento antecipado.
- **Tempo desperdiçado:** Horas semanais gastas em trabalho manual de conciliação financeira que poderia ser automatizado.
- **Decisões no escuro:** Sem visibilidade consolidada, decisões financeiras são tomadas com informação incompleta, levando a alocação ineficiente de recursos.

---

## Target Audience

### Primary Users

- **Pessoa física (uso pessoal):** Profissional que possui contas em múltiplos bancos e quer ter controle financeiro automatizado, sem depender de planilhas. Perfil tech-savvy, confortável com aplicações web.
- **Donos/Sócios de PMEs:** Micro e pequenos empresários que precisam de visibilidade sobre as finanças da empresa, muitas vezes gerenciando tudo sozinhos ou com equipe mínima.

### Secondary Users

- **Financeiro/Contador:** Profissional responsável pela operação financeira do dia-a-dia em PMEs. Precisa de uma ferramenta prática para gestão de fluxo de caixa e conciliação.

### User Needs

1. **Automação da coleta de dados:** Conectar contas bancárias uma vez e ter as movimentações sincronizadas automaticamente.
2. **Inteligência na categorização:** Não querer classificar cada transação manualmente — o sistema deve aprender e categorizar sozinho.
3. **Visão consolidada e visual:** Um dashboard claro que mostre a situação financeira de forma imediata e compreensível.

---

## Solution Overview

### Proposed Solution

Um motor financeiro web (SaaS) composto por:

1. **Camada de Integração:** Conexão com instituições financeiras via Open Finance Brasil para captura automática de movimentações (extratos, transações, saldos).
2. **Motor de Categorização:** Engine de IA que classifica transações automaticamente em categorias (moradia, alimentação, transporte, utilidades, lazer, etc.) com capacidade de aprendizado baseado em correções do usuário.
3. **Dashboard Financeiro:** Painel visual consolidado com visão de saldos, movimentações por categoria, tendências e alertas.

### Key Features (MVP)

- Conexão com bancos via Open Finance (consentimento e captura de dados)
- Sincronização automática de movimentações bancárias
- Categorização automática de transações via IA
- Dashboard com visão consolidada multi-banco
- Visualização de movimentações por categoria, período e conta
- Gestão de categorias (customização pelo usuário)

### Value Proposition

**"Conecte seus bancos uma vez. Veja tudo. Entenda tudo. Sem planilhas, sem trabalho manual."**

Diferente de soluções que dependem de importação manual de arquivos OFX/CSV ou de agregadores que não oferecem inteligência na categorização, o Motor Financeiro combina integração direta via Open Finance com IA para categorização, entregando uma experiência verdadeiramente automatizada.

---

## Business Objectives

### Goals

- **Fase 1 (Pessoal):** Ter um sistema funcional que o autor use diariamente para gerenciar suas finanças pessoais, substituindo completamente planilhas e controle manual.
- **Fase 2 (Validação):** Convidar 5-10 usuários beta (amigos/conhecidos) para validar a proposta de valor e coletar feedback.
- **Fase 3 (Produto):** Lançar como SaaS para PMEs com modelo freemium (pessoal gratuito, empresarial pago).

### Success Metrics

- Uso diário/semanal pelo autor (dogfooding)
- Tempo gasto em gestão financeira manual reduzido em 80%+
- Zero contas esquecidas/atrasadas após adoção
- NPS > 8 entre usuários beta (Fase 2)

### Business Value

- **Curto prazo:** Economia pessoal de tempo e dinheiro (eliminar multas/juros por atraso).
- **Médio prazo:** Aprendizado profundo em .NET, Open Finance, IA e arquitetura SaaS.
- **Longo prazo:** Potencial produto SaaS com receita recorrente no mercado de fintechs para PMEs.

---

## Scope

### In Scope (MVP)

- Integração com Open Finance Brasil (leitura de contas e movimentações)
- Motor de categorização automática com IA
- Dashboard financeiro web responsivo
- Autenticação e gerenciamento de consentimentos Open Finance
- Backend .NET com arquitetura preparada para separação futura (API independente)
- Banco de dados para persistência de movimentações e categorias
- Gestão de categorias customizáveis

### Out of Scope (MVP)

- Contas a pagar (geração automática e alertas) — Fase 2
- Contas a receber — Fase 2
- Multi-tenancy para PMEs — Fase 3
- Aplicativo mobile (iOS/Android)
- Integração com sistemas contábeis (ERP, Nota Fiscal)
- Funcionalidades de pagamento (apenas leitura, não executa pagamentos)
- Relatórios fiscais ou contábeis

### Future Considerations

- **Fase 2:** Contas a pagar automáticas (detecção de recorrências + alertas de vencimento)
- **Fase 2:** Contas a receber
- **Fase 3:** Multi-tenancy e onboarding para PMEs
- **Fase 3:** Relatórios gerenciais avançados (DRE simplificado, fluxo de caixa projetado)
- **Futuro:** App mobile, integração com ERPs, conciliação bancária empresarial

---

## Key Stakeholders

- **Rafael Giovannini (Criador/Developer)** - Influence: High. Único stakeholder, responsável por todas as decisões de produto, tecnologia e priorização. Usuário primário do sistema.

---

## Constraints and Assumptions

### Constraints

- **Orçamento:** Zero/mínimo. Priorizar free tiers e soluções self-hosted. Sem investimento em serviços pagos na fase inicial.
- **Equipe:** Projeto solo (1 pessoa). Todo o desenvolvimento, design e operação são responsabilidade do autor.
- **Open Finance:** Necessita de cadastro como participante ou uso de intermediários homologados pelo BC para acessar as APIs.
- **Regulatório:** Dados financeiros são sensíveis e sujeitos à LGPD. Necessário cuidado com armazenamento e tratamento.
- **Stack:** Backend em .NET (preferência do autor). Frontend a definir na fase de tech-spec.

### Assumptions

- As APIs do Open Finance Brasil estarão disponíveis e acessíveis (diretamente ou via intermediários) para o escopo necessário.
- É possível utilizar modelos de IA (local ou via API com free tier) para categorização de transações com boa acurácia.
- O autor tem disponibilidade contínua para desenvolver o projeto, mesmo sem prazo fixo.
- A infraestrutura pode ser inicialmente hospedada localmente ou em cloud com free tier (Azure, por exemplo, dado o stack .NET).

---

## Success Criteria

- O sistema sincroniza movimentações de pelo menos 2 bancos automaticamente via Open Finance.
- A categorização automática atinge 80%+ de acurácia sem intervenção manual.
- O dashboard exibe visão consolidada de saldos e movimentações de forma clara e útil.
- O autor adota o sistema como ferramenta principal de gestão financeira pessoal.
- O tempo gasto em controle financeiro manual é reduzido significativamente.
- O código segue boas práticas de segurança para dados financeiros (encriptação, LGPD compliance).

---

## Timeline and Milestones

### Target Launch

Sem prazo fixo. Desenvolvimento orgânico no ritmo do autor, priorizando qualidade e aprendizado.

### Key Milestones

- **M1 - Fundação:** Setup do projeto (.NET, banco de dados, autenticação básica)
- **M2 - Open Finance:** Integração funcional com pelo menos 1 instituição financeira (captura de movimentações)
- **M3 - Categorização:** Motor de IA categorizando transações automaticamente
- **M4 - Dashboard MVP:** Painel visual consolidado com dados reais
- **M5 - Beta pessoal:** Sistema funcional para uso diário do autor
- **M6 - Contas a Pagar:** Evolução para Fase 2 (após validação do MVP)

---

## Risks and Mitigation

- **Risk:** Complexidade da integração Open Finance (burocracia, certificações, APIs instáveis)
  - **Likelihood:** High
  - **Mitigation:** Pesquisar intermediários (Pluggy, Belvo) como alternativa de ramp-up. Começar com sandbox/ambiente de testes antes de produção. Ter fallback de importação manual (OFX/CSV) caso a integração direta demore.

- **Risk:** Perda de motivação (projeto pessoal sem deadline)
  - **Likelihood:** Medium
  - **Mitigation:** Usar o próprio sistema desde cedo (dogfooding). Definir milestones pequenos e celebrar cada conquista. Aplicar o framework BMAD para manter disciplina e estrutura.

- **Risk:** Segurança de dados financeiros (vazamento, acesso não autorizado)
  - **Likelihood:** Medium
  - **Mitigation:** Implementar encriptação em repouso e em trânsito desde o dia 1. Seguir OWASP Top 10. Nunca armazenar credenciais bancárias. Usar tokens OAuth do Open Finance com escopos mínimos.

- **Risk:** Custo de infraestrutura escalar além do orçamento zero
  - **Likelihood:** Low
  - **Mitigation:** Começar com free tiers (Azure Free, SQLite local). Arquitetura modular que permita migrar para cloud gradualmente. Monitorar custos desde o início.

- **Risk:** Acurácia da categorização por IA insuficiente
  - **Likelihood:** Medium
  - **Mitigation:** Começar com regras simples (regex/keywords) + IA como segunda camada. Permitir correção manual que alimente o modelo. Abordagem incremental de melhoria.

---

## Next Steps

1. Create Product Requirements Document (PRD) - `/prd`
2. Conduct user research (optional) - `/research`
3. Create UX design (if UI-heavy) - `/create-ux-design`

---

**This document was created using BMAD Method v6 - Phase 1 (Analysis)**

*To continue: Run `/workflow-status` to see your progress and next recommended workflow.*
