package com.samuellima.playlab.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Allure;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class PlaywrightExtension
        implements BeforeEachCallback, AfterTestExecutionCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PlaywrightExtension.class);
    private static final String SESSION_KEY = "playwright-session";

    private static final ExtensionContext.Namespace ENGINE_NAMESPACE =
            ExtensionContext.Namespace.create(PlaywrightExtension.class, "engine");

    private static final Path EVIDENCE_ROOT = Path.of("target", "evidence");

    @Override
    public void beforeEach(ExtensionContext context) {
        String engine = TestConfig.browser();
        Viewport viewport = TestConfig.viewport();

        Browser browser = browser(context, engine);
        BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(TestConfig.baseUrl())
                .setViewportSize(viewport.width(), viewport.height()));

        browserContext.setDefaultTimeout(TestConfig.actionTimeoutMs());

        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        Page page = browserContext.newPage();

        List<String> pageLog = Collections.synchronizedList(new ArrayList<>());
        page.onConsoleMessage(message ->
                pageLog.add("[console:" + message.type() + "] " + message.text()));
        page.onPageError(error -> pageLog.add("[pageerror] " + error));

        page.navigate(TestConfig.baseUrl());

        context.getStore(NAMESPACE).put(SESSION_KEY,
                new Session(browserContext, page, pageLog));

        Allure.parameter("navegador", engine);
        Allure.parameter("viewport", viewport.toString());
    }

    private Browser browser(ExtensionContext context, String engine) {
        return context.getRoot().getStore(ENGINE_NAMESPACE).getOrComputeIfAbsent(
                engine,
                nome -> {
                    Playwright playwright = Playwright.create(new Playwright.CreateOptions()
                            .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
                    PlaywrightAssertions.setDefaultAssertionTimeout(
                            TestConfig.assertionTimeoutMs());
                    return new Engine(playwright,
                            BrowserFactory.launch(playwright, nome, TestConfig.headless()));
                },
                Engine.class).browser();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Session session = session(context);
        if (session == null) {
            return;
        }

        if (context.getExecutionException().isEmpty()) {
            session.browserContext().tracing().stop();
            return;
        }

        Path folder = EVIDENCE_ROOT
                .resolve(context.getRequiredTestClass().getSimpleName())
                .resolve(sanitize(context.getRequiredTestMethod().getName()));

        attachScreenshot(session, folder);
        attachTrace(session, folder);
        attachPageLog(session);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Session session = session(context);
        if (session == null) {
            return;
        }
        closeQuietly(session.page());
        closeQuietly(session.browserContext());
    }


    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
        return parameter.getParameter().getType() == Page.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
        return session(context).page();
    }


    private void attachScreenshot(Session session, Path folder) {
        try {
            byte[] png = session.page().screenshot(new Page.ScreenshotOptions()
                    .setPath(folder.resolve("screenshot.png"))
                    .setFullPage(true));
            Allure.getLifecycle().addAttachment(
                    "Screenshot da falha", "image/png", "png", png);
        } catch (RuntimeException e) {
            System.err.println("Nao foi possivel capturar screenshot: " + e.getMessage());
        }
    }

    private void attachTrace(Session session, Path folder) {
        try {
            Path trace = folder.resolve("trace.zip");
            session.browserContext().tracing().stop(new Tracing.StopOptions().setPath(trace));
            Allure.getLifecycle().addAttachment(
                    "Trace do Playwright (abrir em trace.playwright.dev)",
                    "application/zip", "zip", Files.readAllBytes(trace));
        } catch (Exception e) {
            System.err.println("Nao foi possivel gravar o trace: " + e.getMessage());
        }
    }

    private void attachPageLog(Session session) {
        if (session.pageLog().isEmpty()) {
            return;
        }
        String log = String.join(System.lineSeparator(), session.pageLog());
        Allure.getLifecycle().addAttachment(
                "Console e erros da pagina", "text/plain", "txt",
                log.getBytes(StandardCharsets.UTF_8));
    }


    private Session session(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(SESSION_KEY, Session.class);
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            System.err.println("Falha ao fechar recurso do Playwright: " + e.getMessage());
        }
    }

    private record Session(BrowserContext browserContext,
                           Page page,
                           List<String> pageLog) {
    }

    private record Engine(Playwright playwright, Browser browser)
            implements ExtensionContext.Store.CloseableResource, AutoCloseable {

        @Override
        public void close() {
            try {
                browser.close();
            } finally {
                playwright.close();
            }
        }
    }
}
