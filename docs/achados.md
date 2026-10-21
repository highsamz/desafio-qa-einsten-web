# Achados da análise exploratória

Defeitos e observações encontrados na análise da aplicação **PlayLab — Playwright
Practice Arena** (`https://playwrightlab.github.io/`), antes e durante a construção da
suíte automatizada.

Todos os itens abaixo foram **observados diretamente na aplicação em 20/10/2026**, com o
método de verificação registrado em cada um. Itens que não resistiram à verificação estão
na seção [Hipóteses descartadas](#hipóteses-descartadas) — achado que não se confirma não
vira defeito.

| ID | Achado | Severidade | Como foi detectado |
|---|---|---|---|
| [ACH-01](#ach-01) | Busca sem resultado não comunica estado vazio | Média | Manual |
| [ACH-02](#ach-02) | Controles da tabela sem nome acessível | Alta | axe-core |
| [ACH-03](#ach-03) | Contraste insuficiente em textos e controles | Média | axe-core |
| [ACH-04](#ach-04) | Ordenação da tabela inacessível por teclado e sem estado programático | Alta | Manual |
| [ACH-05](#ach-05) | Mudanças de estado não são anunciadas a tecnologia assistiva | Alta | Manual |
| [ACH-06](#ach-06) | Campo de busca rotulado apenas por *placeholder* | Baixa | Manual |

---

## ACH-01

**Busca sem resultado não comunica estado vazio**

Severidade: **média** · WCAG 4.1.3 (AA) · Jornada: gerenciamento de usuários

Ao pesquisar um termo sem correspondência, o corpo da tabela fica **completamente vazio,
sem mensagem alguma**. Não há "nenhum resultado encontrado", ilustração, nem orientação
para refazer a busca. O único sinal observável é o contador do rodapé mudar para
`Showing 1-0 of 0 entries`.

Para o usuário o resultado é ambíguo: uma tabela em branco é indistinguível de uma falha
de carregamento. Quem usa leitor de tela fica em situação pior, porque o contador também
não tem `aria-live` (ver [ACH-05](#ach-05)) — a busca simplesmente não produz retorno
audível nenhum.

**Como reproduzir**

1. Ir a *Data Tables › User Management*
2. Digitar `zzzzz` no campo "Search users..."
3. Observar o corpo da tabela e o contador

**Esperado:** mensagem explícita de estado vazio, anunciada por região `aria-live`.
**Obtido:** corpo vazio, sem mensagem; contador em `Showing 1-0 of 0 entries`.

**Observação adicional:** o texto `Showing 1-0 of 0 entries` é incoerente em si. Com zero
registros, a faixa `1-0` não descreve nada. O esperado seria `Showing 0 of 0 entries` ou
equivalente.

**Relação com a suíte:** coberto por `USR-04`, que assevera as duas condições
observáveis — zero linhas **e** contador zerado. A **ausência da mensagem não é
asseverada**: asseverar ausência transformaria a correção do defeito em quebra de teste.

---

## ACH-02

**Controles da tabela sem nome acessível**

Severidade: **alta** · WCAG 4.1.2 (A) e 1.3.1 (A) · Impacto axe: `critical`

A análise automatizada com axe-core 4.14.0 sobre o card da tabela
(`[data-testid='table-card']`) acusa três regras distintas, todas de impacto `critical`:

| Regra axe | Elementos | O que significa |
|---|---|---|
| `button-name` | `#pagePrev`, `#pageNext` | As setas de paginação são botões com apenas um ícone SVG dentro, sem texto, sem `aria-label` e sem `title`. Um leitor de tela anuncia "botão", sem dizer o quê. |
| `label` | `#selectAll` e as 5 caixas de seleção de linha | Nenhuma tem rótulo associado. Não há como saber o que se está marcando. |
| `select-name` | `#rowsPerPage`, `#tableFilter` | Os dois selects dos controles da tabela não têm nome acessível. |

Vale contrastar com os botões de ação da linha: `edit-N` e `delete-N` também são
icon-only, mas têm `title="Edit"` / `title="Delete"`, e por isso **não** são acusados. A
correção dos demais é do mesmo tamanho — falta apenas aplicá-la.

**Relação com a suíte:** este escopo **não** está sob o gate automatizado de
acessibilidade, e a decisão é deliberada. Colocar o axe sobre a tabela deixaria a suíte
vermelha desde o primeiro dia, sem proteger regressão nenhuma. O gate roda no formulário
(`A11Y-06`); estes defeitos são entregues como achado, que é o canal correto. A
justificativa completa está em [`estrategia-de-testes.md`](./estrategia-de-testes.md).

---

## ACH-03

**Contraste insuficiente em textos e controles**

Severidade: **média** · WCAG 1.4.3 (AA) · Impacto axe: `serious`

Elementos abaixo da razão mínima de contraste exigida:

- no card do formulário: *badge* do cabeçalho, contador de caracteres do campo Bio, botão
  de envio;
- no card da tabela: botão "Add Row" e o contador `Showing 1-5 of 15 entries`.

O axe também devolve `color-contrast` como **inconclusivo** para alguns nós, nos casos em
que não consegue determinar a cor de fundo efetiva. Esses exigem conferência manual e
estão fora do que a ferramenta decide sozinha.

**Relação com a suíte:** é a única violação do escopo do formulário e está declarada como
*baseline* conhecida em `AccessibilityTest.VIOLACOES_CONHECIDAS`. Enquanto estiver nessa
lista não derruba o teste; qualquer regra **nova** derruba.

---

## ACH-04

**Ordenação da tabela inacessível por teclado e sem estado programático**

Severidade: **alta** · WCAG 2.1.1 (A) e 4.1.2 (A) · **Não detectável pelo axe**

As colunas ordenáveis são `<th class="sortable">` com um `click` no elemento. Faltam três
coisas, e cada uma tem consequência própria:

| Falta | Consequência |
|---|---|
| `tabindex` e `role="button"` (ou um `<button>` interno) | A ordenação é **inalcançável por teclado**. Quem não usa mouse não consegue ordenar a tabela. Violação de nível A. |
| `aria-sort` | Não há como saber, programaticamente, qual coluna está ordenada nem em que direção. |
| Indicador visual com estado | O `↕` do cabeçalho é **estático**: não muda entre crescente e decrescente, e é idêntico em coluna ordenada e não ordenada. |

**Evidência do indicador estático:** clicando duas vezes em "ID", a ordem dos dados muda
de `1,2,3,4,5` para `15,14,13,12,11`, e o texto do cabeçalho permanece `ID ↕` nos dois
estados. Ou seja: a ordem dos dados é o **único** sinal de que a ordenação aconteceu.
Nem o usuário visual nem o leitor de tela recebem confirmação.

**Por que o axe não pega:** o axe verifica se um `aria-sort` presente tem valor válido.
Não existe regra que exija `aria-sort` em cabeçalho de tabela ordenável, porque a
ferramenta não tem como inferir que aquele `th` é clicável e ordena. Achado de análise
manual.

**Relação com a suíte:** `USR-05` assevera o comportamento **funcional** da ordenação
(que ela é numérica e alterna), que é o que funciona. Não assevera o indicador nem o
`aria-sort`, que é o que falta.

---

## ACH-05

**Mudanças de estado não são anunciadas a tecnologia assistiva**

Severidade: **alta** · WCAG 4.1.3 (AA) e 3.3.1 (A) · **Não detectável pelo axe**

A aplicação comunica mudança de estado **apenas visualmente**. Três ocorrências do mesmo
problema:

1. **Contador de resultados da tabela** (`[data-testid=table-info]`): sem `aria-live` e
   sem `role="status"`. Pesquisar e filtrar não produz anúncio nenhum — é o que agrava
   [ACH-01](#ach-01).

2. **Mensagem de sucesso do cadastro** (`[data-testid=form-success]`): sem `aria-live` e
   sem `role="status"`. A confirmação "Registration successful! Welcome aboard." aparece
   na tela e não é anunciada.

3. **Erros de validação do formulário**: os campos em erro **não** recebem
   `aria-invalid="true"`, e as mensagens de erro **não** estão ligadas aos campos por
   `aria-describedby`. Ao submeter o formulário vazio, as quatro mensagens aparecem no
   DOM, o foco **não** é movido para o primeiro campo inválido, e nenhum campo se declara
   inválido. Para quem usa leitor de tela, o envio falha em silêncio.

**Por que o axe não pega:** as regras do axe inspecionam um retrato estático do DOM. Nada
ali está marcado errado — o que falta é marcação que deveria existir para uma mudança que
só acontece em tempo de execução. Detectar isso exige interagir com a aplicação e
observar, o que é análise manual.

**Relação com a suíte:** `FRM-01` e `FRM-03` asseveram o comportamento funcional da
validação (a mensagem certa, no campo certo, e o bloqueio do envio). A dimensão de
acessibilidade dessas mensagens é este achado.

---

## ACH-06

**Campo de busca rotulado apenas por *placeholder***

Severidade: **baixa** · WCAG 2.5.3 (A) e 3.3.2 (AA) · **Aprovado pelo axe**

O campo de pesquisa da tabela (`#tableSearch`) não tem `<label for>` nem `aria-label`.
Seu nome acessível vem do `placeholder`, `"Search users..."`.

Isso **passa** na regra `label` do axe, porque o `placeholder` é aceito como último
recurso no cálculo do nome acessível. O nome existe; a qualidade dele é que é ruim:

- o `placeholder` desaparece assim que o usuário digita, e com ele o rótulo;
- não sobra referência visual persistente do que o campo filtra;
- rótulo e dica de preenchimento ficam sendo o mesmo texto.

Comparativo interno: todos os campos do formulário de cadastro têm `<label for>` correto,
e o `<select>` do dropdown assíncrono também (`<label for="delayedSelect">`). Ou seja, o
padrão correto está estabelecido na aplicação e os controles da tabela são a exceção —
o mesmo recorte do [ACH-02](#ach-02).

Este item é o melhor exemplo da limitação da análise automatizada nesta entrega: a
ferramenta diz verde e o problema existe.

---

## Hipóteses descartadas

Registradas porque a investigação que as descartou vale tanto quanto a que confirma.

### Ausência de semântica de cabeçalho de coluna — **refutada**

Uma análise anterior levantou a suspeita de que as colunas da tabela não expusessem o
papel `columnheader`, o que seria um defeito de estrutura relevante. A observação original
vinha da árvore de acessibilidade de uma ferramenta de automação, que **achata a marcação
de tabela** e reportava as células como `generic`.

Verificado contra o motor de papéis ARIA do próprio Playwright, que implementa o
mapeamento do HTML-AAM:

```
columnheader na tabela = 7
th na tabela           = 7
role=table             = 1
role=row               = 6   (1 de cabeçalho + 5 de dados)
role=cell              = 35
getByRole(columnheader, name="ID") = 1
```

Os sete `<th>` são expostos como `columnheader`, apesar de não declararem `scope`: o
navegador infere o escopo de coluna para `<th>` na linha de um `<thead>`. **A semântica
está correta e não há defeito aqui.**

A lição prática ficou no projeto: árvore de acessibilidade simplificada não serve como
evidência de ausência de semântica. Quem decide é o motor de papéis.

---

## Observações que não são defeito

Comportamentos verificados que mudam a forma de testar, mas que não configuram defeito.

**A pesquisa cobre apenas Name e Email.** Buscar `admin`, `editor` ou `active` devolve
zero resultados, embora esses valores existam nas colunas Role e Status. Não é defeito: o
filtro por função tem controle próprio (`[data-testid=table-filter]`). O que é discutível
é o `placeholder` genérico "Search users...", que não informa o recorte da busca. Registrado
como ponto de UX, não como bug.

**A pesquisa é *case-insensitive*.** `Alice`, `alice`, `ALICE` e `aLiCe` devolvem o mesmo
registro. Confirmado com as quatro variações. `USR-03` explora isso de propósito: busca em
minúsculas contra um nome capitalizado.

**O formulário não limpa os campos no sucesso.** Após o cadastro bem-sucedido, nome,
e-mail, senha e o aceite dos termos permanecem preenchidos, e a mensagem de sucesso
permanece visível por tempo indeterminado (acima de 5 s sem sumir). A limpeza é feita pelo
botão "Reset", que zera campos, erros, contador de caracteres e a mensagem de sucesso.
Comportamento aceitável, mas contraria a expectativa comum de formulário de cadastro.

**O botão do dropdown assíncrono é de uso único.** Depois do carregamento ele fica
desabilitado com o texto "Options Loaded". Não há como recarregar sem recarregar a página.
Irrelevante para a suíte, porque cada teste roda em contexto novo.

**O *hamburger* do menu mobile não tem `data-testid`.** É o único controle relevante da
aplicação sem identificador de teste (`button.hamburger`, com `aria-label="Toggle menu"`).
Não é defeito de produto — é o ponto em que a estratégia de seletores precisa cair para o
nível seguinte, papel acessível mais nome. Detalhado no README.

**O widget da seção "Responsive Testing" reportou valores defasados.** Em viewport de
390x844, `[data-testid=viewport-width]` continuou exibindo `1024px` e o rótulo de
breakpoint continuou "Small Desktop". **Não está sendo tratado como defeito**: a medição
foi feita com emulação de viewport por ferramenta, que pode não disparar o evento de
`resize` que o widget escuta. Distinguir defeito de artefato de emulação exigiria
verificação dedicada em dispositivo ou janela real, que não foi feita. Fica registrado
como pendência de investigação, não como achado.

---

## Instabilidade observada na própria suíte

Não é achado da aplicação, e sim do ambiente de execução. Registrado por honestidade de
diagnóstico.

Na primeira execução em Firefox, logo após o download do binário, o teste `FRM-01` falhou
com `TargetClosedError: Target page, context or browser has been closed`. O mesmo teste
passou nas três execuções seguintes, isoladamente e em grupo, sem nenhuma alteração de
código. Atribuído à inicialização a frio do binário recém-baixado.

Mitigação adotada: a suíte **não baixa navegador durante o teste** (ver
`PlaywrightExtension`), e a instalação é passo próprio, no local e na pipeline. Isso tira
a variável de rede do caminho do teste. O Firefox também é consistentemente mais lento
para iniciar nesta máquina (10 a 18 s por teste, contra 2 a 5 s no Chromium), o que é parte
da justificativa para não rodar a suíte inteira nele.
