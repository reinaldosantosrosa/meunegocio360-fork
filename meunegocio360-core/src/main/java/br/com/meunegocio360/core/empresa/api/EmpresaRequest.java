package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.shared.validation.Cnpj;
import br.com.meunegocio360.core.shared.validation.FusoIana;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
    @NotBlank @Size(min = 3, max = 150) String razaoSocial,
    @Size(max = 150) String nomeFantasia,
    @NotBlank @Pattern(regexp = "\\d{14}") @Cnpj String cnpj,
    @Email @Size(max = 160) String email,
    @Pattern(regexp = "\\d{10,11}") String telefone,
    @NotBlank @FusoIana String fusoHorario,
    @NotBlank @Pattern(regexp = "BRL") String moeda) {

  public EmpresaRequest {
    razaoSocial = aparar(razaoSocial);
    nomeFantasia = aparar(nomeFantasia);
    cnpj = aparar(cnpj);
    email = aparar(email);
    telefone = aparar(telefone);
    fusoHorario = aparar(fusoHorario);
    moeda = aparar(moeda);
    if (fusoHorario == null) {
      fusoHorario = "America/Sao_Paulo";
    }
    if (moeda == null) {
      moeda = "BRL";
    }
  }

  private static String aparar(String valor) {
    return valor == null ? null : valor.trim();
  }
}
