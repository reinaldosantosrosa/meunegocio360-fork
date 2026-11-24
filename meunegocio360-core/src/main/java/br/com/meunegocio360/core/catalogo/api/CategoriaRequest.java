package br.com.meunegocio360.core.catalogo.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 80, message = "deve conter de 2 a 80 caracteres")
        String nome) {

  public CategoriaRequest {
    nome = trim(nome);
  }

  private static String trim(String value) {
    return value == null ? null : value.trim();
  }
}
