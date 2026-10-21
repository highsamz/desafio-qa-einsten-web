package com.samuellima.playlab.support;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class Layout {

    private Layout() {
    }

    public static int documentScrollWidth(Page page) {
        return (int) page.evaluate("() => document.documentElement.scrollWidth");
    }

    public static int viewportWidth(Page page) {
        return (int) page.evaluate("() => document.documentElement.clientWidth");
    }

    public static String overflowX(Locator elemento) {
        return (String) elemento.evaluate(
                "el => getComputedStyle(el).overflowX");
    }
}
