package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import java.time.Instant;
import java.util.UUID;

public record EmpresaResponse(
    UUID id,
    String razaoSocial,
    String nomeFantasia,
    String cnpj,
    String email,
    String telefone,
    String fusoHorario,
    String moeda,
    boolean ativa,
    Instant criadoEm,
    Instant atualizadoEm,
    long versao) {

  public static EmpresaResponse de(Empresa empresa) {
    return new EmpresaResponse(
        empresa.getId(),
        empresa.getRazaoSocial(),
        empresa.getNomeFantasia(),
        empresa.getCnpj(),
        empresa.getEmail(),
        empresa.getTelefone(),
        empresa.getFusoHorario(),
        empresa.getMoeda(),
        empresa.isAtiva(),
        empresa.getCriadoEm(),
        empresa.getAtualizadoEm(),
        empresa.getVersao());
  }
}
