package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.application.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/empresa")
@Tag(
    name = "Empresa",
    description =
        "Cadastro e parâmetros básicos da empresa. EmpresaAtiva resolve o contexto no servidor; "
            + "operações de negócio sem empresa ativa retornam COMPANY_NOT_ACTIVE (422).")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

  private final EmpresaService empresaService;

  public EmpresaController(EmpresaService empresaService) {
    this.empresaService = empresaService;
  }

  @GetMapping
  @Operation(summary = "Consultar a empresa ativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Empresa ativa."),
    @ApiResponse(responseCode = "404", description = "COMPANY_NOT_REGISTERED")
  })
  public EmpresaResponse buscar() {
    return EmpresaResponse.de(empresaService.buscarAtiva());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Cadastrar a empresa ativa (ADMIN).")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Empresa criada com Location."),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(
        responseCode = "409",
        description = "COMPANY_CNPJ_DUPLICATE ou COMPANY_ALREADY_ACTIVE")
  })
  public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest request) {
    EmpresaResponse resposta =
        EmpresaResponse.de(
            empresaService.cadastrar(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj(),
                request.email(),
                request.telefone(),
                request.fusoHorario(),
                request.moeda()));
    return ResponseEntity.created(URI.create("/api/v1/empresa/" + resposta.id())).body(resposta);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Atualizar os dados da empresa (ADMIN), com controle de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Empresa atualizada."),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "COMPANY_NOT_FOUND"),
    @ApiResponse(responseCode = "409", description = "COMPANY_CNPJ_DUPLICATE ou VERSION_CONFLICT")
  })
  public EmpresaResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody EmpresaAtualizacaoRequest request) {
    return EmpresaResponse.de(
        empresaService.atualizar(
            id,
            request.versao(),
            request.razaoSocial(),
            request.nomeFantasia(),
            request.cnpj(),
            request.email(),
            request.telefone(),
            request.fusoHorario(),
            request.moeda()));
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Inativar ou reativar a empresa (ADMIN).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação atualizada."),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED"),
    @ApiResponse(responseCode = "404", description = "COMPANY_NOT_FOUND"),
    @ApiResponse(responseCode = "409", description = "COMPANY_ALREADY_ACTIVE ou VERSION_CONFLICT")
  })
  public ResponseEntity<EmpresaResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody EmpresaSituacaoRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            EmpresaResponse.de(
                empresaService.alterarSituacao(id, request.ativa(), request.versao())));
  }
}
