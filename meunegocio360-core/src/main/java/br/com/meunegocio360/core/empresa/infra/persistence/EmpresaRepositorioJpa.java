package br.com.meunegocio360.core.empresa.infra.persistence;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepositorio;
import br.com.meunegocio360.core.empresa.domain.ErroEmpresa;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class EmpresaRepositorioJpa implements EmpresaRepositorio {

  private final SpringDataEmpresaRepository repository;

  public EmpresaRepositorioJpa(SpringDataEmpresaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Empresa> buscarAtiva() {
    return repository.findFirstByAtivaTrue();
  }

  @Override
  public Optional<Empresa> buscarPorId(UUID id) {
    return repository.findById(id);
  }

  @Override
  public boolean existePorCnpj(String cnpj) {
    return repository.existsByCnpj(cnpj);
  }

  @Override
  public Empresa salvarEAtualizar(Empresa empresa) {
    try {
      return repository.saveAndFlush(empresa);
    } catch (DataIntegrityViolationException exception) {
      String constraint = nomeRestricao(exception);
      if ("uk_empresa_cnpj".equalsIgnoreCase(constraint)) {
        throw ErroEmpresa.cnpjDuplicado();
      }
      if ("uk_empresa_ativa_unica".equalsIgnoreCase(constraint)) {
        throw ErroEmpresa.empresaAtivaJaExiste();
      }
      throw exception;
    }
  }

  private static String nomeRestricao(Throwable exception) {
    for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
      if (causa instanceof ConstraintViolationException violacao) {
        return violacao.getConstraintName();
      }
    }
    return null;
  }
}
