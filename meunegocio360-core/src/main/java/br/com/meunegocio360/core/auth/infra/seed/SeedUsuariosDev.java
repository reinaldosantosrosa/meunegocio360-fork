package br.com.meunegocio360.core.auth.infra.seed;

import br.com.meunegocio360.core.auth.domain.CredencialService;
import br.com.meunegocio360.core.auth.domain.PerfilUsuario;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepositorio;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class SeedUsuariosDev implements ApplicationRunner {

  private final UsuarioRepositorio usuarioRepositorio;
  private final CredencialService credencialService;
  private final Clock clock;
  private final String senhaAdmin;
  private final String senhaComercial;
  private final String senhaFinanceiro;

  public SeedUsuariosDev(
      UsuarioRepositorio usuarioRepositorio,
      CredencialService credencialService,
      Clock clock,
      @Value("${SEED_ADMIN_SENHA:}") String senhaAdmin,
      @Value("${SEED_USUARIO_SENHA:}") String senhaComercial,
      @Value("${SEED_FINANCEIRO_SENHA:}") String senhaFinanceiro) {
    this.usuarioRepositorio = usuarioRepositorio;
    this.credencialService = credencialService;
    this.clock = clock;
    this.senhaAdmin = senhaAdmin;
    this.senhaComercial = senhaComercial;
    this.senhaFinanceiro = senhaFinanceiro;
  }

  @Override
  public void run(ApplicationArguments arguments) {
    validarSenha("SEED_ADMIN_SENHA", senhaAdmin);
    validarSenha("SEED_USUARIO_SENHA", senhaComercial);
    validarSenha("SEED_FINANCEIRO_SENHA", senhaFinanceiro);
    criarSeAusente("Administrador", "admin@meunegocio360.com.br", senhaAdmin, PerfilUsuario.ADMIN);
    criarSeAusente(
        "Operador Comercial",
        "usuario@meunegocio360.com.br",
        senhaComercial,
        PerfilUsuario.OPERADOR_COMERCIAL);
    criarSeAusente(
        "Operador Financeiro",
        "financeiro@meunegocio360.com.br",
        senhaFinanceiro,
        PerfilUsuario.OPERADOR_FINANCEIRO);
  }

  private void criarSeAusente(String nome, String email, String senha, PerfilUsuario perfil) {
    if (!usuarioRepositorio.existePorEmail(email)) {
      usuarioRepositorio.salvar(
          new Usuario(nome, email, credencialService.codificar(senha), perfil, clock.instant()));
    }
  }

  private static void validarSenha(String variavel, String senha) {
    if (senha.length() < 8 || senha.length() > 72) {
      throw new IllegalStateException(variavel + " deve conter entre 8 e 72 caracteres.");
    }
  }
}
