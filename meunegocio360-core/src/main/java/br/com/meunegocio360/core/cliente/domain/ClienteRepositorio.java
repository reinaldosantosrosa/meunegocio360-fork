package br.com.meunegocio360.core.cliente.domain;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositorio {

  PaginaClientes pesquisar(
      UUID empresaId,
      String nome,
      String documento,
      Boolean ativo,
      int pagina,
      int tamanho,
      CampoOrdenacaoCliente ordenacao,
      DirecaoOrdenacao direcao);

  Optional<Cliente> buscarPorIdEEmpresa(UUID id, UUID empresaId);

  boolean existePorEmpresaEDocumento(UUID empresaId, String documento);

  Cliente salvarEAtualizar(Cliente cliente);

  void excluir(Cliente cliente);
}
