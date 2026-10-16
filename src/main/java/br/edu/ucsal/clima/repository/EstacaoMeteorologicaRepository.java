package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstacaoMeteorologicaRepository extends JpaRepository<EstacaoMeteorologica, Long> {
    Optional<EstacaoMeteorologica> findBySiglaIgnoreCase(String sigla);
    boolean existsByRede_IdAndStatusNot(Long redeId, StatusEstacao status);
    boolean existsBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
