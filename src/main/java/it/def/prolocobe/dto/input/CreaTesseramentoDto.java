package it.def.prolocobe.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreaTesseramentoDto(
        @Min(2000) @Max(2100) int anno,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal importo,
        @NotNull @PastOrPresent LocalDate dataPagamento
) {
}
