package br.com.meunegocio360.core.cliente.domain;

import java.util.List;

public record PaginaClientes(List<Cliente> clientes, long totalElements) {}
