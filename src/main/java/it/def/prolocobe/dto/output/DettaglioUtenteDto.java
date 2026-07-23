package it.def.prolocobe.dto.output;

import it.def.prolocobe.enums.RuoloUtente;

import java.util.Set;

public record DettaglioUtenteDto(
        Long id,
        String email,
        Set<RuoloUtente> ruoli,
        boolean attivo,
        boolean deveCambiarePassword
) {
}
