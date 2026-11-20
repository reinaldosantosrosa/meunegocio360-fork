package br.com.meunegocio360.core.empresa.api;

import jakarta.validation.constraints.NotNull;

public record EmpresaSituacaoRequest(@NotNull Boolean ativa, @NotNull Long versao) {}
