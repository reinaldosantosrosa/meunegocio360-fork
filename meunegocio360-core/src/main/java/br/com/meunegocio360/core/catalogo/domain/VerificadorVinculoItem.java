package br.com.meunegocio360.core.catalogo.domain;

import java.util.UUID;

public interface VerificadorVinculoItem {

  boolean possuiVinculo(UUID itemId);
}
