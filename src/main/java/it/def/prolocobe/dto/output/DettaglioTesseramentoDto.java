package it.def.prolocobe.dto.output;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DettaglioTesseramentoDto(
        Long id,
        int anno,
        BigDecimal importo,
        LocalDate dataPagamento
) {
}
