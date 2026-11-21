package br.com.meunegocio360.core.cliente.api;

import jakarta.validation.constraints.NotNull;

public record ClienteSituacaoRequest(@NotNull Boolean ativo, @NotNull Long versao) {}
