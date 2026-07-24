package it.def.prolocobe.dto.output;

import java.time.LocalDate;

public record DettaglioSocioDto(
        Long id,
        Long utenteId,
        String nome,
        String cognome,
        LocalDate dataNascita,
        String telefono,
        String email,
        LocalDate dataIscrizione,
        boolean inRegola,
        boolean accountAttivo
) {
}
