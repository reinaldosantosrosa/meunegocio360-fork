package br.com.meunegocio360.core.empresa.domain;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepositorio {

  Optional<Empresa> buscarAtiva();

  Optional<Empresa> buscarPorId(UUID id);

  boolean existePorCnpj(String cnpj);

  Empresa salvarEAtualizar(Empresa empresa);
}
