package br.edu.ucsal.clima.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
@Getter @Setter @NoArgsConstructor
public class LogAuditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 160)
    private String usuarioEmail;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(nullable = false, length = 100)
    private String entidadeAfetada;

    @Column(length = 2000)
    private String detalhes;

    @PrePersist
    void inicializar() { if (dataHora == null) dataHora = LocalDateTime.now(); }
}
