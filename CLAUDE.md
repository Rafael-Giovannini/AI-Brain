# 🧠 AI-BRAIN CORE ENGINE
## 🛡️ PROTOCOLO DE GOVERNANÇA AGÊNTICA (STACK: BMAD, SPEC-KIT, RALPH, SKINNER)

Este repositório é um motor de execução autônomo. O objetivo é transformar especificações em código funcional através de loops de feedback e controle de versão rigoroso.

---

## 🏗️ ARQUITETURA DO MOTOR
- **Estratégia (BMAD):** Decomposição de tarefas por personas (Architect/Developer/Scrum). Planejamento macro.
- **Definição (Spec-Kit):** Fonte da verdade. O código deve espelhar a spec.
- **Execução (Ralph Wiggum):** Loop TDD (Test-Driven Development) contínuo.
- **Controle (Principal Skinner):** Validação de qualidade e persistência via Git.

---

## 🚦 WORKFLOW DE EXECUÇÃO UNIVERSAL

### 0. Criação de Novo Projeto (Monorepo)
- Este repositório é um **monorepo**. Cada projeto vive em `workspace/<nome-do-projeto>/`.
- **Antes de criar qualquer arquivo de um novo projeto**, crie um branch dedicado: `feature/<nome-do-projeto>`.
- Comando: `git checkout -b feature/<nome-do-projeto>`
- Todos os docs, specs e código do projeto devem ser commitados nesse branch antes de merge no master.

### 1. Ciclo de Vida Completo (Fases)

| Fase | Responsável | Comando | Função |
|------|-------------|---------|--------|
| 1. Análise | BMAD | `/bmad:product-brief` | Definir visão e escopo do produto |
| 2. Planejamento | BMAD | `/bmad:prd` | Requisitos funcionais e não-funcionais |
| 3. Arquitetura | BMAD | `/bmad:architecture` | Design de arquitetura do sistema |
| 4. Sprint | BMAD | `/bmad:sprint-planning` | Decompor epics em stories |
| 5. Especificação | Spec-Kit | `/speckit.specify` | Gerar spec técnica da feature/story |
| 6. Execução | Ralph | `/ralph-loop` | Loop TDD até 100% dos testes |
| 7. Controle | Skinner | (automático) | Commit atômico + validação de qualidade |

### 2. Ciclo de Vida da Task (Dentro de uma Story)
1. **Especificação:** Gere ou atualize arquivos em `specs/` usando `/speckit.specify`. A spec é a fonte da verdade.
2. **Loop Ralph:** Inicie o `/ralph-loop`. O critério de parada é a passagem de 100% dos testes definidos na spec.
3. **Skinner Enforcement:**
   - Commits automáticos para cada unidade de trabalho concluída.
   - Reversão automática (`git reset`) se o loop entrar em estado de alucinação ou erro circular.

---

## 📜 REGRAS DE OURO (THE SKINNER LAWS)
1. **No Spec, No Code:** Nenhuma linha de código é escrita sem um arquivo de especificação ativo.
2. **Atomicidade:** Commits devem ser pequenos, descritivos e funcionais.
3. **Modularidade:** O motor deve buscar reaproveitamento de código consultando o codebase (Grep/Glob) antes de criar novas funções.
4. **Verificabilidade:** Todo código gerado pelo Ralph deve acompanhar seu respectivo arquivo de teste.

---

## 🔧 COMANDOS DO SISTEMA

### BMAD (Estratégia e Planejamento)
- `/bmad:product-brief` -> Criar visão do produto (Phase 1).
- `/bmad:prd` -> Definir requisitos (Phase 2).
- `/bmad:architecture` -> Design de arquitetura (Phase 3).
- `/bmad:sprint-planning` -> Planejar sprints e decompor stories (Phase 4).
- `/bmad:create-story` -> Criar story detalhada.
- `/bmad:dev-story` -> Implementar uma story (planejamento BMAD).
- `/bmad:workflow-status` -> Ver progresso do projeto.
- `/bmad:brainstorm` -> Sessão de brainstorming estruturado.
- `/bmad:research` -> Pesquisa de mercado/competitiva.
- `/bmad:tech-spec` -> Especificação técnica.
- `/bmad:solutioning-gate-check` -> Validar arquitetura contra requisitos.

### Spec-Kit (Especificação Técnica)
- `/speckit.specify` -> Gerar spec técnica de uma feature.
- `/speckit.plan` -> Planejar implementação.
- `/speckit.tasks` -> Gerar tasks a partir da spec.
- `/speckit.implement` -> Executar tasks do plano.
- `/speckit.clarify` -> Clarificar áreas da spec.
- `/speckit.analyze` -> Análise de consistência entre artefatos.

### Ralph (Execução TDD)
- `/ralph-loop` -> Executar ciclo de correção autônoma (TDD loop).

### Skinner (Controle de Qualidade)
- Automático: commits atômicos, validação, reversão em caso de erro circular.

---

## 📁 ESTRUTURA DE DIRETÓRIOS PADRÃO
- `docs/`: Documentos de produto (briefs, PRDs, arquitetura).
- `specs/`: Contratos de funcionalidades (Markdown).
- `src/`: Implementação técnica.
- `tests/`: Suíte de validação para o Skinner.
- `.spec-kit/`: Configurações do motor de especificações.
