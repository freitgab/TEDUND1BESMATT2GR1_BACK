package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.TipoEstacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "estacoes_meteorologicas")
@Getter @Setter @NoArgsConstructor
public class EstacaoMeteorologica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sigla;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEstacao tipoEstacao;

    @Column(length = 1000)
    private String descricaoComplementar;

    @Column(nullable = false)
    private Integer capacidadeMaximaSuportada;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, updatable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEstacao status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rede_id", nullable = false)
    private RedeMonitoramento rede;

    @PrePersist
    void inicializar() {
        if (dataCadastro == null) dataCadastro = LocalDate.now();
        status = StatusEstacao.DISPONIVEL;
    }

    @PreRemove
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative a estacao em vez de exclui-la fisicamente"); }
}
