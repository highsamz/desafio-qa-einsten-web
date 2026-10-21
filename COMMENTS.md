# COMMENTS.md — decisões e o porquê de cada uma

Este arquivo explica **as escolhas técnicas desta entrega em linguagem simples**. Não é um
resumo do README: o README diz *como rodar* e *o que a suíte faz*. Aqui eu digo **por que
está assim e não de outro jeito**, e o que eu abri mão em cada decisão.

Cada decisão segue o mesmo formato, de propósito:

> **O que eu fiz** · **Por que** · **O que custou**

Nenhuma escolha é de graça. Quando o custo não aparece, normalmente é porque ninguém
procurou.

As decisões abaixo são minhas, tomadas a partir de análise de risco e de probabilidade. A
implementação e a redação contaram com apoio de IA; como ela foi usada, revisada e validada
está detalhado em [Uso de Inteligência Artificial](./README.md#uso-de-inteligência-artificial).

---

## Índice

- [O projeto em uma página](#o-projeto-em-uma-página)
- [A decisão de arquitetura](#a-decisão-de-arquitetura)
- [As decisões técnicas, uma a uma](#as-decisões-técnicas-uma-a-uma)
- [Bibliotecas de terceiros](#bibliotecas-de-terceiros)
- [O que eu faria diferente com mais tempo](#o-que-eu-faria-diferente-com-mais-tempo)
- [O que não foi entregue, e por quê](#o-que-não-foi-entregue-e-por-quê)
- [Onde está cada coisa](#onde-está-cada-coisa)

---

## O projeto em uma página

É uma **suíte de testes automatizados** que abre um navegador de verdade e usa o site
[PlayLab](https://playwrightlab.github.io/) como se fosse uma pessoa: digita, clica,
pesquisa, ordena, pagina — e depois **julga** se o que apareceu na tela está certo.

São **11 cenários** — dois acima da faixa de 5 a 8 que o desafio sugere, porque era isso
que faltava para fechar a jornada obrigatória inteira. Não são 40, e isso é decisão, não
falta de tempo.

A aplicação tem 21 seções e centenas de componentes. Uma suíte que cobrisse tudo seria
lenta, frágil e ninguém manteria. Preferi um conjunto pequeno em que **cada cenário prova
um comportamento diferente e nenhum passa por acidente**.

A ordem do trabalho foi:

```
mapear jornadas  →  levantar riscos  →  priorizar  →  só então automatizar
```

E não "abrir o site e sair escrevendo teste do que apareceu na frente". O raciocínio inteiro
está em [`docs/matriz-de-riscos.md`](./docs/matriz-de-riscos.md) e
[`docs/matriz-de-cenarios.md`](./docs/matriz-de-cenarios.md).

Como subproduto da análise, encontrei **7 defeitos reais na aplicação** — e 5 deles nenhuma
ferramenta automatizada detecta. Estão em [`docs/achados.md`](./docs/achados.md).

---

## A decisão de arquitetura

São quatro pastas, e a regra entre elas cabe numa frase:

> **O teste decide o que é certo. O componente sabe onde as coisas estão. O suporte abre o
> navegador. Ninguém faz o trabalho do outro.**

```
tests/        os 11 cenários e TODAS as asserções
   ↓
components/   traduz "o campo de e-mail" no seletor real da página
   ↓
data/         os dados que o teste digita
   
support/      liga e desliga o navegador, lê configuração, guarda evidência
```

**As setas só apontam para baixo.** `tests` conhece todo mundo; `components` conhece `data`;
`support` não conhece nem componente nem teste. Nenhuma seta volta.

**Por que isso importa na prática:** antes de mexer em qualquer classe, eu sei o que posso
quebrar — só o que está *acima* dela no desenho. Sem essa regra, mudar um componente pode
quebrar um teste que eu nem abri, e aí a suíte vira um nó.

O desenho completo das classes, com métodos e relações, está em
[`docs/arquitetura-de-classes.md`](./docs/arquitetura-de-classes.md).

---

## As decisões técnicas, uma a uma

### 1. Java em vez de TypeScript

**O que eu fiz.** O enunciado define TypeScript como *stack* preferencial e permite outra
desde que justificada tecnicamente (§7.1). Escolhi Java.

**Por que.** Vai ser pedido para eu alterar e depurar o código **ao vivo**, na apresentação.
Profundidade na linguagem sustenta isso; familiaridade superficial com a *stack* preferencial
não. Código que eu não consigo modificar sob pressão é passivo, não é ativo.

Vale dizer o que **não** mudou: continua Playwright. Mesma engine, mesmo *auto-waiting*,
mesmo *trace viewer*, mesmo motor de seletores. Só o *binding* é Java.

**O que custou.** Muito, e eu sabia disso antes de começar. O *binding* Java **não** traz
test runner, relatório HTML, modelo de *fixtures* nem configuração de `projects`. Tive que
reconstruir tudo:

| O que o runner TypeScript dá de graça | Como eu reconstruí |
|---|---|
| Test runner | JUnit Jupiter |
| Relatório HTML | Allure |
| *Fixture* `page` | `ParameterResolver` do JUnit, em `PlaywrightExtension` |
| `projects` (navegador × viewport) | Configuração + `@Tag` + *jobs* separados na pipeline |
| Trace e screenshot em falha | `AfterTestExecutionCallback` em `PlaywrightExtension` |

---

### 2. Uma classe por **componente**, não por página

**O que eu fiz.** O padrão clássico é *Page Object*: uma classe por página. Usei **Component
Object**: uma classe por componente da tela.

**Por que.** O PlayLab é **uma página só** com dezenas de componentes independentes —
formulário, tabela, *dropdown*, abas, modais. Uma classe por página viraria um arquivo com
centenas de localizadores sem relação entre si. Ninguém consegue manter isso, nem achar nada
dentro.

O recorte virou o componente: o que está dentro do card da tabela é do
`UserTableComponent`, e nada mais.

**O que custou.** Mais arquivos. Em troca, cada um cabe na cabeça.

---

### 3. O componente **não** assevera

**O que eu fiz.** Nenhuma das três classes de componente tem um único `assertThat`. Elas
oferecem **ações** (`search("alice")`, `submit()`) e **localizadores** (`rows()`,
`emailError()`). Quem julga é o teste.

**Por que.** Duas razões.

A primeira é legibilidade: ler o teste basta para saber o que está sendo provado. Eu não
preciso abrir o componente para descobrir se alguma asserção está escondida lá dentro.

A segunda é técnica, e é a mais importante: os métodos devolvem `Locator`, **não** `String`.

Um `Locator` é uma **pergunta ainda não respondida** ("as linhas do corpo da tabela"), não a
resposta. A asserção do Playwright reavalia essa pergunta no DOM várias vezes até a condição
bater. Se o componente devolvesse texto já resolvido, entregaria o **retrato de um instante**
— e o teste de ordenação voltaria a depender de "ler a tabela depois que o JavaScript
terminou", que é a origem clássica de teste instável.

É a diferença entre uma suíte em que eu confio e uma que eu re-executo até passar.

**O que custou.** O teste fica um pouco mais verboso. Vale cada linha.

---

### 4. Nenhuma espera fixa. Nenhuma.

**O que eu fiz.** Não existe `Thread.sleep` nem `waitForTimeout` em lugar nenhum da suíte.
Dá para conferir com um `grep`.

**Por que.** Espera fixa é lenta quando a aplicação está rápida e insuficiente quando a
pipeline está carregada. É o pior dos dois mundos, e é a causa número um de teste que
"às vezes passa".

Toda espera é **por condição observável**. O *dropdown* assíncrono do PlayLab leva cerca de
2 segundos para carregar, e durante esse tempo a aplicação oferece quatro sinais diferentes —
o *loader* aparece, o `select` habilita, o texto do *placeholder* troca, o número de opções
vai de 1 para 6. O teste espera por esses sinais, nunca pelo relógio.

**Sobre os `timeout` de 10 s no `config.properties`:** eles **não** são espera fixa. São um
**teto**. O Playwright devolve o controle no instante em que a condição é satisfeita; esse
número só limita quanto tempo ele aceita esperar antes de desistir e falhar.

**O que custou.** Nada. Essa decisão só tem lado bom.

---

### 5. Uma janela anônima nova para cada teste — e um navegador só para todos

**O que eu fiz.** Cada cenário roda num `BrowserContext` novo — que é, na prática, uma janela
anônima: cookies, armazenamento e qualquer alteração de DOM morrem junto com ele. O
**navegador**, por outro lado, é aberto uma única vez por execução.

**Por que.** O PlayLab é estático e não tem *backend*. Não existe API para semear dados nem
para limpar. Então **o contexto novo é o reset**.

O resultado é que nenhum cenário depende da ordem de execução nem do resultado de outro. Eu
posso rodar um teste sozinho, ou todos, ou em ordem embaralhada — o resultado é o mesmo.

**A distinção importa, e eu já errei nela.** A primeira versão desta suíte abria um
navegador inteiro por teste e eu justificava isso como "o preço do isolamento". Estava
errado: quem isola é a janela anônima. O navegador novo não acrescentava isolamento nenhum
— só o tempo de abrir o Chromium, de novo, a cada cenário.

Corrigido: `Playwright` e `Browser` vivem no *store* **raiz** do JUnit, que dura a execução
inteira e fecha sozinho no fim; `BrowserContext` e `Page` continuam no *store* por teste.

**O que custou.** Tempo: cerca de 35 segundos para os 11 cenários no Chromium, contra 43
segundos na versão anterior — mesmos cenários, mesmo isolamento.

---

### 6. A `Page` chega como parâmetro, não por herança

**O que eu fiz.** Em vez de `class UserTableTest extends BaseTest`, a classe declara
`@ExtendWith(PlaywrightExtension.class)` e o método de teste recebe a página na assinatura:

```java
void pesquisaLocalizaUsuarioExistente(Page page) { ... }
```

**Por que.** Em Java, uma classe só pode ter **uma** superclasse. Se eu gastar esse espaço
único com `BaseTest`, ele acabou. E com herança o estado chega escondido, por campo herdado:
quem lê o teste não sabe de onde veio a `page`.

Com extensão, o teste **pede** o que precisa, e isso fica visível na assinatura. É exatamente
o modelo de *fixture* que o Playwright entrega pronto em TypeScript — eu só reconstruí em
Java.

**O que custou.** `PlaywrightExtension` é a classe mais densa do projeto. Em compensação, é a
única com essa complexidade: os 9 testes ficaram simples porque ela não ficou.

---

### 7. Seletores por `data-testid`, e índice de coluna descoberto na hora

**O que eu fiz.** Toda busca de elemento usa `data-testid`. Nunca XPath, nunca classe de CSS,
nunca posição fixa.

**Por que.** `data-testid` é um atributo que existe **para** ser usado por teste. Ele muda de
propósito, não por acidente de redesign. Classe de CSS muda quando alguém troca a cor do
botão; texto visível muda quando alguém revisa o português.

**O caso delicado:** para percorrer as células de uma coluna eu preciso de uma posição
(`td:nth-child(3)`). Mas esse `3` **não** está escrito no código. O componente percorre o
cabeçalho da tabela em tempo de execução, compara o `data-testid` de cada `th` e descobre o
índice.

Se a aplicação reordenar as colunas amanhã, o componente **acompanha** — em vez de passar a
ler a coluna errada e me dar um resultado falso, que é o pior tipo de falha porque ninguém
percebe.

**O que custou.** Umas 10 linhas a mais no componente (`columnIndex`). Compra o direito de
usar posição sem que ela seja frágil.

---

### 8. Massa gerada a cada execução, com fábrica própria

**O que eu fiz.** O e-mail de cada cadastro é único: `qa.playlab+<timestamp>-<n>@example.com`.
Nada de dado fixo no código.

**Por que.** Três motivos práticos:

1. o teste não passa a depender de um valor que alguém deixou na tela;
2. o e-mail é **rastreável na evidência** — ao olhar um *screenshot* de falha eu sei
   exatamente qual execução o produziu;
3. se a aplicação ganhar *backend* amanhã, a suíte não precisa mudar de estratégia para
   deixar de colidir com registro já existente.

**Sobre segurança:** não há dado real nem segredo aqui. O domínio `example.com` é reservado
pela **RFC 2606** justamente para documentação e teste, e não resolve para serviço nenhum. A
senha é um literal fictício que existe só para satisfazer a regra de tamanho mínimo de um
formulário público de demonstração.

**O que mudei em relação ao plano inicial.** Eu tinha previsto usar Java Faker. Com 9
cenários e um único tipo de massa, uma fábrica própria de 20 linhas entrega o mesmo e evita
trazer uma biblioteca inteira para o *classpath*. Troquei.

---

### 9. Duas bibliotecas de asserção, com fronteira clara

**O que eu fiz.** `PlaywrightAssertions` para o DOM; AssertJ para objetos em memória.

**Por que.** Não é indecisão, é divisão de trabalho:

| Biblioteca | Quando eu uso | Por quê |
|---|---|---|
| `PlaywrightAssertions` | estado da página | tem *retry*: reavalia o DOM até a condição bater |
| AssertJ | listas, números, objetos | a lista já está resolvida na memória; *retry* não faria sentido |

Na prática, AssertJ aparece em um lugar só: a lista de violações que o axe-core devolve.

**O que custou.** Uma dependência a mais e uma regra a explicar — que é esta tabela.

---

### 10. Acessibilidade com *baseline*, não com "zero violações"

**O que eu fiz.** O teste de acessibilidade não exige zero violações. Ele declara as
violações **já conhecidas** numa lista no código e falha diante de **qualquer violação
nova**.

**Por que.** Uma asserção de zero violações falharia hoje, no primeiro dia. E um teste que
nasce vermelho é um teste que alguém desliga na primeira semana — aí a proteção vira zero.

O recorte que usei é o que se usa para introduzir axe em código que já existe: congela o
conhecido, barra o novo. Os defeitos conhecidos são tratados por **correção**, que é o canal
certo, e não por asserção.

**Onde a análise roda — e isso também é decisão.** O PlayLab tem uma seção chamada
"Accessibility", construída para passar. Rodar o axe ali daria um verde bonito que **não
protege nada**. A análise roda no formulário de cadastro, que é componente real, em uso, e
onde um achado tem consequência.

**O que custou.** Honestidade sobre o alcance: `A11Y-06` é um **portão de regressão**, não um
atestado de acessibilidade. O axe cobre só a parte do WCAG verificável por máquina — entre
30% e 40% dos critérios, pela literatura da própria Deque. Ele não enxerga ordem de foco,
qualidade de texto alternativo, armadilha de teclado nem anúncio de mudança de estado.

Tanto que **5 dos 7 defeitos que encontrei vieram de análise manual**. E o caso mais
incômodo é o [ACH-06](./docs/achados.md): o axe **aprova** o campo de busca da tabela, cujo
único rótulo é o `placeholder`. A ferramenta aceita `placeholder` no cálculo do nome
acessível, então para ela o nome existe. A qualidade dele é que é ruim.

---

### 11. Evidência só quando falha

**O que eu fiz.** A gravação (*trace*) começa **sempre**, mas é descartada quando o teste
passa. Em caso de falha, eu guardo *screenshot* de página inteira, o `trace.zip` navegável e
o log do console da página.

**Por que.** Não dá para decidir **depois** da falha que eu gostaria de ter gravado. Então eu
gravo sempre e jogo fora o que não serve — assim o artefato da pipeline fica enxuto e a
evidência existe quando importa.

O log do console entra porque muita quebra de front **aparece primeiro lá**, antes de virar
sintoma visível na tela.

**Um detalhe que é fácil errar:** a captura roda em `afterTestExecution`, não em `afterEach`.
A diferença é que no primeiro a página **ainda está viva**; no segundo ela já pode ter sido
fechada, e a evidência sai vazia sem nenhum erro aparente.

**O que custou.** A gravação liga em todo teste, mesmo nos que passam. O custo é baixo e o
seguro é alto.

---

### 12. Navegador e resolução por configuração, não por teste parametrizado

**O que eu fiz.** Trocar de navegador é `-Dbrowser=firefox`. Trocar de resolução é
`-Dviewport=mobile`. Não existe `@ParameterizedTest` por navegador.

**Por que.** Um teste parametrizado por navegador **multiplicaria a contagem de cenários** da
suíte. 11 cenários × 3 navegadores viraria 33 — um número que não significa nada. O cenário continua sendo um; o que muda é **onde** ele
roda.

A configuração tem uma ordem de precedência que resolve um problema real:

| Prioridade | De onde vem | Exemplo |
|---|---|---|
| 1ª (ganha) | linha de comando | `mvn test -Dbrowser=firefox` |
| 2ª | variável de ambiente | `BROWSER=firefox` |
| 3ª | `config.properties` | `browser=chromium` |
| 4ª | padrão no código | `"chromium"` |

A pipeline sobrepõe valores sem editar arquivo nenhum (1ª), e a minha máquina continua
funcionando sem eu digitar nada (3ª).

**O que custou.** A execução mobile não aparece como teste na execução local padrão. É alvo
explícito e *job* separado na pipeline.

---

### 13. Sem política de *retry*

**O que eu fiz.** Teste que falha, falha. Não há re-execução automática.

**Por que.** *Retry* esconde instabilidade em vez de resolver. Numa suíte de 11 cenários que
eu mesmo escrevi, uma falha intermitente é informação valiosa — e eu quero vê-la, não
mascará-la.

Aconteceu **uma vez**, na inicialização a frio do Firefox (`TargetClosedError`), e não
reproduziu nas execuções seguintes. Registrei em [`achados.md`](./docs/achados.md) em vez de
esconder atrás de um `retry: 2`.

**O que custou.** A pipeline pode ficar vermelha por um motivo que não é defeito do produto.
Prefiro assim: é visível, e portanto tratável.

---

### 14. Java 21, e não a LTS seguinte

**O que eu fiz.** O `pom.xml` compila para **Java 21**. A pipeline usa a mesma versão.

**Por que.** A suíte não usa nenhum recurso posterior ao 21 — os `record`, `switch` com seta
e blocos de texto que aparecem no código entraram antes disso. Fixar a LTS mais conservadora
que atende ao código amplia o número de máquinas onde a entrega roda sem ajuste.

> **Nota de revisão.** O meu planejamento inicial previa Java 25 — está escrito no
> [desenho de camadas](./docs/desenhos-automacao.excalidraw), feito antes da implementação.
> A implementação ficou no 21, e o `pom.xml`, o `JAVA_VERSION` da pipeline, o README e a
> estratégia de testes dizem 21 — não existe divergência entre o que a documentação afirma
> e o que a execução faz.

**O que custou.** Nada nesta entrega.

---

## Bibliotecas de terceiros

Todas as versões são fixadas em `<properties>` no [`pom.xml`](./pom.xml). **Nenhuma usa
faixa aberta**, para que a mesma revisão do repositório produza a mesma execução daqui a
meses.

| Dependência | Versão | Para quê |
|---|---|---|
| `com.microsoft.playwright:playwright` | 1.63.0 | Automação do navegador — *binding* oficial da Microsoft |
| `org.junit.jupiter:junit-jupiter` | 6.1.3 | Executor dos testes (o Playwright Java não traz um) |
| `io.qameta.allure:allure-jupiter` | 2.35.5 | Relatório HTML com as evidências anexadas |
| `com.deque.html.axe-core:playwright` | 4.14.0 | Análise automatizada de acessibilidade |
| `org.assertj:assertj-core` | 3.27.7 | Asserções sobre listas e objetos em memória |
| `org.slf4j:slf4j-nop` | 2.0.20 | Silencia o aviso de SLF4J sem log em toda execução |

Plugins: Surefire 3.6.0 (execução e filtro por `@Tag`), `exec-maven-plugin` 3.6.4 (instalação
dos navegadores), `allure-maven` 2.15.2 (geração do relatório) e `aspectjweaver` 1.9.25
(necessário para o Allure capturar os `@Step`).

**Três decisões de dependência que não são óbvias:**

**Uso BOMs** (`junit-bom` e `allure-bom`). Eles alinham todos os módulos de cada família numa
versão só. Sem isso é fácil acabar com `junit-jupiter-api` numa versão e `junit-jupiter-engine`
noutra, e descobrir isso num erro de execução confuso.

**Excluo o JUnit 4 que o axe-core arrasta.** Com JUnit 4 no *classpath*, o Surefire pode
escolher o *provider* errado e **ignorar os testes Jupiter em silêncio** — a suíte "passa"
tendo rodado zero testes. É um erro caro justamente porque fica verde.

**Excluo também o Playwright transitivo do axe-core.** O axe 4.14.0 declara a 1.63.0, que é a
mesma que eu uso. Declarar a minha diretamente faz com que a versão da engine seja decisão
explícita deste `pom.xml`, e não efeito colateral de uma dependência de terceiro.

---

## O que eu faria diferente com mais tempo

Em ordem de valor. O detalhamento está em
[`docs/estrategia-de-testes.md`](./docs/estrategia-de-testes.md).

**1. Teclado e ordem de foco (`A11Y-03`, `A11Y-04`).** Devem entrar **junto com a correção**
do [ACH-04](./docs/achados.md), como teste de regressão. Escrevê-los agora criaria um teste
que nasce vermelho — exatamente o que eu critiquei na decisão 10.

**2. *Ratchet* da baseline do axe.** Hoje a *baseline* perdoa a **regra** inteira, não o nó.
Se alguém introduzir contraste ruim num elemento novo do formulário, meu portão não acusa.
O passo é torná-la nó-a-nó e só **encolher**: uma vez corrigida uma violação, ela não pode
voltar.

**3. Sobreposição de elementos em tela estreita (`RSP-03`).** O `RSP-04` já cobre o scroll
horizontal. A sobreposição é outra história: exige comparar as caixas de vários elementos
entre si, e isso merece um cenário próprio e bem feito, não um apêndice.

**4. `FRM-02`, `FRM-04` e `FRM-05`.** Campos obrigatórios vazios, limite de caracteres e
*reset*. São baratos, porque o componente já expõe tudo o que eles precisam.

**5. Interceptação de rota (`page.route`).** Cobriria a seção *Network & API* sem depender de
serviço de terceiro, e habilitaria cenários de erro determinísticos.

**6. Paralelismo.** O estado por teste já fica no `Store` por teste. Falta uma chave de
engine por thread, porque o objeto `Playwright` não é thread-safe. E o §15 veda execução
excessivamente paralela, então entra quando a suíte passar de uns 20 cenários.

**7. Histórico no Allure.** Para identificar teste intermitente **antes** que ele vire
folclore do time.

---

## O que não foi entregue, e por quê

Transparência vale mais que a aparência de completude.

**Da aplicação-alvo:**

- Sem *backend*, nenhuma massa é persistida — **nenhum teste pode validar persistência**
- Sem API para semear ou limpar estado; o reset é o contexto novo
- Os 7 defeitos encontrados não são corrigíveis por esta entrega: o código é de terceiro

**Da responsividade:** eu emulo **resolução**, não **dispositivo**. Não marco `isMobile` nem
`hasTouch`, porque essas opções do Playwright só funcionam no Chromium e quebrariam a
execução em Firefox. Então o que eu valido em 390×844 é *layout* e alcance dos controles —
**não** comportamento de toque. E dentro de layout, só o scroll horizontal (`RSP-04`):
sobreposição de elementos (`RSP-03`) continua verificada à mão.

**Da compatibilidade:** o Firefox roda um subconjunto-âncora, não a suíte inteira. Ele é 3 a
4× mais lento que o Chromium nesta máquina, e o ganho de rodar tudo não paga o tempo.

**Da acessibilidade:** já dito na decisão 10, mas repito porque é importante — `A11Y-06` é
portão de regressão, não atestado.

**Fora de escopo por decisão:** seção *Network & API*, seção *Flaky & Retry*, *drag and
drop*, *hover*, menu de contexto, *Shadow DOM*, *iframes*, *upload*, *date picker*, *wizard*
e teste visual. A justificativa de cada um está na
[matriz de cenários](./docs/matriz-de-cenarios.md).

**Vedado pelo enunciado (§15):** teste de carga, varredura de segurança e exploração de
vulnerabilidade.

**Uma coisa que estava aqui e saiu.** A exclusão de usuário (`USR-08`) estava nesta lista,
com a justificativa de que exigiria tratar diálogo nativo e um ciclo de vida próprio. Fui
verificar antes de defender: **não existe diálogo nenhum** — a exclusão é imediata, e o
contexto novo por teste já cobria a mutação de estado. A justificativa era uma premissa não
verificada, o custo real era um método e um teste, e o cenário entrou. O comportamento
descoberto virou achado ([ACH-07](./docs/achados.md)). Custo presumido também precisa de
evidência.

---

## Onde está cada coisa

| Quero saber… | Vou em |
|---|---|
| como rodar | [`README.md`](./README.md) |
| por que esses 11 cenários | [`docs/matriz-de-cenarios.md`](./docs/matriz-de-cenarios.md) |
| de onde veio a priorização | [`docs/matriz-de-riscos.md`](./docs/matriz-de-riscos.md) |
| a estratégia completa | [`docs/estrategia-de-testes.md`](./docs/estrategia-de-testes.md) |
| os 7 defeitos encontrados | [`docs/achados.md`](./docs/achados.md) |
| as classes, métodos e relações | [`docs/arquitetura-de-classes.md`](./docs/arquitetura-de-classes.md) |
| o desenho das camadas, feito antes do código | [`docs/desenhos-automacao.excalidraw`](./docs/desenhos-automacao.excalidraw) |
| como a IA foi usada, revisada e validada | [`README.md`](./README.md#uso-de-inteligência-artificial) |
