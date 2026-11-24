package br.com.meunegocio360.core.catalogo.infra.persistence;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepositorio;
import br.com.meunegocio360.core.catalogo.domain.ConflitoCategoria;
import br.com.meunegocio360.core.catalogo.domain.PaginaCatalogo;
import br.com.meunegocio360.core.catalogo.domain.SituacaoCatalogo;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepositorioJpa implements CategoriaRepositorio {

  private static final String NAME_CONSTRAINT = "uk_categoria_empresa_nome";
  private final SpringDataCategoriaRepository repository;
  private final SpringDataItemCatalogoRepository itemRepository;

  public CategoriaRepositorioJpa(
      SpringDataCategoriaRepository repository, SpringDataItemCatalogoRepository itemRepository) {
    this.repository = repository;
    this.itemRepository = itemRepository;
  }

  @Override
  public PaginaCatalogo<Categoria> listar(
      UUID empresaId, SituacaoCatalogo situacao, String busca, int page, int size, String sort) {
    Boolean ativo = situacaoAtiva(situacao);
    Page<Categoria> resultado =
        repository.listar(empresaId, ativo, busca, PageRequest.of(page, size, Sort.by(sort)));
    return new PaginaCatalogo<>(
        resultado.getContent(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  public Optional<Categoria> buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return repository.findByIdAndEmpresaId(id, empresaId);
  }

  @Override
  public Optional<Categoria> buscarPorNomeEEmpresa(String nome, UUID empresaId) {
    return repository.findByNomeAndEmpresaId(nome, empresaId);
  }

  @Override
  public Optional<Categoria> buscarPorNomeEEmpresaExcetoId(String nome, UUID empresaId, UUID id) {
    return repository.findByNomeAndEmpresaIdAndIdNot(nome, empresaId, id);
  }

  @Override
  public long contarItensVinculados(UUID categoriaId, UUID empresaId) {
    return itemRepository.countByCategoria_IdAndEmpresaId(categoriaId, empresaId);
  }

  @Override
  public Categoria salvar(Categoria categoria) {
    try {
      return repository.saveAndFlush(categoria);
    } catch (DataIntegrityViolationException exception) {
      if (violouUnicidadeDoNome(exception)) {
        throw new ConflitoCategoria();
      }
      throw exception;
    }
  }

  @Override
  public void excluir(Categoria categoria) {
    repository.delete(categoria);
  }

  private static Boolean situacaoAtiva(SituacaoCatalogo situacao) {
    return switch (situacao) {
      case ATIVO -> true;
      case INATIVO -> false;
      case TODOS -> null;
    };
  }

  private static boolean violouUnicidadeDoNome(Throwable exception) {
    Throwable atual = exception;
    while (atual != null) {
      if (atual instanceof ConstraintViolationException violation
          && NAME_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
        return true;
      }
      String mensagem = atual.getMessage();
      if (mensagem != null && mensagem.contains(NAME_CONSTRAINT)) {
        return true;
      }
      atual = atual.getCause();
    }
    return false;
  }
}
