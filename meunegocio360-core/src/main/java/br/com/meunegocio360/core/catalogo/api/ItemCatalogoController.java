package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.ItemCatalogoService;
import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import br.com.meunegocio360.core.shared.error.ProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/itens-catalogo")
@Tag(name = "Itens de catálogo", description = "Produtos e serviços vendáveis da empresa ativa.")
@SecurityRequirement(name = "bearerAuth")
public class ItemCatalogoController {

  private final ItemCatalogoService itemCatalogoService;

  public ItemCatalogoController(ItemCatalogoService itemCatalogoService) {
    this.itemCatalogoService = itemCatalogoService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Listar produtos e serviços com filtros combináveis e paginação.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de itens.",
        content = @Content(schema = @Schema(implementation = PaginaCatalogoResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Filtro ou ordenação inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ACCESS_DENIED\",\"status\":403}"))),
    @ApiResponse(
        responseCode = "422",
        description = "Não há empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"COMPANY_NOT_ACTIVE\",\"status\":422}")))
  })
  public PaginaCatalogoResponse<ItemCatalogoResponse> listar(
      @Parameter(description = "Busca por nome ou código, ignorando maiúsculas e acentos.")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "Filtro por tipo: PRODUTO ou SERVICO.")
          @RequestParam(required = false)
          TipoItemCatalogo tipo,
      @RequestParam(required = false) UUID categoriaId,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Campo permitido: nome, preco ou criadoEm.")
          @RequestParam(defaultValue = "nome")
          String sort) {
    return itemCatalogoService.listar(q, tipo, categoriaId, situacao, page, size, sort);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Cadastrar produto ou serviço no catálogo.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Item criado.",
        content =
            @Content(
                schema = @Schema(implementation = ItemCatalogoResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44\",\"tipo\":\"PRODUTO\",\"nome\":\"Mouse sem fio\",\"unidade\":\"UN\",\"preco\":\"89.90\",\"categoria\":null,\"ativo\":true,\"versao\":0}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "409",
        description = "Código duplicado nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"ITEM_CODE_DUPLICATE\",\"status\":409}"))),
    @ApiResponse(
        responseCode = "422",
        description = "Empresa inativa ou categoria inválida.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public ResponseEntity<ItemCatalogoResponse> cadastrar(
      @Valid @RequestBody ItemCatalogoRequest request) {
    ItemCatalogoResponse item = itemCatalogoService.cadastrar(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(item.id())
            .toUri();
    return ResponseEntity.created(location).body(item);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Consultar item da empresa ativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Item encontrado."),
    @ApiResponse(
        responseCode = "404",
        description = "Item inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ITEM_NOT_FOUND\",\"status\":404}")))
  })
  public ItemCatalogoResponse consultar(@PathVariable UUID id) {
    return itemCatalogoService.consultar(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Atualizar item com controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Item atualizado."),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Item inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Código duplicado ou versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Tipo imutável ou categoria inválida.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public ItemCatalogoResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody AtualizarItemCatalogoRequest request) {
    return itemCatalogoService.atualizar(id, request);
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Inativar ou reativar item de catálogo.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação atualizada."),
    @ApiResponse(
        responseCode = "404",
        description = "Item inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public ItemCatalogoResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody AlterarSituacaoCatalogoRequest request) {
    return itemCatalogoService.alterarSituacao(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Excluir item apenas se não houver vendas ou compras vinculadas.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Item excluído."),
    @ApiResponse(
        responseCode = "404",
        description = "Item inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Item possui histórico e deve ser inativado.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"ITEM_HAS_HISTORY\",\"status\":422}")))
  })
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    itemCatalogoService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
