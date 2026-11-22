package br.com.meunegocio360.core.fornecedor.domain;

import java.util.Optional;
import java.util.UUID;

public interface FornecedorRepositorio {

  PaginaFornecedores listar(
      UUID empresaId,
      SituacaoFornecedor situacao,
      String buscaNome,
      String buscaDocumento,
      int page,
      int size,
      String sort);

  Optional<Fornecedor> buscarPorIdEEmpresa(UUID id, UUID empresaId);

  Optional<Fornecedor> buscarPorDocumentoEEmpresa(String documento, UUID empresaId);

  Optional<Fornecedor> buscarPorDocumentoEEmpresaExcetoId(
      String documento, UUID empresaId, UUID id);

  Fornecedor salvar(Fornecedor fornecedor);

  void excluir(Fornecedor fornecedor);
}
