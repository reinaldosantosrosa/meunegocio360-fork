package br.com.meunegocio360.core.empresa.application;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepositorio;
import br.com.meunegocio360.core.empresa.domain.ErroEmpresa;
import java.time.Clock;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

  private final EmpresaRepositorio empresaRepositorio;
  private final Clock clock;

  public EmpresaService(EmpresaRepositorio empresaRepositorio, Clock clock) {
    this.empresaRepositorio = empresaRepositorio;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Empresa buscarAtiva() {
    return empresaRepositorio.buscarAtiva().orElseThrow(ErroEmpresa::empresaNaoRegistrada);
  }

  @Transactional
  public Empresa cadastrar(
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda) {
    if (empresaRepositorio.existePorCnpj(cnpj)) {
      throw ErroEmpresa.cnpjDuplicado();
    }
    if (empresaRepositorio.buscarAtiva().isPresent()) {
      throw ErroEmpresa.empresaAtivaJaExiste();
    }
    return empresaRepositorio.salvarEAtualizar(
        new Empresa(
            razaoSocial, nomeFantasia, cnpj, email, telefone, fusoHorario, moeda, clock.instant()));
  }

  @Transactional
  public Empresa atualizar(
      UUID id,
      long versao,
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda) {
    Empresa empresa =
        empresaRepositorio.buscarPorId(id).orElseThrow(ErroEmpresa::empresaNaoEncontrada);
    validarVersao(empresa, versao);
    if (!empresa.getCnpj().equals(cnpj) && empresaRepositorio.existePorCnpj(cnpj)) {
      throw ErroEmpresa.cnpjDuplicado();
    }
    empresa.atualizar(
        razaoSocial, nomeFantasia, cnpj, email, telefone, fusoHorario, moeda, clock.instant());
    return salvarComControleDeConcorrencia(empresa);
  }

  @Transactional
  public Empresa alterarSituacao(UUID id, boolean ativa, long versao) {
    Empresa empresa =
        empresaRepositorio.buscarPorId(id).orElseThrow(ErroEmpresa::empresaNaoEncontrada);
    validarVersao(empresa, versao);
    empresa.alterarSituacao(ativa, clock.instant());
    return salvarComControleDeConcorrencia(empresa);
  }

  private Empresa salvarComControleDeConcorrencia(Empresa empresa) {
    try {
      return empresaRepositorio.salvarEAtualizar(empresa);
    } catch (ObjectOptimisticLockingFailureException exception) {
      throw ErroEmpresa.versaoConflitante();
    }
  }

  private static void validarVersao(Empresa empresa, long versao) {
    if (empresa.getVersao() != versao) {
      throw ErroEmpresa.versaoConflitante();
    }
  }
}
