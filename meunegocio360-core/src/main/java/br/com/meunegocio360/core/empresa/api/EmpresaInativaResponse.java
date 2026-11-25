package br.com.meunegocio360.core.empresa.api;

import java.util.UUID;

public record EmpresaInativaResponse(
    UUID id, String razaoSocial, String nomeFantasia, String cnpj, long versao) {}
