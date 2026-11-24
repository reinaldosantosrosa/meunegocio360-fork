package br.com.meunegocio360.core.catalogo.domain;

import java.util.Optional;
import java.util.UUID;

public interface ItemCatalogoRepositorio {

  PaginaCatalogo<ItemCatalogo> listar(
      UUID empresaId,
      SituacaoCatalogo situacao,
      TipoItemCatalogo tipo,
      UUID categoriaId,
      String busca,
      int page,
      int size,
      String sort);

  Optional<ItemCatalogo> buscarPorIdEEmpresa(UUID id, UUID empresaId);

  Optional<ItemCatalogo> buscarPorCodigoEEmpresa(String codigo, UUID empresaId);

  Optional<ItemCatalogo> buscarPorCodigoEEmpresaExcetoId(String codigo, UUID empresaId, UUID id);

  ItemCatalogo salvar(ItemCatalogo item);

  void excluir(ItemCatalogo item);
}
