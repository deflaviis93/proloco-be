package it.def.prolocobe.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record AttivaAccountDto(
        @NotNull @PastOrPresent LocalDate dataPagamento
) {
}
