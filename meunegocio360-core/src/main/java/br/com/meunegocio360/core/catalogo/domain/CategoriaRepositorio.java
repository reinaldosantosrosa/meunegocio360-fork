package br.com.meunegocio360.core.catalogo.domain;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepositorio {

  PaginaCatalogo<Categoria> listar(
      UUID empresaId, SituacaoCatalogo situacao, String busca, int page, int size, String sort);

  Optional<Categoria> buscarPorIdEEmpresa(UUID id, UUID empresaId);

  Optional<Categoria> buscarPorNomeEEmpresa(String nome, UUID empresaId);

  Optional<Categoria> buscarPorNomeEEmpresaExcetoId(String nome, UUID empresaId, UUID id);

  long contarItensVinculados(UUID categoriaId, UUID empresaId);

  Categoria salvar(Categoria categoria);

  void excluir(Categoria categoria);
}
