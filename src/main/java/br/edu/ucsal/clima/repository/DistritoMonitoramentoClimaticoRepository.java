package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistritoMonitoramentoClimaticoRepository extends JpaRepository<DistritoMonitoramentoClimatico, Long> {
    Optional<DistritoMonitoramentoClimatico> findByNomeIgnoreCase(String nome);
    List<DistritoMonitoramentoClimatico> findByOrgaoGestor_Id(Long orgaoGestorId);
    boolean existsByOrgaoGestor_IdAndStatus(Long orgaoGestorId, StatusEntidade status);
    boolean existsByMeteorologistaChefe_IdAndIdNot(Long tecnicoId, Long distritoId);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByMeteorologistaChefe_Id(Long tecnicoId);
}
