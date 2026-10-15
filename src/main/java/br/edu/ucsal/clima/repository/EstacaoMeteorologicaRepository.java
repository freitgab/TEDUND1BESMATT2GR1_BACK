package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacaoMeteorologicaRepository extends JpaRepository<EstacaoMeteorologica, Long> { }
