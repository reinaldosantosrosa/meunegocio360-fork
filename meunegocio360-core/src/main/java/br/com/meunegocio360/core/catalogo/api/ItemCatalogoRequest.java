package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.domain.TipoItemCatalogo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ItemCatalogoRequest(
    @NotNull(message = "é obrigatório") TipoItemCatalogo tipo,
    @Size(max = 40, message = "deve conter no máximo 40 caracteres")
        @Pattern(regexp = ".{1,40}", message = "não pode estar vazio")
        String codigo,
    @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 150, message = "deve conter de 2 a 150 caracteres")
        String nome,
    @Size(max = 500, message = "deve conter no máximo 500 caracteres") String descricao,
    @Pattern(regexp = "[A-Z]{1,6}", message = "deve conter de 1 a 6 letras maiúsculas")
        String unidade,
    @NotBlank(message = "é obrigatório")
        @Pattern(
            regexp = "\\d{1,10}(\\.\\d{1,2})?",
            message = "deve ser um decimal não negativo com no máximo 2 casas")
        String preco,
    UUID categoriaId) {

  public ItemCatalogoRequest {
    codigo = trimOpcional(codigo);
    nome = trim(nome);
    descricao = trimOpcional(descricao);
    unidade = trim(unidade);
    preco = trim(preco);
  }

  private static String trim(String value) {
    return value == null ? null : value.trim();
  }

  private static String trimOpcional(String value) {
    String trimmed = trim(value);
    return trimmed == null || trimmed.isEmpty() ? null : trimmed;
  }
}
