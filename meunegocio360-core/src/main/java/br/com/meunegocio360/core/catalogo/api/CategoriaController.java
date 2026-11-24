package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.CategoriaService;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Categorias planas do catálogo da empresa ativa.")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

  private final CategoriaService categoriaService;

  public CategoriaController(CategoriaService categoriaService) {
    this.categoriaService = categoriaService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Listar categorias paginadas, com busca e filtro por situação.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de categorias.",
        content = @Content(schema = @Schema(implementation = PaginaCatalogoResponse.class))),
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
  public PaginaCatalogoResponse<CategoriaResponse> listar(
      @Parameter(description = "Busca por nome, ignorando maiúsculas e acentos.")
          @RequestParam(required = false)
          String q,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Campo permitido para ordenação: nome.")
          @RequestParam(defaultValue = "nome")
          String sort) {
    return categoriaService.listar(q, situacao, page, size, sort);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Cadastrar categoria para a empresa ativa.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Categoria criada.",
        content =
            @Content(
                schema = @Schema(implementation = CategoriaResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44\",\"nome\":\"Informática\",\"ativo\":true,\"versao\":0}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Nome inválido.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "409",
        description = "Nome duplicado na empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"CATEGORY_NAME_DUPLICATE\",\"status\":409}"))),
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
  public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
    CategoriaResponse categoria = categoriaService.cadastrar(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(categoria.id())
            .toUri();
    return ResponseEntity.created(location).body(categoria);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Renomear categoria com controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Categoria atualizada."),
    @ApiResponse(
        responseCode = "404",
        description = "Categoria inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"CATEGORY_NOT_FOUND\",\"status\":404}"))),
    @ApiResponse(
        responseCode = "409",
        description = "Nome duplicado ou versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public CategoriaResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody AtualizarCategoriaRequest request) {
    return categoriaService.atualizar(id, request);
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Inativar ou reativar categoria sem alterar os itens vinculados.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação atualizada."),
    @ApiResponse(
        responseCode = "404",
        description = "Categoria inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public CategoriaResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody AlterarSituacaoCatalogoRequest request) {
    return categoriaService.alterarSituacao(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Excluir categoria somente se não houver itens vinculados.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Categoria excluída."),
    @ApiResponse(
        responseCode = "404",
        description = "Categoria inexistente na empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Categoria vinculada a itens.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"CATEGORY_IN_USE\",\"status\":422}")))
  })
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    categoriaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
