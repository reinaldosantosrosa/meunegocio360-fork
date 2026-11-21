package br.com.meunegocio360.core.fornecedor.domain;

import java.util.List;

public record PaginaFornecedores(
    List<Fornecedor> content, int page, int size, long totalElements, int totalPages) {}
