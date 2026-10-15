package br.edu.ucsal.clima.repository;

import br.edu.ucsal.clima.domain.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> { }
