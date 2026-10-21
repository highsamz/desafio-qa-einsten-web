# Matriz de riscos

Análise de risco que antecede a automação. É esta matriz que justifica a seleção de
cenários da [matriz de cenários](./matriz-de-cenarios.md) — a ordem é risco primeiro,
cenário depois, nunca o contrário.

Aplicação analisada: **PlayLab — Playwright Practice Arena**, em 20/10/2026.

---

## Jornadas identificadas

Levantadas a partir da navegação pela aplicação e da leitura do DOM, não do enunciado.

| # | Jornada | Componentes envolvidos | Criticidade |
|---|---|---|---|
| J1 | **Gerenciamento de usuários** — localizar, ordenar, paginar e agir sobre registros | Tabela de usuários, busca, filtro por função, seletor de registros por página, paginação, ações de linha | Alta |
| J2 | **Cadastro de usuário** — preencher, validar e submeter | Formulário de registro, 12 campos, validação própria, mensagens de erro e sucesso, reset | Alta |
| J3 | **Seleção em componentes assíncronos** — aguardar e escolher | Dropdown com carregamento atrasado, dropdown customizado, dropdown pesquisável, selects em cascata | Média |
| J4 | **Compra** — carrinho, cupom, checkout em etapas | Produtos, carrinho, cupons, modal de checkout em 3 passos | Média |
| J5 | **Envio de arquivos** — selecionar, validar e enviar | Upload simples e múltiplo, zona de arraste, lista de arquivos | Média |
| J6 | **Preenchimento assistido em múltiplas etapas** | Wizard de 4 passos com validação por etapa | Média |
| J7 | **Consumo de dados externos** | Seção Network & API, consumindo `jsonplaceholder.typicode.com` | Baixa |

**J1 e J2 concentram o risco** e recebem a maior parte da cobertura. J1 é a jornada
obrigatória do desafio e também, de forma independente, a de maior criticidade: é a tela
onde um erro se materializa como decisão errada sobre uma pessoa.

---

## Critérios de priorização

Sem fórmula numérica. Três perguntas, nesta ordem:

1. **Se isso quebrar, o usuário toma uma decisão errada ou perde trabalho?**
   Determina o impacto. Exibir o usuário errado numa tabela de administração é pior do que
   um carrossel que não gira.

2. **Qual a chance real de quebrar?**
   Determina a probabilidade. Componente com lógica própria (ordenação, paginação,
   validação, assincronia) quebra; texto estático não.

3. **Quanto custa manter o teste vivo?**
   Determina se automatizar vale a pena. Interação que depende de coordenada, de tempo ou
   de serviço de terceiro tem custo de manutenção que supera o risco que protege.

Prioridades resultantes: **P0** entra na suíte agora; **P1** entra quando a suíte crescer;
**P2** fica fora, com justificativa.

---

## Riscos funcionais

| ID | Risco | Jornada | Impacto | Probabilidade | Prioridade | Automatizado |
|---|---|---|---|---|---|---|
| RF-01 | A tabela carrega vazia, incompleta ou com estrutura diferente da esperada | J1 | Alto | Média | P0 | `USR-01` |
| RF-02 | A busca não encontra registro existente, ou encontra o registro errado | J1 | Alto | Média | P0 | `USR-03` |
| RF-03 | A busca sem correspondência deixa a tela ambígua, indistinguível de falha de carregamento | J1 | Médio | Alta | P0 | `USR-04` |
| RF-04 | A ordenação numérica é aplicada como texto, colocando `9` antes de `15` | J1 | Alto | Média | P0 | `USR-05` |
| RF-05 | A paginação repete ou perde registros na travessia das páginas | J1 | Alto | Média | P0 | `USR-06` |
| RF-06 | O cadastro válido é recusado, ou aceito sem confirmar | J2 | Alto | Média | P0 | `FRM-01` |
| RF-07 | E-mail inválido é aceito, ou o erro é exibido no campo errado | J2 | Alto | Alta | P0 | `FRM-03` |
| RF-08 | O componente assíncrono nunca carrega, ou carrega opções incompletas | J3 | Médio | Alta | P0 | `DYN-01` |
| RF-09 | Campo obrigatório vazio é aceito | J2 | Alto | Baixa | P1 | Não |
| RF-10 | O reset do formulário não limpa estado residual (erros, contador, sucesso) | J2 | Baixo | Média | P1 | Não |
| RF-11 | O limite de caracteres do campo não é respeitado | J2 | Baixo | Baixa | P1 | Não |
| RF-12 | A exclusão de usuário apaga o registro errado | J1 | **Crítico** | Baixa | P0 | `USR-08` — ver nota |
| RF-13 | O filtro por função e o seletor de registros por página não combinam com a busca | J1 | Médio | Média | P1 | Não |
| RF-14 | O upload aceita extensão não permitida ou arquivo acima do limite | J5 | Médio | Média | P2 | Não |
| RF-15 | O checkout conclui pedido com dados inválidos | J4 | Médio | Média | P2 | Não |

**Nota sobre RF-12.** É o maior impacto de toda a matriz e ficou fora da suíte por um
ciclo, sob uma justificativa técnica que **não resistiu à verificação**: afirmava-se que a
exclusão dispara um diálogo nativo do navegador e que, por mutar estado sem backend que
restaure, o cenário exigiria ciclo de vida próprio.

Ao implementar o cenário, nenhuma das duas premissas se confirmou:

- **não existe diálogo de confirmação**, nativo nem próprio. Um handler de `page.onDialog`
  registrado antes do clique nunca é acionado. A exclusão é imediata e irreversível, o que
  é por si um defeito de produto, registrado como [ACH-07](./achados.md#ach-07);
- **a mutação de estado já estava coberta** pelo isolamento da suíte: o `BrowserContext`
  novo por teste, somado ao fato de a aplicação não persistir nada, devolve os 15 registros
  na execução seguinte. Nenhum ciclo de vida especial foi necessário.

Fica registrado como está, e não reescrito como se a decisão tivesse sido outra: a
justificativa anterior era uma premissa não verificada sobre a aplicação, e o custo de
automatizar era de um método no componente e um teste. A lição é a mesma da seção de
hipóteses descartadas de [`achados.md`](./achados.md#hipóteses-descartadas) — custo
presumido também precisa de evidência.

---

## Riscos técnicos

| ID | Risco | Impacto | Prioridade | Como foi tratado |
|---|---|---|---|---|
| RT-01 | Teste instável por espera fixa em componente assíncrono | Alto | P0 | Nenhuma `waitForTimeout` na suíte. Toda sincronização é por condição observável. `DYN-01` exercita as quatro condições do dropdown |
| RT-02 | Asserção que passa sem comprovar comportamento | Alto | P0 | Caso concreto tratado: os `span` de erro do formulário estão sempre visíveis e apenas vazios, então a asserção é sobre **texto**, nunca visibilidade |
| RT-03 | Seletor quebrando a cada mudança de layout | Médio | P0 | Hierarquia `data-testid` → papel acessível → CSS estrutural. Nenhum XPath, nenhum índice fixo |
| RT-04 | Leitura de coluna errada se a aplicação reordenar as colunas | Médio | P0 | O índice da célula é **descoberto no cabeçalho em tempo de execução**, não fixado no código |
| RT-05 | Estado vazando entre testes | Alto | P0 | `BrowserContext` novo por teste — é ele, e não o `Browser`, que garante o isolamento. Nenhum teste depende de ordem |
| RT-06 | Download de navegador no meio da execução, poluindo tempo e saída | Médio | P0 | `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`. Instalação é passo próprio, com erro acionável se faltar |
| RT-07 | Falha sem evidência suficiente para diagnosticar | Alto | P0 | Trace, screenshot e console da página anexados ao relatório, apenas em falha |
| RT-08 | Dependência de serviço de terceiro derrubando a suíte por indisponibilidade alheia | Médio | P0 | Seção Network & API deliberadamente fora de escopo |
| RT-09 | Inicialização a frio do Firefox causando falha isolada | Baixo | P1 | Observado e registrado em [`achados.md`](./achados.md#instabilidade-observada-na-própria-suíte). Firefox roda apenas o subconjunto `crossbrowser` |
| RT-10 | Suíte lenta a ponto de deixar de ser executada | Médio | P1 | ~35 s em Chromium para os 11 cenários. Era ~43 s quando cada teste abria o seu próprio `Browser` — tratado: a engine passou a ser aberta uma vez por execução, no *store* raiz do JUnit, e o isolamento continua vindo do `BrowserContext` novo por teste |

---

## Riscos de experiência do usuário

| ID | Risco | Impacto | Prioridade | Situação |
|---|---|---|---|---|
| RX-01 | Tela vazia sem explicação após busca sem resultado | Médio | P0 | **Confirmado** — [ACH-01](./achados.md#ach-01) |
| RX-02 | Contador com texto incoerente (`Showing 1-0 of 0 entries`) | Baixo | P1 | **Confirmado** — [ACH-01](./achados.md#ach-01) |
| RX-03 | Ordenação sem indicação de qual coluna e qual direção está ativa | Médio | P0 | **Confirmado** — [ACH-04](./achados.md#ach-04) |
| RX-04 | Escopo da busca não informado — o campo diz "Search users..." mas filtra só Name e Email | Baixo | P1 | **Confirmado** — observação em [`achados.md`](./achados.md#observações-que-não-são-defeito) |
| RX-05 | Tabela larga inutilizável em tela estreita | Médio | P0 | **Não confirmado** — a tabela rola dentro de `.table-wrapper` (`overflow-x: auto`) e a página **não** ganha scroll horizontal em 390x844. Padrão responsivo correto, agora protegido por `RSP-04` |
| RX-06 | Controles principais inalcançáveis em tela reduzida | Médio | P0 | **Não confirmado** — busca, filtro, seletor de página, contador e paginação permanecem visíveis e operáveis em 390x844 |
| RX-07 | Ação destrutiva executada por um clique, sem confirmação e sem desfazer | Alto | P0 | **Confirmado** — [ACH-07](./achados.md#ach-07). Descoberto ao automatizar `USR-08` |

---

## Riscos de acessibilidade

A análise cruza duas fontes: o axe-core, que cobre a parte verificável por máquina, e a
inspeção manual, que cobre o resto. A separação importa — metade dos achados de
acessibilidade desta entrega **não** é detectável por ferramenta.

| ID | Risco | WCAG | Impacto | Detectado por | Situação |
|---|---|---|---|---|---|
| RA-01 | Controle operável sem nome acessível | 4.1.2 (A) | Alto | axe | **Confirmado** — [ACH-02](./achados.md#ach-02) |
| RA-02 | Funcionalidade inalcançável por teclado | 2.1.1 (A) | Alto | Manual | **Confirmado** — [ACH-04](./achados.md#ach-04) |
| RA-03 | Mudança de estado não anunciada a tecnologia assistiva | 4.1.3 (AA) | Alto | Manual | **Confirmado** — [ACH-05](./achados.md#ach-05) |
| RA-04 | Erro de validação sem vínculo programático com o campo | 3.3.1 (A) | Alto | Manual | **Confirmado** — [ACH-05](./achados.md#ach-05) |
| RA-05 | Contraste abaixo do mínimo | 1.4.3 (AA) | Médio | axe | **Confirmado** — [ACH-03](./achados.md#ach-03) |
| RA-06 | Campo rotulado apenas por *placeholder* | 3.3.2 (AA) | Baixo | Manual | **Confirmado** — [ACH-06](./achados.md#ach-06). O axe **aprova** |
| RA-07 | Tabela sem semântica de cabeçalho de coluna | 1.3.1 (A) | Alto | — | **Refutado** com evidência — [hipóteses descartadas](./achados.md#hipóteses-descartadas) |
| RA-08 | Modal criando armadilha de teclado | 2.1.2 (A) | Alto | Manual | Não verificado — fora de escopo |
| RA-09 | Ordem de foco incoerente com a ordem visual | 2.4.3 (A) | Médio | Manual | Não verificado — fora de escopo |

---

## Riscos não apresentados no enunciado

Itens que a análise levantou e que não constam das listas de cenários de referência do
desafio. Estão aqui porque identificá-los é parte do trabalho.

| ID | Risco | Por que importa |
|---|---|---|
| RN-01 | **O indicador de ordenação é estático** | O enunciado fala em "cabeçalhos com indicador", sugerindo que o indicador comunica estado. Ele não comunica: é o mesmo `↕` em coluna ordenada, não ordenada, crescente e decrescente. Um teste que asseverasse o indicador passaria sempre — e não provaria nada |
| RN-02 | **Os `span` de erro estão sempre visíveis** | Qualquer suíte escrita contra este formulário com asserção de visibilidade nasce com falso positivo. É uma armadilha silenciosa no caminho mais óbvio de implementação |
| RN-03 | **O `data-testid` da linha segue o id do registro, não a posição** | Na página 2 as linhas são `table-row-6` a `table-row-10`. Isso é bom — permite endereçar por identidade — mas quebra a intuição de que `table-row-1` é a primeira linha visível |
| RN-04 | **A aplicação comunica estado só visualmente** | Contador de resultados, mensagem de sucesso e estado de ordenação compartilham a mesma causa raiz. Não são três defeitos isolados: é um padrão arquitetural ausente. Corrigir caso a caso trata sintoma |
| RN-05 | **`Playwright.create()` baixa navegador sob demanda** | Risco de pipeline, não de produto. Sem desligar isso, a primeira execução de um agente limpo baixa mais de 100 MB **dentro** do teste, inflando a duração relatada e corrompendo o canal de saída do Surefire |
| RN-06 | **A exclusão não pede confirmação** | O enunciado trata a ação da linha como passo funcional da jornada ("executar uma ação disponível"). A dimensão que ele não menciona é de segurança de uso: um clique apaga o registro, sem confirmação, sem desfazer e sem anúncio a tecnologia assistiva. Num painel administrativo real, é o caminho mais curto para perda de dado — e é o mesmo padrão de RN-04: estado que só se comunica visualmente |

---

## Frequência de execução recomendada

| Conjunto | Comando | Quando | Por quê |
|---|---|---|---|
| Suíte completa, Chromium | `mvn test` | A cada *push* e *pull request* | ~35 s. Barato o suficiente para ser o portão padrão |
| Subconjunto `crossbrowser`, Firefox | `mvn test -Dgroups=crossbrowser -Dbrowser=firefox` | A cada *pull request* | Compatibilidade de engine muda pouco: diferença aparece em renderização e eventos, não em lógica. Dois cenários-âncora bastam. ~21 s, dos quais 12 s são a abertura do Firefox |
| `RSP-04` e jornada crítica em 390x844 | `mvn test -Dgroups=mobile -Dviewport=mobile` | A cada *pull request* | Regressão responsiva vem de CSS, que muda com frequência |
| `A11Y-06` | Junto da suíte completa | A cada *push* | O custo é o da execução; não há razão para separar |
| Verificação manual de acessibilidade | — | A cada mudança de componente de formulário ou tabela | Teclado, ordem de foco e anúncio de estado não são automatizáveis por ferramenta. É onde moram [ACH-04](./achados.md#ach-04), [ACH-05](./achados.md#ach-05) e [ACH-06](./achados.md#ach-06) |
