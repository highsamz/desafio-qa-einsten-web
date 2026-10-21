package com.samuellima.playlab.tests.accessibility;

import static org.assertj.core.api.Assertions.assertThat;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Page;
import com.samuellima.playlab.support.PlaywrightExtension;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightExtension.class)
@Feature("Acessibilidade")
@Tag("a11y")
class AccessibilityTest {

    private static final String ESCOPO = "[data-testid='registration-card']";

    private static final List<String> TAGS_WCAG =
            List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa");

    private static final Set<String> VIOLACOES_CONHECIDAS = Set.of("color-contrast");

    @Test
    @DisplayName("A11Y-06 | A analise automatizada do formulario nao acusa violacao de acessibilidade nova")
    @Story("Analise automatizada com axe-core")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            Executa o axe-core sobre o card do formulario de cadastro, com as regras das
            tags WCAG 2.0 A/AA e WCAG 2.1 A/AA, e falha diante de qualquer violacao que
            nao esteja na baseline declarada no codigo.

            O resultado completo e anexado ao relatorio em toda execucao, passando ou
            falhando, para que a evidencia de acessibilidade exista mesmo quando nada
            quebrou.

            Escopo deliberado: a analise NAO cobre a tabela de usuarios. A tabela tem
            violacoes criticas conhecidas (controles sem nome acessivel: setas de
            paginacao, caixas de selecao e os dois selects dos controles da tabela),
            registradas como ACH-02. Colocar o gate ali deixaria a suite vermelha desde o
            primeiro dia sem proteger regressao nenhuma. Esses defeitos sao entregues como
            achado, que e o canal correto.

            Limitacoes da ferramenta, detalhadas no README: o axe cobre a parte
            verificavel por maquina do WCAG e nao alcanca ordem de foco, qualidade de
            texto alternativo, armadilha de teclado nem anuncio de mudanca de estado. Os
            achados ACH-01, ACH-04 e ACH-05 desta entrega sairam de analise manual
            exatamente porque o axe nao os detecta, e o ACH-06 e o caso mais incomodo:
            a ferramenta aprova e o problema existe.
            """)
    void analiseAutomatizadaDoFormularioNaoAcusaViolacaoNova(Page page) {
        AxeResults resultado = new AxeBuilder(page)
                .include(ESCOPO)
                .withTags(TAGS_WCAG)
                .analyze();

        anexarRelatorio(resultado);

        assertThat(resultado.isErrored())
                .as("O axe-core nao conseguiu executar: %s", resultado.getErrorMessage())
                .isFalse();

        List<Rule> novas = resultado.getViolations().stream()
                .filter(regra -> !VIOLACOES_CONHECIDAS.contains(regra.getId()))
                .toList();

        assertThat(novas)
                .as("Violacoes de acessibilidade novas em %s:%n%s", ESCOPO, descrever(novas))
                .isEmpty();
    }

    private void anexarRelatorio(AxeResults resultado) {
        String relatorio = """
                Escopo analisado : %s
                URL              : %s
                axe-core         : %s
                Regras aprovadas : %d
                Violacoes        : %d
                Inconclusivas    : %d

                --- violacoes ---
                %s
                --- inconclusivas (exigem verificacao manual) ---
                %s
                """.formatted(
                ESCOPO,
                resultado.getUrl(),
                resultado.getTestEngine() == null ? "?" : resultado.getTestEngine().getVersion(),
                resultado.getPasses().size(),
                resultado.getViolations().size(),
                resultado.getIncomplete().size(),
                descrever(resultado.getViolations()),
                descrever(resultado.getIncomplete()));

        Allure.getLifecycle().addAttachment(
                "Analise de acessibilidade (axe-core)", "text/plain", "txt",
                relatorio.getBytes(StandardCharsets.UTF_8));
    }

    private String descrever(List<Rule> regras) {
        if (regras.isEmpty()) {
            return "(nenhuma)";
        }
        return regras.stream()
                .map(regra -> """
                        [%s] %s
                          regra     : %s
                          baseline  : %s
                          elementos : %s
                          referencia: %s"""
                        .formatted(
                                regra.getImpact(),
                                regra.getHelp(),
                                regra.getId(),
                                VIOLACOES_CONHECIDAS.contains(regra.getId()) ? "sim" : "NAO",
                                regra.getNodes().stream()
                                        .map(no -> String.valueOf(no.getTarget()))
                                        .collect(Collectors.joining(", ")),
                                regra.getHelpUrl()))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
