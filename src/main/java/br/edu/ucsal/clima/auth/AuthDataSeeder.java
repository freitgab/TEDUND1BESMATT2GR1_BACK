package br.edu.ucsal.clima.auth;

import br.edu.ucsal.clima.domain.entity.Perfil;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.repository.PerfilRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.HashSet;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AuthDataSeeder implements CommandLineRunner {
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.password}")
    private String senhaInicial;

    @Override
    @Transactional
    public void run(String... args) {
        for (NomePerfil nome : NomePerfil.values()) {
            perfilRepository.findByNome(nome).orElseGet(() -> {
                var perfil = new Perfil();
                perfil.setNome(nome);
                perfil.setDescricao(nome.name());
                return perfilRepository.save(perfil);
            });
        }
        criarUsuario("Administrador de Teste", "admin@clima.gov.br", NomePerfil.values());
        criarUsuario("Gestor de Teste", "gestor@clima.gov.br", NomePerfil.ADMINISTRADOR);
        criarUsuario("Tecnico de Teste", "tecnico@clima.gov.br", NomePerfil.COLABORADOR);
        criarUsuario("Pesquisador de Teste", "pesquisador@clima.gov.br", NomePerfil.USUARIO);
    }

    private void criarUsuario(String nome, String email, NomePerfil... nomesPerfis) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) return;
        var usuario = new Usuario();
        usuario.setNomeCompleto(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senhaInicial));
        usuario.setPerfis(new HashSet<>(Arrays.stream(nomesPerfis)
                .map(nomePerfil -> perfilRepository.findByNome(nomePerfil).orElseThrow()).toList()));
        usuarioRepository.save(usuario);
    }
}
