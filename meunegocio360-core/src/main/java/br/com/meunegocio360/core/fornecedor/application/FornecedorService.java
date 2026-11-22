package br.com.meunegocio360.core.fornecedor.application;

import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.fornecedor.api.AlterarSituacaoFornecedorRequest;
import br.com.meunegocio360.core.fornecedor.api.AtualizarFornecedorRequest;
import br.com.meunegocio360.core.fornecedor.api.FornecedorRequest;
import br.com.meunegocio360.core.fornecedor.api.FornecedorResponse;
import br.com.meunegocio360.core.fornecedor.api.PaginaFornecedorResponse;
import br.com.meunegocio360.core.fornecedor.domain.ConflitoFornecedor;
import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import br.com.meunegocio360.core.fornecedor.domain.FornecedorRepositorio;
import br.com.meunegocio360.core.fornecedor.domain.PaginaFornecedores;
import br.com.meunegocio360.core.fornecedor.domain.SituacaoFornecedor;
import br.com.meunegocio360.core.fornecedor.domain.VerificadorVinculoFornecedor;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

  private static final String DOCUMENT_DUPLICATE_CODE = "SUPPLIER_DOCUMENT_DUPLICATE";
  private final FornecedorRepositorio fornecedorRepositorio;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoFornecedor> verificadoresVinculo;
  private final Clock clock;

  public FornecedorService(
      FornecedorRepositorio fornecedorRepositorio,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoFornecedor> verificadoresVinculo,
      Clock clock) {
    this.fornecedorRepositorio = fornecedorRepositorio;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public PaginaFornecedorResponse<FornecedorResponse> listar(
      String q, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size, sort);
    SituacaoFornecedor filtro = parseSituacao(situacao);
    String busca = normalizarBusca(q);
    String buscaDocumento = digitosOuNulo(busca);
    PaginaFornecedores resultado =
        fornecedorRepositorio.listar(
            empresaAtiva.obterId(), filtro, busca, buscaDocumento, page, size, sort);
    return new PaginaFornecedorResponse<>(
        resultado.content().stream().map(FornecedorService::resposta).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }

  @Transactional
  public FornecedorResponse cadastrar(FornecedorRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    String documento = normalizarDocumento(request.documento());
    if (fornecedorRepositorio.buscarPorDocumentoEEmpresa(documento, empresaId).isPresent()) {
      throw documentoDuplicado();
    }

    Fornecedor fornecedor =
        new Fornecedor(
            empresaId,
            request.tipoPessoa(),
            request.nome(),
            documento,
            request.email(),
            request.telefone(),
            clock.instant());
    return salvar(fornecedor);
  }

  @Transactional(readOnly = true)
  public FornecedorResponse consultar(UUID id) {
    return resposta(buscarPorIdEEmpresa(id, empresaAtiva.obterId()));
  }

  @Transactional
  public FornecedorResponse atualizar(UUID id, AtualizarFornecedorRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    Fornecedor fornecedor = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(fornecedor, request.versao());
    String documento = normalizarDocumento(request.documento());
    fornecedorRepositorio
        .buscarPorDocumentoEEmpresaExcetoId(documento, empresaId, id)
        .ifPresent(FornecedorService::lancarDocumentoDuplicado);

    fornecedor.atualizar(
        request.tipoPessoa(),
        request.nome(),
        documento,
        request.email(),
        request.telefone(),
        clock.instant());
    return salvar(fornecedor);
  }

  @Transactional
  public FornecedorResponse alterarSituacao(UUID id, AlterarSituacaoFornecedorRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    Fornecedor fornecedor = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(fornecedor, request.versao());
    fornecedor.alterarSituacao(request.ativo(), clock.instant());
    return salvar(fornecedor);
  }

  @Transactional
  public void excluir(UUID id) {
    Fornecedor fornecedor = buscarPorIdEEmpresa(id, empresaAtiva.obterId());
    if (verificadoresVinculo.stream().anyMatch(verificador -> verificador.possuiVinculo(id))) {
      throw new ErroDeNegocio(
          422,
          "SUPPLIER_HAS_HISTORY",
          "Fornecedor possui histórico",
          "O fornecedor possui pedidos de compra ou contas a pagar vinculados e não pode ser excluído. Inative o fornecedor.");
    }
    fornecedorRepositorio.excluir(fornecedor);
  }

  private FornecedorResponse salvar(Fornecedor fornecedor) {
    try {
      return resposta(fornecedorRepositorio.salvar(fornecedor));
    } catch (ConflitoFornecedor conflito) {
      throw documentoDuplicado();
    } catch (ObjectOptimisticLockingFailureException conflito) {
      throw versaoDesatualizada();
    }
  }

  private Fornecedor buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return fornecedorRepositorio
        .buscarPorIdEEmpresa(id, empresaId)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "SUPPLIER_NOT_FOUND",
                    "Fornecedor não encontrado",
                    "O fornecedor não existe na empresa ativa."));
  }

  private static void validarVersao(Fornecedor fornecedor, Long versaoRecebida) {
    if (fornecedor.getVersao() != versaoRecebida) {
      throw versaoDesatualizada();
    }
  }

  private static void validarPaginacao(int page, int size, String sort) {
    if (page < 0 || size < 1 || size > 100) {
      throw new ErroDeNegocio(
          400,
          "VALIDATION_ERROR",
          "Dados inválidos",
          "page deve ser maior ou igual a 0 e size deve estar entre 1 e 100.");
    }
    if (!"nome".equals(sort) && !"criadoEm".equals(sort)) {
      throw new ErroDeNegocio(
          400,
          "VALIDATION_ERROR",
          "Dados inválidos",
          "sort deve ser um dos campos permitidos: nome, criadoEm.");
    }
  }

  private static SituacaoFornecedor parseSituacao(String situacao) {
    try {
      return SituacaoFornecedor.valueOf(situacao.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new ErroDeNegocio(
          400, "VALIDATION_ERROR", "Dados inválidos", "situacao deve ser ATIVO, INATIVO ou TODOS.");
    }
  }

  private static String normalizarBusca(String q) {
    if (q == null || q.isBlank()) {
      return null;
    }
    return q.trim();
  }

  private static String digitosOuNulo(String value) {
    if (value == null) {
      return null;
    }
    String digitos = value.replaceAll("\\D", "");
    return digitos.isEmpty() ? null : digitos;
  }

  private static String normalizarDocumento(String documento) {
    return documento.replaceAll("\\D", "");
  }

  private static void lancarDocumentoDuplicado(Fornecedor fornecedor) {
    throw documentoDuplicado();
  }

  private static ErroDeNegocio documentoDuplicado() {
    return new ErroDeNegocio(
        409,
        DOCUMENT_DUPLICATE_CODE,
        "Documento já cadastrado",
        "Já existe um fornecedor com este documento nesta empresa; ele pode estar inativo.");
  }

  private static ErroDeNegocio versaoDesatualizada() {
    return new ErroDeNegocio(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "O fornecedor foi alterado por outra operação. Consulte os dados atuais e tente novamente.");
  }

  private static FornecedorResponse resposta(Fornecedor fornecedor) {
    return new FornecedorResponse(
        fornecedor.getId(),
        fornecedor.getTipoPessoa(),
        fornecedor.getNome(),
        fornecedor.getDocumento(),
        fornecedor.getEmail(),
        fornecedor.getTelefone(),
        fornecedor.isAtivo(),
        fornecedor.getCriadoEm(),
        fornecedor.getAtualizadoEm(),
        fornecedor.getVersao());
  }
}
