package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.domain.TipoPessoa;
import br.com.meunegocio360.core.shared.validation.DocumentoBrasil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@ValidDocumentoCliente
public record ClienteRequest(
    @NotNull TipoPessoa tipoPessoa,
    @NotBlank @Size(min = 2, max = 150) String nome,
    @NotBlank @Size(max = 18) String documento,
    @Email @Size(max = 160) String email,
    @Pattern(regexp = "\\d{10,11}") String telefone)
    implements DadosCliente {

  public ClienteRequest {
    nome = aparar(nome);
    documento = DocumentoBrasil.normalizar(documento);
    email = aparar(email);
    telefone = aparar(telefone);
  }

  private static String aparar(String valor) {
    return valor == null ? null : valor.trim();
  }
}
