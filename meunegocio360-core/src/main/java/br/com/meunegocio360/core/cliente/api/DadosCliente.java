package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.domain.TipoPessoa;

public interface DadosCliente {

  TipoPessoa tipoPessoa();

  String documento();
}
