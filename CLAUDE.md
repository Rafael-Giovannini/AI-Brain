# 🧠 AI-BRAIN CORE ENGINE
## 🛡️ PROTOCOLO DE GOVERNANÇA AGÊNTICA (STACK: BMAD, RALPH, SKINNER, KIRO)

Este repositório é um motor de execução autônomo. O objetivo é transformar especificações em código funcional através de loops de feedback e controle de versão rigoroso.

---

## 🏗️ ARQUITETURA DO MOTOR
- **Contexto (Kiro):** Indexação semântica e recuperação de conhecimento do repositório.
- **Definição (Spec-Kit):** Fonte da verdade. O código deve espelhar a spec.
- **Estratégia (BMAD):** Decomposição de tarefas por personas (Architect/Developer/Scrum).
- **Execução (Ralph Wiggum):** Loop TDD (Test-Driven Development) contínuo.
- **Controle (Principal Skinner):** Validação de qualidade e persistência via Git.

---

## 🚦 WORKFLOW DE EXECUÇÃO UNIVERSAL

### 1. Sincronização de Memória
- Antes de qualquer tarefa, execute `kiro index` para garantir que o motor conhece o estado atual do código.

### 2. Ciclo de Vida da Task
1. **Iniciação:** Use `/bmad-plan` para entender o impacto da nova tarefa.
2. **Especificação:** Gere ou atualize arquivos em `specs/` usando `specify`.
3. **Loop Ralph:** Inicie o `/ralph-loop`. O critério de parada é a passagem de 100% dos testes definidos na spec.
4. **Skinner Enforcement:** - Commits automáticos para cada unidade de trabalho concluída.
   - Reversão automática (`git reset`) se o loop entrar em estado de alucinação ou erro circular.

---

## 📜 REGRAS DE OURO (THE SKINNER LAWS)
1. **No Spec, No Code:** Nenhuma linha de código é escrita sem um arquivo de especificação ativo.
2. **Atomicidade:** Commits devem ser pequenos, descritivos e funcionais.
3. **Modularidade:** O motor deve buscar reaproveitamento de código consultando o **Kiro** antes de criar novas funções.
4. **Verificabilidade:** Todo código gerado pelo Ralph deve acompanhar seu respectivo arquivo de teste.

---

## 🔧 COMANDOS DO SISTEMA
- `/bmad-architect` -> Analisar estrutura e design pattern.
- `/bmad-developer` -> Implementar lógica e testes.
- `/ralph-loop` -> Executar ciclo de correção autônoma.
- `kiro query "[pergunta]"` -> Consultar o mapa mental do repositório.

---

## 📁 ESTRUTURA DE DIRETÓRIOS PADRÃO
- `specs/`: Contratos de funcionalidades (Markdown).
- `src/`: Implementação técnica.
- `tests/`: Suíte de validação para o Skinner.
- `.spec-kit/`: Configurações do motor de especificações.