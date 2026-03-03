# Feature Specification: Motor Financeiro

**Feature Branch**: `001-motor-financeiro`
**Created**: 2026-03-03
**Status**: Draft
**Input**: Sprint plan do Motor Financeiro — sistema de gestão financeira pessoal com integração Open Finance, categorização automática por IA e dashboard analítico.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastro e Autenticação Segura (Priority: P1)

Como pessoa física que deseja organizar suas finanças, quero me cadastrar no sistema com email e senha, confirmar minha conta, fazer login de forma segura e recuperar minha senha caso a esqueça, para ter acesso protegido ao meu painel financeiro.

**Why this priority**: Sem autenticação, nenhuma outra funcionalidade pode ser acessada. É a fundação de segurança e identidade do sistema. Todo dado financeiro é sensível e requer isolamento por usuário.

**Independent Test**: Pode ser testado criando uma conta, confirmando por email, fazendo login, logout e recuperação de senha. Entrega valor como sistema de identidade standalone.

**Acceptance Scenarios**:

1. **Given** um visitante não registrado, **When** preenche email válido e senha forte (mínimo 8 caracteres, maiúscula, número, caractere especial) e submete o formulário de registro, **Then** recebe email de confirmação com link válido por 24 horas.
2. **Given** um usuário com email confirmado, **When** insere credenciais corretas no formulário de login, **Then** é autenticado e redirecionado ao dashboard com sessão ativa.
3. **Given** um usuário autenticado, **When** solicita logout, **Then** a sessão é invalidada e o acesso às rotas protegidas é bloqueado.
4. **Given** um atacante tentando login, **When** erra credenciais 5 vezes em menos de 1 minuto, **Then** o sistema bloqueia temporariamente novas tentativas daquele IP.
5. **Given** um usuário que esqueceu a senha, **When** solicita recuperação informando seu email, **Then** recebe link de reset válido por 1 hora (sem revelar se o email existe no sistema).
6. **Given** um usuário com sessão ativa, **When** a sessão de curta duração expira, **Then** o sistema renova a sessão automaticamente de forma silenciosa usando token de longa duração.
7. **Given** um token de longa duração reutilizado (possível roubo), **When** o sistema detecta a reutilização, **Then** revoga toda a família de tokens do usuário por segurança.

---

### User Story 2 - Conexão com Banco via Open Finance (Priority: P2)

Como usuário cadastrado, quero conectar meus bancos via Open Finance (consentimento regulatório), para que o sistema acesse automaticamente minhas contas e movimentações bancárias.

**Why this priority**: A conexão bancária é o mecanismo de entrada de dados primário do sistema. Sem dados bancários, as funcionalidades de categorização e dashboard não têm conteúdo.

**Independent Test**: Pode ser testado selecionando uma instituição financeira, completando o fluxo de consentimento OAuth, e verificando que a conexão aparece na lista com status ativo.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado, **When** seleciona uma instituição financeira na lista de instituições suportadas, **Then** é redirecionado para o fluxo de consentimento da instituição.
2. **Given** um usuário que completou o consentimento OAuth, **When** retorna ao sistema, **Then** a conexão é registrada com status "ativo" e os tokens de acesso são armazenados de forma encriptada.
3. **Given** um usuário com conexões ativas, **When** acessa a lista de conexões, **Then** vê cada conexão com nome da instituição, status (ativo/expirado/revogado) e data de expiração do consentimento.
4. **Given** um consentimento próximo de expirar (7 dias), **When** o usuário acessa o sistema, **Then** vê alerta visual indicando a necessidade de renovação.
5. **Given** um usuário que deseja revogar consentimento, **When** confirma a revogação de uma conexão, **Then** o consentimento é revogado na instituição e no sistema, mas os dados já importados são mantidos.
6. **Given** uma falha temporária na comunicação com a instituição, **When** o sistema tenta conectar, **Then** realiza até 3 tentativas com intervalo crescente antes de reportar falha.

---

### User Story 3 - Visualização de Contas e Sincronização de Transações (Priority: P3)

Como usuário com banco conectado, quero ver todas as minhas contas bancárias com saldos atualizados e ter minhas transações sincronizadas automaticamente, para manter um panorama financeiro sempre atualizado.

**Why this priority**: A listagem de contas e sincronização de transações formam o pipeline de dados que alimenta todo o restante do sistema (categorização, dashboard, busca).

**Independent Test**: Pode ser testado verificando que após conexão bancária, as contas aparecem com saldos, e as transações são sincronizadas periodicamente sem intervenção manual.

**Acceptance Scenarios**:

1. **Given** um usuário com banco conectado, **When** acessa a listagem de contas, **Then** vê todas as contas (corrente, poupança) com nome, instituição, tipo, saldo e moeda.
2. **Given** o sistema com sincronização configurada, **When** o intervalo programado é atingido (padrão: a cada 6 horas), **Then** as transações novas são buscadas e armazenadas sem duplicatas.
3. **Given** um usuário que deseja dados imediatos, **When** aciona "sincronizar agora", **Then** a sincronização é executada sob demanda e o resultado (sucesso/falha) é exibido.
4. **Given** transações já existentes no sistema, **When** uma sincronização retorna transações duplicadas, **Then** o sistema identifica e descarta duplicatas automaticamente.
5. **Given** uma falha na sincronização, **When** o erro é temporário, **Then** o sistema faz até 3 tentativas com intervalo crescente; se persistir, registra falha e notifica o usuário na interface.
6. **Given** um usuário, **When** acessa o histórico de sincronizações, **Then** vê data/hora, status (sucesso/parcial/falha) e quantidade de transações importadas de cada execução.

---

### User Story 4 - Categorização Automática por IA (Priority: P4)

Como usuário com transações sincronizadas, quero que elas sejam categorizadas automaticamente por inteligência artificial, para entender meus padrões de gasto sem esforço manual.

**Why this priority**: A categorização transforma dados brutos (transações) em informação acionável (distribuição de gastos), viabilizando dashboards e análises.

**Independent Test**: Pode ser testado verificando que após sincronização, transações recebem categorias automaticamente com indicador de confiança, e transações de baixa confiança são marcadas para revisão.

**Acceptance Scenarios**:

1. **Given** transações novas após sincronização, **When** o processo de categorização é executado, **Then** cada transação recebe uma categoria e um índice de confiança (0 a 1).
2. **Given** uma transação com confiança abaixo de 0.7, **When** exibida na lista, **Then** é marcada visualmente como "precisa revisão".
3. **Given** o usuário corrige a categoria de uma transação, **When** salva a correção, **Then** o sistema registra a correção e marca a transação como categorizada manualmente.
4. **Given** o usuário corrigiu a categoria de uma transação, **When** solicita "aplicar a similares", **Then** o sistema identifica transações com mesma descrição e aplica a mesma categoria.
5. **Given** 3 ou mais correções consistentes para o mesmo padrão de descrição, **When** o sistema analisa o histórico, **Then** cria automaticamente uma regra de categorização local para aquele padrão.
6. **Given** regras de categorização locais existentes, **When** novas transações são categorizadas, **Then** as regras locais são consultadas antes da IA (priorizando personalização e reduzindo custos).
7. **Given** a IA indisponível temporariamente, **When** transações precisam ser categorizadas, **Then** são classificadas como "Outros" com baixa confiança, para categorização posterior.

---

### User Story 5 - Dashboard Financeiro Consolidado (Priority: P5)

Como usuário com contas e transações categorizadas, quero um dashboard que mostre minha situação financeira de forma visual e consolidada, para tomar decisões informadas sobre meu dinheiro.

**Why this priority**: O dashboard é a interface principal de valor do produto — onde o usuário consome toda a informação processada pelas funcionalidades anteriores.

**Independent Test**: Pode ser testado verificando que o dashboard exibe saldos consolidados, gráficos de distribuição de gastos por categoria, lista de transações filtráveis, e detalhamento individual.

**Acceptance Scenarios**:

1. **Given** um usuário com contas conectadas, **When** acessa o dashboard, **Then** vê saldo total consolidado, saldo por conta, e variação em relação ao período anterior (positiva/negativa com indicador visual).
2. **Given** um usuário no dashboard, **When** visualiza os gráficos de gastos, **Then** vê distribuição de gastos por categoria (pizza/donut) e valores absolutos por categoria (barras) para o período selecionado.
3. **Given** um usuário, **When** seleciona um filtro de período (hoje, semana, mês, trimestre, ou datas customizadas), **Then** todas as visualizações (saldos, gráficos, transações) se atualizam conforme o período.
4. **Given** um usuário, **When** seleciona uma conta específica no filtro de conta, **Then** todas as visualizações se restringem à conta selecionada; "Todas as contas" é o padrão.
5. **Given** um usuário na lista de transações, **When** clica em uma transação, **Then** vê detalhes completos: banco, data/hora, valor, categoria, descrição original, se a categorização foi automática ou manual, e índice de confiança.
6. **Given** um usuário na lista de transações, **When** ordena por coluna (data, valor, categoria), **Then** a lista se reordena conforme selecionado; paginação funciona com até 50 itens por página.
7. **Given** um usuário, **When** clica em uma categoria no gráfico, **Then** a lista de transações é filtrada para exibir apenas as transações daquela categoria.

---

### User Story 6 - Gestão de Categorias Personalizáveis (Priority: P6)

Como usuário, quero criar e personalizar minhas próprias categorias de transação, além das categorias padrão oferecidas, para organizar gastos de forma que faça sentido para minha realidade.

**Why this priority**: Personalização de categorias aumenta a relevância do sistema para cada usuário, mas depende da estrutura de categorização já estar funcional.

**Independent Test**: Pode ser testado criando, editando e excluindo categorias customizadas, e verificando que as categorias padrão do sistema não podem ser excluídas.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado, **When** acessa a gestão de categorias, **Then** vê a lista de categorias padrão (Moradia, Alimentação, Transporte, Saúde, Educação, Lazer, Utilidades, Renda, Transferência, Outros) e suas categorias customizadas, cada uma com cor e ícone.
2. **Given** um usuário, **When** cria uma nova categoria customizada (nome, cor, ícone), **Then** a categoria fica disponível para categorização manual e automática.
3. **Given** um usuário com menos de 50 categorias customizadas, **When** tenta criar a 51ª, **Then** o sistema rejeita com mensagem de limite atingido.
4. **Given** um usuário que deseja excluir uma categoria customizada, **When** confirma a exclusão, **Then** as transações daquela categoria migram para "Outros" (ou categoria escolhida pelo usuário).
5. **Given** categorias padrão do sistema, **When** um usuário tenta excluí-las, **Then** a ação é bloqueada (apenas desativação é permitida).

---

### User Story 7 - Busca e Filtros Avançados em Transações (Priority: P7)

Como usuário, quero buscar transações por texto livre (descrição) e por faixa de valor, combinando com filtros de período, conta e categoria, para encontrar rapidamente movimentações específicas.

**Why this priority**: Busca avançada é uma funcionalidade de conveniência que enriquece a experiência, mas o sistema é funcional sem ela.

**Independent Test**: Pode ser testado digitando termos de busca e verificando que transações correspondentes aparecem nos resultados, inclusive com combinação de filtros.

**Acceptance Scenarios**:

1. **Given** um usuário com transações, **When** digita um termo de busca no campo de pesquisa, **Then** após breve pausa de digitação (300ms), os resultados são filtrados por correspondência textual na descrição.
2. **Given** um usuário, **When** define faixa de valor (mínimo e/ou máximo), **Then** apenas transações dentro da faixa são exibidas.
3. **Given** um usuário com busca ativa, **When** combina busca textual com filtros de período, conta e categoria, **Then** todos os filtros são aplicados simultaneamente (AND lógico).

---

### User Story 8 - Importação Manual de Extratos (Priority: P8)

Como usuário com contas em bancos sem Open Finance, quero importar extratos manualmente via arquivo (OFX ou CSV), para centralizar todo meu histórico financeiro em um único lugar.

**Why this priority**: Importação manual é fallback para bancos sem Open Finance. Importante para cobertura completa, mas não é o fluxo principal.

**Independent Test**: Pode ser testado fazendo upload de um arquivo OFX ou CSV, visualizando o preview das transações extraídas, confirmando a importação e verificando que as transações aparecem na lista geral.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado, **When** faz upload de um arquivo OFX ou CSV válido (até 10MB), **Then** o sistema extrai as transações e exibe um preview em tabela antes da importação.
2. **Given** um preview de transações extraídas, **When** o usuário confirma a importação, **Then** as transações são armazenadas, deduplicadas contra existentes, e categorizáveis normalmente.
3. **Given** um arquivo inválido (extensão errada, formato corrompido, acima de 10MB), **When** o usuário tenta upload, **Then** recebe mensagem de erro clara indicando o problema.
4. **Given** um arquivo CSV sem colunas padrão, **When** o sistema faz parse, **Then** tenta detectar automaticamente as colunas de data, descrição e valor.

---

### User Story 9 - Tendências e Comparativos Mensais (Priority: P9)

Como usuário com histórico de transações, quero comparar meus gastos entre meses e visualizar tendências, para identificar padrões e ajustar hábitos financeiros ao longo do tempo.

**Why this priority**: Análise de tendências agrega valor analítico, mas depende de volume suficiente de dados históricos para ser relevante.

**Independent Test**: Pode ser testado verificando gráfico de tendência mensal (últimos 6-12 meses) e comparativo entre mês atual e anterior por categoria.

**Acceptance Scenarios**:

1. **Given** um usuário com pelo menos 2 meses de dados, **When** acessa a visualização de tendências, **Then** vê gráfico de linha com gastos totais por mês (últimos 6-12 meses) e média móvel como referência.
2. **Given** um usuário, **When** acessa o comparativo mensal, **Then** vê indicadores de aumento/diminuição por categoria entre mês atual e anterior (setas + cores).
3. **Given** um mês sem dados de transação, **When** exibido no gráfico de tendências, **Then** aparece como valor zero, sem quebrar a visualização.

---

### User Story 10 - Perfil, Dados da Conta e Preferências (Priority: P10)

Como usuário, quero visualizar e editar meus dados pessoais, configurar preferências de exibição (moeda, fuso horário, idioma) e exercer meus direitos de privacidade (exportar e excluir dados), para manter controle total sobre minha conta.

**Why this priority**: Gestão de perfil e preferências é complementar — o sistema funciona com configurações padrão sensatas. Direitos de privacidade (LGPD) são obrigatórios mas não bloqueiam uso diário.

**Independent Test**: Pode ser testado editando nome, alterando email (com confirmação), exportando dados em formato legível, e solicitando exclusão de conta.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado, **When** acessa seu perfil, **Then** vê seus dados pessoais e pode editar nome e dados pessoais.
2. **Given** um usuário que deseja alterar email, **When** submete novo email, **Then** recebe email de confirmação no novo endereço antes da alteração efetivar.
3. **Given** um usuário que deseja exportar seus dados (LGPD), **When** solicita exportação, **Then** recebe arquivo com todos os seus dados (pessoais, contas, transações, categorias, regras).
4. **Given** um usuário que deseja excluir sua conta (LGPD), **When** confirma a exclusão, **Then** a conta é desativada imediatamente e todos os dados são removidos permanentemente após 30 dias.
5. **Given** um usuário, **When** configura preferências (moeda padrão, fuso horário, idioma), **Then** as configurações persistem entre sessões e afetam a formatação de valores e datas em todo o sistema.

---

### Edge Cases

- **Conexão duplicada com mesma instituição:** O sistema permite múltiplas conexões com a mesma instituição financeira (ex: contas PF e PJ no mesmo banco), mas exibe alerta informando que já existe conexão ativa com aquela instituição antes de prosseguir.
- **Open Finance indisponível >24h:** O sistema aplica circuit breaker (abre após 5 falhas consecutivas, half-open após 30s). Dados em cache e já importados permanecem acessíveis. Sincronizações falhas são registradas no histórico e retentadas no próximo ciclo. Usuário vê status de falha na interface.
- **Exclusão de conta com syncs pendentes:** Ao solicitar exclusão, o sistema cancela imediatamente todos os jobs de sincronização pendentes, revoga todos os consentimentos Open Finance ativos nas instituições, inicia soft delete da conta, e envia email confirmando o encerramento e o prazo de 30 dias para remoção permanente.
- **Transações em moeda estrangeira:** O sistema armazena valor e moeda originais da transação, exibindo com símbolo da moeda correspondente. Saldos consolidados no dashboard somam apenas transações em BRL; transações em outras moedas são exibidas individualmente mas excluídas das agregações de saldo.
- **Sincronizações simultâneas na mesma conexão:** O sistema limita a 1 sincronização por conexão (FR-025). Tentativas concorrentes são rejeitadas com mensagem informativa.
- **IA indisponível por período prolongado:** Transações são classificadas como "Outros" com confiança 0.0, marcadas como "precisa revisão" (FR-040). Regras de categorização locais continuam funcionando. Quando a IA volta, transações pendentes podem ser recategorizadas no próximo batch.
- **CSV com encoding diferente:** O sistema tenta detectar encoding automaticamente (UTF-8, depois ISO-8859-1). Para colunas não reconhecidas, exibe tela de mapeamento manual onde o usuário associa colunas do arquivo aos campos esperados (data, descrição, valor). Também permite seleção manual de encoding.
- **Saldos negativos:** O sistema exibe saldos negativos normalmente com indicador visual (cor vermelha). Saldos negativos são incluídos no cálculo do saldo consolidado.
- **Volume alto (>10.000 transações/mês):** O sistema usa paginação obrigatória (50 itens/página), índices compostos no PostgreSQL, e lazy loading. Particionamento por data será implementado quando o volume justificar (>500k transações totais).
- **Tokens revogados externamente pela instituição:** Na próxima tentativa de sincronização, se o token for rejeitado (HTTP 401/403), o sistema marca a conexão como "revogado" e notifica o usuário na interface com orientação para reconectar.

## Requirements *(mandatory)*

### Functional Requirements

**Autenticação e Identidade**
- **FR-001**: O sistema DEVE permitir cadastro de usuário com email e senha, com validação de formato de email e força de senha (mínimo 8 caracteres, incluindo maiúscula, número e caractere especial).
- **FR-002**: O sistema DEVE enviar email de confirmação após cadastro, com link válido por 24 horas.
- **FR-003**: O sistema DEVE autenticar usuários com sessão de curta duração (15 minutos) renovável automaticamente via token de longa duração (7 dias), com rotação de tokens a cada renovação.
- **FR-004**: O sistema DEVE detectar reutilização de token de longa duração e revogar toda a família de tokens associada.
- **FR-005**: O sistema DEVE bloquear tentativas de login após 5 falhas em 1 minuto por IP.
- **FR-006**: O sistema DEVE oferecer fluxo de recuperação de senha via email, com token de reset válido por 1 hora, sem revelar se o email está cadastrado.
- **FR-007**: O sistema DEVE invalidar todas as sessões ativas após troca de senha.

**Perfil e Privacidade**
- **FR-008**: O sistema DEVE permitir ao usuário visualizar e editar seus dados pessoais (nome).
- **FR-009**: O sistema DEVE exigir confirmação por email para alteração de endereço de email.
- **FR-010**: O sistema DEVE permitir exportação de todos os dados pessoais em formato legível (JSON), conforme LGPD.
- **FR-011**: O sistema DEVE permitir exclusão de conta com desativação imediata e remoção permanente de todos os dados após 30 dias. Ao excluir, o sistema DEVE cancelar jobs de sincronização pendentes, revogar consentimentos Open Finance ativos, e enviar email confirmando encerramento.

**Integração Open Finance**
- **FR-012**: O sistema DEVE listar instituições financeiras suportadas para conexão via Open Finance.
- **FR-013**: O sistema DEVE iniciar fluxo de consentimento OAuth com a instituição financeira selecionada.
- **FR-014**: O sistema DEVE armazenar tokens de acesso bancário de forma encriptada (AES-256).
- **FR-015**: O sistema DEVE exibir lista de conexões do usuário com status (ativo, expirado, revogado) e data de expiração.
- **FR-016**: O sistema DEVE alertar o usuário quando consentimento estiver a 7 dias de expirar.
- **FR-017**: O sistema DEVE permitir renovação e revogação de consentimento, mantendo dados já importados após revogação.
- **FR-018**: O sistema DEVE implementar retry com intervalo crescente (3 tentativas) e circuit breaker para comunicação com provedores externos.
- **FR-018a**: O sistema DEVE alertar o usuário quando este tentar conectar uma instituição financeira com a qual já possui conexão ativa, permitindo prosseguir após confirmação.
- **FR-018b**: O sistema DEVE detectar revogação externa de tokens (HTTP 401/403 durante sync), marcar a conexão como "revogado", e notificar o usuário na interface com orientação para reconectar.

**Contas Bancárias**
- **FR-019**: O sistema DEVE listar todas as contas conectadas do usuário com nome, tipo (corrente, poupança), instituição, saldo e moeda. Transações em moeda estrangeira DEVEM ser armazenadas com valor e moeda originais. Saldos consolidados no dashboard DEVEM somar apenas transações em BRL.
- **FR-020**: O sistema DEVE garantir isolamento de dados — cada usuário vê apenas suas próprias contas e transações.

**Sincronização de Transações**
- **FR-021**: O sistema DEVE sincronizar transações automaticamente em intervalo configurável (padrão: 6 horas).
- **FR-022**: O sistema DEVE permitir sincronização manual sob demanda.
- **FR-023**: O sistema DEVE deduplicar transações durante sincronização para evitar duplicatas.
- **FR-024**: O sistema DEVE processar transações em lotes para eficiência.
- **FR-025**: O sistema DEVE limitar a 1 sincronização simultânea por conexão.
- **FR-026**: O sistema DEVE registrar histórico de sincronizações (status, quantidade, erros).

**Importação Manual**
- **FR-027**: O sistema DEVE aceitar upload de arquivos OFX e CSV (máximo 10MB).
- **FR-028**: O sistema DEVE extrair transações do arquivo e exibir preview antes de confirmar importação.
- **FR-029**: O sistema DEVE deduplicar transações importadas contra existentes.
- **FR-030**: O sistema DEVE detectar automaticamente colunas de arquivos CSV (data, descrição, valor). Se a detecção automática falhar, DEVE exibir tela de mapeamento manual para o usuário associar colunas. DEVE tentar detectar encoding automaticamente (UTF-8, depois ISO-8859-1) com opção de seleção manual.

**Categorização**
- **FR-031**: O sistema DEVE fornecer categorias padrão pré-definidas (Moradia, Alimentação, Transporte, Saúde, Educação, Lazer, Utilidades, Renda, Transferência, Outros).
- **FR-032**: O sistema DEVE permitir criação de até 50 categorias customizadas por usuário (nome, cor, ícone).
- **FR-033**: O sistema DEVE impedir exclusão de categorias padrão (apenas desativação).
- **FR-034**: O sistema DEVE migrar transações para outra categoria ao excluir uma categoria customizada.
- **FR-035**: O sistema DEVE categorizar automaticamente transações usando inteligência artificial, com índice de confiança (0 a 1).
- **FR-036**: O sistema DEVE marcar transações com confiança abaixo de 0.7 como "precisa revisão".
- **FR-037**: O sistema DEVE consultar regras de categorização locais antes de consultar IA (prioridade: regra local > cache > IA).
- **FR-038**: O sistema DEVE permitir correção manual de categoria, com opção de aplicar a transações similares.
- **FR-039**: O sistema DEVE criar automaticamente regra de categorização local após 3 correções consistentes para o mesmo padrão.
- **FR-040**: O sistema DEVE classificar como "Outros" com baixa confiança quando a IA estiver indisponível.

**Dashboard e Visualização**
- **FR-041**: O sistema DEVE exibir saldo total consolidado e saldo individual por conta no dashboard.
- **FR-042**: O sistema DEVE exibir variação de saldo em relação ao período anterior (positiva/negativa).
- **FR-043**: O sistema DEVE exibir gráficos de distribuição de gastos por categoria (pizza/donut e barras).
- **FR-044**: O sistema DEVE permitir filtro por período pré-definido (hoje, semana, mês, trimestre) e customizado (data início/fim).
- **FR-045**: O sistema DEVE permitir filtro por conta bancária em todas as visualizações, com "Todas as contas" como padrão.
- **FR-046**: O sistema DEVE exibir lista de transações paginada (máximo 50 por página) com ordenação por data, valor ou categoria.
- **FR-047**: O sistema DEVE exibir detalhes completos de cada transação (banco, data/hora, valor, categoria, descrição, tipo de categorização, confiança).
- **FR-048**: O sistema DEVE permitir busca textual em descrições de transações com tempo de resposta ágil.
- **FR-049**: O sistema DEVE permitir busca por faixa de valor (mínimo/máximo), combinável com outros filtros.
- **FR-050**: O sistema DEVE permitir drill-down de categoria no gráfico para transações correspondentes.

**Tendências e Comparativos**
- **FR-051**: O sistema DEVE exibir gráfico de tendência de gastos mensais (últimos 6-12 meses) com média móvel.
- **FR-052**: O sistema DEVE exibir comparativo entre mês atual e anterior por categoria (aumento/diminuição).

**Preferências**
- **FR-053**: O sistema DEVE permitir configuração de moeda padrão (BRL default), fuso horário (America/Sao_Paulo default) e idioma (pt-BR default).
- **FR-054**: O sistema DEVE persistir preferências entre sessões e aplicá-las na formatação de valores e datas.

**Segurança e Auditoria**
- **FR-055**: O sistema DEVE registrar log de auditoria para todas as ações sensíveis (login, logout, login falhado, criação/exclusão de conexão, alteração de perfil, exportação/exclusão de dados).
- **FR-056**: O sistema DEVE mascarar dados sensíveis (tokens, senhas) em todos os logs.
- **FR-057**: O sistema DEVE sanitizar todas as entradas de usuário para prevenir injeção.

### Key Entities

- **Usuário (User)**: Pessoa física que utiliza o sistema. Possui email, senha (hash), nome, status de confirmação, e preferências. Centro de todo isolamento de dados.
- **Conexão (Connection)**: Vínculo OAuth entre um usuário e uma instituição financeira via Open Finance. Possui tokens encriptados, status de consentimento (ativo/expirado/revogado) e data de expiração.
- **Conta (Account)**: Conta bancária pertencente a um usuário, obtida via conexão Open Finance ou criada automaticamente para importações manuais. Possui nome, tipo (corrente/poupança), instituição, saldo e moeda.
- **Transação (Transaction)**: Movimentação financeira individual em uma conta. Possui data, descrição, valor, tipo (crédito/débito), categoria, índice de confiança da categorização, e flag de categorização manual.
- **Categoria (Category)**: Classificação de transações. Pode ser padrão do sistema (imutável) ou customizada pelo usuário. Possui nome, cor e ícone.
- **Regra de Categorização (CategorizationRule)**: Regra local criada pelo sistema baseada em correções do usuário. Mapeia padrão de descrição para uma categoria, por usuário.
- **Histórico de Sincronização (SyncHistory)**: Registro de cada execução de sincronização. Possui data/hora, status (sucesso/parcial/falha), quantidade de transações e mensagem de erro.
- **Token de Renovação (RefreshToken)**: Token de longa duração para renovação de sessão. Possui hash, data de expiração, família de tokens e status (ativo/revogado).
- **Preferência do Usuário (UserPreference)**: Configurações pessoais de exibição (moeda, fuso horário, idioma).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Usuários completam o cadastro (registro + confirmação de email + primeiro login) em menos de 3 minutos.
- **SC-002**: Usuários conectam sua primeira instituição financeira em menos de 5 minutos após o primeiro login.
- **SC-003**: 80% ou mais das transações são categorizadas corretamente pela IA sem intervenção manual.
- **SC-004**: A taxa de categorização automática correta melhora progressivamente, atingindo 90% após 30 dias de uso com correções do usuário.
- **SC-005**: Transações são sincronizadas automaticamente dentro de 30 segundos por lote de até 100 transações.
- **SC-006**: O dashboard carrega completamente (saldos, gráficos, lista de transações) em menos de 3 segundos.
- **SC-007**: Busca textual em transações retorna resultados em menos de 1 segundo.
- **SC-008**: O sistema suporta ao menos 100 usuários simultâneos sem degradação perceptível.
- **SC-009**: 95% das ações de categorização manual (correção + aplicar a similares) são completadas em 2 cliques ou menos.
- **SC-010**: Exportação de dados (LGPD) é gerada em menos de 30 segundos para contas com até 50.000 transações.
- **SC-011**: O sistema mantém 99.5% de disponibilidade mensal (excluindo manutenções programadas).
- **SC-012**: Cobertura de testes automatizados ≥ 90% no backend.

## Clarifications

### Session 2026-03-03

- Q: O sistema deve permitir múltiplas conexões com a mesma instituição financeira? → A: Permitir com aviso — exibir alerta informando conexão ativa existente, permitir prosseguir após confirmação.
- Q: Como tratar sincronizações pendentes ao excluir conta? → A: Cancelar jobs pendentes imediatamente, revogar consentimentos Open Finance, enviar email confirmando encerramento.
- Q: Como tratar transações em moeda estrangeira importadas via arquivo? → A: Armazenar valor e moeda originais, exibir com símbolo da moeda. Saldos consolidados somam apenas BRL.
- Q: Como tratar revogação externa de tokens pela instituição? → A: Detectar na próxima sync (HTTP 401/403), marcar conexão como "revogado", notificar usuário na interface para reconectar.
- Q: Como tratar CSVs com encoding diferente ou colunas não detectáveis? → A: Auto-detect encoding (UTF-8, depois ISO-8859-1) e colunas; se falhar, exibir tela de mapeamento manual com seleção de encoding.

## Assumptions

- O provedor de Open Finance (Pluggy) será usado como intermediário para conexões bancárias, com sandbox disponível para desenvolvimento.
- O serviço de email transacional (Resend) será usado para envio de emails de confirmação e recuperação, com tier gratuito suficiente para a fase inicial.
- A categorização por IA será feita via serviço externo (OpenAI GPT-4o-mini), com custo operacional aceito pelo projeto.
- O idioma principal da interface é Português Brasileiro (pt-BR).
- A moeda principal é Real Brasileiro (BRL), mas o sistema deve suportar exibição de outras moedas.
- O sistema será acessado primariamente via navegador web (desktop e mobile responsivo), sem aplicativo nativo na primeira versão.
- Notificações ao usuário serão feitas na interface (in-app), sem push notifications ou email para alertas operacionais na primeira versão.
- O projeto é operado por desenvolvedor solo com capacidade de ~15 pontos por fase (~2 semanas cada).
