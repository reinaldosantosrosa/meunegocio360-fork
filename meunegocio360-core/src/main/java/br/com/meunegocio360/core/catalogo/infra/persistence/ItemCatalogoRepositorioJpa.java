package br.com.meunegocio360.core.catalogo.infra.persistence;

import br.com.meunegocio360.core.catalogo.domain.ConflitoCodigoItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogoRepositorio;
import br.com.meunegocio360.core.catalogo.domain.PaginaCatalogo;
import br.com.meunegocio360.core.catalogo.domain.SituacaoCatalogo;
import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ItemCatalogoRepositorioJpa implements ItemCatalogoRepositorio {

  private static final String CODE_CONSTRAINT = "uk_item_catalogo_empresa_codigo";
  private final SpringDataItemCatalogoRepository repository;

  public ItemCatalogoRepositorioJpa(SpringDataItemCatalogoRepository repository) {
    this.repository = repository;
  }

  @Override
  public PaginaCatalogo<ItemCatalogo> listar(
      UUID empresaId,
      SituacaoCatalogo situacao,
      TipoItemCatalogo tipo,
      UUID categoriaId,
      String busca,
      int page,
      int size,
      String sort) {
    Boolean ativo =
        switch (situacao) {
          case ATIVO -> true;
          case INATIVO -> false;
          case TODOS -> null;
        };
    Page<ItemCatalogo> resultado =
        repository.listar(
            empresaId, ativo, tipo, categoriaId, busca, PageRequest.of(page, size, Sort.by(sort)));
    return new PaginaCatalogo<>(
        resultado.getContent(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  public Optional<ItemCatalogo> buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return repository.findByIdAndEmpresaId(id, empresaId);
  }

  @Override
  public Optional<ItemCatalogo> buscarPorCodigoEEmpresa(String codigo, UUID empresaId) {
    return repository.findByCodigoAndEmpresaId(codigo, empresaId);
  }

  @Override
  public Optional<ItemCatalogo> buscarPorCodigoEEmpresaExcetoId(
      String codigo, UUID empresaId, UUID id) {
    return repository.findByCodigoAndEmpresaIdAndIdNot(codigo, empresaId, id);
  }

  @Override
  public ItemCatalogo salvar(ItemCatalogo item) {
    try {
      return repository.saveAndFlush(item);
    } catch (DataIntegrityViolationException exception) {
      if (violouUnicidadeDoCodigo(exception)) {
        throw new ConflitoCodigoItemCatalogo();
      }
      throw exception;
    }
  }

  @Override
  public void excluir(ItemCatalogo item) {
    repository.delete(item);
  }

  private static boolean violouUnicidadeDoCodigo(Throwable exception) {
    Throwable atual = exception;
    while (atual != null) {
      if (atual instanceof ConstraintViolationException violation
          && CODE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
        return true;
      }
      String mensagem = atual.getMessage();
      if (mensagem != null && mensagem.contains(CODE_CONSTRAINT)) {
        return true;
      }
      atual = atual.getCause();
    }
    return false;
  }
}
