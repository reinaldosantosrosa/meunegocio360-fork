package br.com.meunegocio360.core.catalogo.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AlterarSituacaoCatalogoRequest(
    @NotNull(message = "é obrigatório") Boolean ativo,
    @NotNull(message = "é obrigatório") @PositiveOrZero(message = "deve ser maior ou igual a 0")
        Long versao) {}
