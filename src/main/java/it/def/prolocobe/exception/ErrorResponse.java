package it.def.prolocobe.exception;

import java.util.Map;

public record ErrorResponse(String message, Map<String, String> dettagli) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
