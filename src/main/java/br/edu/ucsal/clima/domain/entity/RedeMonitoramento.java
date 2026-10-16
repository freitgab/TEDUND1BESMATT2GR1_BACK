package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "redes_monitoramento")
@Getter @Setter @NoArgsConstructor
public class RedeMonitoramento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sigla;

    @Column(nullable = false, length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distrito_id", nullable = false)
    private DistritoMonitoramentoClimatico distrito;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rede_tipos_monitoramento", joinColumns = @JoinColumn(name = "rede_id"))
    @Column(name = "tipo_monitoramento", nullable = false, length = 80)
    private Set<String> tiposMonitoramento = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meteorologista_responsavel_id")
    private TecnicoMeteorologista meteorologistaResponsavel;

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
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative a rede em vez de exclui-la fisicamente"); }
}
