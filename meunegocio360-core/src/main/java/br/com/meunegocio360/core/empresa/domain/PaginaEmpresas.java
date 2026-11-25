package br.com.meunegocio360.core.empresa.domain;

import java.util.List;

public record PaginaEmpresas(
    List<Empresa> content, int page, int size, long totalElements, int totalPages) {}
