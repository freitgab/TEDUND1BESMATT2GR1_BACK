package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.TecnicoMeteorologista;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TecnicoMeteorologistaRepository extends JpaRepository<TecnicoMeteorologista, Long> {
    Optional<TecnicoMeteorologista> findByEmailIgnoreCase(String email);
    boolean existsByMatriculaIgnoreCase(String matricula);
    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByDistrito_IdAndStatus(Long distritoId, StatusEntidade status);
    boolean existsByUsuario_Id(Long usuarioId);
}
