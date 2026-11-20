package br.com.meunegocio360.core.empresa.domain;

public class ErroEmpresa extends RuntimeException {

  private final int status;
  private final String code;
  private final String title;

  private ErroEmpresa(int status, String code, String title, String detail) {
    super(detail);
    this.status = status;
    this.code = code;
    this.title = title;
  }

  public int getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }

  public static ErroEmpresa empresaNaoRegistrada() {
    return new ErroEmpresa(
        404,
        "COMPANY_NOT_REGISTERED",
        "Empresa não encontrada",
        "Não há empresa ativa cadastrada.");
  }

  public static ErroEmpresa empresaNaoEncontrada() {
    return new ErroEmpresa(
        404, "COMPANY_NOT_FOUND", "Empresa não encontrada", "A empresa não existe.");
  }

  public static ErroEmpresa cnpjDuplicado() {
    return new ErroEmpresa(
        409,
        "COMPANY_CNPJ_DUPLICATE",
        "CNPJ já cadastrado",
        "Já existe uma empresa com este CNPJ.");
  }

  public static ErroEmpresa empresaAtivaJaExiste() {
    return new ErroEmpresa(
        409, "COMPANY_ALREADY_ACTIVE", "Empresa ativa já existe", "Já existe uma empresa ativa.");
  }

  public static ErroEmpresa versaoConflitante() {
    return new ErroEmpresa(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "A empresa foi alterada por outra operação. Consulte a versão atual e tente novamente.");
  }

  public static ErroEmpresa empresaNaoAtiva() {
    return new ErroEmpresa(
        422,
        "COMPANY_NOT_ACTIVE",
        "Empresa ativa obrigatória",
        "A operação exige uma empresa ativa.");
  }
}
