package br.com.meunegocio360.core.catalogo.domain;

import java.util.List;

public record PaginaCatalogo<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
