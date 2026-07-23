package it.def.prolocobe.dto.output;

import it.def.prolocobe.enums.RuoloUtente;

import java.time.Instant;
import java.util.Set;

public record LoginRispostaDto(
        String token,
        Instant scadenza,
        Set<RuoloUtente> ruoli,
        boolean deveCambiarePassword
) {
}
