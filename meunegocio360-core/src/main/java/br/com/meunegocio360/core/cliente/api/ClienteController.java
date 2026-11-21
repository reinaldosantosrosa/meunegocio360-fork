package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ClienteService;
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
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/clientes")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
@Tag(
    name = "Clientes",
    description = "Cadastro, pesquisa e manutenção de clientes no contexto da empresa ativa.")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

  private final ClienteService clienteService;

  public ClienteController(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @GetMapping
  @Operation(summary = "Pesquisar clientes de forma paginada.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de clientes.",
        content = @Content(schema = @Schema(implementation = ClientePaginaResponse.class))),
    @ApiResponse(responseCode = "400", description = "Parâmetro inválido."),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "422", description = "COMPANY_NOT_ACTIVE")
  })
  public ClientePaginaResponse pesquisar(
      @Parameter(description = "Busca por trecho do nome ou dos dígitos do documento.")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "ATIVO (padrão), INATIVO ou TODOS.")
          @RequestParam(defaultValue = "ATIVO")
          String situacao,
      @Parameter(description = "Índice da página, começando em zero.")
          @RequestParam(defaultValue = "0")
          String page,
      @Parameter(description = "Tamanho da página, de 1 a 100.") @RequestParam(defaultValue = "20")
          String size,
      @Parameter(description = "nome (padrão) ou criadoEm; aceita direção asc/desc.")
          @RequestParam(defaultValue = "nome")
          String sort) {
    return ClientePaginaResponse.de(clienteService.pesquisar(q, situacao, page, size, sort));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar cliente na empresa ativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cliente encontrado."),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "CUSTOMER_NOT_FOUND"),
    @ApiResponse(responseCode = "422", description = "COMPANY_NOT_ACTIVE")
  })
  public ClienteResponse buscar(@PathVariable UUID id) {
    return ClienteResponse.de(clienteService.buscar(id));
  }

  @PostMapping
  @Operation(summary = "Cadastrar cliente (ADMIN ou OPERADOR_COMERCIAL).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Cliente criado com Location.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClienteResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90\",\"tipoPessoa\":\"FISICA\",\"nome\":\"Maria da Silva\",\"documento\":\"52998224725\",\"email\":\"maria@exemplo.com.br\",\"telefone\":\"11987654321\",\"ativo\":true,\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":0}"))),
    @ApiResponse(
        responseCode = "400",
        description = "VALIDATION_ERROR",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "403",
        description = "ACCESS_DENIED",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ACCESS_DENIED\",\"status\":403}"))),
    @ApiResponse(
        responseCode = "409",
        description = "CUSTOMER_DOCUMENT_DUPLICATE",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"CUSTOMER_DOCUMENT_DUPLICATE\",\"status\":409}"))),
    @ApiResponse(
        responseCode = "422",
        description = "COMPANY_NOT_ACTIVE",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"COMPANY_NOT_ACTIVE\",\"status\":422}")))
  })
  public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
    ClienteResponse resposta =
        ClienteResponse.de(
            clienteService.cadastrar(
                request.tipoPessoa(),
                request.nome(),
                request.documento(),
                request.email(),
                request.telefone()));
    return ResponseEntity.created(URI.create("/api/v1/clientes/" + resposta.id())).body(resposta);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Editar cliente com controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cliente atualizado."),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "CUSTOMER_NOT_FOUND"),
    @ApiResponse(
        responseCode = "409",
        description = "CUSTOMER_DOCUMENT_DUPLICATE ou VERSION_CONFLICT"),
    @ApiResponse(responseCode = "422", description = "COMPANY_NOT_ACTIVE")
  })
  public ClienteResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody ClienteAtualizacaoRequest request) {
    return ClienteResponse.de(
        clienteService.atualizar(
            id,
            request.tipoPessoa(),
            request.nome(),
            request.documento(),
            request.email(),
            request.telefone(),
            request.versao()));
  }

  @PatchMapping("/{id}/situacao")
  @Operation(summary = "Inativar ou reativar cliente.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação do cliente atualizada."),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "CUSTOMER_NOT_FOUND"),
    @ApiResponse(responseCode = "409", description = "VERSION_CONFLICT"),
    @ApiResponse(responseCode = "422", description = "COMPANY_NOT_ACTIVE")
  })
  public ClienteResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody ClienteSituacaoRequest request) {
    return ClienteResponse.de(
        clienteService.alterarSituacao(id, request.ativo(), request.versao()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Excluir cliente somente se não houver vínculos.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Cliente excluído."),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "CUSTOMER_NOT_FOUND"),
    @ApiResponse(responseCode = "422", description = "CUSTOMER_HAS_HISTORY ou COMPANY_NOT_ACTIVE")
  })
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    clienteService.excluir(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
