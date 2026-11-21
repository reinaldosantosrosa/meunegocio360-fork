package br.com.meunegocio360.core.cliente.application;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import java.util.List;

public record ResultadoBuscaClientes(
    List<Cliente> clientes, int page, int size, long totalElements, int totalPages) {}
