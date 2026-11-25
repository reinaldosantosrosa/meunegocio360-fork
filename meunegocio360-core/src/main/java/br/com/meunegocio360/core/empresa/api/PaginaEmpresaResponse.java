package br.com.meunegocio360.core.empresa.api;

import java.util.List;

public record PaginaEmpresaResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
