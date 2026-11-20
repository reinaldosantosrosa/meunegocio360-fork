package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<Cnpj, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    if (!value.matches("\\d{14}")
        || value.chars().allMatch(character -> character == value.charAt(0))) {
      return false;
    }

    int primeiroDigito = calcularDigito(value, 12, new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
    int segundoDigito =
        calcularDigito(value, 13, new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
    return value.charAt(12) - '0' == primeiroDigito && value.charAt(13) - '0' == segundoDigito;
  }

  private static int calcularDigito(String value, int limite, int[] pesos) {
    int soma = 0;
    for (int indice = 0; indice < limite; indice++) {
      soma += (value.charAt(indice) - '0') * pesos[indice];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
