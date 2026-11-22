package br.com.meunegocio360.core.fornecedor.domain;

import java.util.UUID;

public interface VerificadorVinculoFornecedor {

  boolean possuiVinculo(UUID fornecedorId);
}
