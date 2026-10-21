package com.samuellima.playlab.data;

import java.util.concurrent.atomic.AtomicInteger;

public final class RegistrationDataFactory {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final String SENHA_FICTICIA = "SenhaDeTeste123";

    private RegistrationDataFactory() {
    }

    public static RegistrationData valido() {
        int n = SEQUENCIA.incrementAndGet();
        long marca = System.currentTimeMillis();
        return new RegistrationData(
                "Mariana Teste " + n,
                "qa.playlab+" + marca + "-" + n + "@example.com",
                SENHA_FICTICIA,
                "11999990000",
                "Massa ficticia gerada pela suite automatizada.",
                "br");
    }
}
