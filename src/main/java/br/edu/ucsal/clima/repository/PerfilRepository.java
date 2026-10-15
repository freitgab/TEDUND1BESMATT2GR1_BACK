package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.Perfil;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(NomePerfil nome);
}
