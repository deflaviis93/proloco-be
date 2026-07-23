package it.def.prolocobe.dto.input;

import it.def.prolocobe.enums.RuoloUtente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreaUtenteDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "La password deve contenere almeno 8 caratteri") String password,
        @NotEmpty Set<RuoloUtente> ruoli
) {
}
