package com.samuellima.playlab.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = load();

    private TestConfig() {
    }

    public static String baseUrl() {
        return get("base.url", "https://playwrightlab.github.io/");
    }

    public static String browser() {
        return get("browser", "chromium");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }


    public static Viewport viewport() {
        String profile = get("viewport", "desktop");
        int width = getInt("viewport." + profile + ".width", -1);
        int height = getInt("viewport." + profile + ".height", -1);
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException(
                    "Perfil de viewport '" + profile + "' nao tem viewport." + profile
                            + ".width/.height definidos no config.properties");
        }
        return new Viewport(profile, width, height);
    }

    public static double actionTimeoutMs() {
        return getInt("timeout.action.ms", 10_000);
    }

    public static double assertionTimeoutMs() {
        return getInt("timeout.assertion.ms", 10_000);
    }

    private static int getInt(String key, int defaultValue) {
        String raw = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Configuracao '" + key + "' deveria ser um inteiro, veio: " + raw, e);
        }
    }

    private static String get(String key, String defaultValue) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        String fromEnv = System.getenv(key.toUpperCase().replace('.', '_'));
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel carregar config.properties", e);
        }
        return properties;
    }
}
