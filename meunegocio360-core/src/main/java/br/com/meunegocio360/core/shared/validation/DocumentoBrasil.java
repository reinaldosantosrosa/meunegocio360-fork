package br.com.meunegocio360.core.shared.validation;

public final class DocumentoBrasil {

  private DocumentoBrasil() {}

  public static String normalizar(String documento) {
    if (documento == null) {
      return null;
    }
    String valor = documento.trim();
    if (valor.matches("\\d{11}|\\d{14}")) {
      return valor;
    }
    if (valor.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")
        || valor.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")) {
      return valor.replaceAll("\\D", "");
    }
    return valor;
  }

  public static boolean cpfValido(String cpf) {
    if (cpf == null || !cpf.matches("\\d{11}") || sequenciaRepetida(cpf)) {
      return false;
    }
    int primeiroDigito = calcularDigito(cpf, 9, 10);
    int segundoDigito = calcularDigito(cpf, 10, 11);
    return cpf.charAt(9) - '0' == primeiroDigito && cpf.charAt(10) - '0' == segundoDigito;
  }

  public static boolean cnpjValido(String cnpj) {
    if (cnpj == null || !cnpj.matches("\\d{14}") || sequenciaRepetida(cnpj)) {
      return false;
    }
    int primeiroDigito = calcularDigitoCnpj(cnpj, 12);
    int segundoDigito = calcularDigitoCnpj(cnpj, 13);
    return cnpj.charAt(12) - '0' == primeiroDigito && cnpj.charAt(13) - '0' == segundoDigito;
  }

  private static int calcularDigito(String documento, int limite, int pesoInicial) {
    int soma = 0;
    for (int indice = 0; indice < limite; indice++) {
      soma += (documento.charAt(indice) - '0') * (pesoInicial - indice);
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }

  private static int calcularDigitoCnpj(String cnpj, int limite) {
    int soma = 0;
    for (int indice = 0; indice < limite; indice++) {
      int peso = (limite - 1 - indice) % 8 + 2;
      soma += (cnpj.charAt(indice) - '0') * peso;
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }

  private static boolean sequenciaRepetida(String documento) {
    return documento.chars().allMatch(caractere -> caractere == documento.charAt(0));
  }
}
