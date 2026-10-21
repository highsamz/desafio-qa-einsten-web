package com.samuellima.playlab.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class DelayedDropdownComponent {

    private static final String PLACEHOLDER_INICIAL = "Click button to load...";
    private static final String PLACEHOLDER_CARREGADO = "Select a category...";

    private final Locator root;
    private final Locator loadButton;
    private final Locator select;
    private final Locator loader;
    private final Locator result;

    public DelayedDropdownComponent(Page page) {
        this.root = page.getByTestId("delayed-dropdown-card");
        this.loadButton = page.getByTestId("load-delayed-dropdown-btn");
        this.select = page.getByTestId("delayed-select");
        this.loader = page.getByTestId("delayed-dropdown-loader");
        this.result = page.getByTestId("delayed-dropdown-result");
    }


    @Step("Disparar o carregamento das opcoes")
    public DelayedDropdownComponent load() {
        loadButton.click();
        return this;
    }

    @Step("Selecionar a opcao \"{rotulo}\"")
    public DelayedDropdownComponent selectByLabel(String rotulo) {
        select.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(rotulo));
        return this;
    }

    public Locator root() {
        return root;
    }

    public Locator loadButton() {
        return loadButton;
    }

    public Locator select() {
        return select;
    }

    public Locator loader() {
        return loader;
    }

    public Locator options() {
        return select.locator("option");
    }

    public Locator selectableOptions() {
        return select.locator("option:not([value=''])");
    }

    public Locator result() {
        return result;
    }

    public String placeholderInicial() {
        return PLACEHOLDER_INICIAL;
    }

    public String placeholderCarregado() {
        return PLACEHOLDER_CARREGADO;
    }
}
