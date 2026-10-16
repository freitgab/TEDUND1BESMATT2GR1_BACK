package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.OrgaoGestorMeteorologia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgaoGestorMeteorologiaRepository extends JpaRepository<OrgaoGestorMeteorologia, Long> {
    Optional<OrgaoGestorMeteorologia> findBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByDiretorGeral_Id(Long diretorGeralId);
    boolean existsByDiretorGeral_IdAndIdNot(Long diretorGeralId, Long id);
}
