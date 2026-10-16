package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByEstacao_Id(Long estacaoId);
}
