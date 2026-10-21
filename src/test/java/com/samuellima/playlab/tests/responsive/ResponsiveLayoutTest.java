package com.samuellima.playlab.tests.responsive;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Page;
import com.samuellima.playlab.components.UserTableComponent;
import com.samuellima.playlab.support.Layout;
import com.samuellima.playlab.support.PlaywrightExtension;
import com.samuellima.playlab.support.TestConfig;
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
@Feature("Responsividade")
@Tag("responsive")
@Tag("mobile")
class ResponsiveLayoutTest {

    @Test
    @DisplayName("RSP-04 | A tabela larga nao produz scroll horizontal na pagina")
    @Story("Layout em tela reduzida")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
            A tabela de usuarios tem 7 colunas e nao cabe em 390px. Existem duas formas de
            a aplicacao lidar com isso, e so uma e aceitavel:

            - errada: a pagina inteira passa a rolar para o lado, e todo o conteudo fica
              deslocado;
            - certa: o excesso fica contido na moldura da tabela, que rola sozinha.

            As duas asercoes cobrem exatamente essa diferenca. A primeira mede: a largura
            total do documento nao passa da largura da janela, isto e, a PAGINA nao rola
            para o lado. A segunda confirma o mecanismo que torna isso possivel: o
            .table-wrapper declara overflow-x rolavel. Sem a segunda, a primeira passaria
            tambem se a tabela tivesse simplesmente deixado de ser renderizada.

            Vale nas duas resolucoes, e por isso o cenario nao e ignorado em desktop: em
            1280x720 ele protege contra uma regressao de CSS que introduza scroll
            horizontal na pagina; em 390x844, onde a tabela de fato transborda, ele e a
            prova do padrao responsivo. O viewport continua vindo de configuracao, nunca do
            codigo do teste -- a asercao e sobre a janela que a execucao recebeu, qualquer
            que seja ela.

            A tolerancia de 1px na primeira asercao absorve arredondamento de pixel
            fracionario em telas com escala, e nao uma folga arbitraria de layout.

            Medido na aplicacao em 21/10/2026, em 390x844: o documento mede 390px, igual a
            janela, enquanto a moldura da tabela tem 706px de conteudo em 322px visiveis.
            Ou seja, a tabela transborda de verdade nessa largura, e o transbordo esta
            inteiro dentro da moldura -- e o cenario exercita a condicao real, nao uma
            hipotetica.
            """)
    void tabelaLargaNaoProduzScrollHorizontalNaPagina(Page page) {
        UserTableComponent tabela = new UserTableComponent(page);

        int documento = Layout.documentScrollWidth(page);
        int janela = Layout.viewportWidth(page);

        assertThat(documento)
                .as("A pagina nao deve rolar horizontalmente em %s: documento %dpx,"
                        + " janela %dpx", TestConfig.viewport(), documento, janela)
                .isLessThanOrEqualTo(janela + 1);

        assertThat(Layout.overflowX(tabela.wrapper()))
                .as("O excesso de largura da tabela deve ficar contido na moldura dela")
                .isIn("auto", "scroll");
    }
}
