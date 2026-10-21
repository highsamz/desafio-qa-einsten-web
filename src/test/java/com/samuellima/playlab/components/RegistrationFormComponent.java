package com.samuellima.playlab.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.samuellima.playlab.data.RegistrationData;
import io.qameta.allure.Step;

public class RegistrationFormComponent {

    private final Locator root;
    private final Locator fullName;
    private final Locator email;
    private final Locator password;
    private final Locator phone;
    private final Locator bio;
    private final Locator country;
    private final Locator terms;
    private final Locator submitButton;
    private final Locator successMessage;

    public RegistrationFormComponent(Page page) {
        this.root = page.getByTestId("registration-form");
        this.fullName = page.getByTestId("input-fullname");
        this.email = page.getByTestId("input-email");
        this.password = page.getByTestId("input-password");
        this.phone = page.getByTestId("input-phone");
        this.bio = page.getByTestId("textarea-bio");
        this.country = page.getByTestId("select-country");
        this.terms = page.getByTestId("check-terms");
        this.submitButton = page.getByTestId("btn-register");
        this.successMessage = page.getByTestId("form-success");
    }


    @Step("Preencher o formulario de cadastro")
    public RegistrationFormComponent fill(RegistrationData dados) {
        fullName.fill(dados.fullName());
        email.fill(dados.email());
        password.fill(dados.password());
        phone.fill(dados.phone());
        bio.fill(dados.bio());
        country.selectOption(dados.countryCode());
        terms.check();
        return this;
    }

    @Step("Informar o e-mail \"{valor}\"")
    public RegistrationFormComponent fillEmail(String valor) {
        email.fill(valor);
        return this;
    }

    @Step("Enviar o formulario de cadastro")
    public RegistrationFormComponent submit() {
        submitButton.click();
        return this;
    }

    public Locator successMessage() {
        return successMessage;
    }

    public Locator fullNameError() {
        return root.getByTestId("error-fullname");
    }

    public Locator emailError() {
        return root.getByTestId("error-email");
    }

    public Locator passwordError() {
        return root.getByTestId("error-password");
    }

    public Locator termsError() {
        return root.getByTestId("error-terms");
    }
}
