package com.samuellima.playlab.tests.forms;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.samuellima.playlab.components.RegistrationFormComponent;
import com.samuellima.playlab.data.RegistrationData;
import com.samuellima.playlab.data.RegistrationDataFactory;
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
@Feature("Cadastro de usuario")
@Tag("forms")
class RegistrationFormTest {

    private static final String MENSAGEM_SUCESSO = "Registration successful! Welcome aboard.";
    private static final String MENSAGEM_EMAIL_INVALIDO = "Valid email is required";

    @Test
    @DisplayName("FRM-01 | O cadastro com dados validos confirma o sucesso e nao acusa erro em campo nenhum")
    @Story("Cadastro valido")
    @Severity(SeverityLevel.BLOCKER)
    @Description("""
            Caminho feliz do formulario, com massa gerada pela fabrica para esta execucao,
            e-mail unico em dominio example.com (reservado pela RFC 2606 para teste).

            A asercao tem duas metades deliberadas. A primeira e a mensagem de sucesso.
            A segunda, menos obvia e mais importante, e que os quatro campos de erro
            continuam vazios: um formulario que confirma o cadastro e ao mesmo tempo
            acusa erro em algum campo esta quebrado, e so a segunda metade pega isso.
            """)
    @Tag("crossbrowser")
    void cadastroComDadosValidosConfirmaSucesso(Page page) {
        RegistrationFormComponent formulario = new RegistrationFormComponent(page);
        RegistrationData dados = RegistrationDataFactory.valido();

        formulario.fill(dados).submit();

        assertThat(formulario.successMessage()).isVisible();
        assertThat(formulario.successMessage()).containsText(MENSAGEM_SUCESSO);

        assertThat(formulario.fullNameError()).isEmpty();
        assertThat(formulario.emailError()).isEmpty();
        assertThat(formulario.passwordError()).isEmpty();
        assertThat(formulario.termsError()).isEmpty();
    }

    @Test
    @DisplayName("FRM-03 | O e-mail invalido bloqueia o envio e acusa o erro apenas no campo de e-mail")
    @Story("Validacao de e-mail")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            Cenario negativo do formulario. Todos os campos vao validos, exceto o e-mail,
            que vai sem arroba. Isso isola a regra sob teste: o unico motivo possivel de
            falha e a validacao de e-mail.

            A asercao e sobre o TEXTO da mensagem, nunca sobre a visibilidade dela. Os
            span de erro desta aplicacao estao sempre no DOM e sempre visiveis, apenas
            vazios enquanto nao ha erro, de modo que uma asercao de visibilidade passaria
            tambem com o formulario funcionando errado. Esse e o tipo de asercao generica
            que nao comprova comportamento.

            As tres asercoes finais provam o recorte: o erro apareceu no e-mail e em mais
            nenhum campo, e o sucesso nao foi exibido.
            """)
    void emailInvalidoBloqueiaEnvioEAcusaErroNoCampoCorreto(Page page) {
        RegistrationFormComponent formulario = new RegistrationFormComponent(page);
        RegistrationData dados = RegistrationDataFactory.valido();

        formulario.fill(dados)
                .fillEmail("email-invalido-sem-arroba")
                .submit();

        assertThat(formulario.emailError()).hasText(MENSAGEM_EMAIL_INVALIDO);
        assertThat(formulario.successMessage()).isHidden();

        assertThat(formulario.fullNameError()).isEmpty();
        assertThat(formulario.passwordError()).isEmpty();
        assertThat(formulario.termsError()).isEmpty();
    }
}
