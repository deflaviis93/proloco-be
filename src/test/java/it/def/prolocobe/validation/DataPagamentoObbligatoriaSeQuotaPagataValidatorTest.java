package it.def.prolocobe.validation;

import it.def.prolocobe.dto.input.CreaSocioDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataPagamentoObbligatoriaSeQuotaPagataValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void nonValido_quandoQuotaPagataSenzaData() {
        CreaSocioDto dto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15),
                true, null);

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void valido_quandoQuotaPagataConData() {
        CreaSocioDto dto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15),
                true, LocalDate.of(2026, 1, 15));

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void valido_quandoQuotaNonPagataSenzaData() {
        CreaSocioDto dto = new CreaSocioDto("Mario", "Rossi", LocalDate.of(1980, 1, 1),
                "3331234567", "mario.rossi@example.com", LocalDate.of(2026, 1, 15),
                false, null);

        assertTrue(validator.validate(dto).isEmpty());
    }
}
