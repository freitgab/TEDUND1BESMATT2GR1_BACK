package br.edu.ucsal.clima.security;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
        var autoridades = usuario.getPerfis().stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome().name()))
                .toList();
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(autoridades)
                .disabled(usuario.getStatus() != StatusEntidade.ATIVO)
                .build();
    }
}
