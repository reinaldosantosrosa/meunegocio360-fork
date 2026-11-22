package br.com.meunegocio360.core.fornecedor.infra.persistence;

import br.com.meunegocio360.core.fornecedor.domain.ConflitoFornecedor;
import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import br.com.meunegocio360.core.fornecedor.domain.FornecedorRepositorio;
import br.com.meunegocio360.core.fornecedor.domain.PaginaFornecedores;
import br.com.meunegocio360.core.fornecedor.domain.SituacaoFornecedor;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class FornecedorRepositorioJpa implements FornecedorRepositorio {

  private static final String DOCUMENT_CONSTRAINT = "uk_fornecedor_empresa_documento";

  private final SpringDataFornecedorRepository repository;

  public FornecedorRepositorioJpa(SpringDataFornecedorRepository repository) {
    this.repository = repository;
  }

  @Override
  public PaginaFornecedores listar(
      UUID empresaId,
      SituacaoFornecedor situacao,
      String buscaNome,
      String buscaDocumento,
      int page,
      int size,
      String sort) {
    Boolean ativo =
        switch (situacao) {
          case ATIVO -> true;
          case INATIVO -> false;
          case TODOS -> null;
        };
    Page<Fornecedor> resultado =
        repository.listar(
            empresaId, ativo, buscaNome, buscaDocumento, PageRequest.of(page, size, Sort.by(sort)));
    return new PaginaFornecedores(
        resultado.getContent(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  public Optional<Fornecedor> buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return repository.findByIdAndEmpresaId(id, empresaId);
  }

  @Override
  public Optional<Fornecedor> buscarPorDocumentoEEmpresa(String documento, UUID empresaId) {
    return repository.findByDocumentoAndEmpresaId(documento, empresaId);
  }

  @Override
  public Optional<Fornecedor> buscarPorDocumentoEEmpresaExcetoId(
      String documento, UUID empresaId, UUID id) {
    return repository.findByDocumentoAndEmpresaIdAndIdNot(documento, empresaId, id);
  }

  @Override
  public Fornecedor salvar(Fornecedor fornecedor) {
    try {
      return repository.saveAndFlush(fornecedor);
    } catch (DataIntegrityViolationException exception) {
      if (violouUnicidadeDeDocumento(exception)) {
        throw new ConflitoFornecedor();
      }
      throw exception;
    }
  }

  @Override
  public void excluir(Fornecedor fornecedor) {
    repository.delete(fornecedor);
  }

  private static boolean violouUnicidadeDeDocumento(Throwable exception) {
    Throwable atual = exception;
    while (atual != null) {
      if (atual instanceof ConstraintViolationException violation
          && DOCUMENT_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
        return true;
      }
      String mensagem = atual.getMessage();
      if (mensagem != null && mensagem.contains(DOCUMENT_CONSTRAINT)) {
        return true;
      }
      atual = atual.getCause();
    }
    return false;
  }
}
