package it.def.prolocobe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DataPagamentoObbligatoriaSeQuotaPagataValidator.class)
public @interface DataPagamentoObbligatoriaSeQuotaPagata {

    String message() default "La data di pagamento è obbligatoria quando la quota è indicata come pagata";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
