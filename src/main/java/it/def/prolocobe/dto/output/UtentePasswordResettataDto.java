package it.def.prolocobe.dto.output;

public record UtentePasswordResettataDto(
        DettaglioUtenteDto utente,
        String passwordTemporanea
) {
}
