package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.SoftwareColetaTransmissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoftwareColetaTransmissaoRepository extends JpaRepository<SoftwareColetaTransmissao, Long> {
    List<SoftwareColetaTransmissao> findByEstacao_Id(Long estacaoId);
}
