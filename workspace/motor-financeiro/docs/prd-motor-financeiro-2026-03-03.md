# Product Requirements Document: Motor Financeiro

**Date:** 2026-03-03
**Author:** rafael.giovannini
**Version:** 1.0
**Project Type:** SaaS Web App (backend preparado para separação futura)
**Project Level:** 2 (Medium)
**Status:** Draft

---

## Document Overview

This Product Requirements Document (PRD) defines the functional and non-functional requirements for Motor Financeiro. It serves as the source of truth for what will be built and provides traceability from requirements through implementation.

**Related Documents:**
- Product Brief: `docs/product-brief-motor-financeiro-2026-03-03.md`

---

## Executive Summary

Motor financeiro SaaS que se conecta diretamente às instituições financeiras brasileiras via Open Finance (APIs regulamentadas pelo Banco Central) para capturar movimentações em tempo real e categorizá-las automaticamente usando inteligência artificial. O sistema gera uma visão consolidada de todas as contas bancárias em um dashboard unificado, eliminando o trabalho manual de conciliação financeira.

Inicialmente desenvolvido para uso pessoal do autor, com visão de evoluir para produto SaaS para PMEs.

---

## Product Goals

### Business Objectives

- **Fase 1 (Pessoal):** Ter um sistema funcional que o autor use diariamente para gerenciar suas finanças pessoais, substituindo completamente planilhas e controle manual.
- **Fase 2 (Validação):** Convidar 5-10 usuários beta (amigos/conhecidos) para validar a proposta de valor e coletar feedback.
- **Fase 3 (Produto):** Lançar como SaaS para PMEs com modelo freemium (pessoal gratuito, empresarial pago).

### Success Metrics

- Uso diário/semanal pelo autor (dogfooding)
- Tempo gasto em gestão financeira manual reduzido em 80%+
- Zero contas esquecidas/atrasadas após adoção
- NPS > 8 entre usuários beta (Fase 2)
- Sincronização funcional de pelo menos 2 bancos via Open Finance
- Categorização automática com acurácia >= 80% sem intervenção manual

---

## Functional Requirements

Functional Requirements (FRs) define **what** the system does - specific features and behaviors.

Each requirement includes:
- **ID**: Unique identifier (FR-001, FR-002, etc.)
- **Priority**: Must Have / Should Have / Could Have / Won't Have (MoSCoW)
- **Description**: What the system should do
- **Acceptance Criteria**: How to verify it's complete

---

### FR-001: Cadastro de Usuário

**Priority:** Must Have

**Description:**
O sistema deve permitir que novos usuários se cadastrem utilizando email e senha.

**Acceptance Criteria:**
- [ ] Usuário cria conta com email e senha
- [ ] Sistema valida formato de email e força da senha (mínimo 8 caracteres, maiúscula, número, caractere especial)
- [ ] Email de confirmação é enviado
- [ ] Dados sensíveis são armazenados encriptados
- [ ] Duplicatas de email são rejeitadas com mensagem clara

**Dependencies:** Nenhuma

---

### FR-002: Login/Logout com Sessão Segura

**Priority:** Must Have

**Description:**
O sistema deve permitir login com credenciais e manter sessão segura via tokens.

**Acceptance Criteria:**
- [ ] Usuário faz login com email e senha
- [ ] Sessão gerenciada via JWT com tempo de expiração
- [ ] Refresh token implementado para renovação silenciosa
- [ ] Logout invalida a sessão/token
- [ ] Rate limiting aplicado para proteção contra brute force

**Dependencies:** FR-001

---

### FR-003: Recuperação de Senha

**Priority:** Should Have

**Description:**
O sistema deve permitir que o usuário recupere acesso à conta via email.

**Acceptance Criteria:**
- [ ] Usuário solicita reset de senha informando email
- [ ] Link de recuperação enviado por email com token temporário
- [ ] Token expira após 1 hora
- [ ] Usuário define nova senha através do link
- [ ] Sessões anteriores são invalidadas após troca de senha

**Dependencies:** FR-001

---

### FR-004: Conexão com Instituição Financeira via Open Finance

**Priority:** Must Have

**Description:**
O sistema deve permitir que o usuário conecte suas contas bancárias através do fluxo de consentimento do Open Finance Brasil.

**Acceptance Criteria:**
- [ ] Usuário seleciona instituição financeira de uma lista de instituições suportadas
- [ ] Sistema inicia fluxo OAuth/redirecionamento para a instituição
- [ ] Usuário autoriza leitura de dados (contas e transações)
- [ ] Consentimento é armazenado de forma segura no sistema
- [ ] Sistema confirma conexão bem-sucedida ao usuário

**Dependencies:** FR-001, FR-002

---

### FR-005: Listagem de Contas Bancárias Conectadas

**Priority:** Must Have

**Description:**
Após consentimento, o sistema deve exibir as contas bancárias do usuário com seus respectivos saldos.

**Acceptance Criteria:**
- [ ] Sistema lista todas as contas conectadas (corrente, poupança, etc.)
- [ ] Exibe saldo atual de cada conta
- [ ] Identifica instituição financeira e tipo de conta
- [ ] Atualiza saldos na sincronização

**Dependencies:** FR-004

---

### FR-006: Sincronização Automática de Movimentações

**Priority:** Must Have

**Description:**
O sistema deve buscar transações das contas conectadas periodicamente e armazená-las no banco de dados.

**Acceptance Criteria:**
- [ ] Sincronização automática executada em intervalo configurável (padrão: a cada 6 horas)
- [ ] Transações novas são armazenadas sem duplicatas (deduplicação por ID/hash)
- [ ] Sincronização manual disponível sob demanda (botão "sincronizar agora")
- [ ] Histórico de sincronizações visível (última sync, status, quantidade de transações)
- [ ] Erros de sincronização são logados e o usuário é notificado

**Dependencies:** FR-004, FR-005

---

### FR-007: Gestão de Consentimentos

**Priority:** Must Have

**Description:**
O sistema deve permitir que o usuário visualize, renove e revogue consentimentos Open Finance.

**Acceptance Criteria:**
- [ ] Lista de consentimentos ativos com data de expiração
- [ ] Alerta quando consentimento está próximo de expirar (7 dias)
- [ ] Opção de renovar consentimento (redireciona para fluxo OAuth)
- [ ] Opção de revogar consentimento (remove acesso; dados já importados permanecem)
- [ ] Status claro: ativo, expirado, revogado

**Dependencies:** FR-004

---

### FR-008: Importação Manual de Movimentações (OFX/CSV)

**Priority:** Should Have

**Description:**
Como fallback, o sistema deve permitir importação manual de extratos bancários via upload de arquivos OFX ou CSV.

**Acceptance Criteria:**
- [ ] Upload de arquivos OFX e CSV
- [ ] Parser identifica e extrai transações corretamente
- [ ] Deduplicação com transações já existentes
- [ ] Preview das transações antes de confirmar importação
- [ ] Transações importadas são categorizáveis como as automáticas

**Dependencies:** FR-001, FR-002

---

### FR-009: Categorização Automática de Transações

**Priority:** Must Have

**Description:**
O sistema deve categorizar automaticamente cada transação utilizando inteligência artificial.

**Acceptance Criteria:**
- [ ] Cada transação nova recebe uma categoria automaticamente
- [ ] Acurácia mínima de 80% na categorização sem intervenção manual
- [ ] Categorias padrão: Moradia, Alimentação, Transporte, Saúde, Educação, Lazer, Utilidades, Renda, Transferência, Outros
- [ ] Nível de confiança da classificação é registrado
- [ ] Transações com baixa confiança são marcadas para revisão

**Dependencies:** FR-006, FR-012

---

### FR-010: Correção Manual de Categoria

**Priority:** Must Have

**Description:**
O sistema deve permitir que o usuário altere a categoria de uma transação, registrando a correção para aprendizado futuro.

**Acceptance Criteria:**
- [ ] Usuário altera categoria de qualquer transação com 1-2 cliques
- [ ] Correção é registrada no histórico (categoria original → nova categoria)
- [ ] Dado de correção é armazenado para alimentar o modelo de aprendizado
- [ ] Opção de aplicar a correção a transações similares (mesmo descrição/estabelecimento)

**Dependencies:** FR-009

---

### FR-011: Aprendizado Baseado em Correções (Feedback Loop)

**Priority:** Should Have

**Description:**
O sistema deve melhorar a categorização futura com base nas correções feitas pelo usuário.

**Acceptance Criteria:**
- [ ] Sistema identifica padrões nas correções (mesma descrição → mesma categoria)
- [ ] Após N correções consistentes para um padrão, classificação futura é ajustada
- [ ] Aprendizado é por usuário (cada usuário tem seu perfil de categorização)
- [ ] Acurácia melhora progressivamente com o uso

**Dependencies:** FR-009, FR-010

---

### FR-012: Gestão de Categorias Customizáveis

**Priority:** Must Have

**Description:**
O sistema deve permitir que o usuário crie, edite e exclua categorias de transação.

**Acceptance Criteria:**
- [ ] Categorias padrão pré-definidas no sistema (não podem ser excluídas, apenas desativadas)
- [ ] Usuário cria novas categorias com nome e ícone/cor
- [ ] Usuário edita nome e cor de categorias customizadas
- [ ] Exclusão de categoria: transações são movidas para "Outros" ou categoria escolhida
- [ ] Limite razoável de categorias customizadas (ex: 50)

**Dependencies:** FR-001

---

### FR-013: Visão Consolidada de Saldos Multi-Banco

**Priority:** Must Have

**Description:**
O dashboard deve exibir o saldo total consolidado e o saldo individual por conta bancária.

**Acceptance Criteria:**
- [ ] Saldo total (soma de todas as contas) exibido em destaque
- [ ] Saldo individual por conta com identificação do banco
- [ ] Data/hora da última sincronização visível
- [ ] Variação de saldo em relação ao período anterior (positiva/negativa)

**Dependencies:** FR-005, FR-006

---

### FR-014: Visualização de Movimentações por Período

**Priority:** Must Have

**Description:**
O sistema deve permitir visualizar a lista de transações filtrada por período de tempo.

**Acceptance Criteria:**
- [ ] Filtros pré-definidos: hoje, última semana, último mês, último trimestre
- [ ] Filtro customizado com seleção de data início/fim
- [ ] Lista de transações paginada (20-50 itens por página)
- [ ] Ordenação por data (padrão: mais recente primeiro), valor e categoria
- [ ] Total de entradas e saídas do período selecionado

**Dependencies:** FR-006

---

### FR-015: Visualização de Gastos por Categoria

**Priority:** Must Have

**Description:**
O dashboard deve exibir gráficos mostrando a distribuição de gastos por categoria.

**Acceptance Criteria:**
- [ ] Gráfico de pizza/donut mostrando percentual por categoria
- [ ] Gráfico de barras mostrando valores absolutos por categoria
- [ ] Filtro por período aplicável aos gráficos
- [ ] Interação: clicar em categoria filtra as transações correspondentes
- [ ] Exibição de valor e percentual por categoria

**Dependencies:** FR-009, FR-014

---

### FR-016: Filtro por Conta Bancária

**Priority:** Must Have

**Description:**
O sistema deve permitir filtrar todas as visualizações por conta bancária específica ou exibir todas.

**Acceptance Criteria:**
- [ ] Seletor de conta disponível em todas as telas de visualização
- [ ] Opção "Todas as contas" como padrão
- [ ] Filtro persiste durante a sessão de navegação
- [ ] Saldos e gráficos atualizam conforme conta selecionada

**Dependencies:** FR-005

---

### FR-017: Tendências e Comparativo Mensal

**Priority:** Should Have

**Description:**
O dashboard deve exibir gráficos de tendência comparando gastos entre meses.

**Acceptance Criteria:**
- [ ] Gráfico de linha: gastos totais por mês (últimos 6-12 meses)
- [ ] Comparativo mês atual vs mês anterior por categoria
- [ ] Indicadores de aumento/diminuição de gastos (setas/cores)
- [ ] Média móvel de gastos como referência

**Dependencies:** FR-014, FR-015

---

### FR-018: Busca/Pesquisa em Transações

**Priority:** Should Have

**Description:**
O sistema deve permitir buscar transações por texto (descrição, estabelecimento, valor).

**Acceptance Criteria:**
- [ ] Campo de busca com pesquisa por texto na descrição
- [ ] Filtro combinável com período e conta
- [ ] Resultados exibidos em tempo real (debounce de 300ms)
- [ ] Busca por faixa de valor (de/até)

**Dependencies:** FR-006

---

### FR-019: Detalhamento de Transação Individual

**Priority:** Must Have

**Description:**
O sistema deve exibir todos os detalhes de uma transação ao selecioná-la.

**Acceptance Criteria:**
- [ ] Exibe: banco de origem, data/hora, valor, categoria atribuída, descrição original do banco
- [ ] Exibe se categorização foi automática ou manual
- [ ] Opção de alterar categoria diretamente na tela de detalhes
- [ ] Informação de confiança da classificação IA

**Dependencies:** FR-006, FR-009

---

### FR-020: Configuração de Preferências do Usuário

**Priority:** Could Have

**Description:**
O sistema deve permitir que o usuário configure preferências pessoais.

**Acceptance Criteria:**
- [ ] Configuração de moeda padrão (BRL default)
- [ ] Configuração de fuso horário
- [ ] Configuração de idioma (PT-BR default)
- [ ] Preferências persistem entre sessões

**Dependencies:** FR-001

---

### FR-021: Visualização de Perfil e Dados da Conta

**Priority:** Should Have

**Description:**
O sistema deve permitir que o usuário visualize e edite seus dados pessoais.

**Acceptance Criteria:**
- [ ] Exibe nome, email, data de cadastro
- [ ] Permite edição de nome e dados pessoais
- [ ] Alteração de email requer confirmação por email
- [ ] Opção de exclusão de conta (LGPD - direito ao esquecimento)

**Dependencies:** FR-001

---

## Non-Functional Requirements

Non-Functional Requirements (NFRs) define **how** the system performs - quality attributes and constraints.

---

### NFR-001: Tempo de Resposta da API

**Priority:** Must Have

**Description:**
As APIs do sistema devem responder dentro de limites aceitáveis para boa experiência do usuário.

**Acceptance Criteria:**
- [ ] 95% das requisições com resposta em < 500ms
- [ ] Nenhuma requisição > 2s (exceto sincronização Open Finance)

**Rationale:**
Performance percebida impacta diretamente a adoção e uso diário do sistema.

---

### NFR-002: Carregamento do Dashboard

**Priority:** Must Have

**Description:**
O dashboard deve carregar rapidamente para incentivar uso frequente.

**Acceptance Criteria:**
- [ ] Primeira renderização significativa < 3s com cache
- [ ] Primeira renderização significativa < 5s sem cache
- [ ] Indicador de loading para dados assíncronos

**Rationale:**
O dashboard é a tela principal; lentidão aqui desmotiva o uso diário (dogfooding).

---

### NFR-003: Sincronização Open Finance

**Priority:** Should Have

**Description:**
A sincronização de transações deve ser eficiente e não bloquear o uso do sistema.

**Acceptance Criteria:**
- [ ] Processamento de até 1.000 transações/sync em < 30s
- [ ] Sincronização executada em background (não bloqueia UI)
- [ ] Progresso visível quando sincronização manual é acionada

**Rationale:**
Sync pesada não deve comprometer a experiência do usuário.

---

### NFR-004: Encriptação de Dados em Repouso

**Priority:** Must Have

**Description:**
Todos os dados financeiros sensíveis devem ser encriptados no banco de dados.

**Acceptance Criteria:**
- [ ] Dados financeiros encriptados com AES-256
- [ ] Credenciais e tokens nunca armazenados em texto plano
- [ ] Chaves de encriptação gerenciadas de forma segura (não no código-fonte)

**Rationale:**
Dados financeiros são altamente sensíveis; comprometimento do banco não deve expor dados legíveis.

---

### NFR-005: Encriptação em Trânsito

**Priority:** Must Have

**Description:**
Todas as comunicações devem ser encriptadas.

**Acceptance Criteria:**
- [ ] Todas as comunicações via HTTPS com TLS 1.2+
- [ ] Sem exceções para endpoints internos expostos
- [ ] Certificados válidos e renovados automaticamente

**Rationale:**
Dados financeiros em trânsito são alvo de interceptação; TLS é requisito mínimo.

---

### NFR-006: Conformidade LGPD

**Priority:** Must Have

**Description:**
O sistema deve cumprir a Lei Geral de Proteção de Dados.

**Acceptance Criteria:**
- [ ] Dados pessoais tratados conforme LGPD
- [ ] Usuário pode solicitar exportação de todos os seus dados
- [ ] Usuário pode solicitar exclusão completa de conta e dados
- [ ] Política de privacidade clara e acessível
- [ ] Consentimento explícito para coleta e uso de dados

**Rationale:**
Dados financeiros são dados sensíveis sob a LGPD; compliance é obrigatório.

---

### NFR-007: Autenticação Segura

**Priority:** Must Have

**Description:**
O sistema de autenticação deve seguir melhores práticas de segurança.

**Acceptance Criteria:**
- [ ] JWT com expiração curta (15-30 min para access token)
- [ ] Refresh tokens com rotação
- [ ] Rate limiting: máximo 5 tentativas de login falhadas por minuto/IP
- [ ] Senhas hasheadas com bcrypt ou Argon2

**Rationale:**
Autenticação fraca é o vetor de ataque mais comum; proteção desde o dia 1.

---

### NFR-008: Proteção OWASP Top 10

**Priority:** Must Have

**Description:**
O sistema deve ser protegido contra as 10 vulnerabilidades mais comuns do OWASP.

**Acceptance Criteria:**
- [ ] Proteção contra SQL Injection (parameterized queries/ORM)
- [ ] Proteção contra XSS (sanitização de output, CSP headers)
- [ ] Proteção contra CSRF (tokens anti-CSRF)
- [ ] Headers de segurança configurados (HSTS, X-Frame-Options, etc.)
- [ ] Dependências auditadas regularmente (vulnerabilidades conhecidas)

**Rationale:**
Aplicação financeira é alvo prioritário; OWASP Top 10 é o mínimo aceitável.

---

### NFR-009: Capacidade Inicial

**Priority:** Should Have

**Description:**
O sistema deve suportar a carga esperada para fase pessoal/beta.

**Acceptance Criteria:**
- [ ] Suportar 1-10 usuários simultâneos sem degradação perceptível
- [ ] Arquitetura preparada para escalar até 100 usuários
- [ ] Banco de dados dimensionado para 500.000+ transações

**Rationale:**
Fase 1 é pessoal, mas a arquitetura deve permitir crescimento para Fase 2/3.

---

### NFR-010: Disponibilidade

**Priority:** Should Have

**Description:**
O sistema deve estar disponível de forma confiável.

**Acceptance Criteria:**
- [ ] 99% uptime (aceitar downtime planejado para manutenção na Fase 1)
- [ ] Graceful degradation se Open Finance estiver indisponível (dados em cache permanecem acessíveis)

**Rationale:**
Para dogfooding funcionar, o sistema precisa estar disponível quando o autor precisa consultar.

---

### NFR-011: Backup de Dados

**Priority:** Must Have

**Description:**
Os dados devem ser protegidos contra perda.

**Acceptance Criteria:**
- [ ] Backup automático diário do banco de dados
- [ ] Retenção mínima de 30 dias
- [ ] Procedimento de restore testado e documentado
- [ ] Backup armazenado em local diferente do banco principal

**Rationale:**
Perda de dados financeiros históricos é irrecuperável e inaceitável.

---

### NFR-012: Tratamento de Falhas de Integração

**Priority:** Must Have

**Description:**
O sistema deve lidar graciosamente com falhas nas integrações externas.

**Acceptance Criteria:**
- [ ] Se Open Finance falhar, transações anteriores permanecem acessíveis
- [ ] Retry com backoff exponencial para falhas temporárias
- [ ] Máximo de 3 retries antes de marcar como falha
- [ ] Todas as falhas logadas com detalhes para diagnóstico
- [ ] Usuário notificado sobre falhas de sincronização

**Rationale:**
APIs externas são inerentemente instáveis; resiliência é essencial.

---

### NFR-013: Responsividade

**Priority:** Must Have

**Description:**
A interface deve funcionar em diferentes tamanhos de tela.

**Acceptance Criteria:**
- [ ] UI funcional e utilizável em desktop (1024px+)
- [ ] UI funcional e utilizável em mobile (375px+)
- [ ] Layout adaptativo (não apenas "encolhido")

**Rationale:**
Consultar finanças no celular é caso de uso comum; responsividade é must have.

---

### NFR-014: Acessibilidade Básica

**Priority:** Could Have

**Description:**
A interface deve seguir práticas básicas de acessibilidade.

**Acceptance Criteria:**
- [ ] HTML semântico correto
- [ ] Contraste mínimo WCAG AA (4.5:1 para texto normal)
- [ ] Navegação por teclado funcional

**Rationale:**
Boas práticas de acessibilidade melhoram a experiência para todos os usuários.

---

### NFR-015: Cobertura de Testes

**Priority:** Must Have

**Description:**
O código deve ter cobertura de testes abrangente para garantir confiabilidade.

**Acceptance Criteria:**
- [ ] Mínimo 90% de cobertura no backend (unit + integration tests)
- [ ] Testes de integração para fluxos críticos (auth, sync, categorização)
- [ ] Testes executam em CI/CD pipeline

**Rationale:**
Projeto solo exige alta cobertura de testes como rede de segurança contra regressões. Critério definido pelo stakeholder: 90% mínimo.

---

### NFR-016: Logging e Observabilidade

**Priority:** Must Have

**Description:**
O sistema deve ter logging estruturado em todas as camadas para rastreabilidade completa.

**Acceptance Criteria:**
- [ ] Logs estruturados (JSON) em todas as camadas: API, serviços, integração, banco
- [ ] Correlation ID para rastreabilidade de requisições end-to-end
- [ ] Níveis de log: Debug, Info, Warning, Error, Critical
- [ ] Logs de erro incluem stack trace e contexto suficiente para diagnóstico
- [ ] Rotação de logs configurada para não consumir disco indefinidamente

**Rationale:**
Logs completos são essenciais para debugging em projeto solo; sem equipe de suporte, o sistema precisa "contar" o que aconteceu. Requisito explícito do stakeholder.

---

### NFR-017: Auditoria de Ações do Usuário

**Priority:** Must Have

**Description:**
Todas as ações sensíveis devem ser registradas em log de auditoria imutável.

**Acceptance Criteria:**
- [ ] Login/logout registrados com timestamp, IP e user-agent
- [ ] Alterações de dados pessoais logadas (antes/depois)
- [ ] Conexão/revogação de consentimentos Open Finance logada
- [ ] Alterações de categoria registradas
- [ ] Log de auditoria é append-only (imutável)
- [ ] Retenção mínima de 1 ano

**Rationale:**
Dados financeiros exigem trilha de auditoria para conformidade e investigação de incidentes.

---

### NFR-018: Proteção de Tokens e Segredos

**Priority:** Must Have

**Description:**
Tokens, chaves de API e segredos devem ser protegidos adequadamente.

**Acceptance Criteria:**
- [ ] Tokens Open Finance armazenados encriptados no banco
- [ ] Chaves de API e segredos em variáveis de ambiente ou vault (nunca em código-fonte)
- [ ] Segredos não aparecem em logs (mascaramento automático)
- [ ] Rotação de segredos possível sem downtime

**Rationale:**
Comprometimento de tokens Open Finance daria acesso a dados financeiros reais.

---

### NFR-019: Isolamento de Dados por Usuário

**Priority:** Must Have

**Description:**
Dados de um usuário nunca devem ser acessíveis por outro.

**Acceptance Criteria:**
- [ ] Todas as queries incluem filtro por user_id
- [ ] Validação de ownership em todos os endpoints (não apenas na query)
- [ ] Testes automatizados verificando isolamento de dados
- [ ] Nenhum endpoint expõe IDs sequenciais (usar UUIDs)

**Rationale:**
Isolamento de dados é a base de segurança em qualquer sistema multi-usuário com dados financeiros.

---

### NFR-020: Sanitização de Inputs

**Priority:** Must Have

**Description:**
Todos os inputs do usuário devem ser sanitizados e validados.

**Acceptance Criteria:**
- [ ] Validação de tipo, formato e tamanho em todos os inputs no backend
- [ ] Sanitização contra injection em todas as camadas (SQL, NoSQL, XSS)
- [ ] Rejeição de inputs malformados com mensagem de erro clara (sem expor internals)
- [ ] Upload de arquivos (OFX/CSV) validado: tipo, tamanho máximo, conteúdo

**Rationale:**
Input validation é a primeira linha de defesa; dados financeiros exigem rigor máximo.

---

## Epics

Epics are logical groupings of related functionality that will be broken down into user stories during sprint planning (Phase 4).

Each epic maps to multiple functional requirements and will generate 2-10 stories.

---

### EPIC-001: Autenticação e Gestão de Usuário

**Description:**
Sistema completo de registro, login e gestão de perfil do usuário, com segurança reforçada desde o dia 1.

**Functional Requirements:**
- FR-001 (Cadastro de Usuário)
- FR-002 (Login/Logout com Sessão Segura)
- FR-003 (Recuperação de Senha)
- FR-021 (Visualização de Perfil e Dados da Conta)

**Story Count Estimate:** 4-6

**Priority:** Must Have

**Business Value:**
Base fundamental do sistema. Sem autenticação segura, nenhuma funcionalidade financeira pode ser disponibilizada. Garante que dados sensíveis são protegidos desde o primeiro acesso.

---

### EPIC-002: Integração Open Finance

**Description:**
Conexão com instituições financeiras brasileiras via Open Finance para captura automática de contas e movimentações, incluindo gestão de consentimentos e fallback de importação manual.

**Functional Requirements:**
- FR-004 (Conexão via Open Finance / Consentimento)
- FR-005 (Listagem de Contas Conectadas)
- FR-006 (Sincronização Automática de Movimentações)
- FR-007 (Gestão de Consentimentos)
- FR-008 (Importação Manual OFX/CSV)

**Story Count Estimate:** 6-9

**Priority:** Must Have

**Business Value:**
Core do produto — é a fonte dos dados financeiros. Sem integração com bancos, não há dados para categorizar ou visualizar. O fallback de importação manual garante usabilidade mesmo quando a integração direta não está disponível.

---

### EPIC-003: Motor de Categorização IA

**Description:**
Engine de classificação automática de transações por categoria usando inteligência artificial, com ciclo de feedback do usuário para melhoria contínua da acurácia.

**Functional Requirements:**
- FR-009 (Categorização Automática de Transações)
- FR-010 (Correção Manual de Categoria)
- FR-011 (Aprendizado Baseado em Correções)
- FR-012 (Gestão de Categorias Customizáveis)

**Story Count Estimate:** 5-7

**Priority:** Must Have

**Business Value:**
Diferencial competitivo do produto. Transforma dados brutos em informação organizada automaticamente, eliminando o trabalho manual que é o principal ponto de dor do usuário.

---

### EPIC-004: Dashboard e Visualização Financeira

**Description:**
Painel visual consolidado com saldos multi-banco, movimentações, gráficos por categoria, filtros, busca e detalhamento de transações.

**Functional Requirements:**
- FR-013 (Visão Consolidada de Saldos)
- FR-014 (Visualização por Período)
- FR-015 (Gastos por Categoria)
- FR-016 (Filtro por Conta)
- FR-017 (Tendências e Comparativo Mensal)
- FR-018 (Busca em Transações)
- FR-019 (Detalhamento de Transação)
- FR-020 (Configuração de Preferências)

**Story Count Estimate:** 6-10

**Priority:** Must Have

**Business Value:**
Interface com o usuário onde o valor do produto é percebido. O dashboard é a materialização da proposta de valor: "Veja tudo. Entenda tudo. Sem planilhas."

---

## User Stories (High-Level)

User stories follow the format: "As a [user type], I want [goal] so that [benefit]."

These are preliminary stories. Detailed stories will be created in Phase 4 (Implementation).

---

### EPIC-001: Autenticação e Gestão de Usuário

- Como **usuário novo**, quero me cadastrar com email e senha para ter acesso ao sistema.
- Como **usuário registrado**, quero fazer login de forma segura para acessar minhas finanças.
- Como **usuário**, quero recuperar minha senha caso a esqueça para não perder acesso à minha conta.

### EPIC-002: Integração Open Finance

- Como **usuário**, quero conectar meus bancos via Open Finance para que minhas movimentações sejam capturadas automaticamente.
- Como **usuário**, quero que minhas transações sejam sincronizadas periodicamente para ter dados sempre atualizados.
- Como **usuário**, quero gerenciar (ver, renovar, revogar) os consentimentos das minhas contas conectadas para manter controle sobre meus dados.

### EPIC-003: Motor de Categorização IA

- Como **usuário**, quero que minhas transações sejam categorizadas automaticamente para não precisar classificar manualmente cada uma.
- Como **usuário**, quero corrigir a categoria de uma transação para que o sistema aprenda e melhore com o tempo.
- Como **usuário**, quero criar e personalizar categorias para organizar meus gastos da forma que faz sentido para mim.

### EPIC-004: Dashboard e Visualização Financeira

- Como **usuário**, quero ver o saldo consolidado de todas as minhas contas em um único lugar para ter visão geral imediata.
- Como **usuário**, quero visualizar meus gastos por categoria e período para entender para onde meu dinheiro está indo.
- Como **usuário**, quero comparar meus gastos entre meses para identificar tendências e ajustar hábitos.

---

## User Personas

### Persona Primária: Rafael (Uso Pessoal)

**Perfil:** Profissional de tecnologia, 30+, com contas em múltiplos bancos (2-4). Tech-savvy, confortável com aplicações web. Quer controle financeiro sem perder tempo com planilhas.

**Dores:**
- Acessa cada banco manualmente para ver movimentações
- Categoriza gastos em planilha — processo tedioso e inconsistente
- Esquece compromissos financeiros por falta de visibilidade consolidada

**Objetivos:**
- Ver toda sua vida financeira em um único dashboard
- Ter transações categorizadas automaticamente
- Não gastar mais de 5 minutos por dia em gestão financeira

### Persona Secundária (Futuro): Carlos (Dono de PME)

**Perfil:** Micro/pequeno empresário, gerencia finanças da empresa sozinho. Não é técnico mas usa ferramentas web no dia-a-dia. Precisa de visibilidade sem complexidade.

**Dores:**
- Perde controle de gastos com múltiplas contas empresariais
- Não tem ERP mas precisa de algum controle financeiro

**Objetivos:**
- Visão clara de fluxo de caixa
- Conciliação bancária automatizada

---

## User Flows

### Flow 1: Onboarding (Primeiro Uso)

1. Usuário acessa a aplicação → Tela de cadastro
2. Preenche dados (email, senha) → Confirma email
3. Faz login → Dashboard vazio com CTA "Conecte seu primeiro banco"
4. Clica em conectar → Seleciona instituição → Fluxo OAuth Open Finance
5. Autoriza leitura → Sistema sincroniza contas e transações
6. Dashboard atualizado com dados reais → Transações categorizadas

### Flow 2: Uso Diário (Rotina)

1. Login → Dashboard consolidado com saldos e gráficos
2. Revisa transações recentes → Verifica categorias
3. Corrige categorias erradas (se houver) → Sistema aprende
4. Consulta gráfico de gastos por categoria → Identifica padrões
5. Logout ou fecha app

### Flow 3: Gestão de Contas

1. Acessa área de contas → Lista de contas conectadas
2. Verifica status dos consentimentos → Identifica expiração
3. Renova consentimentos → Fluxo OAuth
4. (Opcional) Conecta novo banco → Fluxo de conexão
5. Verifica sincronização → Dados atualizados

---

## Dependencies

### Internal Dependencies

- **EPIC-001 → EPIC-002, 003, 004:** Autenticação é pré-requisito para todos os demais epics
- **EPIC-002 → EPIC-003:** Dados de transações (Open Finance) alimentam o motor de categorização
- **EPIC-002 → EPIC-004:** Dashboard precisa de dados sincronizados para exibir
- **EPIC-003 → EPIC-004:** Dashboard exibe categorias geradas pelo motor de IA

### External Dependencies

- **Open Finance Brasil APIs:** Dependência crítica. Necessita acesso via intermediário (Pluggy, Belvo) ou registro direto. Instabilidade da API afeta sincronização.
- **Modelo de IA para Categorização:** API com free tier (OpenAI, Claude) ou modelo local (Ollama). Custo e latência impactam viabilidade.
- **Provedor de Email:** Para confirmação de cadastro e recuperação de senha (ex: SendGrid free tier, Resend).
- **Banco de Dados:** PostgreSQL e/ou MongoDB, dependendo da necessidade por domínio. Hospedagem local ou cloud free tier.

---

## Assumptions

- As APIs do Open Finance Brasil estarão disponíveis e acessíveis (diretamente ou via intermediários) para o escopo necessário (leitura de contas e transações).
- É possível utilizar modelos de IA (local ou via API com free tier) para categorização de transações com acurácia >= 80%.
- O autor tem disponibilidade contínua para desenvolver o projeto, mesmo sem prazo fixo.
- A infraestrutura pode ser hospedada localmente ou em cloud com free tier (Azure, dado o stack .NET).
- PostgreSQL e/ou MongoDB são adequados para o volume de dados esperado (500k+ transações).
- O ecossistema .NET 8+ oferece bibliotecas maduras para Open Finance, JWT e integração com bancos de dados.

---

## Out of Scope

Os seguintes itens estão **explicitamente fora do escopo** do MVP:

- **Contas a pagar** (geração automática e alertas) — Fase 2
- **Contas a receber** — Fase 2
- **Multi-tenancy para PMEs** — Fase 3
- **Aplicativo mobile** (iOS/Android)
- **Integração com sistemas contábeis** (ERP, Nota Fiscal)
- **Funcionalidades de pagamento** (sistema é somente leitura, não executa pagamentos)
- **Relatórios fiscais ou contábeis**
- **Notificações push ou alertas via email** (pode ser adicionado como Should Have se sobrar tempo)
- **Importação de investimentos** (ações, fundos, CDBs)

---

## Open Questions

Nenhuma questão em aberto no momento. Questões técnicas serão endereçadas na fase de Architecture (Phase 3).

---

## Approval & Sign-off

### Stakeholders

- **Rafael Giovannini (Criador/Developer/Product Owner)** — Único stakeholder. Responsável por todas as decisões.

### Approval Status

- [x] Product Owner (rafael.giovannini)

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-03-03 | rafael.giovannini | Initial PRD |

---

## Next Steps

### Phase 3: Architecture

Run `/bmad:architecture` to create system architecture based on these requirements.

The architecture will address:
- All 21 functional requirements (FRs)
- All 20 non-functional requirements (NFRs)
- Technical stack decisions (.NET 8+, PostgreSQL/MongoDB, frontend)
- Data models and APIs
- System components and integration patterns
- Security architecture

### Phase 4: Sprint Planning

After architecture is complete, run `/bmad:sprint-planning` to:
- Break epics into detailed user stories
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
| EPIC-001 | Autenticação e Gestão de Usuário | FR-001, FR-002, FR-003, FR-021 | 4-6 |
| EPIC-002 | Integração Open Finance | FR-004, FR-005, FR-006, FR-007, FR-008 | 6-9 |
| EPIC-003 | Motor de Categorização IA | FR-009, FR-010, FR-011, FR-012 | 5-7 |
| EPIC-004 | Dashboard e Visualização Financeira | FR-013, FR-014, FR-015, FR-016, FR-017, FR-018, FR-019, FR-020 | 6-10 |
| **Total** | | **21 FRs** | **21-32 stories** |

---

## Appendix B: Prioritization Details

### Functional Requirements by Priority

| Priority | Count | Percentage | FRs |
|----------|-------|------------|-----|
| **Must Have** | 13 | 62% | FR-001, FR-002, FR-004, FR-005, FR-006, FR-007, FR-009, FR-010, FR-012, FR-013, FR-014, FR-015, FR-016, FR-019 |
| **Should Have** | 6 | 29% | FR-003, FR-008, FR-011, FR-017, FR-018, FR-021 |
| **Could Have** | 2 | 9% | FR-020 |

### Non-Functional Requirements by Priority

| Priority | Count | Percentage | NFRs |
|----------|-------|------------|------|
| **Must Have** | 14 | 70% | NFR-001, NFR-002, NFR-004, NFR-005, NFR-006, NFR-007, NFR-008, NFR-011, NFR-012, NFR-013, NFR-015, NFR-016, NFR-017, NFR-018, NFR-019, NFR-020 |
| **Should Have** | 4 | 20% | NFR-003, NFR-009, NFR-010 |
| **Could Have** | 2 | 10% | NFR-014 |

### Overall Distribution

- **Total Requirements:** 41 (21 FRs + 20 NFRs)
- **Must Have:** 27 (66%) — Core do MVP, inegociável
- **Should Have:** 10 (24%) — Importante, implementar se possível
- **Could Have:** 4 (10%) — Nice to have, skip se necessário
