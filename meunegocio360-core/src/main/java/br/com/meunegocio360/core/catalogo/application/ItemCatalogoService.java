package br.com.meunegocio360.core.catalogo.application;

import br.com.meunegocio360.core.catalogo.api.AlterarSituacaoCatalogoRequest;
import br.com.meunegocio360.core.catalogo.api.AtualizarItemCatalogoRequest;
import br.com.meunegocio360.core.catalogo.api.ItemCatalogoRequest;
import br.com.meunegocio360.core.catalogo.api.ItemCatalogoResponse;
import br.com.meunegocio360.core.catalogo.api.PaginaCatalogoResponse;
import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepositorio;
import br.com.meunegocio360.core.catalogo.domain.ConflitoCodigoItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogoRepositorio;
import br.com.meunegocio360.core.catalogo.domain.PaginaCatalogo;
import br.com.meunegocio360.core.catalogo.domain.SituacaoCatalogo;
import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.VerificadorVinculoItem;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemCatalogoService {

  private final ItemCatalogoRepositorio itemRepositorio;
  private final CategoriaRepositorio categoriaRepositorio;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoItem> verificadoresVinculo;
  private final Clock clock;

  public ItemCatalogoService(
      ItemCatalogoRepositorio itemRepositorio,
      CategoriaRepositorio categoriaRepositorio,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoItem> verificadoresVinculo,
      Clock clock) {
    this.itemRepositorio = itemRepositorio;
    this.categoriaRepositorio = categoriaRepositorio;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public PaginaCatalogoResponse<ItemCatalogoResponse> listar(
      String q,
      TipoItemCatalogo tipo,
      UUID categoriaId,
      String situacao,
      int page,
      int size,
      String sort) {
    validarPaginacao(page, size, sort);
    SituacaoCatalogo filtro = parseSituacao(situacao);
    PaginaCatalogo<ItemCatalogo> resultado =
        itemRepositorio.listar(
            empresaAtiva.obterId(),
            filtro,
            tipo,
            categoriaId,
            normalizarBusca(q),
            page,
            size,
            sort);
    return new PaginaCatalogoResponse<>(
        resultado.content().stream().map(ItemCatalogoService::resposta).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }

  @Transactional
  public ItemCatalogoResponse cadastrar(ItemCatalogoRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    validarCodigoDisponivel(request.codigo(), empresaId);
    Categoria categoria = categoriaValida(request.categoriaId(), empresaId, null);
    ItemCatalogo item =
        new ItemCatalogo(
            empresaId,
            categoria,
            request.tipo(),
            request.codigo(),
            request.nome(),
            request.descricao(),
            unidade(request.unidade(), request.tipo()),
            preco(request.preco()),
            clock.instant());
    return salvar(item);
  }

  @Transactional(readOnly = true)
  public ItemCatalogoResponse consultar(UUID id) {
    return resposta(buscarPorIdEEmpresa(id, empresaAtiva.obterId()));
  }

  @Transactional
  public ItemCatalogoResponse atualizar(UUID id, AtualizarItemCatalogoRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    ItemCatalogo item = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(item, request.versao());
    if (item.getTipo() != request.tipo()) {
      throw new ErroDeNegocio(
          422,
          "ITEM_TYPE_IMMUTABLE",
          "Tipo do item imutável",
          "O tipo do item não pode ser alterado após o cadastro.");
    }
    validarCodigoDisponivel(request.codigo(), empresaId, id);
    Categoria categoria = categoriaValida(request.categoriaId(), empresaId, item.getCategoria());
    item.atualizar(
        categoria,
        request.codigo(),
        request.nome(),
        request.descricao(),
        unidade(request.unidade(), request.tipo()),
        preco(request.preco()),
        clock.instant());
    return salvar(item);
  }

  @Transactional
  public ItemCatalogoResponse alterarSituacao(UUID id, AlterarSituacaoCatalogoRequest request) {
    ItemCatalogo item = buscarPorIdEEmpresa(id, empresaAtiva.obterId());
    validarVersao(item, request.versao());
    item.alterarSituacao(request.ativo(), clock.instant());
    return salvar(item);
  }

  @Transactional
  public void excluir(UUID id) {
    ItemCatalogo item = buscarPorIdEEmpresa(id, empresaAtiva.obterId());
    if (verificadoresVinculo.stream().anyMatch(verificador -> verificador.possuiVinculo(id))) {
      throw new ErroDeNegocio(
          422,
          "ITEM_HAS_HISTORY",
          "Item com histórico",
          "O item possui vendas ou compras vinculadas e não pode ser excluído. Inative o item.");
    }
    itemRepositorio.excluir(item);
  }

  private ItemCatalogoResponse salvar(ItemCatalogo item) {
    try {
      return resposta(itemRepositorio.salvar(item));
    } catch (ConflitoCodigoItemCatalogo conflito) {
      throw codigoDuplicado();
    } catch (ObjectOptimisticLockingFailureException conflito) {
      throw versaoDesatualizada();
    }
  }

  private Categoria categoriaValida(UUID categoriaId, UUID empresaId, Categoria categoriaAtual) {
    if (categoriaId == null) {
      return null;
    }
    return categoriaRepositorio
        .buscarPorIdEEmpresa(categoriaId, empresaId)
        .filter(
            categoria ->
                categoria.isAtivo()
                    || (categoriaAtual != null && categoriaId.equals(categoriaAtual.getId())))
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    422,
                    "CATEGORY_INVALID",
                    "Categoria inválida",
                    "A categoria não existe ou está inativa na empresa ativa."));
  }

  private void validarCodigoDisponivel(String codigo, UUID empresaId) {
    if (codigo != null && itemRepositorio.buscarPorCodigoEEmpresa(codigo, empresaId).isPresent()) {
      throw codigoDuplicado();
    }
  }

  private void validarCodigoDisponivel(String codigo, UUID empresaId, UUID id) {
    if (codigo != null
        && itemRepositorio.buscarPorCodigoEEmpresaExcetoId(codigo, empresaId, id).isPresent()) {
      throw codigoDuplicado();
    }
  }

  private ItemCatalogo buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return itemRepositorio
        .buscarPorIdEEmpresa(id, empresaId)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "ITEM_NOT_FOUND",
                    "Item não encontrado",
                    "O item não existe na empresa ativa."));
  }

  private static BigDecimal preco(String valor) {
    return new BigDecimal(valor).setScale(2);
  }

  private static String unidade(String unidade, TipoItemCatalogo tipo) {
    if (unidade != null) {
      return unidade;
    }
    return tipo == TipoItemCatalogo.PRODUTO ? "UN" : "H";
  }

  private static void validarVersao(ItemCatalogo item, Long versaoRecebida) {
    if (item.getVersao() != versaoRecebida) {
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
    if (!List.of("nome", "preco", "criadoEm").contains(sort)) {
      throw new ErroDeNegocio(
          400,
          "VALIDATION_ERROR",
          "Dados inválidos",
          "sort deve ser um dos campos permitidos: nome, preco, criadoEm.");
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

  private static ErroDeNegocio codigoDuplicado() {
    return new ErroDeNegocio(
        409,
        "ITEM_CODE_DUPLICATE",
        "Código já cadastrado",
        "Já existe um item com este código nesta empresa; ele pode estar inativo.");
  }

  private static ErroDeNegocio versaoDesatualizada() {
    return new ErroDeNegocio(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "O item foi alterado por outra operação. Consulte os dados atuais e tente novamente.");
  }

  private static ItemCatalogoResponse resposta(ItemCatalogo item) {
    ItemCatalogoResponse.CategoriaResumo categoria =
        item.getCategoria() == null
            ? null
            : new ItemCatalogoResponse.CategoriaResumo(
                item.getCategoria().getId(), item.getCategoria().getNome());
    return new ItemCatalogoResponse(
        item.getId(),
        item.getTipo(),
        item.getCodigo(),
        item.getNome(),
        item.getDescricao(),
        item.getUnidade(),
        item.getPreco().toPlainString(),
        categoria,
        item.isAtivo(),
        item.getCriadoEm(),
        item.getAtualizadoEm(),
        item.getVersao());
  }
}
