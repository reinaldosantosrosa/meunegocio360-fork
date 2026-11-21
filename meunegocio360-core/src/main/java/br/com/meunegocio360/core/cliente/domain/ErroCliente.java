package br.com.meunegocio360.core.cliente.domain;

public class ErroCliente extends RuntimeException {

  private final int status;
  private final String code;
  private final String title;
  private final String field;
  private final String fieldMessage;

  private ErroCliente(
      int status, String code, String title, String detail, String field, String fieldMessage) {
    super(detail);
    this.status = status;
    this.code = code;
    this.title = title;
    this.field = field;
    this.fieldMessage = fieldMessage;
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

  public String getField() {
    return field;
  }

  public String getFieldMessage() {
    return fieldMessage;
  }

  public static ErroCliente documentoDuplicado() {
    return new ErroCliente(
        409,
        "CUSTOMER_DOCUMENT_DUPLICATE",
        "Documento já cadastrado",
        "Já existe um cliente com este documento; ele pode estar inativo.",
        null,
        null);
  }

  public static ErroCliente clienteNaoEncontrado() {
    return new ErroCliente(
        404, "CUSTOMER_NOT_FOUND", "Cliente não encontrado", "O cliente não existe.", null, null);
  }

  public static ErroCliente clienteComHistorico() {
    return new ErroCliente(
        422,
        "CUSTOMER_HAS_HISTORY",
        "Cliente possui histórico",
        "O cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. "
            + "Inative o cliente.",
        null,
        null);
  }

  public static ErroCliente versaoConflitante() {
    return new ErroCliente(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "O cliente foi alterado por outra operação. Consulte a versão atual e tente novamente.",
        null,
        null);
  }

  public static ErroCliente parametroInvalido(String field, String fieldMessage) {
    return new ErroCliente(
        400,
        "VALIDATION_ERROR",
        "Dados inválidos",
        "Um ou mais campos são inválidos.",
        field,
        fieldMessage);
  }
}
