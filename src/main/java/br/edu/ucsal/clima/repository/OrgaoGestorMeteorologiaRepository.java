package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.OrgaoGestorMeteorologia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgaoGestorMeteorologiaRepository extends JpaRepository<OrgaoGestorMeteorologia, Long> {
    boolean existsBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByDiretorGeral_Id(Long diretorGeralId);
    boolean existsByDiretorGeral_IdAndIdNot(Long diretorGeralId, Long id);
}
