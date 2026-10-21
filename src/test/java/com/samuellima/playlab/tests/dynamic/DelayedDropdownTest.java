package com.samuellima.playlab.tests.dynamic;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.samuellima.playlab.components.DelayedDropdownComponent;
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
@Feature("Componentes assincronos")
@Tag("dynamic")
class DelayedDropdownTest {

    @Test
    @DisplayName("DYN-01 | O dropdown assincrono sinaliza carregamento, habilita as opcoes e registra a selecao")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Carregamento atrasado de opcoes")
    @Description("""
            O carregamento leva cerca de 2 segundos (medido na aplicacao em 20/10/2026) e
            nao envolve rede: e um temporizador no proprio JS. O teste percorre o ciclo
            completo em tres etapas, cada uma esperando por um estado diferente.

            1. Estado inicial. O select chega desabilitado, com uma unica opcao de
               placeholder, e o loader oculto. Asseverar isso primeiro importa porque
               define o ponto de partida: sem essa asercao, um select que viesse pronto
               faria o resto do teste passar sem ter exercitado assincronia nenhuma.

            2. Estado de carregamento. Asseverado imediatamente apos o clique, enquanto a
               janela de 2 segundos esta aberta. E uma asercao sobre estado transitorio,
               e esta aqui de proposito: o loader e a unica confirmacao que o usuario
               recebe de que algo esta acontecendo.

            3. Estado carregado. Quatro condicoes independentes mudam, e o teste assevera
               todas: o loader volta a ocultar, o select habilita, o placeholder troca de
               texto e as cinco opcoes reais aparecem. A contagem de opcoes e a asercao
               mais forte do conjunto, porque nenhuma delas passa por acidente.

            Por fim, a selecao e refletida na area de resultado, provando que a opcao
            carregada dinamicamente e de fato selecionavel.
            """)
    void dropdownAssincronoCarregaEPermiteSelecao(Page page) {
        DelayedDropdownComponent dropdown = new DelayedDropdownComponent(page);

        assertThat(dropdown.select()).isDisabled();
        assertThat(dropdown.options()).hasCount(1);
        assertThat(dropdown.options().first()).hasText(dropdown.placeholderInicial());
        assertThat(dropdown.loader()).isHidden();

        dropdown.load();

        assertThat(dropdown.loader()).isVisible();
        assertThat(dropdown.loadButton()).isDisabled();

        assertThat(dropdown.selectableOptions()).hasCount(5);
        assertThat(dropdown.select()).isEnabled();
        assertThat(dropdown.loader()).isHidden();
        assertThat(dropdown.options().first()).hasText(dropdown.placeholderCarregado());

        dropdown.selectByLabel("Electronics");

        assertThat(dropdown.select()).hasValue("electronics");
        assertThat(dropdown.result()).hasText("Selected: Electronics");
    }
}
