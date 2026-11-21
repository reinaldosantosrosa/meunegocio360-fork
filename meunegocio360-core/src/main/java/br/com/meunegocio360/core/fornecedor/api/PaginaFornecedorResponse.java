package br.com.meunegocio360.core.fornecedor.api;

import java.util.List;

public record PaginaFornecedorResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
