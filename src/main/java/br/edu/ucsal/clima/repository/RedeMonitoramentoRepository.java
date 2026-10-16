package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.RedeMonitoramento;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RedeMonitoramentoRepository extends JpaRepository<RedeMonitoramento, Long> {
    Optional<RedeMonitoramento> findBySiglaIgnoreCase(String sigla);
    List<RedeMonitoramento> findByDistrito_Id(Long distritoId);
    boolean existsBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
    boolean existsByDistrito_IdAndStatus(Long distritoId, StatusEntidade status);
}
