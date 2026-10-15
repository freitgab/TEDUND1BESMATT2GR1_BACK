package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> { }
