package br.com.meunegocio360.core.cliente.infra.persistence;

import br.com.meunegocio360.core.cliente.domain.CampoOrdenacaoCliente;
import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepositorio;
import br.com.meunegocio360.core.cliente.domain.DirecaoOrdenacao;
import br.com.meunegocio360.core.cliente.domain.ErroCliente;
import br.com.meunegocio360.core.cliente.domain.PaginaClientes;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepositorioJpa implements ClienteRepositorio {

  private final SpringDataClienteRepository repository;

  public ClienteRepositorioJpa(SpringDataClienteRepository repository) {
    this.repository = repository;
  }

  @Override
  public PaginaClientes pesquisar(
      UUID empresaId,
      String nome,
      String documento,
      Boolean ativo,
      int pagina,
      int tamanho,
      CampoOrdenacaoCliente ordenacao,
      DirecaoOrdenacao direcao) {
    String propriedade =
        switch (ordenacao) {
          case NOME -> "nome";
          case CRIADO_EM -> "criadoEm";
        };
    Sort.Direction sentido =
        direcao == DirecaoOrdenacao.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    var resultado =
        repository.pesquisar(
            empresaId,
            nome,
            documento,
            ativo,
            PageRequest.of(pagina, tamanho, Sort.by(sentido, propriedade)));
    return new PaginaClientes(resultado.getContent(), resultado.getTotalElements());
  }

  @Override
  public Optional<Cliente> buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return repository.findByIdAndEmpresaId(id, empresaId);
  }

  @Override
  public boolean existePorEmpresaEDocumento(UUID empresaId, String documento) {
    return repository.existsByEmpresaIdAndDocumento(empresaId, documento);
  }

  @Override
  public Cliente salvarEAtualizar(Cliente cliente) {
    try {
      return repository.saveAndFlush(cliente);
    } catch (DataIntegrityViolationException exception) {
      if ("uk_cliente_empresa_documento".equalsIgnoreCase(nomeRestricao(exception))) {
        throw ErroCliente.documentoDuplicado();
      }
      throw exception;
    }
  }

  @Override
  public void excluir(Cliente cliente) {
    try {
      repository.delete(cliente);
      repository.flush();
    } catch (DataIntegrityViolationException exception) {
      throw ErroCliente.clienteComHistorico();
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
