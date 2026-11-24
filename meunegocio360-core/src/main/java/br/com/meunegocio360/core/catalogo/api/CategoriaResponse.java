package br.com.meunegocio360.core.catalogo.api;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponse(
    UUID id, String nome, boolean ativo, Instant criadoEm, Instant atualizadoEm, long versao) {}
