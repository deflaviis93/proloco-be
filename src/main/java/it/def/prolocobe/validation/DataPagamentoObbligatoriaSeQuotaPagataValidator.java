package it.def.prolocobe.validation;

import it.def.prolocobe.dto.input.CreaSocioDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DataPagamentoObbligatoriaSeQuotaPagataValidator
        implements ConstraintValidator<DataPagamentoObbligatoriaSeQuotaPagata, CreaSocioDto> {

    @Override
    public boolean isValid(CreaSocioDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        return !dto.quotaPagata() || dto.dataUltimoPagamento() != null;
    }
}
