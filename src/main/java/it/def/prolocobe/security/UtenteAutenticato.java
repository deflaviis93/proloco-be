package it.def.prolocobe.security;

import it.def.prolocobe.enums.RuoloUtente;

import java.util.Set;

public record UtenteAutenticato(
        Long id,
        String email,
        Set<RuoloUtente> ruoli,
        boolean deveCambiarePassword
) {
}
