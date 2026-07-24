package it.def.prolocobe.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CreaSocioDto(
        @NotBlank String nome,
        @NotBlank String cognome,
        @PastOrPresent LocalDate dataNascita,
        String telefono,
        @NotBlank @Email String email,
        @NotNull @PastOrPresent LocalDate dataIscrizione
) {
}
