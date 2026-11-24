package br.com.meunegocio360.core.catalogo.application;

import br.com.meunegocio360.core.catalogo.api.AlterarSituacaoCatalogoRequest;
import br.com.meunegocio360.core.catalogo.api.AtualizarCategoriaRequest;
import br.com.meunegocio360.core.catalogo.api.CategoriaRequest;
import br.com.meunegocio360.core.catalogo.api.CategoriaResponse;
import br.com.meunegocio360.core.catalogo.api.PaginaCatalogoResponse;
import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepositorio;
import br.com.meunegocio360.core.catalogo.domain.ConflitoCategoria;
import br.com.meunegocio360.core.catalogo.domain.PaginaCatalogo;
import br.com.meunegocio360.core.catalogo.domain.SituacaoCatalogo;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

  private final CategoriaRepositorio categoriaRepositorio;
  private final EmpresaAtiva empresaAtiva;
  private final Clock clock;

  public CategoriaService(
      CategoriaRepositorio categoriaRepositorio, EmpresaAtiva empresaAtiva, Clock clock) {
    this.categoriaRepositorio = categoriaRepositorio;
    this.empresaAtiva = empresaAtiva;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public PaginaCatalogoResponse<CategoriaResponse> listar(
      String q, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size, sort);
    SituacaoCatalogo filtro = parseSituacao(situacao);
    PaginaCatalogo<Categoria> resultado =
        categoriaRepositorio.listar(
            empresaAtiva.obterId(), filtro, normalizarBusca(q), page, size, sort);
    return new PaginaCatalogoResponse<>(
        resultado.content().stream().map(CategoriaService::resposta).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }

  @Transactional
  public CategoriaResponse cadastrar(CategoriaRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    if (categoriaRepositorio.buscarPorNomeEEmpresa(request.nome(), empresaId).isPresent()) {
      throw nomeDuplicado();
    }
    return salvar(new Categoria(empresaId, request.nome(), clock.instant()));
  }

  @Transactional
  public CategoriaResponse atualizar(UUID id, AtualizarCategoriaRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    Categoria categoria = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(categoria, request.versao());
    categoriaRepositorio
        .buscarPorNomeEEmpresaExcetoId(request.nome(), empresaId, id)
        .ifPresent(
            categoriaExistente -> {
              throw nomeDuplicado();
            });
    categoria.renomear(request.nome(), clock.instant());
    return salvar(categoria);
  }

  @Transactional
  public CategoriaResponse alterarSituacao(UUID id, AlterarSituacaoCatalogoRequest request) {
    Categoria categoria = buscarPorIdEEmpresa(id, empresaAtiva.obterId());
    validarVersao(categoria, request.versao());
    categoria.alterarSituacao(request.ativo(), clock.instant());
    return salvar(categoria);
  }

  @Transactional
  public void excluir(UUID id) {
    UUID empresaId = empresaAtiva.obterId();
    Categoria categoria = buscarPorIdEEmpresa(id, empresaId);
    long quantidadeItens = categoriaRepositorio.contarItensVinculados(id, empresaId);
    if (quantidadeItens > 0) {
      throw new ErroDeNegocio(
          422,
          "CATEGORY_IN_USE",
          "Categoria em uso",
          "A categoria possui "
              + quantidadeItens
              + " item(ns) vinculado(s) e não pode ser excluída. Inative a categoria.");
    }
    categoriaRepositorio.excluir(categoria);
  }

  private CategoriaResponse salvar(Categoria categoria) {
    try {
      return resposta(categoriaRepositorio.salvar(categoria));
    } catch (ConflitoCategoria conflito) {
      throw nomeDuplicado();
    } catch (ObjectOptimisticLockingFailureException conflito) {
      throw versaoDesatualizada();
    }
  }

  private Categoria buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return categoriaRepositorio
        .buscarPorIdEEmpresa(id, empresaId)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "CATEGORY_NOT_FOUND",
                    "Categoria não encontrada",
                    "A categoria não existe na empresa ativa."));
  }

  private static void validarVersao(Categoria categoria, Long versaoRecebida) {
    if (categoria.getVersao() != versaoRecebida) {
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
    if (!"nome".equals(sort)) {
      throw new ErroDeNegocio(
          400, "VALIDATION_ERROR", "Dados inválidos", "sort deve ser o campo permitido: nome.");
    }
  }

  private static SituacaoCatalogo parseSituacao(String situacao) {
    try {
      return SituacaoCatalogo.valueOf(situacao.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new ErroDeNegocio(
          400, "VALIDATION_ERROR", "Dados inválidos", "situacao deve ser ATIVO, INATIVO ou TODOS.");
    }
  }

  private static String normalizarBusca(String q) {
    return q == null || q.isBlank() ? null : q.trim();
  }

  private static ErroDeNegocio nomeDuplicado() {
    return new ErroDeNegocio(
        409,
        "CATEGORY_NAME_DUPLICATE",
        "Nome de categoria já cadastrado",
        "Já existe uma categoria com este nome nesta empresa.");
  }

  private static ErroDeNegocio versaoDesatualizada() {
    return new ErroDeNegocio(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "A categoria foi alterada por outra operação. Consulte os dados atuais e tente novamente.");
  }

  private static CategoriaResponse resposta(Categoria categoria) {
    return new CategoriaResponse(
        categoria.getId(),
        categoria.getNome(),
        categoria.isAtivo(),
        categoria.getCriadoEm(),
        categoria.getAtualizadoEm(),
        categoria.getVersao());
  }
}
