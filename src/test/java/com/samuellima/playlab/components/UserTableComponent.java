package com.samuellima.playlab.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class UserTableComponent {

    private final Locator root;
    private final Locator table;
    private final Locator rows;
    private final Locator searchField;
    private final Locator summary;
    private final Locator pagination;

    public UserTableComponent(Page page) {
        this.root = page.getByTestId("table-card");
        this.table = page.getByTestId("data-table");
        this.rows = page.getByTestId("table-body").locator("tr");
        this.searchField = page.getByTestId("table-search");
        this.summary = page.getByTestId("table-info");
        this.pagination = page.getByTestId("pagination");
    }

    public enum Column {
        ID("sort-id", true),
        NAME("sort-name", true),
        EMAIL("sort-email", true),
        ROLE("col-role", false),
        STATUS("sort-status", true),
        ACTIONS("col-actions", false);

        private final String testId;
        private final boolean sortable;

        Column(String testId, boolean sortable) {
            this.testId = testId;
            this.sortable = sortable;
        }

        public String testId() {
            return testId;
        }

        public boolean sortable() {
            return sortable;
        }
    }

    @Step("Pesquisar usuarios por \"{termo}\"")
    public UserTableComponent search(String termo) {
        searchField.fill(termo);
        return this;
    }

    @Step("Ordenar pela coluna {coluna}")
    public UserTableComponent sortBy(Column coluna) {
        if (!coluna.sortable()) {
            throw new IllegalArgumentException(
                    "A coluna " + coluna + " nao e ordenavel na aplicacao.");
        }
        columnHeader(coluna).click();
        return this;
    }

    @Step("Excluir o usuario de id {userId}")
    public UserTableComponent deleteUser(int userId) {
        deleteButton(userId).click();
        return this;
    }

    @Step("Ir para a pagina {numero} da tabela")
    public UserTableComponent goToPage(int numero) {
        pageButton(numero).click();
        return this;
    }

    @Step("Avancar para a proxima pagina da tabela")
    public UserTableComponent goToNextPage() {
        nextPageButton().click();
        return this;
    }

    public Locator wrapper() {
        return root.locator(".table-wrapper");
    }

    public Locator rows() {
        return rows;
    }

    public Locator summary() {
        return summary;
    }

    public Locator columnHeader(Column coluna) {
        return table.locator("th[data-testid='" + coluna.testId() + "']");
    }

    public Locator columnHeaders() {
        return table.locator("thead th");
    }

    public Locator pageButton(int numero) {
        return pagination.getByTestId("page-" + numero);
    }

    public Locator previousPageButton() {
        return pagination.getByTestId("page-prev");
    }

    public Locator nextPageButton() {
        return pagination.getByTestId("page-next");
    }

    public Locator rowForUserId(int userId) {
        return root.getByTestId("table-row-" + userId);
    }

    public Locator deleteButton(int userId) {
        return root.getByTestId("delete-" + userId);
    }


    public Locator columnCells(Column coluna) {
        return rows.locator("td:nth-child(" + (columnIndex(coluna) + 1) + ")");
    }

    private int columnIndex(Column coluna) {
        Locator headers = columnHeaders();
        int total = headers.count();
        for (int i = 0; i < total; i++) {
            if (coluna.testId().equals(headers.nth(i).getAttribute("data-testid"))) {
                return i;
            }
        }
        throw new IllegalStateException(
                "Coluna " + coluna + " (" + coluna.testId() + ") nao esta no cabecalho da tabela.");
    }
}
