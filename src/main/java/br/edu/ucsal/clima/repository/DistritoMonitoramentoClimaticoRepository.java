package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistritoMonitoramentoClimaticoRepository extends JpaRepository<DistritoMonitoramentoClimatico, Long> {
    boolean existsByOrgaoGestor_IdAndStatus(Long orgaoGestorId, StatusEntidade status);
    boolean existsByMeteorologistaChefe_IdAndIdNot(Long tecnicoId, Long distritoId);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByMeteorologistaChefe_Id(Long tecnicoId);
}
