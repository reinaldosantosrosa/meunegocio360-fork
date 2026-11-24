package br.com.meunegocio360.core.catalogo.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AtualizarCategoriaRequest(
    @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 80, message = "deve conter de 2 a 80 caracteres")
        String nome,
    @NotNull(message = "é obrigatório") @PositiveOrZero(message = "deve ser maior ou igual a 0")
        Long versao) {

  public AtualizarCategoriaRequest {
    nome = trim(nome);
  }

  private static String trim(String value) {
    return value == null ? null : value.trim();
  }
}
