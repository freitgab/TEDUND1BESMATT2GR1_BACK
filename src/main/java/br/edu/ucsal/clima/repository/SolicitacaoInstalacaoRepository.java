package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.SolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoInstalacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SolicitacaoInstalacaoRepository extends JpaRepository<SolicitacaoInstalacao, Long> {
    List<SolicitacaoInstalacao> findBySolicitante_IdOrderByDataSolicitacaoDesc(Long solicitanteId);
    List<SolicitacaoInstalacao> findBySolicitante_Id(Long solicitanteId);
    List<SolicitacaoInstalacao> findAllByOrderByDataSolicitacaoDesc();
    boolean existsByEstacao_Id(Long estacaoId);
    boolean existsByEstacao_IdAndStatusIn(Long estacaoId, Collection<StatusSolicitacaoInstalacao> statuses);
}
