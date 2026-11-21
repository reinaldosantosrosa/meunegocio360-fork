package br.com.meunegocio360.core.cliente.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DocumentoClienteValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumentoCliente {

  String message() default "documento inválido para o tipo de pessoa informado";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
