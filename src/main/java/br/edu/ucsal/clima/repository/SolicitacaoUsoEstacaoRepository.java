package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.SolicitacaoUsoEstacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface SolicitacaoUsoEstacaoRepository extends JpaRepository<SolicitacaoUsoEstacao, Long> {

    @Query("""
        SELECT COUNT(s) > 0 FROM SolicitacaoUsoEstacao s
        WHERE s.estacao.id = :estacaoId
          AND s.dataUso = :dataUso
          AND s.status = br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso.APROVADA
          AND (:horaInicio < s.horaFim AND :horaFim > s.horaInicio)
          AND (:ignorarSolicitacaoId IS NULL OR s.id <> :ignorarSolicitacaoId)
    """)
    boolean existeConflitoHorario(@Param("estacaoId") Long estacaoId,
                                  @Param("dataUso") LocalDate dataUso,
                                  @Param("horaInicio") LocalTime horaInicio,
                                  @Param("horaFim") LocalTime horaFim,
                                  @Param("ignorarSolicitacaoId") Long ignorarSolicitacaoId);

    List<SolicitacaoUsoEstacao> findBySolicitante_IdOrderByDataSolicitacaoDesc(Long solicitanteId);

    List<SolicitacaoUsoEstacao> findBySolicitante_Id(Long solicitanteId);

    List<SolicitacaoUsoEstacao> findAllByOrderByDataSolicitacaoDesc();

    boolean existsByEstacao_Id(Long estacaoId);

    boolean existsByEstacao_IdAndStatusIn(Long estacaoId, Collection<StatusSolicitacaoUso> statuses);
}
