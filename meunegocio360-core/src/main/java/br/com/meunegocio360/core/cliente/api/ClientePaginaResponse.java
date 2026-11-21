package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ResultadoBuscaClientes;
import java.util.List;

public record ClientePaginaResponse(
    List<ClienteResponse> content, int page, int size, long totalElements, int totalPages) {

  public static ClientePaginaResponse de(ResultadoBuscaClientes resultado) {
    return new ClientePaginaResponse(
        resultado.clientes().stream().map(ClienteResponse::de).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }
}
