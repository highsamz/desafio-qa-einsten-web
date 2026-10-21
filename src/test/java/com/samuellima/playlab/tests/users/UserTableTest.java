package com.samuellima.playlab.tests.users;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.samuellima.playlab.components.UserTableComponent;
import com.samuellima.playlab.components.UserTableComponent.Column;
import com.samuellima.playlab.support.PlaywrightExtension;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightExtension.class)
@Feature("Gerenciamento de usuarios")
@Tag("users")
class UserTableTest {

    @Test
    @DisplayName("USR-01 | A tabela de usuarios carrega com a estrutura e os dados esperados")
    @Story("Carregamento e estrutura da tabela")
    @Severity(SeverityLevel.BLOCKER)
    @Description("""
            Primeiro teste da jornada: se a tabela nao carrega com a estrutura certa,
            nenhum outro cenario de tabela tem valor. Assevera o contrato visivel do
            componente: as seis colunas nomeadas, quais delas oferecem ordenacao, o
            tamanho da pagina, o contador de registros e o dado do primeiro usuario.

            A diferenca entre ID e Role aqui nao e cosmetica. Na aplicacao, so as colunas
            ordenaveis exibem o indicador; Role nao exibe. Asseverar essa assimetria
            protege contra a regressao em que a coluna perde (ou ganha) a ordenacao sem
            que ninguem perceba.
            """)
    @Tag("crossbrowser")
    @Tag("mobile")
    void tabelaCarregaComEstruturaEDadosEsperados(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        assertThat(tabela.columnHeader(Column.ID)).containsText("ID");
        assertThat(tabela.columnHeader(Column.NAME)).containsText("Name");
        assertThat(tabela.columnHeader(Column.EMAIL)).containsText("Email");
        assertThat(tabela.columnHeader(Column.ROLE)).containsText("Role");
        assertThat(tabela.columnHeader(Column.STATUS)).containsText("Status");
        assertThat(tabela.columnHeader(Column.ACTIONS)).containsText("Actions");

        assertThat(tabela.columnHeader(Column.ID)).containsText("↕");
        assertThat(tabela.columnHeader(Column.ROLE)).not().containsText("↕");

        assertThat(tabela.rows()).hasCount(5);
        assertThat(tabela.summary()).hasText("Showing 1-5 of 15 entries");
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"1", "2", "3", "4", "5"});

        assertThat(tabela.rowForUserId(1)).containsText("Alice Johnson");
        assertThat(tabela.rowForUserId(1)).containsText("alice@example.com");
    }

    @Test
    @DisplayName("USR-03 | A pesquisa localiza um usuario existente ignorando maiusculas")
    @Story("Localizar usuario")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            Jornada critica da tabela: encontrar uma pessoa. O termo e informado em
            minusculas de proposito, contra um nome que esta capitalizado na aplicacao
            ("alice" contra "Alice Johnson"). Uma asercao so passa a provar duas coisas:
            que a pesquisa filtra, e que ela normaliza maiusculas.

            Este e um dos testes que a pipeline re-executa em 390x844. Como o
            preenchimento e o clique do Playwright exigem que o elemento esteja visivel e
            habilitado, interagir com o campo nessa largura ja prova que o controle
            permanece alcancavel em tela reduzida.
            """)
    @Tag("mobile")
    void pesquisaLocalizaUsuarioExistente(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        tabela.search("alice");

        assertThat(tabela.rows()).hasCount(1);
        assertThat(tabela.summary()).hasText("Showing 1-1 of 1 entries");
        assertThat(tabela.rowForUserId(1)).containsText("Alice Johnson");
        assertThat(tabela.rowForUserId(1)).containsText("alice@example.com");
    }

    @Test
    @DisplayName("USR-04 | A pesquisa sem correspondencia esvazia a tabela e zera o contador")
    @Story("Localizar usuario")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
            Cenario negativo da tabela. A aplicacao nao exibe mensagem alguma de estado
            vazio: o corpo da tabela simplesmente fica sem linhas, e o unico sinal
            observavel e o contador indo para zero. Por isso a asercao e dupla, zero
            linhas E contador zerado. Asseverar so a contagem de linhas seria fraco:
            passaria tambem se a tabela tivesse quebrado e deixado de renderizar.

            A ausencia de mensagem de vazio esta registrada como achado (ACH-01) e NAO e
            asseverada aqui. Asseverar a ausencia transformaria a correcao do defeito em
            quebra de teste, que e o avesso do que um teste deve fazer.
            """)
    void pesquisaSemCorrespondenciaEsvaziaTabela(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        tabela.search("zzzzz");

        assertThat(tabela.rows()).hasCount(0);
        assertThat(tabela.summary()).hasText("Showing 1-0 of 0 entries");

        assertThat(tabela.previousPageButton()).isDisabled();
        assertThat(tabela.nextPageButton()).isDisabled();
    }

    @Test
    @DisplayName("USR-05 | A ordenacao por ID e numerica e alterna entre crescente e decrescente")
    @Story("Ordenar registros")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            Ordenar por ID e o cenario onde mora o defeito classico: ordenacao
            lexicografica aplicada a numero. Se a aplicacao ordenasse como texto, o
            decrescente comecaria em "9", porque "9" > "15" na comparacao de strings.
            Asseverar que a primeira pagina do decrescente e exatamente 15, 14, 13, 12, 11
            prova que a comparacao e numerica, e nao apenas que "alguma coisa mudou".

            A asercao e feita sobre o Locator da coluna inteira, com hasText recebendo o
            array na ordem esperada. Isso mantem o retry do Playwright reavaliando a
            tabela ate o JS de ordenacao terminar, sem nenhuma espera fixa.
            """)
    void ordenacaoPorIdEhNumericaEAlterna(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        tabela.sortBy(Column.ID);
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"1", "2", "3", "4", "5"});

        tabela.sortBy(Column.ID);
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"15", "14", "13", "12", "11"});

        assertThat(tabela.summary()).hasText("Showing 1-5 of 15 entries");
    }

    @Test
    @DisplayName("USR-06 | A navegacao entre paginas percorre os 15 registros sem repetir nem pular")
    @Story("Navegar entre paginas")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            Ultimo passo da jornada obrigatoria. Nao basta que o clique troque a pagina:
            o teste assevera que cada pagina traz a faixa correta de IDs e que o contador
            acompanha, o que em conjunto prova que nenhum registro e repetido nem perdido
            na travessia das tres paginas.

            Assevera tambem os extremos, que e onde a paginacao costuma errar: "anterior"
            desabilitado na primeira pagina e "proxima" desabilitado na ultima.
            """)
    void navegacaoEntrePaginasPercorreTodosOsRegistros(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        assertThat(tabela.previousPageButton()).isDisabled();
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"1", "2", "3", "4", "5"});

        tabela.goToPage(2);
        assertThat(tabela.summary()).hasText("Showing 6-10 of 15 entries");
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"6", "7", "8", "9", "10"});
        assertThat(tabela.previousPageButton()).isEnabled();

        tabela.goToNextPage();
        assertThat(tabela.summary()).hasText("Showing 11-15 of 15 entries");
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"11", "12", "13", "14", "15"});

        assertThat(tabela.nextPageButton()).isDisabled();
    }

    @Test
    @DisplayName("USR-08 | A exclusao remove o usuario correto, preserva os vizinhos e atualiza o contador")
    @Story("Executar acao na tabela")
    @Severity(SeverityLevel.BLOCKER)
    @Description("""
            Passo final da jornada obrigatoria: executar uma acao disponivel na tabela e
            validar o resultado da operacao. E o cenario que protege o unico risco
            classificado como critico na matriz (RF-12): apagar o registro errado.

            A exclusao e imediata. Nao ha dialogo de confirmacao, nativo nem proprio:
            verificado em 21/10/2026 com um handler de page.onDialog registrado antes do
            clique, que nunca foi acionado. Esta registrado como achado (ACH-07) --
            acao destrutiva sem confirmacao e sem desfazer. O teste assevera o
            comportamento que a aplicacao tem, nao o que ela deveria ter.

            Quatro asercoes, cada uma cobrindo um modo de falha diferente:

            1. a linha do usuario 3 desaparece: a acao surtiu efeito;
            2. os vizinhos 2 e 4 continuam presentes. Esta e a asercao que diferencia
               "removeu" de "removeu o certo" -- sem ela, o teste passaria tambem se a
               exclusao tivesse esvaziado a tabela inteira;
            3. a primeira pagina passa a exibir 1, 2, 4, 5, 6: a exclusao nao deixou
               lacuna, a pagina se recompos puxando o primeiro registro da pagina
               seguinte;
            4. o contador vai de 15 para 14: o total acompanhou a remocao.

            O estado nao precisa ser restaurado. Cada teste roda em BrowserContext novo e
            a aplicacao nao persiste nada, de modo que os 15 registros voltam na proxima
            execucao -- o isolamento da suite cobre a mutacao, sem ciclo de vida especial
            para este cenario.
            """)
    void exclusaoRemoveUsuarioCorretoEAtualizaContador(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        assertThat(tabela.rowForUserId(3)).isVisible();
        assertThat(tabela.summary()).hasText("Showing 1-5 of 15 entries");

        tabela.deleteUser(3);

        assertThat(tabela.rowForUserId(3)).hasCount(0);
        assertThat(tabela.rowForUserId(2)).isVisible();
        assertThat(tabela.rowForUserId(4)).isVisible();
        assertThat(tabela.columnCells(Column.ID))
                .hasText(new String[] {"1", "2", "4", "5", "6"});
        assertThat(tabela.summary()).hasText("Showing 1-5 of 14 entries");
    }
}
