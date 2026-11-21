package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.shared.validation.DocumentoBrasil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentoClienteValidator
    implements ConstraintValidator<ValidDocumentoCliente, DadosCliente> {

  @Override
  public boolean isValid(DadosCliente cliente, ConstraintValidatorContext context) {
    if (cliente == null || cliente.tipoPessoa() == null || cliente.documento() == null) {
      return true;
    }

    String documento = DocumentoBrasil.normalizar(cliente.documento());
    boolean valido =
        switch (cliente.tipoPessoa()) {
          case FISICA -> DocumentoBrasil.cpfValido(documento);
          case JURIDICA -> DocumentoBrasil.cnpjValido(documento);
        };
    if (!valido) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode("documento")
          .addConstraintViolation();
    }
    return valido;
  }
}
