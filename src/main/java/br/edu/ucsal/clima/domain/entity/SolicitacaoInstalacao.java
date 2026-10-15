package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_instalacao")
@Getter @Setter @NoArgsConstructor
public class SolicitacaoInstalacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estacao_id", nullable = false)
    private EstacaoMeteorologica estacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 1000)
    private String sensoresSoftwares;

    @Column(length = 1000)
    private String urlDownload;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoLicencaSoftware tipoSoftware;

    @Column(nullable = false, length = 1000)
    private String objetivoUso;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacaoInstalacao status;

    @Column(length = 1000)
    private String justificativaReprovacao;

    @PrePersist
    void inicializar() {
        if (dataSolicitacao == null) dataSolicitacao = LocalDateTime.now();
        if (status == null) status = StatusSolicitacaoInstalacao.PENDENTE;
    }
}
