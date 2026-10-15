package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "solicitacoes_uso_estacao", indexes = @Index(name = "idx_solicitacao_estacao_data_status", columnList = "estacao_id,data_uso,status"))
@Getter @Setter @NoArgsConstructor
public class SolicitacaoUsoEstacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estacao_id", nullable = false)
    private EstacaoMeteorologica estacao;

    @Column(name = "data_uso", nullable = false)
    private LocalDate dataUso;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 160)
    private String nomeUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distrito_id", nullable = false)
    private DistritoMonitoramentoClimatico distrito;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacaoUso status;

    @Column(length = 1000)
    private String justificativaReprovacao;

    @Column
    private LocalDateTime dataLiberacao;

    @PrePersist
    void inicializar() {
        if (dataSolicitacao == null) dataSolicitacao = LocalDateTime.now();
        if (status == null) status = StatusSolicitacaoUso.PENDENTE;
    }
}
