package com.samuellima.playlab.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public final class BrowserFactory {

    private BrowserFactory() {
    }

    public static Browser launch(Playwright playwright, String engine, boolean headless) {
        BrowserType type = switch (engine.toLowerCase()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalArgumentException(
                    "Navegador nao suportado: '" + engine
                            + "'. Use chromium, firefox ou webkit.");
        };
        try {
            return type.launch(new BrowserType.LaunchOptions().setHeadless(headless));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Nao foi possivel abrir o navegador '" + engine + "'. Se ele nao estiver"
                            + " instalado, rode:" + System.lineSeparator()
                            + "  mvn exec:java -D exec.args=\"install " + engine + "\""
                            + System.lineSeparator() + "Causa original: " + e.getMessage(), e);
        }
    }
}
