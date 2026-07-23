package it.def.prolocobe.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneratorePasswordTemporanea {

    private static final String CARATTERI = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int LUNGHEZZA = 12;

    private final SecureRandom random = new SecureRandom();

    public String genera() {
        StringBuilder sb = new StringBuilder(LUNGHEZZA);
        for (int i = 0; i < LUNGHEZZA; i++) {
            sb.append(CARATTERI.charAt(random.nextInt(CARATTERI.length())));
        }
        return sb.toString();
    }
}
