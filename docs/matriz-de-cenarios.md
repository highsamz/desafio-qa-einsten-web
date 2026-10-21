# Matriz de cenários

Seleção de cenários da suíte, derivada da [matriz de riscos](./matriz-de-riscos.md). Cada
linha diz o que protege e por que entrou ou ficou fora.

Os IDs seguem a numeração de referência do enunciado, para facilitar a conferência. Onde um
cenário desta suíte cobre mais de um ID de referência, a coluna **Ref.** indica quais.

Legenda de tipo: **F** funcional · **N** negativo · **A** acessibilidade · **R**
responsividade · **S** sincronização.

---

## Cenários automatizados

**11 cenários**, em 5 classes de teste — três acima da faixa de 5 a 8 sugerida, desvio
deliberado e permitido pelo §3 ("é permitido implementar cenários adicionais"). A faixa foi
excedida para fechar os seis passos da jornada obrigatória do §4 e para que a
responsividade deixasse de ser apenas declarada, não por acúmulo de cenários redundantes:
nenhum dos 11 é variação de outro.

| ID | Ref. | Jornada | Descrição | Tipo | Risco protegido | Prior. | Autom. | Status |
|---|---|---|---|---|---|---|---|---|
| **USR-01** | USR-01, USR-02 | Gerenciamento de usuários | A tabela carrega com as 6 colunas nomeadas, 5 registros na primeira página, contador `Showing 1-5 of 15 entries`, IDs 1 a 5 e os dados do primeiro usuário. Assevera também a assimetria do indicador de ordenação: presente nas colunas ordenáveis, ausente em Role | F | [RF-01](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **USR-03** | USR-03 | Gerenciamento de usuários | A pesquisa por `alice` (minúsculas) localiza "Alice Johnson" (capitalizado): uma linha, contador `1-1 of 1` e o registro correto. Prova filtragem e normalização de maiúsculas numa só asserção | F | [RF-02](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **USR-04** | USR-04 | Gerenciamento de usuários | A pesquisa por termo sem correspondência esvazia a tabela: zero linhas **e** contador `Showing 1-0 of 0 entries`, com paginação desabilitada nos dois extremos | N | [RF-03](./matriz-de-riscos.md#riscos-funcionais), [RX-01](./matriz-de-riscos.md#riscos-de-experiência-do-usuário) | P0 | Sim | ✅ Passando |
| **USR-05** | USR-05 | Gerenciamento de usuários | A ordenação por ID alterna crescente (`1,2,3,4,5`) e decrescente (`15,14,13,12,11`), provando comparação **numérica** e não lexicográfica. O universo de registros não muda | F | [RF-04](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **USR-06** | USR-06 | Gerenciamento de usuários | A travessia das 3 páginas apresenta as faixas `1-5`, `6-10` e `11-15` sem repetir nem perder registro, com "anterior" desabilitado na primeira e "próxima" desabilitado na última | F | [RF-05](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **USR-08** | USR-08, USR-07 | Gerenciamento de usuários | A exclusão de um registro remove a linha do usuário 3, **preserva os vizinhos 2 e 4**, recompõe a primeira página como `1,2,4,5,6` sem deixar lacuna, e o contador vai de 15 para 14. Fecha o passo final da jornada obrigatória: executar uma ação e validar o resultado | F | [RF-12](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **FRM-01** | FRM-01 | Cadastro de usuário | O cadastro com massa válida gerada pela fábrica confirma o sucesso **e** mantém os quatro campos de erro vazios | F | [RF-06](./matriz-de-riscos.md#riscos-funcionais) | P0 | Sim | ✅ Passando |
| **FRM-03** | FRM-03, FRM-06 | Cadastro de usuário | Com todos os campos válidos exceto o e-mail, o envio é bloqueado e o texto `Valid email is required` aparece **somente** no campo de e-mail. A asserção é sobre texto, nunca visibilidade | N | [RF-07](./matriz-de-riscos.md#riscos-funcionais), [RT-02](./matriz-de-riscos.md#riscos-técnicos) | P0 | Sim | ✅ Passando |
| **DYN-01** | DYN-01, DYN-02, DYN-07 | Componentes assíncronos | Ciclo completo do dropdown de carregamento atrasado (~2 s): estado inicial desabilitado, estado de carregamento sinalizado, 5 opções habilitadas com placeholder trocado, e a seleção refletida no resultado | S | [RF-08](./matriz-de-riscos.md#riscos-funcionais), [RT-01](./matriz-de-riscos.md#riscos-técnicos) | P0 | Sim | ✅ Passando |
| **A11Y-06** | A11Y-06 | Cadastro de usuário | Análise automatizada com axe-core sobre o card do formulário, tags WCAG 2.0/2.1 A e AA, falhando diante de qualquer violação fora da *baseline* declarada | A | [RA-01](./matriz-de-riscos.md#riscos-de-acessibilidade), [RA-05](./matriz-de-riscos.md#riscos-de-acessibilidade) | P0 | Sim | ✅ Passando |
| **RSP-04** | RSP-04 | Gerenciamento de usuários | A largura total do documento não passa da largura da janela — a **página** não rola para o lado — e o excesso da tabela fica contido no `.table-wrapper`, que declara `overflow-x` rolável. As duas asserções juntas separam o padrão responsivo correto dos dois jeitos errados: página deslocada, ou tabela que deixou de renderizar | R | [RX-05](./matriz-de-riscos.md#riscos-de-experiência-do-usuário) | P0 | Sim | ✅ Passando |

### Execuções adicionais sobre os mesmos cenários

Não são cenários novos: são os cenários acima executados em outra configuração. O viewport
e o navegador vêm de configuração, nunca do código do teste.

| ID | Ref. | Descrição | Tipo | Como executa | Status |
|---|---|---|---|---|---|
| **RSP-02** | RSP-02, RSP-05 | A jornada crítica (`USR-01` e `USR-03`, marcados `@Tag("mobile")`) re-executada em **390x844** — o `RSP-04` acima é cenário, este é re-execução. Como o Playwright exige elemento visível e habilitado para preencher e clicar, interagir com a busca nessa largura prova que o controle permanece alcançável | R | `mvn test -Dgroups=mobile -Dviewport=mobile`, e *job* próprio na pipeline | ✅ Passando |
| **CMP-01** | §5.7 | Os cenários-âncora `USR-01` e `FRM-01`, marcados `@Tag("crossbrowser")`, executados em **Firefox** | F | `mvn test -Dgroups=crossbrowser -Dbrowser=firefox`, e *job* próprio na pipeline | ✅ Passando |

**Por que o mobile não é um cenário à parte.** O enunciado pede "pelo menos uma execução em
resolução mobile" (§3.6 e §9.1) — uma execução, não um cenário adicional. Transformá-lo em
teste próprio duplicaria a jornada em código para variar um número que já vem de
configuração. O custo assumido é que a
execução mobile não aparece como teste na execução local padrão: ela é um alvo explícito,
documentado no README e visível como *job* separado na pipeline.

---

## Cenários avaliados e não automatizados

Justificar a não automação é parte da entrega. A ordem é de maior para menor valor
represado.

### Alto valor, represado por decisão consciente

| ID | Ref. | Descrição | Risco | Prior. | Justificativa |
|---|---|---|---|---|---|
| FRM-02 | FRM-02 | Submeter o formulário sem preencher os obrigatórios | [RF-09](./matriz-de-riscos.md#riscos-funcionais) | P1 | Cobertura parcial já existe: `FRM-01` assevera que os quatro campos de erro ficam vazios no caminho válido, o que exercita os mesmos elementos. O cenário dedicado agregaria as quatro mensagens simultâneas — valor real, mas incremental sobre `FRM-03` |
| FRM-05 | FRM-05 | Validar o reset do formulário | [RF-10](./matriz-de-riscos.md#riscos-funcionais) | P1 | Verificado manualmente e funciona: limpa campos, erros, contador de caracteres e a mensagem de sucesso. Baixo impacto se quebrar. O componente já expõe `reset()`, então o custo de automatizar depois é de minutos |
| USR-07 | USR-07 | Validar a situação apresentada para um usuário | — | P1 | A coluna Status é dado estático renderizado em *badge*, sem lógica própria. `USR-01` assevera o conteúdo da primeira linha e `USR-08` assevera a linha inteira dos vizinhos preservados. Não há comportamento a proteger além do que já está coberto |
| A11Y-03, A11Y-04 | A11Y-03, A11Y-04 | Navegação por teclado e ordem de foco | [RA-02](./matriz-de-riscos.md#riscos-de-acessibilidade), [RA-09](./matriz-de-riscos.md#riscos-de-acessibilidade) | P1 | Automatizável, e o achado mais grave da aplicação está aqui ([ACH-04](./achados.md#ach-04): a ordenação não é alcançável por teclado). Não entrou porque o teste nasceria **vermelho** contra um defeito conhecido. Entra junto com a correção, como teste de regressão |
| FRM-04 | FRM-04 | Validar limites de caracteres | [RF-11](./matriz-de-riscos.md#riscos-funcionais) | P1 | O campo Nome tem `maxlength="50"` (corte pelo navegador) e o Bio tem contador `0/200` em JS. São dois mecanismos diferentes, portanto dois cenários. Represado pelo limite de cenários |
| FRM-07 | FRM-07 | Associação entre campos e labels | [RA-06](./matriz-de-riscos.md#riscos-de-acessibilidade) | P1 | Já coberto indiretamente: a regra `label` do axe roda em `A11Y-06` sobre o escopo do formulário e passa. O caso que a ferramenta **não** pega está documentado como [ACH-06](./achados.md#ach-06) |
| RSP-03 | RSP-03 | Ausência de sobreposição relevante em tela estreita | [RX-05](./matriz-de-riscos.md#riscos-de-experiência-do-usuário) | P1 | Verificado manualmente em 390x844 e **correto**. Automatizar exige comparar as caixas de vários elementos entre si — bem mais caro que a medição de `RSP-04`, que é uma leitura só. Vale, como cenário próprio e bem feito |

### Valor baixo frente ao custo de manutenção

| ID | Descrição | Justificativa |
|---|---|---|
| §5.4 | Drag and drop, hover, menu de contexto, modais, iframes, Shadow DOM | Interações que dependem de posição, de sequência de eventos sintéticos ou de travessia de contexto. Alto custo de manutenção para risco baixo: são componentes de demonstração, sem regra de negócio |
| J4 | Shopping — carrinho, cupom, checkout | Jornada ampla, que exigiria massa própria e vários cenários para ter valor. Fora do escopo obrigatório do desafio |
| — | Carrossel, media player, galeria | Componentes de apresentação. Asseverar estado final de animação é caro e instável |
| DYN-03 | Tabela com colunas que mudam de posição | A técnica que este cenário exigiria **já está implementada** em `UserTableComponent.columnCells`, que descobre o índice da coluna no cabeçalho em tempo de execução ([RT-04](./matriz-de-riscos.md#riscos-técnicos)). O cenário dedicado validaria a técnica, não a aplicação |
| DYN-06 | Comportamento diante de múltiplos cliques | O botão do dropdown fica desabilitado após o primeiro clique, o que já impede o cenário. Sem contrato a asseverar |
| UPL-01 a UPL-05 | Upload de arquivo | Opcional no enunciado. O `setInputFiles` do Playwright torna o cenário simples, mas exigiria arquivos de teste versionados. Represado pelo limite de cenários |
| §5.6 | Wizard de múltiplas etapas, date picker | Valor real, represado pelo limite de cenários. O date picker é bom candidato, por ser componente customizado com lógica própria |

### Deliberadamente fora de escopo

| ID | Descrição | Justificativa |
|---|---|---|
| §15 | Flaky & Retry Elements | A seção é **deliberadamente não determinística**. Não existe contrato a asseverar: um teste ali mediria sorte. Se fosse necessário cobrir, o caminho seria asseverar a política de retry, não o elemento |
| §14 | Network & API | Depende de `jsonplaceholder.typicode.com`, serviço de terceiro. Um teste de interface ali falharia por indisponibilidade alheia ao produto, gerando alarme falso e erodindo a confiança na suíte. O caminho correto é teste de contrato, ou interceptação de rota pelo Playwright para servir resposta controlada |
| §18 | Seção "Accessibility (a11y)" | É *sandbox* construída para passar. Rodar o axe ali produziria verde que não protege nada. A análise roda no formulário real, onde um achado tem consequência |
| — | Tabela de usuários sob o gate do axe | A tabela tem violações **críticas** conhecidas ([ACH-02](./achados.md#ach-02)). Colocar o gate ali deixaria a suíte vermelha desde o primeiro dia sem proteger regressão. Entregue como achado, que é o canal correto |
| — | Teste visual | Diferencial citado no enunciado. Sem *baseline* estável e sem política de aprovação de diferença, teste visual produz ruído. Entraria depois da suíte funcional estar madura, e em escopo seletivo |

---

## Rastreabilidade

| Cenário | Classe de teste | Método |
|---|---|---|
| USR-01 | `tests/users/UserTableTest.java` | `tabelaCarregaComEstruturaEDadosEsperados` |
| USR-03 | `tests/users/UserTableTest.java` | `pesquisaLocalizaUsuarioExistente` |
| USR-04 | `tests/users/UserTableTest.java` | `pesquisaSemCorrespondenciaEsvaziaTabela` |
| USR-05 | `tests/users/UserTableTest.java` | `ordenacaoPorIdEhNumericaEAlterna` |
| USR-06 | `tests/users/UserTableTest.java` | `navegacaoEntrePaginasPercorreTodosOsRegistros` |
| USR-08 | `tests/users/UserTableTest.java` | `exclusaoRemoveUsuarioCorretoEAtualizaContador` |
| RSP-04 | `tests/responsive/ResponsiveLayoutTest.java` | `tabelaLargaNaoProduzScrollHorizontalNaPagina` |
| FRM-01 | `tests/forms/RegistrationFormTest.java` | `cadastroComDadosValidosConfirmaSucesso` |
| FRM-03 | `tests/forms/RegistrationFormTest.java` | `emailInvalidoBloqueiaEnvioEAcusaErroNoCampoCorreto` |
| DYN-01 | `tests/dynamic/DelayedDropdownTest.java` | `dropdownAssincronoCarregaEPermiteSelecao` |
| A11Y-06 | `tests/accessibility/AccessibilityTest.java` | `analiseAutomatizadaDoFormularioNaoAcusaViolacaoNova` |

O ID do cenário é o prefixo do `@DisplayName` de cada teste, então aparece no relatório
Allure e na saída do Surefire sem precisar de consulta a esta tabela.
