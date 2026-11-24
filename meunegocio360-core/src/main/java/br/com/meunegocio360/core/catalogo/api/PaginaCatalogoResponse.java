package br.com.meunegocio360.core.catalogo.api;

import java.util.List;

public record PaginaCatalogoResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
