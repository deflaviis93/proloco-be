package it.def.prolocobe.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiaPasswordDto(
        @NotBlank String passwordAttuale,
        @NotBlank @Size(min = 8, message = "La nuova password deve contenere almeno 8 caratteri") String nuovaPassword
) {
}
