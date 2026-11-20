package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.ZoneId;

public class FusoIanaValidator implements ConstraintValidator<FusoIana, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || ZoneId.getAvailableZoneIds().contains(value);
  }
}
