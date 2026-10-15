package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "distritos_monitoramento_climatico")
@Getter @Setter @NoArgsConstructor
public class DistritoMonitoramentoClimatico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 160)
    private String emailCentral;

    @Column(length = 30)
    private String telefoneCentral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meteorologista_chefe_id")
    private TecnicoMeteorologista meteorologistaChefe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orgao_gestor_id", nullable = false)
    private OrgaoGestorMeteorologia orgaoGestor;

    @Column(nullable = false, updatable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntidade status;

    @PrePersist
    void inicializar() {
        if (dataCadastro == null) dataCadastro = LocalDate.now();
        if (status == null) status = StatusEntidade.ATIVO;
    }

    @PreRemove
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative o DMC em vez de exclui-lo fisicamente"); }
}
