package it.def.prolocobe.dto;

import java.time.LocalDate;

public record SocioDto(
        Long id,
        String nome,
        String cognome,
        LocalDate dataNascita,
        String telefono,
        String email,
        LocalDate dataIscrizione
) {
}
