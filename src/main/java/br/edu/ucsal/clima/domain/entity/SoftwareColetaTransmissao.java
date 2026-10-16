package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "softwares_coleta_transmissao")
@Getter @Setter @NoArgsConstructor
public class SoftwareColetaTransmissao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String urlDownload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoLicencaSoftware tipoSoftware;

    @Column(nullable = false, length = 1000)
    private String objetivoUso;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "software_variaveis_monitoradas", joinColumns = @JoinColumn(name = "software_id"))
    @Column(name = "variavel", nullable = false, length = 80)
    private Set<String> variaveisMonitoradas = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntidade status = StatusEntidade.ATIVO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estacao_id", nullable = false)
    private EstacaoMeteorologica estacao;

    @PreRemove
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative o software em vez de exclui-lo fisicamente"); }
}
