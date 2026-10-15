package br.edu.ucsal.clima.auth;

import br.edu.ucsal.clima.auth.dto.LoginRequest;
import br.edu.ucsal.clima.auth.dto.LoginResponse;
import br.edu.ucsal.clima.auth.dto.MeResponse;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import br.edu.ucsal.clima.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        Usuario usuario = buscarUsuario(authentication.getName());
        return ResponseEntity.ok(new LoginResponse(jwtService.gerarToken(usuario.getEmail()),
                usuario.getNomeCompleto(), usuario.getEmail(), perfis(usuario)));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        Usuario usuario = buscarUsuario(authentication.getName());
        return ResponseEntity.ok(new MeResponse(usuario.getNomeCompleto(), usuario.getEmail(), perfis(usuario)));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));
    }

    private List<NomePerfil> perfis(Usuario usuario) {
        return usuario.getPerfis().stream().map(perfil -> perfil.getNome())
                .sorted(Comparator.comparing(Enum::name)).toList();
    }
}
