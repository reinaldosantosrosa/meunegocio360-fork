package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.fornecedor.application.FornecedorService;
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
@RequestMapping("/api/v1/fornecedores")
@Tag(name = "Fornecedores", description = "Cadastro e consulta de fornecedores da empresa ativa.")
@SecurityRequirement(name = "bearerAuth")
public class FornecedorController {

  private final FornecedorService fornecedorService;

  public FornecedorController(FornecedorService fornecedorService) {
    this.fornecedorService = fornecedorService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Listar fornecedores paginados, com busca e filtro por situação.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de fornecedores.",
        content = @Content(schema = @Schema(implementation = PaginaFornecedorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "O perfil comercial não pode acessar fornecedores.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
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
  public PaginaFornecedorResponse<FornecedorResponse> listar(
      @Parameter(description = "Busca por nome (ignorando acentos e caixa) ou documento.")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "Situação a listar; o padrão é ATIVO.")
          @RequestParam(defaultValue = "ATIVO")
          String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Campo permitido para ordenação: nome ou criadoEm.")
          @RequestParam(defaultValue = "nome")
          String sort) {
    return fornecedorService.listar(q, situacao, page, size, sort);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Cadastrar fornecedor pessoa física ou jurídica.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Fornecedor criado."),
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
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ACCESS_DENIED\",\"status\":403}"))),
    @ApiResponse(
        responseCode = "409",
        description = "Documento já cadastrado nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"SUPPLIER_DOCUMENT_DUPLICATE\",\"status\":409}"))),
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
  public ResponseEntity<FornecedorResponse> cadastrar(
      @Valid @RequestBody FornecedorRequest request) {
    FornecedorResponse fornecedor = fornecedorService.cadastrar(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(fornecedor.id())
            .toUri();
    return ResponseEntity.created(location).body(fornecedor);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Consultar fornecedor da empresa ativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Fornecedor encontrado."),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Fornecedor inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"SUPPLIER_NOT_FOUND\",\"status\":404}")))
  })
  public FornecedorResponse consultar(@PathVariable UUID id) {
    return fornecedorService.consultar(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Atualizar dados de fornecedor com controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Fornecedor atualizado."),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
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
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Fornecedor inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Documento duplicado ou versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public FornecedorResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody AtualizarFornecedorRequest request) {
    return fornecedorService.atualizar(id, request);
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Inativar ou reativar fornecedor.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação atualizada."),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Fornecedor inexistente nesta empresa.",
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
  public FornecedorResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody AlterarSituacaoFornecedorRequest request) {
    return fornecedorService.alterarSituacao(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_FINANCEIRO')")
  @Operation(summary = "Excluir fornecedor somente se nenhum vínculo histórico existir.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Fornecedor excluído."),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Fornecedor inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Fornecedor possui histórico e não pode ser excluído.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"SUPPLIER_HAS_HISTORY\",\"status\":422}")))
  })
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    fornecedorService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
