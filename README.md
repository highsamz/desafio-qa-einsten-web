# PlayLab — Suíte de Testes Front Web

Automação Front Web da aplicação **PlayLab — Playwright Practice Arena**, construída como
desafio técnico de Engenharia de Qualidade.

**11 cenários automatizados** cobrindo a jornada de gerenciamento de usuários de ponta a
ponta — da carga da tabela à exclusão de um registro —, formulário, componente assíncrono,
cenário negativo, acessibilidade e responsividade, com pipeline no GitHub Actions,
relatório Allure e evidências de diagnóstico.

[![Testes Front Web](https://github.com/highsamz/desafio-qa-einsten-web/actions/workflows/tests.yml/badge.svg)](https://github.com/highsamz/desafio-qa-einsten-web/actions/workflows/tests.yml)

---

## Índice

- [Descrição da solução](#descrição-da-solução)
- [Aplicação utilizada](#aplicação-utilizada)
- [Escopo implementado](#escopo-implementado)
- [Stack utilizada](#stack-utilizada)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Execução](#execução)
- [Relatório e evidências](#relatório-e-evidências)
- [Arquitetura do projeto](#arquitetura-do-projeto)
- [Estratégia de seletores](#estratégia-de-seletores)
- [Estratégia de sincronização](#estratégia-de-sincronização)
- [Estratégia de dados de teste](#estratégia-de-dados-de-teste)
- [Decisões técnicas](#decisões-técnicas)
- [Pipeline](#pipeline)
- [Achados](#achados)
- [Limitações conhecidas](#limitações-conhecidas)
- [Uso de Inteligência Artificial](#uso-de-inteligência-artificial)
- [Próximos passos](#próximos-passos)

---

## Descrição da solução

A suíte parte de uma **análise de risco** da aplicação, não de uma lista de funcionalidades.
A ordem foi: mapear jornadas, levantar riscos funcionais, técnicos, de experiência e de
acessibilidade, priorizar, e só então escolher o que automatizar. O raciocínio está em
[`docs/matriz-de-riscos.md`](./docs/matriz-de-riscos.md) e a seleção resultante em
[`docs/matriz-de-cenarios.md`](./docs/matriz-de-cenarios.md).

A análise exploratória produziu **7 defeitos confirmados** na aplicação, em
[`docs/achados.md`](./docs/achados.md) — cinco deles indetectáveis por ferramenta
automatizada. O mesmo documento registra uma hipótese que **não** se confirmou e foi
descartada com evidência.

Cada cenário prova um comportamento distinto. Nenhum é variação de outro, e nenhum passa
por acidente.

### Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/estrategia-de-testes.md`](./docs/estrategia-de-testes.md) | Objetivo, escopo, estratégia de cobertura e automação, critérios de entrada e saída |
| [`docs/matriz-de-riscos.md`](./docs/matriz-de-riscos.md) | Jornadas, 47 riscos classificados, critérios de priorização, frequência de execução |
| [`docs/matriz-de-cenarios.md`](./docs/matriz-de-cenarios.md) | Cenários automatizados e **não** automatizados, com justificativa e rastreabilidade |
| [`docs/achados.md`](./docs/achados.md) | Defeitos encontrados, com reprodução e evidência |
| [`COMMENTS.md`](./COMMENTS.md) | As decisões técnicas e o porquê de cada uma, em linguagem simples |
| [`docs/arquitetura-de-classes.md`](./docs/arquitetura-de-classes.md) | Desenho das classes, seus métodos e como elas se relacionam |
| [`docs/desenhos-automacao.excalidraw`](./docs/desenhos-automacao.excalidraw) | Desenho das camadas, feito antes da implementação |

---

## Aplicação utilizada

**PlayLab — Playwright Practice Arena** — <https://playwrightlab.github.io/>

Página única, estática, servida pelo GitHub Pages, com 21 seções de componentes de
interface. **Sem backend**, o que tem três consequências diretas sobre a estratégia:

- nenhuma massa é persistida, logo não há como validar persistência;
- não existe API para semear nem limpar estado — o reset é o contexto novo do navegador;
- recarregar a página restaura os dados originais (15 usuários).

A aplicação expõe `data-testid` de forma deliberada e em larga escala, o que sustenta a
estratégia de seletores.

---

## Escopo implementado

Conferência contra os entregáveis obrigatórios do enunciado (§9.1):

| Exigência | Situação | Onde |
|---|---|---|
| Entre 5 e 8 testes automatizados | **11 cenários — acima da faixa**, por decisão declarada logo abaixo | 5 classes de teste |
| Jornada principal da tabela de usuários | ✅ Coberta de ponta a ponta, incluindo a ação e a validação do seu resultado | `USR-01`, `USR-03`, `USR-04`, `USR-05`, `USR-06`, `USR-08` |
| Cenário de formulário | ✅ | `FRM-01` |
| Cenário negativo | ✅ Dois | `USR-04`, `FRM-03` |
| Cenário dinâmico ou assíncrono | ✅ | `DYN-01` |
| Validação de acessibilidade | ✅ Automatizada **e** manual | `A11Y-06` e [`achados.md`](./docs/achados.md) |
| Execução em resolução mobile | ✅ 390x844, com cenário de layout **e** re-execução da jornada crítica | `RSP-04` + *job* `mobile` (`-Dgroups=mobile -Dviewport=mobile`) |
| Configuração para mais de um navegador | ✅ Chromium, Firefox, WebKit | `-Dbrowser=` |
| Relatório e evidências | ✅ Allure + trace, screenshot e console | [Relatório e evidências](#relatório-e-evidências) |
| Pipeline no GitHub Actions | ✅ 4 *jobs* | [`.github/workflows/tests.yml`](./.github/workflows/tests.yml) |

**Sobre os 11 cenários numa faixa de 5 a 8.** O desvio é deliberado e o enunciado o
permite: o §3 diz que "é permitido implementar cenários adicionais". A faixa foi excedida
por duas razões, nesta ordem:

1. **cobrir a jornada obrigatória do §4 inteira** — os seis passos, da carga da tabela até
   executar uma ação e validar o resultado. Cortar `USR-05` (ordenação), `USR-06`
   (paginação) ou `USR-08` (exclusão) devolveria a suíte à faixa e deixaria a jornada pedida
   incompleta, o que é o pior dos dois desvios;
2. **não deixar a responsividade apenas declarada** — `RSP-04` assevera geometria de layout,
   que a re-execução da jornada em 390x844 não prova.

Nenhum dos 11 é variação de outro, e o §12 do enunciado é explícito: a quantidade de testes
não é indicador de qualidade.

### Os 11 cenários

| ID | Cenário | Tipo |
|---|---|---|
| `USR-01` | A tabela carrega com a estrutura e os dados esperados | Funcional |
| `USR-03` | A pesquisa localiza um usuário existente ignorando maiúsculas | Funcional |
| `USR-04` | A pesquisa sem correspondência esvazia a tabela e zera o contador | **Negativo** |
| `USR-05` | A ordenação por ID é numérica e alterna entre crescente e decrescente | Funcional |
| `USR-06` | A navegação entre páginas percorre os 15 registros sem repetir nem pular | Funcional |
| `USR-08` | A exclusão remove o usuário correto, preserva os vizinhos e atualiza o contador | Funcional |
| `FRM-01` | O cadastro com dados válidos confirma o sucesso e não acusa erro em campo nenhum | Funcional |
| `FRM-03` | O e-mail inválido bloqueia o envio e acusa o erro apenas no campo de e-mail | **Negativo** |
| `DYN-01` | O dropdown assíncrono sinaliza carregamento, habilita as opções e registra a seleção | **Sincronização** |
| `A11Y-06` | A análise automatizada do formulário não acusa violação de acessibilidade nova | **Acessibilidade** |
| `RSP-04` | A tabela larga não produz scroll horizontal na página | **Responsividade** |

**Sobre mobile: uma re-execução e um cenário, e a diferença entre os dois.** O enunciado
pede "pelo menos uma *execução* em resolução mobile", e a resposta é dupla porque são dois
problemas diferentes:

- **A jornada crítica é re-executada**, não duplicada. `USR-01` e `USR-03`, marcados
  `@Tag("mobile")`, rodam de novo em 390x844 por um *job* próprio. O viewport vem de
  configuração e nunca do código do teste, então copiar a jornada num arquivo novo para
  variar um número seria duplicação sem ganho.
- **`RSP-04` é cenário próprio** porque assevera algo que só existe em tela estreita:
  geometria de layout. Re-executar a jornada prova que os controles continuam alcançáveis —
  o Playwright exige elemento visível e habilitado para preencher e clicar —, mas não diz
  nada sobre a página rolar para o lado. São perguntas distintas, e a segunda precisava de
  asserção própria.

O detalhamento está em
[`matriz-de-cenarios.md`](./docs/matriz-de-cenarios.md#execuções-adicionais-sobre-os-mesmos-cenários).

---

## Stack utilizada

| Camada | Escolha | Versão |
|---|---|---|
| Linguagem | Java (LTS) | 21 |
| Build | Maven | 3.8+ |
| Runner | JUnit Jupiter | 6.1.3 |
| Automação | Playwright for Java | 1.63.0 |
| Asserções de DOM | `PlaywrightAssertions` (com *retry*) | — |
| Asserções de coleção | AssertJ | 3.27.7 |
| Acessibilidade | `com.deque.html.axe-core:playwright` | 4.14.0 |
| Relatório | Allure | 2.35.5 |
| CI | GitHub Actions | — |

### Por que Java e não Playwright + TypeScript

O enunciado define TypeScript como stack preferencial e permite outra **desde que
justificada tecnicamente** (§7.1).

A justificativa é a **apresentação técnica**. Será pedido para alterar e depurar o código
ao vivo, e profundidade na linguagem sustenta isso melhor do que familiaridade superficial
com a stack preferencial. Código que o autor não consegue modificar sob pressão é passivo,
não ativo.

**O framework não muda.** Continua Playwright: mesma engine, mesmo *auto-waiting*, mesmo
trace viewer, mesmo motor de seletores por papel acessível. Só o *binding* é Java.

**O custo foi assumido conscientemente.** O *binding* Java não traz test runner, reporter
HTML, modelo de *fixtures* nem configuração de `projects`. Tudo isso foi reconstruído:

| O que o runner TypeScript dá de graça | Como foi reconstruído aqui |
|---|---|
| Test runner | JUnit Jupiter |
| Reporter HTML | Allure |
| *Fixture* `page` | `ParameterResolver` do JUnit, em `PlaywrightExtension` |
| `projects` (navegador × viewport) | Configuração (`-Dbrowser`, `-Dviewport`) + `@Tag` + *jobs* da pipeline |
| Trace e screenshot em falha | `AfterTestExecutionCallback` em `PlaywrightExtension` |

A reconstrução da *fixture* é o ponto mais interessante: `PlaywrightExtension` implementa
`ParameterResolver` e entrega a `Page` como **parâmetro do método de teste**, exatamente
como a *fixture* do runner TypeScript.

```java
@Test
void pesquisaLocalizaUsuarioExistente(Page page) { ... }
```

---

## Pré-requisitos

- **JDK 21+** — `java -version`
- **Maven 3.8+** — `mvn -v`
- Conexão com internet (a aplicação-alvo é remota)

Opcional, para abrir o relatório localmente:

- **Allure CLI** — não é necessário. O `allure-maven` baixa o que precisa.

---

## Instalação

```bash
git clone https://github.com/highsamz/desafio-qa-einsten-web.git
cd desafio-qa-einsten-web
mvn -B test-compile
```

### Instalar os navegadores do Playwright

**Passo obrigatório e separado.** A suíte **não** baixa navegador durante a execução dos
testes — ver [Decisões técnicas](#4-a-instalação-de-navegador-é-passo-próprio).

```bash
mvn exec:java -D exec.args="install chromium"
```

Outros navegadores, conforme a necessidade:

```bash
mvn exec:java -D exec.args="install firefox"
mvn exec:java -D exec.args="install webkit"
```

Em Linux, para instalar também as bibliotecas de sistema:

```bash
mvn exec:java -D exec.args="install --with-deps chromium"
```

> O `mainClass` (`com.microsoft.playwright.CLI`) e o `classpathScope=test` já estão
> configurados no `pom.xml`, por isso o comando é curto e idêntico no local e na pipeline.

Se um navegador não estiver instalado, a falha vem com a instrução do que rodar — não com
um erro de driver:

```
Nao foi possivel abrir o navegador 'chromium'. Se ele nao estiver instalado, rode:
  mvn exec:java -D exec.args="install chromium"
```

---

## Execução

### Suíte completa

```bash
mvn test
```

11 cenários em Chromium, 1280x720, *headless*. Leva cerca de 35 segundos.

### Acompanhar no navegador visível

```bash
mvn test -Dheadless=false
```

### Execução por navegador

```bash
mvn test -Dbrowser=chromium
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit
```

Subconjunto de compatibilidade (os cenários-âncora `USR-01` e `FRM-01`):

```bash
mvn test -Dgroups=crossbrowser -Dbrowser=firefox
```

### Execução mobile

```bash
mvn test -Dgroups=mobile -Dviewport=mobile
```

Re-executa a jornada crítica em **390x844**. O `-Dviewport=mobile` pode ser combinado com
qualquer seleção de testes:

```bash
mvn test -Dviewport=mobile
```

### Execução por grupo

Grupos disponíveis (`@Tag`): `users`, `forms`, `dynamic`, `a11y`, `responsive`, `mobile`,
`crossbrowser`.

```bash
mvn test -Dgroups=users
mvn test -Dgroups=a11y
mvn test "-Dgroups=forms,dynamic"
mvn test "-DexcludedGroups=a11y"
```

### Execução de um cenário específico

```bash
mvn test -Dtest=UserTableTest
mvn test -Dtest='UserTableTest#ordenacaoPorIdEhNumericaEAlterna'
```

### Configuração

Os valores padrão estão em
[`src/test/resources/config.properties`](./src/test/resources/config.properties):

| Chave | Descrição | Padrão |
|---|---|---|
| `base.url` | URL da aplicação | `https://playwrightlab.github.io/` |
| `browser` | `chromium`, `firefox` ou `webkit` | `chromium` |
| `headless` | Executar sem interface gráfica | `true` |
| `viewport` | Perfil ativo: `desktop` ou `mobile` | `desktop` |
| `viewport.desktop.width` / `.height` | Resolução desktop | `1280` / `720` |
| `viewport.mobile.width` / `.height` | Resolução mobile | `390` / `844` |
| `timeout.action.ms` | Teto de espera de ações e locators | `10000` |
| `timeout.assertion.ms` | Teto de espera das asserções | `10000` |

Precedência de leitura, implementada em `TestConfig`:

```
-Dchave  >  variável de ambiente  >  config.properties  >  default no código
```

A variável de ambiente usa o nome em maiúsculas com ponto virando *underscore*:
`BASE_URL`, `VIEWPORT_MOBILE_WIDTH`.

**Nenhum valor de configuração é segredo.** A aplicação-alvo é pública e estática, não há
autenticação, e o repositório não contém token, senha nem credencial.

---

## Relatório e evidências

### Gerar e abrir o relatório Allure

```bash
mvn test
mvn allure:report     # gera em target/site/allure-maven-plugin
mvn allure:serve      # gera e abre no navegador
```

> O Allure exige **duas etapas separadas**: gerar e publicar. O `allure:serve` faz as duas
> e sobe um servidor local.

O relatório traz, por cenário: o ID e a descrição, os passos (`@Step`) com os valores
reais usados, a severidade, e **em qual navegador e em qual viewport o teste rodou**. Sem
esse último dado, um resultado de pipeline multi-navegador não diz onde falhou.

### Evidências em falha

Capturadas **apenas quando o teste falha**, para manter o artefato da pipeline enxuto:

| Evidência | Onde fica | Para quê |
|---|---|---|
| **Trace do Playwright** | `target/evidence/<Classe>/<metodo>/trace.zip` | *Screencast* quadro a quadro, snapshots do DOM navegáveis, rede e console. Abre em <https://trace.playwright.dev> |
| **Screenshot** | `target/evidence/<Classe>/<metodo>/screenshot.png` | Página inteira no momento da falha |
| **Console e erros da página** | Anexo no relatório | Muitas quebras de front aparecem aqui antes de virarem sintoma visível |

Todas as três são anexadas ao relatório Allure **e** gravadas em disco.

O trace **começa sempre** e é descartado quando o teste passa — não dá para decidir depois
da falha que se queria ter gravado.

**Trace em vez de vídeo** é escolha deliberada. O enunciado pede "trace **ou** vídeo"; o
trace contém o *screencast* e muito mais, e dispensa gravar vídeo de teste que passou.

---

## Arquitetura do projeto

```
.
├── .github/workflows/tests.yml        pipeline (4 jobs)
├── docs/
│   ├── estrategia-de-testes.md
│   ├── matriz-de-riscos.md
│   ├── matriz-de-cenarios.md
│   └── achados.md
├── pom.xml
└── src/test/
    ├── java/com/samuellima/playlab/
    │   ├── support/                   infraestrutura
    │   │   ├── TestConfig             leitura de configuração com precedência
    │   │   ├── Viewport               resolução, com o perfil que a originou
    │   │   ├── BrowserFactory         nome da engine → BrowserType
    │   │   ├── Layout                 medidas de layout lidas do navegador
    │   │   └── PlaywrightExtension    ciclo de vida, injeção de Page, evidências
    │   ├── components/                Component Objects
    │   │   ├── UserTableComponent
    │   │   ├── RegistrationFormComponent
    │   │   └── DelayedDropdownComponent
    │   ├── data/                      massa de teste
    │   │   ├── RegistrationData
    │   │   └── RegistrationDataFactory
    │   └── tests/
    │       ├── users/UserTableTest            USR-01, 03, 04, 05, 06, 08
    │       ├── forms/RegistrationFormTest     FRM-01, 03
    │       ├── dynamic/DelayedDropdownTest    DYN-01
    │       ├── accessibility/AccessibilityTest A11Y-06
    │       └── responsive/ResponsiveLayoutTest RSP-04
    └── resources/
        ├── config.properties
        └── allure.properties
```

**Não existe `src/main`.** É projeto exclusivamente de teste, sem código de produção.

A estrutura diverge da sugerida no enunciado (`src/pages`, `tests/`) porque aquela é o
*layout* de um projeto Node. O Maven impõe `src/test/java`. Dentro disso, a divisão por
jornada (`users`, `forms`, `dynamic`, `accessibility`) segue exatamente a sugestão.

### Component Objects, não Page Objects

A aplicação é **uma única página** com dezenas de componentes independentes. Um objeto por
página viraria uma classe com centenas de locators sem relação entre si. O recorte é o
componente: o que está dentro do `table-card` é do `UserTableComponent`, e nada mais.

### Direção de dependência

```
tests/        intenção e asserções        ← data/ (massa por execução)
   ↓
components/   locators e ações
   ↓
Page          API do Playwright           ← support/ (contexto novo por teste)
   ↓
DOM da aplicação
```

Unidirecional. **Nenhum teste chama `page.locator(...)` direto**; nenhum componente conhece
testes.

### O componente não assevera

Componentes expõem ações e `Locator`s. Quem decide o que é certo é o teste.

Expor `Locator` em vez de `String` é deliberado: a asserção do teste fica com o *retry* do
`PlaywrightAssertions`, que reavalia o DOM até a condição bater. Devolver texto já
resolvido entregaria um retrato de um instante e reintroduziria instabilidade.

Uma camada dedicada de validação foi considerada e **descartada**: o enunciado lista entre
os itens a evitar "abstrações que escondam completamente a intenção do teste".

### Critério de abstração

Para cada alteração plausível, a resposta deve ser **um** arquivo:

| Pedido | Onde se mexe |
|---|---|
| Corrigir um seletor | `components/` |
| Adicionar uma asserção | `tests/` |
| Adaptar cenário para mobile | configuração — `-Dviewport=mobile` |
| Executar só um grupo | `@Tag` + `-Dgroups` |
| Trocar de navegador | configuração — `-Dbrowser=firefox` |
| Mudar o teto de espera | `config.properties` |

Se alguma exigir dois lugares, há abstração sobrando.

---

## Estratégia de seletores

Ordem de preferência, da mais estável para a menos:

**1. `data-testid`** — a aplicação os expõe deliberadamente e em larga escala. É o contrato
de teste mais estável possível: não muda com layout, texto ou idioma.

```java
page.getByTestId("table-search")
page.getByTestId("table-row-" + userId)
```

**2. Papel acessível e nome visível** — quando não há `data-testid`. Único caso relevante
na aplicação: o *hamburger* do menu mobile, que tem `aria-label="Toggle menu"` e nenhum
identificador de teste.

**3. CSS estrutural** — apenas **dentro** de um componente já ancorado por `data-testid`.

**Nunca:** XPath, XPath absoluto, ou seletor cuja única âncora é posição.

### O caso delicado: percorrer as células de uma coluna

A tabela de usuários **não** tem `data-testid` por célula. Ler a coluna de IDs exige
chegar à célula por posição, o que o enunciado pede para evitar.

A solução é não fixar a posição no código. O índice é **descoberto no cabeçalho em tempo de
execução**, comparando o `data-testid` de cada `th`:

```java
public Locator columnCells(Column coluna) {
    return rows.locator("td:nth-child(" + (columnIndex(coluna) + 1) + ")");
}

private int columnIndex(Column coluna) {
    Locator headers = columnHeaders();
    for (int i = 0; i < headers.count(); i++) {
        if (coluna.testId().equals(headers.nth(i).getAttribute("data-testid"))) {
            return i;
        }
    }
    throw new IllegalStateException("Coluna " + coluna + " não está no cabeçalho.");
}
```

Se a aplicação reordenar as colunas, o componente **acompanha**, em vez de passar a ler a
coluna errada e produzir falso resultado. É o que separa isso de um seletor por posição: a
posição vem do contrato visível da tabela, não de um número que alguém chutou.

Como efeito colateral, isso é exatamente a técnica que o cenário `DYN-03` do enunciado
("tabela cujas colunas alteram de posição") exigiria — já implementada.

### Um detalhe que muda a forma de endereçar linha

O `data-testid` da linha segue o **id do registro**, não a posição na página: na página 2
as linhas são `table-row-6` a `table-row-10`. Isso permite endereçar a linha por
identidade, o que é melhor, mas quebra a intuição de que `table-row-1` seja a primeira
linha visível.

---

## Estratégia de sincronização

**Nenhuma `waitForTimeout` na suíte.** Toda espera é por condição observável. Esta é uma
afirmação verificável:

```bash
grep -rn "waitForTimeout\|Thread.sleep" src/
```

### O componente assíncrono

O dropdown de carregamento atrasado resolve em **~2 segundos** (medido na aplicação) e
oferece **quatro condições observáveis** distintas. `DYN-01` assevera todas:

| Condição | Antes | Depois |
|---|---|---|
| Loader | oculto | visível, depois oculto |
| `select` | `disabled` | habilitado |
| *Placeholder* | `Click button to load...` | `Select a category...` |
| Contagem de opções | 1 | 6 |

A contagem de opções é a asserção mais forte do conjunto, porque nenhuma delas passa por
acidente.

### Os tetos de espera não são esperas

`timeout.action.ms` e `timeout.assertion.ms` (ambos 10 s) **não** fazem o teste esperar: o
Playwright devolve o controle no instante em que a condição é satisfeita. O número só
limita quanto tempo ele aceita esperar antes de falhar, e está dimensionado com folga sobre
os 2 s medidos, para absorver CI lento.

### Uma asserção deliberadamente sobre estado transitório

```java
dropdown.load();
assertThat(dropdown.loader()).isVisible();      // dentro da janela de ~2 s
assertThat(dropdown.loadButton()).isDisabled();
```

O loader é a **única** confirmação que o usuário recebe de que algo está acontecendo, então
vale asseverar. A latência entre o clique e a asserção é de milissegundos contra uma janela
de 2 segundos — margem de três ordens de grandeza.

### A asserção que o caminho óbvio erraria

Os `span` de erro do formulário estão **sempre** no DOM e **sempre** visíveis, apenas
vazios enquanto não há erro:

```html
<span class="error-msg" data-testid="error-email"></span>
```

Portanto `assertThat(emailError()).isVisible()` **passa mesmo sem erro nenhum**. É
exatamente a "asserção genérica que não comprova o comportamento" que o enunciado pede para
evitar, e está no caminho mais natural de implementação. A asserção desta suíte é sobre
**texto**:

```java
assertThat(formulario.emailError()).hasText("Valid email is required");
```

---

## Estratégia de dados de teste

Massa gerada **por execução** pela `RegistrationDataFactory`, nunca fixada no código:

```java
RegistrationData dados = RegistrationDataFactory.valido();
formulario.fill(dados).submit();
```

O e-mail é único por execução, no domínio `example.com` — reservado pela **RFC 2606**
justamente para documentação e teste, e que não resolve para serviço nenhum.

Três razões para gerar em vez de fixar, mesmo sem backend:

1. o teste não passa a depender de um valor que alguém possa ter deixado na tela;
2. o e-mail de cada execução é **rastreável na evidência** — dá para saber qual execução
   produziu aquele screenshot;
3. se a aplicação ganhar backend, a suíte não precisa mudar de estratégia para deixar de
   colidir com registro existente.

`RegistrationData` é um `record`, e não uma fila de `String` nos parâmetros: o compilador
passa a impedir a troca de ordem entre nome, e-mail e senha, que é o erro mais comum
nessa assinatura.

**Nenhum dado real, nenhum segredo, nenhuma credencial versionada.** A senha usada é um
literal fictício que existe apenas para satisfazer a regra de tamanho mínimo de um
formulário público de demonstração.

### Isolamento

**`BrowserContext` novo por teste.** Sem backend não há API para semear nem limpar massa; o
contexto novo é o reset — descarta cookies, storage e qualquer mutação de DOM do teste
anterior.

Nenhum teste depende de ordem nem de resultado de outro. Verificado na prática: a suíte foi
executada completa, por classe, por método isolado e por grupo, com o mesmo resultado.

---

## Decisões técnicas

### 1. O ciclo de vida usa os dois escopos do `Store` do JUnit, não `ThreadLocal`

`PlaywrightExtension` guarda estado em **dois** escopos do `Store` do `ExtensionContext`, e
a divisão é a decisão:

| Escopo | O que vive nele | Por quê |
|---|---|---|
| `getRoot()` — a execução inteira | `Playwright` e `Browser` | Abrir a engine é caro e **não** é o que isola teste nenhum |
| por teste | `BrowserContext` e `Page` | É aqui que mora o isolamento: contexto novo descarta cookies, storage e mutação de DOM |

**O isolamento vem do contexto, não do navegador.** Uma versão anterior desta suíte abria um
`Playwright` e um `Browser` por teste, justificando com "o preço do isolamento". A
justificativa estava errada: o `BrowserContext` novo já garante tudo o que se queria, e a
engine nova por teste era custo sem contrapartida. Medido: **~43 s antes, ~35 s depois**,
com os mesmos cenários e o mesmo isolamento.

O `Browser` sai do ar sozinho porque `Engine` implementa `Store.CloseableResource` **e**
`AutoCloseable` — o Jupiter 5 fecha a primeira, o Jupiter 6 fecha a segunda, e declarar as
duas mantém o fechamento garantido numa atualização de versão. A chave do *store* inclui o
nome da engine, de modo que trocar `-Dbrowser` não reaproveita o navegador errado.

Um `ThreadLocal` resolveria o mesmo problema — e é o padrão mais comum em suítes Java — mas
seria reimplementar à mão um escopo que o framework já oferece, com o risco de vazar a
entrada se o `remove()` falhar. Fica registrado, para quando o paralelismo entrar, que o
objeto `Playwright` **não é thread-safe**: a chave passaria a ser por thread.

### 2. A `Page` chega como parâmetro do teste, não por herança

`PlaywrightExtension` implementa `ParameterResolver`. Herança de uma `BaseTest` resolveria,
mas gastaria o único slot de superclasse de cada teste e tornaria o estado implícito. Como
parâmetro, a dependência do teste é declarada na assinatura — e é o equivalente direto da
*fixture* `page` do runner TypeScript.

### 3. Navegador por configuração, não por `@ParameterizedTest`

Um teste parametrizado por navegador **multiplicaria a contagem de cenários** da suíte e
sem acrescentar um só comportamento coberto. Com `-Dbrowser`, o cenário continua
sendo um; o que muda é onde ele roda.

### 4. A instalação de navegador é passo próprio

A suíte cria o Playwright com `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`.

Sem isso, o primeiro `Playwright.create()` de uma máquina sem os binários baixa mais de
100 MB **dentro** da execução do teste. Três consequências, todas observadas na prática
durante o desenvolvimento:

- a execução fica lenta sem explicação aparente;
- o tempo de rede entra na duração do teste no relatório;
- o progresso do download escreve direto no `stdout` do JVM filho e **corrompe o canal do
  Surefire** (`Corrupted channel by directly writing to native stream in forked JVM`).

Em troca, a falha de navegador ausente vem com a instrução do que rodar.

### 5. Duas bibliotecas de asserção, com fronteira clara

| Biblioteca | Para quê | Por quê |
|---|---|---|
| `PlaywrightAssertions` | Todo estado de DOM | Tem *retry* embutido: reavalia até a condição bater |
| AssertJ | Coleções que não são DOM (lista de violações do axe) | Mensagem de falha descritiva via `as(...)` |

A fronteira é simples: se o alvo é um `Locator`, é `PlaywrightAssertions`.

### 6. Exclusões deliberadas no `pom.xml`

A dependência do axe traz duas coisas indesejadas:

- **`junit:junit` (JUnit 4)** — com JUnit 4 no classpath, o Surefire pode selecionar o
  *provider* errado e **ignorar silenciosamente** os testes Jupiter;
- **`com.microsoft.playwright:playwright`** — a versão da engine passa a ser efeito
  colateral de uma dependência transitiva. Declarada diretamente, é decisão explícita do
  `pom`.

### 7. `allure-bom` 2.35.5, e não 3.0.0

O Allure 3 está publicado. A entrega fica na linha 2.x, que é a combinação verificada com
`allure-maven` 2.15.2 neste projeto. Subir uma versão *major* de reporter sem necessidade
funcional, num projeto que precisa gerar relatório de forma confiável na pipeline, é risco
sem retorno.

### 8. `slf4j-nop` para silenciar o aviso do Allure

O Allure traz `slf4j-api` sem implementação, e o SLF4J reclama em toda execução. O
*binding* no-op silencia o aviso sem inventar um sistema de log que esta suíte não usa — o
diagnóstico sai por trace, screenshot e console da página.

### 9. Sem política de *retry*

A suíte não reexecuta teste que falhou. *Retry* esconde instabilidade, e instabilidade
escondida é a razão pela qual suítes deixam de ser levadas a sério. Falha intermitente é
investigada e corrigida, ou o cenário sai.

### 10. O gate de acessibilidade usa *baseline*, não "zero violações"

Uma asserção de zero violações **falharia hoje**, e teste que nasce vermelho é teste que
será desligado na primeira semana.

O recorte é o que se usa para introduzir axe em código existente: as violações conhecidas
ficam **declaradas no código**, rastreadas como achado, e o teste falha diante de
**qualquer violação nova**. O gate protege regressão; defeito conhecido se trata por
correção, não por asserção.

```java
private static final Set<String> VIOLACOES_CONHECIDAS = Set.of("color-contrast");
```

A análise roda no **formulário de cadastro**, e não na seção "Accessibility" da aplicação —
aquela é *sandbox* construída para passar, e verde ali não protege nada. Também **não** roda
na tabela, que tem violações críticas conhecidas ([ACH-02](./docs/achados.md#ach-02)):
colocar o gate ali deixaria a suíte vermelha desde o primeiro dia sem proteger regressão
nenhuma.

---

## Pipeline

[`.github/workflows/tests.yml`](./.github/workflows/tests.yml) — quatro *jobs*:

| Job | O que faz |
|---|---|
| **Suíte completa** | 11 cenários em Chromium, 1280x720 |
| **Compatibilidade (Firefox)** | Cenários-âncora `USR-01` e `FRM-01` |
| **Responsividade (390x844)** | `RSP-04` e a jornada crítica re-executada em resolução mobile |
| **Relatório Allure** | Junta os resultados dos três e gera **um** relatório |

Mínimo exigido pelo enunciado (§7.4), todo contemplado: checkout, configuração do ambiente,
instalação de dependências, **instalação dos navegadores** (passo próprio), execução dos
testes, publicação de relatório e evidências.

Diferenciais implementados:

| Diferencial | Como |
|---|---|
| **Execução manual** | `workflow_dispatch` com escolha de navegador, viewport e grupo |
| **Execução por navegador** | *Job* dedicado de Firefox + entrada do dispatch |
| **Execução por tag** | Entrada `grupo` do dispatch → `-Dgroups` |
| **Paralelismo** | Os três *jobs* de teste rodam em paralelo, cada um sequencial dentro do seu runner. Paralelismo no nível de *job* respeita a vedação do §15 a execução excessivamente paralela |
| **Cache de dependências** | Maven (via `setup-java`) e **binários dos navegadores**, chaveados pelo hash do `pom.xml` — subir a versão do Playwright invalida o cache automaticamente |
| **Evidências preservadas em falha** | `if: always()` em todos os uploads. Evidência de falha é justamente o que não pode ser perdido quando o passo falha |
| **Relatório consolidado** | O *job* de relatório baixa os resultados dos três e gera um relatório único, com desktop, Firefox e mobile lado a lado |
| **Resumo na execução** | Tabela de resultados no `GITHUB_STEP_SUMMARY`, com link para o trace viewer |

O *job* de relatório roda com `if: always()`: relatório de execução que falhou é o mais
importante de todos.

---

## Achados

A análise exploratória encontrou **7 defeitos confirmados**. Detalhamento, reprodução e
evidência em [`docs/achados.md`](./docs/achados.md).

| ID | Achado | Severidade | Detectado por |
|---|---|---|---|
| ACH-01 | Busca sem resultado não comunica estado vazio | Média | Manual |
| ACH-02 | Controles da tabela sem nome acessível | Alta | axe-core |
| ACH-03 | Contraste insuficiente em textos e controles | Média | axe-core |
| ACH-04 | Ordenação inacessível por teclado e sem estado programático | Alta | Manual |
| ACH-05 | Mudanças de estado não são anunciadas a tecnologia assistiva | Alta | Manual |
| ACH-06 | Campo de busca rotulado apenas por *placeholder* | Baixa | Manual — o axe **aprova** |
| ACH-07 | Exclusão de usuário sem confirmação e sem desfazer | Alta | Manual — descoberto ao automatizar `USR-08` |

**Os defeitos de acessibilidade têm uma causa raiz comum.** Contador de resultados sem
`aria-live`, mensagem de sucesso sem `role="status"`, ordenação sem `aria-sort` e erros de
validação sem `aria-invalid` não são quatro defeitos isolados: são o mesmo padrão ausente —
a aplicação comunica mudança de estado **apenas visualmente**. Corrigir caso a caso trata
sintoma.

**Uma hipótese foi descartada.** Suspeitou-se que as colunas da tabela não expusessem o
papel `columnheader`. A observação vinha de uma árvore de acessibilidade que **achata a
marcação de tabela** e reportava as células como `generic`. Verificado contra o motor de
papéis ARIA do próprio Playwright: os 7 `<th>` **são** expostos como `columnheader`, apesar
de não declararem `scope`. **Não há defeito.** Achado que não se confirma não vira achado.

---

## Limitações conhecidas

**Da aplicação-alvo**

- Sem backend: nenhuma massa é persistida, nenhum teste pode validar persistência
- Sem API para semear ou limpar estado — o reset é o contexto novo
- 7 defeitos confirmados, nenhum corrigível por esta entrega

**Da análise automatizada de acessibilidade**

O axe-core cobre a parte do WCAG verificável por máquina — pela literatura da própria
Deque, entre 30% e 40% dos critérios. **Fora do alcance da ferramenta**, e portanto
exigindo validação manual:

| O que o axe não alcança | Achado correspondente |
|---|---|
| Se uma funcionalidade é operável por teclado | [ACH-04](./docs/achados.md#ach-04) |
| Se mudança de estado é anunciada a tecnologia assistiva | [ACH-05](./docs/achados.md#ach-05) |
| Se o erro de validação está ligado ao campo que o causou | [ACH-05](./docs/achados.md#ach-05) |
| Se a ordem de foco é coerente com a ordem visual | Não verificado |
| Se um modal cria armadilha de teclado | Não verificado |

E o inverso também ocorre: o axe **aprova** o campo de busca da tabela, cujo único rótulo é
o `placeholder` ([ACH-06](./docs/achados.md#ach-06)) — a ferramenta aceita `placeholder` no
cálculo do nome acessível, então o nome existe; a qualidade dele é que é ruim.

**`A11Y-06` é um portão de regressão, não um atestado de acessibilidade.** Cinco dos sete
achados desta entrega vieram de análise manual.

**Da responsividade**

Emulamos **resolução**, não dispositivo. Não marcamos `isMobile` nem `hasTouch`, porque
essas opções do Playwright são exclusivas do Chromium e quebrariam a execução em Firefox.
O que se valida em 390x844 é layout e alcance dos controles, **não** comportamento de toque.

`RSP-04` cobre scroll horizontal da página e a contenção do excesso no `.table-wrapper`.
Fica fora, e só foi verificado à mão: **sobreposição de elementos** (`RSP-03`), que exigiria
comparar as caixas de vários elementos entre si.

**Da suíte**

- A re-execução da jornada em 390x844 não aparece como teste na execução local padrão: é
  alvo explícito (`-Dgroups=mobile -Dviewport=mobile`) e *job* separado na pipeline.
  `RSP-04`, por ser cenário próprio, roda sempre
- A suíte roda sequencialmente, em ~35 s para 11 cenários em Chromium. A arquitetura suporta
  paralelismo — o estado por teste está no `Store` por teste —, mas exigiria uma chave de
  engine por thread, porque `Playwright` não é thread-safe. O §15 veda execução
  excessivamente paralela e nesta escala o ganho não justifica o risco
- Instabilidade observada **uma vez** na inicialização a frio do Firefox
  (`TargetClosedError`), não reproduzida nas execuções seguintes. Registrada em
  [`achados.md`](./docs/achados.md#instabilidade-observada-na-própria-suíte) em vez de
  escondida
- O Firefox custa caro para **abrir** (12 s nesta máquina, contra 4 s no Chromium), mas
  pouco para rodar depois que abriu (2,8 s por cenário). Com a engine aberta uma vez por
  execução, esse custo virou constante em vez de multiplicar-se por cenário — o que
  enfraquece a justificativa antiga de não rodar a suíte inteira nele, e abre a porta para
  ampliar o subconjunto `crossbrowser` quando houver razão de risco para isso

---

## Uso de Inteligência Artificial

Conforme exigido pelo enunciado (§11).

### Ferramenta

**Claude (Anthropic)**, via **Claude Code**, operado com a janela de contexto mantida em
torno de 250k tokens, para que o histórico de decisões permanecesse disponível dentro da
mesma sessão em vez de ser reconstruído a cada etapa.

### Em quais atividades foi utilizada

- **inspeção da aplicação**: automação de navegador para extrair o mapa de `data-testid`,
  medir o tempo do componente assíncrono, sondar o comportamento da validação do formulário
  e rodar o axe-core de calibração;
- **levantamento de hipóteses de defeito**, confirmadas ou refutadas uma a uma contra a
  aplicação — uma delas foi descartada;
- **redação do código da suíte**, dentro da arquitetura definida antes da geração;
- **redação da documentação** de `docs/`, deste README e do workflow da pipeline;
- **contraponto às decisões técnicas**: usada deliberadamente para levantar pontos de vista
  não considerados e para testar se cada escolha se sustentava antes de ser implementada.

### O que foi gerado ou sugerido pela ferramenta

O código da suíte e o texto da documentação foram redigidos com apoio da ferramenta, dentro
de uma arquitetura definida **antes** da geração. Código que fugisse desse desenho não
entrava.

**A arquitetura veio antes.** A divisão em camadas, a direção das dependências e o papel de
cada pacote foram desenhados pelo autor antes da implementação — o desenho está em
[`docs/desenhos-automacao.excalidraw`](./docs/desenhos-automacao.excalidraw) e aparece
transcrito na [estratégia de testes](./docs/estrategia-de-testes.md). A geração produziu
código dentro desse desenho.

O detalhamento de classe e de método **não** foi definido de antemão: veio na geração, e
cada trecho foi conferido pelo autor antes de ser aceito. A documentação de classes
([`docs/arquitetura-de-classes.md`](./docs/arquitetura-de-classes.md)) foi produzida
depois e serviu para **confirmar** que o resultado correspondia ao desenho de camadas
original.

### Como o conteúdo foi revisado e validado

A revisão aconteceu em três momentos.

**Antes da geração.** A arquitetura em camadas e a direção das dependências foram definidas
pelo autor. Código que fugisse desse desenho seria recusado.

**Na aceitação.** Nenhum trecho foi aceito sem ser lido e conferido. O aceite é do autor.

**Depois da geração.** Nada foi aceito por confiança na ferramenta. A validação foi, em
todos os casos, **confrontar a afirmação com a realidade**:

| O que foi validado | Como |
|---|---|
| Comportamento da aplicação | Inspecionado diretamente no DOM e no navegador, não presumido. Toda asserção da suíte tem origem em observação registrada |
| Versões das dependências | Conferidas no **Maven Central**, não na memória da ferramenta |
| API da biblioteca axe | Inspecionada com `javap` sobre o JAR, antes de escrever o teste |
| A suíte funciona | **Executada**: 11/11 em Chromium, subconjunto em Firefox, subconjunto em 390x844, relatório gerado |
| A evidência em falha funciona | **Falha induzida de propósito** (`-Dbase.url` apontando para uma rota 404) para confirmar que screenshot, trace e console são gravados e anexados |
| O bloqueio de download funciona | Verificado com `PLAYWRIGHT_BROWSERS_PATH` apontando para diretório vazio: a execução falhou com a mensagem acionável e o diretório **permaneceu vazio** |
| A hipótese do `columnheader` | **Refutada** com sonda sobre o motor de papéis ARIA do Playwright |

### O que a revisão pegou

Registrado porque é o argumento mais forte a favor de revisar o que a ferramenta produz —
e porque é a lista concreta do trabalho de conferência desta entrega:

1. **Um documento de contexto de sessão anterior, redigido com apoio de IA, continha três
   erros factuais sobre a aplicação**, que foram para o plano e tiveram de ser corrigidos
   contra a aplicação real: afirmava que o dropdown assíncrono *"não tem spinner"* (tem:
   `[data-testid=delayed-dropdown-loader]`), que o formulário *"limpa os campos"* no
   sucesso (não limpa — os campos permanecem preenchidos), e listava uma contagem de
   cenários acima da faixa do enunciado sob o título *"entre 5 e 8 testes"*, sem declarar o
   desvio. A suíte hoje tem 11 cenários e o desvio está declarado no
   [escopo implementado](#escopo-implementado).

2. **Um achado de acessibilidade não confirmado quase virou defeito.** A suspeita sobre o
   `columnheader` vinha de uma árvore de acessibilidade simplificada. Verificada contra o
   motor de papéis, foi **refutada**.

3. **Uma conclusão intermediária errada durante a inspeção.** A partir de uma única
   observação ("ADMIN" devolve zero resultados), concluiu-se que a busca era
   *case-sensitive*. A sonda sistemática com `Alice`, `alice`, `ALICE` e `aLiCe` mostrou o
   contrário: a busca é *case-insensitive* e simplesmente **não cobre** as colunas Role e
   Status.

4. **Um defeito latente no próprio código da suíte.** O `page.navigate("/")` inicial
   descartava silenciosamente qualquer caminho configurado em `base.url`, porque `/` é
   absoluto. Descoberto justamente ao tentar induzir uma falha — o teste *passou* com uma
   URL inválida. Corrigido para navegar à `base.url` completa.

5. **Erros mecânicos.** Comentário XML com `--` dentro, que torna o `pom.xml`
   impossível de parsear; e um caractere cirílico num comentário Java.

6. **Índice de busca do Maven Central desatualizado.** A API de busca reportava
   Playwright 1.52.0 como a mais recente, quando a 1.63.0 já estava publicada. Resolvido
   consultando o `maven-metadata.xml` diretamente.

7. **Uma afirmação falsa sobre a aplicação sobreviveu até ser testada.** A justificativa de
   `USR-08` dizia, em três documentos, que a exclusão de usuário "dispara diálogo nativo do
   navegador" e que por isso o cenário exigiria ciclo de vida próprio. Ao automatizar o
   cenário, um handler de `page.onDialog` registrado antes do clique **nunca foi acionado**:
   não existe confirmação nenhuma. A justificativa estava errada de duas formas — não há
   diálogo, e o `BrowserContext` por teste já resolvia a mutação de estado. O cenário entrou
   na suíte e o comportamento virou achado ([ACH-07](./docs/achados.md#ach-07)).

### O que foi alterado manualmente

A correção aconteceu **no aceite**, e não depois: trecho que divergisse da arquitetura ou
que não resistisse à conferência contra a aplicação não entrava. Os sete itens da seção
anterior são as alterações concretas — sete afirmações ou trechos recusados, corrigidos ou
refutados, cada um com o método de verificação registrado.

Não houve reescrita em bloco de código já aceito, porque a conferência era **condição de
aceite** e não uma etapa posterior. O esforço do autor concentrou-se na análise da
aplicação, no levantamento de risco, na priorização, no desenho das camadas e na
conferência de cada trecho contra a aplicação real.

### Quais decisões técnicas foram tomadas pelo autor

As decisões abaixo são do autor, tomadas a partir de análise de risco e de probabilidade,
e **implementadas** com apoio da ferramenta.

A ferramenta participou como **contraponto**: trouxe pontos de vista que não haviam sido
considerados, apresentou o custo de cada alternativa e testou se a justificativa se
sustentava sob questionamento. Isso fortaleceu a defesa de cada escolha — mas a escolha, e
a responsabilidade por ela, são do autor.

- **Stack Java em vez da preferencial TypeScript**, com a justificativa da apresentação
  técnica e o custo da reconstrução de runner, reporter e *fixtures* assumido
  conscientemente
- **Component Objects em vez de Page Objects**, pelo formato de página única da aplicação
- **Descartar a camada dedicada de validação**, por conflitar com a vedação do enunciado a
  abstrações que escondam a intenção do teste
- **Seleção e quantidade dos cenários**: cobrir a jornada obrigatória por inteiro, incluindo
  ordenação, paginação e exclusão — ainda que isso leve a suíte acima da faixa sugerida —, e
  tratar a re-execução mobile da jornada como **execução configurada**, reservando cenário
  próprio para o que só se verifica em tela estreita (`RSP-04`)
- **Cobrir o passo final da jornada obrigatória** (`USR-08`, exclusão), testando a
  justificativa que o mantinha fora em vez de aceitá-la — foi assim que se descobriu que a
  premissa do diálogo nativo era falsa
- **Nenhuma `waitForTimeout`**, como requisito não negociável da suíte
- **Nenhum commit automatizado**: cada alteração é revisada e commitada manualmente pelo
  autor, para que o histórico do repositório reflita entendimento e não geração
- **Nome do projeto e do pacote** (`com.samuellima.playlab`)
- **Repositório próprio para esta entrega**, em vez de reaproveitar o histórico de um
  desafio anterior

O autor é responsável por todo o conteúdo entregue e deve ser capaz de explicar, modificar
e depurar qualquer trecho, incluindo o produzido com apoio de IA.

---

## Próximos passos

Em ordem de valor. Detalhamento em
[`estrategia-de-testes.md`](./docs/estrategia-de-testes.md#próximos-passos).

1. **`A11Y-03` e `A11Y-04` — teclado e ordem de foco.** Devem entrar **junto com a correção**
   de [ACH-04](./docs/achados.md#ach-04), como teste de regressão
2. ***Ratchet* da baseline do axe.** Fazer a *baseline* só encolher — hoje ela perdoa a
   regra inteira, então uma violação de `color-contrast` num elemento novo do formulário
   passaria —, e ampliar o escopo para a tabela quando
   [ACH-02](./docs/achados.md#ach-02) for corrigido
3. **`RSP-03` — sobreposição de elementos em tela estreita.** O par `RSP-04` já está
   coberto; a sobreposição exigiria comparar as caixas de vários elementos entre si, e vale
   como cenário próprio e bem feito
4. **`FRM-02`, `FRM-04` e `FRM-05`.** Obrigatórios vazios, limite de caracteres e reset —
   baratos, porque os componentes já expõem o necessário
5. **Interceptação de rota (`page.route`) para a seção Network & API.** Cobre a seção sem
   depender de serviço de terceiro e habilita cenários de erro determinísticos
6. **Paralelismo**, quando a suíte passar de ~20 cenários — com chave de engine por thread,
   porque `Playwright` não é thread-safe
7. **Histórico no Allure**, para identificar teste intermitente antes que ele vire folclore
