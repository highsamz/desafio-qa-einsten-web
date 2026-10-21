# Estratégia de testes — PlayLab Front Web

Documento que define o que esta suíte protege, o que deliberadamente não protege, e o
raciocínio por trás das duas decisões.

Aplicação-alvo: **PlayLab — Playwright Practice Arena** (`https://playwrightlab.github.io/`)
— página única, estática, servida pelo GitHub Pages, sem backend.

Documentos relacionados: [matriz de riscos](./matriz-de-riscos.md) ·
[matriz de cenários](./matriz-de-cenarios.md) · [achados](./achados.md)

---

## Objetivo

Estabelecer a base de automação Front Web de uma plataforma administrativa, cobrindo as
jornadas de maior risco com testes estáveis, legíveis e sustentáveis, e produzindo
evidência suficiente para diagnosticar falha sem reexecutar o teste.

O objetivo **não** é cobertura máxima. Com 21 seções e centenas de componentes na
aplicação, cobertura ampla produziria uma suíte lenta, frágil e que ninguém manteria. A
meta é um conjunto pequeno em que cada cenário prove um comportamento distinto e nenhum
passe por acidente.

---

## Escopo

**Jornada obrigatória — gerenciamento de usuários (coberta por inteiro):** carregamento e
estrutura da tabela, localização de registro, comportamento sem correspondência,
ordenação por coluna, navegação entre páginas e execução de uma ação disponível com
validação do seu resultado (exclusão).

**Formulário:** cadastro com dados válidos e validação de e-mail inválido.

**Componente assíncrono:** dropdown de carregamento atrasado, do estado inicial à seleção.

**Acessibilidade:** análise automatizada com axe-core sobre o formulário de cadastro,
complementada por análise manual registrada em [`achados.md`](./achados.md).

**Responsividade:** `RSP-04`, que assevera geometria de layout em tela estreita, mais a
jornada crítica re-executada em 390x844.

**Compatibilidade:** Chromium na suíte completa; Firefox num subconjunto-âncora.

Total: **11 cenários automatizados**, detalhados na
[matriz de cenários](./matriz-de-cenarios.md). São dois acima da faixa de 5 a 8 sugerida
pelo §3 do enunciado, que permite cenários adicionais: a faixa foi excedida para fechar a
jornada obrigatória inteira, e não por acúmulo de cenários redundantes.

---

## Fora de escopo

Resumo. A justificativa de cada item está na
[matriz de cenários](./matriz-de-cenarios.md#cenários-avaliados-e-não-automatizados).

| Fora de escopo | Razão em uma linha |
|---|---|
| Seção Network & API | Depende de serviço de terceiro; falharia por indisponibilidade alheia ao produto |
| Seção Flaky & Retry | Deliberadamente não determinística: não há contrato a asseverar |
| Seção "Accessibility" | *Sandbox* construída para passar; verde ali não protege nada |
| Tabela sob o gate do axe | Tem violações críticas conhecidas; o gate nasceria vermelho sem proteger regressão |
| Navegação por teclado da ordenação | Nasceria vermelho contra [ACH-04](./achados.md#ach-04); entra junto da correção |
| Drag and drop, hover, menu de contexto, Shadow DOM, iframes | Alto custo de manutenção para risco baixo em componentes sem regra de negócio |
| Shopping, carrossel, media player, galeria | Fora do escopo obrigatório; apresentação, não comportamento |
| Upload, date picker, wizard | Valor real, represado pelo limite de cenários |
| Teste de carga, varredura de segurança, exploração de vulnerabilidade | Vedado pelo enunciado (§15) |
| Teste visual | Sem *baseline* estável e sem política de aprovação de diferença, produz ruído |

---

## Riscos identificados

A análise completa está na [matriz de riscos](./matriz-de-riscos.md), com 15 riscos
funcionais, 10 técnicos, 6 de experiência, 9 de acessibilidade e 5 não previstos no
enunciado.

O que a análise concluiu, em três pontos:

**1. O risco está concentrado em duas jornadas.** Gerenciamento de usuários e cadastro
respondem por quase todo o impacto alto. É onde um erro se materializa como decisão errada
sobre uma pessoa. As outras cinco jornadas são componentes de demonstração.

**2. Os defeitos de acessibilidade têm uma causa raiz comum.** Contador de resultados sem
`aria-live`, mensagem de sucesso sem `role="status"`, ordenação sem `aria-sort` e erros de
validação sem `aria-invalid` não são quatro defeitos isolados: são o mesmo padrão ausente
— **a aplicação comunica mudança de estado apenas visualmente**
([RN-04](./matriz-de-riscos.md#riscos-não-apresentados-no-enunciado)). Tratar caso a caso
seria tratar sintoma.

**3. O caminho mais óbvio de implementação de teste é uma armadilha.** Os `span` de erro do
formulário estão **sempre** no DOM e **sempre** visíveis, apenas vazios enquanto não há
erro. Uma suíte escrita com asserção de visibilidade — o reflexo natural — nasce com falso
positivo e passa com o formulário quebrado
([RN-02](./matriz-de-riscos.md#riscos-não-apresentados-no-enunciado)). Por isso a asserção
desta suíte é sobre **texto**.

---

## Estratégia de cobertura

**Um cenário, um comportamento.** Nenhum cenário é variação de outro. `USR-03` e `USR-04`
exercitam o mesmo campo de busca, mas um prova filtragem e o outro prova tratamento de
vazio — caminhos distintos, falhas distintas.

**Asserção que falha por um motivo só.** Ordenação e paginação são cenários separados
mesmo tendo custo baixo e componente comum: juntá-los produziria um teste com duas razões
de falhar, que obriga a ler o stack trace para saber o que quebrou.

**Asserção dupla onde o sinal único é ambíguo.** Em `USR-04`, zero linhas sozinho passaria
também se a tabela tivesse quebrado e deixado de renderizar. Só o par *zero linhas **e**
contador zerado* comprova que a filtragem funcionou.

**Asserção de recorte nos cenários negativos.** `FRM-03` não se contenta com "o erro
apareceu": assevera que apareceu **no campo de e-mail** e que os outros três campos
continuam limpos. É a diferença entre provar a regra e observar que algo deu errado.

**Nenhuma asserção sobre defeito conhecido.** `USR-04` não assevera a ausência da mensagem
de estado vazio, embora a ausência seja real ([ACH-01](./achados.md#ach-01)). Asseverar
ausência transformaria a correção do defeito em quebra de teste — o avesso do que um teste
deve fazer. Defeito conhecido se corrige; não se congela em asserção.

---

## Critérios de priorização

Sem fórmula numérica. Três perguntas, em ordem, detalhadas na
[matriz de riscos](./matriz-de-riscos.md#critérios-de-priorização):

1. **Se quebrar, o usuário decide errado ou perde trabalho?** → impacto
2. **Qual a chance real de quebrar?** → probabilidade (lógica própria quebra; texto estático não)
3. **Quanto custa manter o teste vivo?** → viabilidade

**P0** entra agora · **P1** entra quando a suíte crescer · **P2** fica fora.

O terceiro critério é o que mais decide — mas o custo tem de ser **medido**, não presumido.
`USR-08` (exclusão) tem o maior impacto da matriz e foi mantido fora por um ciclo sob a
justificativa de que exigiria tratar diálogo nativo e um ciclo de vida próprio. Ao ser
implementado, nenhuma das duas coisas se confirmou: não existe diálogo de confirmação
([ACH-07](./achados.md#ach-07)) e o `BrowserContext` novo por teste já cobre a mutação de
estado. O cenário entrou. `DYN-01` tem impacto médio e entrou desde o início, porque
sincronização é o risco técnico mais provável de toda a suíte.

---

## Estratégia de automação

### Ferramentas

Todas as versões são fixadas em `<properties>` no [`pom.xml`](../pom.xml) — nenhuma
dependência usa faixa aberta, para que a mesma revisão do repositório produza a mesma
execução meses depois.

| Camada | Ferramenta | Versão |
|---|---|---|
| Linguagem | Java (LTS) | 21 |
| Build | Maven | 3.8+ |
| Runner | JUnit Jupiter (`junit-bom`) | 6.1.3 |
| Execução e grupos por *tag* | Maven Surefire | 3.6.0 |
| Automação de navegador | Playwright for Java | 1.63.0 |
| Asserções de DOM | `PlaywrightAssertions` (com *retry*) | do Playwright |
| Asserções de coleção | AssertJ | 3.27.7 |
| Acessibilidade | `com.deque.html.axe-core:playwright` | 4.14.0 |
| Relatório | Allure (`allure-bom`) | 2.35.5 |
| CI | GitHub Actions | — |

**Java 21, e não a LTS seguinte.** A suíte não usa nenhum recurso posterior ao 21: os
`record`, `switch` com seta e blocos de texto que aparecem no código entraram antes disso.
Fixar a LTS mais conservadora que atende ao código amplia o número de máquinas em que a
entrega roda sem ajuste — e o mesmo número está no `pom.xml`, no `JAVA_VERSION` da
pipeline e nos pré-requisitos do README, de modo que não existe divergência entre o que o
documento diz e o que a execução faz.

**Geração de massa sem dependência externa.** O plano inicial previa Java Faker. Com 9
cenários e um único tipo de massa, uma fábrica própria de 20 linhas
(`RegistrationDataFactory`) entrega o mesmo e evita trazer uma biblioteca inteira para o
*classpath* de teste.

### Arquitetura

**Component Objects, não Page Objects.** A aplicação é uma página única com dezenas de
componentes independentes. Um objeto por página viraria uma classe com centenas de
locators sem relação entre si. O recorte é o componente.

```
tests/        intenção e asserções        ← data/ (massa por execução)
   ↓
components/   locators e ações
   ↓
Page          API do Playwright           ← support/ (contexto novo por teste)
   ↓
DOM da aplicação
```

Dependência unidirecional. **Nenhum teste chama `page.locator(...)` direto**; nenhum
componente conhece testes.

**O componente não assevera.** Ele expõe ações e `Locator`s; quem decide o que é certo é o
teste. Uma camada dedicada de validação foi considerada e **descartada**: o enunciado lista
entre os itens a evitar "abstrações que escondam completamente a intenção do teste".

**Critério de abstração.** Para cada alteração plausível, a resposta deve ser *um* arquivo:

| Pedido | Onde se mexe |
|---|---|
| Corrigir um seletor | `components/` |
| Adicionar uma asserção | `tests/` |
| Adaptar cenário para mobile | configuração (`-Dviewport=mobile`) |
| Executar só um grupo | `@Tag` + `-Dgroups` |
| Trocar de navegador | configuração (`-Dbrowser=firefox`) |

Se alguma exigir dois lugares, há abstração sobrando.

### Seletores

Ordem de preferência:

1. **`data-testid`** — a aplicação os expõe deliberadamente e em larga escala. É o
   contrato de teste mais estável possível: não muda com layout, texto ou idioma.
2. **Papel acessível e nome visível** (`getByRole`, `getByLabel`) — quando não há
   `data-testid`. Único caso relevante na aplicação: o *hamburger* do menu mobile, que tem
   `aria-label="Toggle menu"` e nenhum identificador de teste.
3. **CSS estrutural** — apenas dentro de um componente já ancorado por `data-testid`, como
   `td:nth-child(n)` para percorrer células de uma coluna.

**Nunca:** XPath, XPath absoluto, ou seletor cuja única âncora é posição.

Sobre o nível 3: a posição da célula **não** é fixada no código. O índice é descoberto no
cabeçalho em tempo de execução, comparando o `data-testid` de cada `th`. Se a aplicação
reordenar as colunas, o componente acompanha em vez de passar a ler a coluna errada e
produzir falso resultado. É o que separa isso de um seletor por posição.

### Sincronização

**Nenhuma `waitForTimeout` na suíte.** Toda espera é por condição observável.

O dropdown assíncrono resolve em ~2 s (medido) e oferece quatro condições observáveis
distintas — loader, estado habilitado do select, texto do placeholder e contagem de
opções. `DYN-01` assevera as quatro. A contagem de opções é a asserção mais forte do
conjunto, porque nenhuma delas passa por acidente.

Os tetos de espera (`timeout.action.ms` e `timeout.assertion.ms`, ambos 10 s) não são
esperas: o Playwright devolve o controle no instante em que a condição é satisfeita. O
número só limita quanto tempo ele aceita esperar antes de falhar, e está dimensionado com
folga sobre os 2 s medidos para absorver CI lento.

Uma asserção da suíte é deliberadamente sobre **estado transitório**: o loader do dropdown,
asseverado imediatamente após o clique, dentro da janela de 2 s. Está lá porque o loader é
a única confirmação que o usuário recebe de que algo está acontecendo. A latência entre o
clique e a asserção é de milissegundos contra uma janela de 2 s — margem de três ordens de
grandeza.

### Isolamento e independência

**`BrowserContext` novo por teste.** A aplicação não tem backend: não existe API para
semear nem limpar massa, e recarregar restaura o estado original. O contexto novo é o
reset — descarta cookies, storage e qualquer mutação de DOM do teste anterior.

**Nenhum teste depende de ordem nem de resultado de outro.** Verificado na prática: a
suíte é executada por classe, por método isolado e por grupo (`-Dgroups`), com o mesmo
resultado.

**O que é por teste e o que é por execução.** `BrowserContext` e `Page` ficam no `Store`
com escopo de teste; `Playwright` e `Browser` ficam no `Store` **raiz**, abertos uma vez e
fechados pelo JUnit no fim de tudo. A fronteira é o que separa isolamento de desperdício: o
contexto novo é o que isola, a engine nova não isolaria nada e custaria a inicialização a
cada cenário. Um `ThreadLocal` resolveria o escopo por teste, mas seria reimplementar à mão
o que o framework já oferece.

### Dados de teste

Massa gerada por execução pela `RegistrationDataFactory`, nunca fixada no código, com
e-mail único por execução no domínio `example.com` (reservado pela RFC 2606 para teste).
Três razões: o teste não passa a depender de valor deixado na tela; o e-mail torna a
evidência rastreável até a execução que a produziu; e se a aplicação ganhar backend, a
suíte não precisa mudar de estratégia para deixar de colidir.

**Nenhum dado real, nenhum segredo, nenhuma credencial versionada.**

### Evidências

Capturadas **apenas em falha**, para manter o artefato da pipeline enxuto:

- **Trace do Playwright** — abre em `trace.playwright.dev`, com *screencast* quadro a
  quadro, snapshots do DOM, rede e console. O trace começa sempre e é descartado quando o
  teste passa: não dá para decidir depois da falha que se queria ter gravado.
- **Screenshot** de página inteira.
- **Console e erros da página** — muitas quebras de front aparecem primeiro aqui, antes de
  virarem sintoma visível.

Trace em vez de vídeo é escolha deliberada. O enunciado pede "trace **ou** vídeo"; o trace
contém o screencast e muito mais, e dispensa gravar vídeo de teste que passou.

Todo teste, passando ou falhando, registra no relatório **em qual navegador e em qual
viewport** rodou. Sem isso, um resultado de pipeline multi-navegador não diz onde falhou.

### Acessibilidade: baseline em vez de "zero violações"

Uma asserção de zero violações **falharia hoje**, e teste que nasce vermelho é teste que
será desligado na primeira semana.

O recorte adotado é o que se usa para introduzir axe em código existente: as violações
conhecidas ficam **declaradas no código** e rastreadas como achado, e o teste falha diante
de **qualquer violação nova**. O gate protege contra regressão; defeito conhecido se trata
por correção, não por asserção.

No escopo do formulário a *baseline* tem um item: `color-contrast`
([ACH-03](./achados.md#ach-03)). Qualquer outra regra derruba o teste.

---

## Navegadores e resoluções

### Navegadores

| Navegador | Escopo | Comando |
|---|---|---|
| **Chromium** | Suíte completa, 11 cenários | `mvn test` |
| **Firefox** | Subconjunto `crossbrowser`: `USR-01` e `FRM-01` | `mvn test -Dgroups=crossbrowser -Dbrowser=firefox` |
| **WebKit** | Suportado, não executado por padrão | `mvn test -Dbrowser=webkit` |

**Por que não a suíte inteira em todos.** Diferença entre engines aparece em renderização,
layout e implementação de eventos — não em lógica de aplicação. Dois cenários-âncora
cobrem isso: `USR-01` exercita tabela, ordenação e layout; `FRM-01` exercita formulário,
`select`, `checkbox` e submissão.

Sobre o custo do Firefox, vale corrigir uma medição: antes, com um navegador por teste, ele
levava de 10 a 18 s **por cenário** e isso sustentava a decisão de não rodar a suíte inteira
nele. Com a engine aberta uma vez por execução, o custo é de abertura (12 s) e não de
cenário (2,8 s cada). A decisão de manter o subconjunto permanece, mas o argumento agora é
outro: diferença entre engines aparece em renderização e eventos, não em lógica de
aplicação, e dois cenários-âncora cobrem isso. Se houver razão de risco, ampliar o
subconjunto hoje é barato.

**Por que configuração e não `@ParameterizedTest`.** Um teste parametrizado por navegador
multiplicaria a contagem de cenários da suíte sem acrescentar comportamento coberto. O
cenário continua sendo um; o que muda é onde ele roda.

### Resoluções

| Perfil | Resolução | Por que esta |
|---|---|---|
| `desktop` | **1280x720** | Menor largura comum de desktop em uso. Se o layout funciona aqui, funciona acima. Evita o falso conforto de testar em 1920 e nunca exercitar quebra de grade |
| `mobile` | **390x844** | iPhone 14/15, referência de mercado. Abaixo do breakpoint de 768 px da aplicação, portanto exercita o caminho responsivo de verdade |

O viewport vem de configuração, **nunca fixado no teste**. O teste não sabe em que
resolução está rodando — é o que permite a mesma jornada rodar em desktop e em mobile sem
duplicar código.

Isso vale também para `RSP-04`, e é o que faz dele um cenário honesto em vez de um teste
que só existe num perfil: ele assevera a janela que a execução recebeu, qualquer que seja
ela. Em 390x844, onde a tabela de fato transborda, é a prova do padrão responsivo; em
1280x720 continua protegendo contra uma regressão de CSS que introduza scroll horizontal na
página. Por isso não é ignorado em desktop — a suíte não tem teste ignorado.

**A divisão de trabalho entre os dois.** A re-execução da jornada prova **alcance**: como o
Playwright exige elemento visível e habilitado para preencher e clicar, interagir com a
busca em 390px já prova que o controle continua operável. `RSP-04` prova **geometria**, que
alcance nenhum demonstra. São perguntas diferentes, e misturá-las num só cenário deixaria as
duas mal respondidas.

**Limitação assumida:** emulamos **resolução**, não dispositivo. Não marcamos `isMobile`
nem `hasTouch`, porque essas opções do Playwright são exclusivas do Chromium e quebrariam a
execução em Firefox. O que se valida em 390x844 é layout e alcance dos controles, **não**
comportamento de toque. Fica fora, verificado apenas à mão, o `RSP-03` (sobreposição de
elementos), que exigiria comparar as caixas de vários elementos entre si.

---

## Critérios de entrada e saída

### Entrada

- Aplicação-alvo acessível em `https://playwrightlab.github.io/`
- JDK 21+ e Maven 3.8+ disponíveis
- Navegadores do Playwright instalados (passo próprio; a suíte não baixa navegador durante
  o teste)
- Riscos analisados e cenários priorizados — [matriz de riscos](./matriz-de-riscos.md)

### Saída

A execução é considerada aprovada quando:

- **100% dos 11 cenários passam em Chromium**, sem *retry* e sem teste ignorado
- O subconjunto `crossbrowser` passa em Firefox
- O subconjunto `mobile` passa em 390x844
- `A11Y-06` não acusa violação fora da *baseline* declarada
- O relatório Allure é gerado e publicado
- Em caso de falha, a evidência (trace, screenshot, console) está anexada e preservada no
  artefato da pipeline

**Sem política de retry.** A suíte não reexecuta teste que falhou. Retry esconde
instabilidade, e instabilidade escondida é a razão pela qual suítes deixam de ser levadas a
sério. Falha intermitente é investigada e corrigida, ou o cenário sai.

---

## Limitações conhecidas

**Da aplicação-alvo**

- Não há backend: nenhuma massa é persistida e nenhum teste pode validar persistência
- Não há API para semear ou limpar estado — o reset é o contexto novo
- Seis defeitos confirmados, em [`achados.md`](./achados.md), nenhum deles corrigível por
  esta entrega

**Da análise automatizada de acessibilidade**

O axe-core cobre a parte do WCAG verificável por máquina — na literatura da própria Deque,
algo entre 30% e 40% dos critérios. Fora do alcance da ferramenta:

| O que o axe não alcança | Achado correspondente |
|---|---|
| Se uma funcionalidade é operável por teclado | [ACH-04](./achados.md#ach-04) |
| Se mudança de estado é anunciada a tecnologia assistiva | [ACH-05](./achados.md#ach-05) |
| Se erro de validação está ligado ao campo que o causou | [ACH-05](./achados.md#ach-05) |
| Se a ordem de foco é coerente com a ordem visual | Não verificado |
| Se o texto alternativo descreve a imagem adequadamente | Não aplicável aqui |
| Se um modal cria armadilha de teclado | Não verificado |

E o inverso também acontece: o axe **aprova** o campo de busca da tabela, cujo único rótulo
é o `placeholder` ([ACH-06](./achados.md#ach-06)). A ferramenta aceita `placeholder` no
cálculo do nome acessível; o nome existe, a qualidade dele é que é ruim.

**Conclusão prática:** `A11Y-06` é um portão de regressão, não um atestado de
acessibilidade. Cinco dos sete achados desta entrega vieram de análise manual.

**Da suíte**

- A execução mobile não aparece como teste na execução local padrão: é alvo explícito
  (`-Dgroups=mobile -Dviewport=mobile`) e *job* separado na pipeline
- A suíte leva ~35 s para 11 cenários em Chromium. Era ~43 s quando cada teste abria o seu
  próprio `Browser`; a engine passou a ser aberta uma vez por execução, e o isolamento
  continua vindo do `BrowserContext` novo por teste
- A suíte roda sequencialmente. A arquitetura suporta paralelismo — o estado está no
  `Store` por teste, sem compartilhamento — mas o enunciado veda execução excessivamente
  paralela (§15) e nesta escala o ganho não justifica o risco
- Instabilidade observada uma vez na inicialização a frio do Firefox, registrada em
  [`achados.md`](./achados.md#instabilidade-observada-na-própria-suíte)

---

## Próximos passos

Em ordem de valor.

**1. `A11Y-03` e `A11Y-04` — teclado e ordem de foco.** Devem entrar **junto com a
correção** de [ACH-04](./achados.md#ach-04), como teste de regressão. Automatizáveis com
`page.keyboard.press("Tab")` e asserção sobre o elemento focado.

**2. Ratchet da baseline do axe.** Hoje a *baseline* é uma lista estática **por regra**, o
que deixa um furo conhecido: uma violação de `color-contrast` num elemento novo do
formulário não derrubaria o portão, porque a regra já está perdoada. O passo é torná-la
nó-a-nó e decrescente — ao corrigir um nó, removê-lo da lista, de forma que a baseline só
encolha. Ampliar o escopo para a tabela assim que [ACH-02](./achados.md#ach-02) for
corrigido.

**3. `RSP-03` — ausência de sobreposição em tela estreita.** O `RSP-04` já cobre o scroll
horizontal; a sobreposição exigiria comparar as caixas de vários elementos entre si, o que
é bem mais caro que a leitura única de `RSP-04` e merece cenário próprio e bem feito.

**4. `FRM-02`, `FRM-04` e `FRM-05`.** Obrigatórios vazios, limite de caracteres e reset.
Baratos, porque os componentes já expõem o necessário.

**5. Interceptação de rota para a seção Network & API.** `page.route` para servir resposta
controlada, cobrindo a seção sem depender de serviço de terceiro. Habilita também cenários
de erro (400, 401, 500) que hoje são inalcançáveis de forma determinística.

**6. Paralelismo.** Habilitar execução paralela do JUnit quando a suíte passar de ~20
cenários, momento em que o tempo sequencial começa a incomodar. Exige uma chave de engine
por thread no *store* raiz, porque o objeto `Playwright` não é thread-safe.

**7. Relatório com histórico.** Preservar `history` entre execuções da pipeline, para que o
Allure mostre tendência e identifique teste intermitente antes que ele vire folclore.
