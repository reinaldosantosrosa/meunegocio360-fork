package br.com.meunegocio360.core.cliente.application;

import br.com.meunegocio360.core.cliente.domain.CampoOrdenacaoCliente;
import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepositorio;
import br.com.meunegocio360.core.cliente.domain.DirecaoOrdenacao;
import br.com.meunegocio360.core.cliente.domain.ErroCliente;
import br.com.meunegocio360.core.cliente.domain.PaginaClientes;
import br.com.meunegocio360.core.cliente.domain.SituacaoCliente;
import br.com.meunegocio360.core.cliente.domain.TipoPessoa;
import br.com.meunegocio360.core.cliente.domain.VerificadorVinculoCliente;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.shared.validation.DocumentoBrasil;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

  private static final int TAMANHO_PADRAO = 20;
  private static final int TAMANHO_MAXIMO = 100;

  private final ClienteRepositorio clienteRepositorio;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoCliente> verificadoresVinculo;
  private final Clock clock;

  public ClienteService(
      ClienteRepositorio clienteRepositorio,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoCliente> verificadoresVinculo,
      Clock clock) {
    this.clienteRepositorio = clienteRepositorio;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public ResultadoBuscaClientes pesquisar(
      String q, String situacao, String pagina, String tamanho, String sort) {
    UUID empresaId = empresaAtiva.obterId();
    int paginaValidada = inteiro(pagina, "page");
    int tamanhoValidado = inteiro(tamanho, "size");
    if (paginaValidada < 0) {
      throw ErroCliente.parametroInvalido("page", "deve ser maior ou igual a zero");
    }
    if (tamanhoValidado < 1 || tamanhoValidado > TAMANHO_MAXIMO) {
      throw ErroCliente.parametroInvalido("size", "deve estar entre 1 e 100");
    }

    Ordenacao ordenacao = ordenar(sort);
    Boolean ativo = situacao(situacao);
    String busca = q == null ? null : q.trim();
    if (busca != null && busca.isEmpty()) {
      busca = null;
    }
    String documento = busca == null ? null : busca.replaceAll("\\D", "");
    if (documento != null && documento.isEmpty()) {
      documento = null;
    }

    PaginaClientes resultado =
        clienteRepositorio.pesquisar(
            empresaId,
            busca,
            documento,
            ativo,
            paginaValidada,
            tamanhoValidado,
            ordenacao.campo(),
            ordenacao.direcao());
    int totalPages =
        resultado.totalElements() == 0
            ? 0
            : (int)
                Math.min(Integer.MAX_VALUE, (resultado.totalElements() - 1) / tamanhoValidado + 1);
    return new ResultadoBuscaClientes(
        resultado.clientes(),
        paginaValidada,
        tamanhoValidado,
        resultado.totalElements(),
        totalPages);
  }

  @Transactional(readOnly = true)
  public Cliente buscar(UUID id) {
    return clienteRepositorio
        .buscarPorIdEEmpresa(id, empresaAtiva.obterId())
        .orElseThrow(ErroCliente::clienteNaoEncontrado);
  }

  @Transactional
  public Cliente cadastrar(
      TipoPessoa tipoPessoa, String nome, String documento, String email, String telefone) {
    UUID empresaId = empresaAtiva.obterId();
    String documentoNormalizado = DocumentoBrasil.normalizar(documento);
    if (clienteRepositorio.existePorEmpresaEDocumento(empresaId, documentoNormalizado)) {
      throw ErroCliente.documentoDuplicado();
    }
    return clienteRepositorio.salvarEAtualizar(
        new Cliente(
            empresaId, tipoPessoa, nome, documentoNormalizado, email, telefone, clock.instant()));
  }

  @Transactional
  public Cliente atualizar(
      UUID id,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone,
      long versao) {
    Cliente cliente = buscarPorIdEEmpresaAtiva(id);
    validarVersao(cliente, versao);
    String documentoNormalizado = DocumentoBrasil.normalizar(documento);
    if (!cliente.getDocumento().equals(documentoNormalizado)
        && clienteRepositorio.existePorEmpresaEDocumento(
            cliente.getEmpresaId(), documentoNormalizado)) {
      throw ErroCliente.documentoDuplicado();
    }
    cliente.atualizar(tipoPessoa, nome, documentoNormalizado, email, telefone, clock.instant());
    return salvarComControleDeConcorrencia(cliente);
  }

  @Transactional
  public Cliente alterarSituacao(UUID id, boolean ativo, long versao) {
    Cliente cliente = buscarPorIdEEmpresaAtiva(id);
    validarVersao(cliente, versao);
    if (cliente.isAtivo() == ativo) {
      return cliente;
    }
    cliente.alterarSituacao(ativo, clock.instant());
    return salvarComControleDeConcorrencia(cliente);
  }

  @Transactional
  public void excluir(UUID id) {
    Cliente cliente = buscarPorIdEEmpresaAtiva(id);
    if (verificadoresVinculo.stream().anyMatch(verificador -> verificador.possuiVinculo(id))) {
      throw ErroCliente.clienteComHistorico();
    }
    clienteRepositorio.excluir(cliente);
  }

  private Cliente buscarPorIdEEmpresaAtiva(UUID id) {
    return clienteRepositorio
        .buscarPorIdEEmpresa(id, empresaAtiva.obterId())
        .orElseThrow(ErroCliente::clienteNaoEncontrado);
  }

  private Cliente salvarComControleDeConcorrencia(Cliente cliente) {
    try {
      return clienteRepositorio.salvarEAtualizar(cliente);
    } catch (ObjectOptimisticLockingFailureException exception) {
      throw ErroCliente.versaoConflitante();
    }
  }

  private static int inteiro(String valor, String campo) {
    try {
      return Integer.parseInt(valor);
    } catch (NumberFormatException exception) {
      throw ErroCliente.parametroInvalido(campo, "deve ser um número inteiro válido");
    }
  }

  private static Boolean situacao(String valor) {
    try {
      return switch (SituacaoCliente.valueOf(valor.toUpperCase(Locale.ROOT))) {
        case ATIVO -> true;
        case INATIVO -> false;
        case TODOS -> null;
      };
    } catch (IllegalArgumentException exception) {
      throw ErroCliente.parametroInvalido("situacao", "deve ser ATIVO, INATIVO ou TODOS");
    }
  }

  private static Ordenacao ordenar(String valor) {
    String[] partes = valor.split(",", -1);
    if (partes.length < 1 || partes.length > 2) {
      throw ErroCliente.parametroInvalido(
          "sort", "deve ser nome ou criadoEm, opcionalmente com direção");
    }
    CampoOrdenacaoCliente campo =
        switch (partes[0].trim()) {
          case "nome" -> CampoOrdenacaoCliente.NOME;
          case "criadoEm" -> CampoOrdenacaoCliente.CRIADO_EM;
          default -> throw ErroCliente.parametroInvalido("sort", "só aceita nome ou criadoEm");
        };
    DirecaoOrdenacao direcao;
    if (partes.length == 1 || partes[1].isBlank()) {
      direcao = DirecaoOrdenacao.ASC;
    } else {
      try {
        direcao = DirecaoOrdenacao.valueOf(partes[1].trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException exception) {
        throw ErroCliente.parametroInvalido("sort", "a direção deve ser asc ou desc");
      }
    }
    return new Ordenacao(campo, direcao);
  }

  private static void validarVersao(Cliente cliente, long versao) {
    if (cliente.getVersao() != versao) {
      throw ErroCliente.versaoConflitante();
    }
  }

  private record Ordenacao(CampoOrdenacaoCliente campo, DirecaoOrdenacao direcao) {}
}
