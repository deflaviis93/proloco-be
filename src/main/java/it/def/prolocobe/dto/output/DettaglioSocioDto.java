package it.def.prolocobe.dto.output;

import java.time.LocalDate;

public record DettaglioSocioDto(
        Long id,
        String nome,
        String cognome,
        LocalDate dataNascita,
        String telefono,
        String email,
        LocalDate dataIscrizione,
        boolean quotaPagata,
        LocalDate dataUltimoPagamento,
        boolean accountAttivo
) {
}
