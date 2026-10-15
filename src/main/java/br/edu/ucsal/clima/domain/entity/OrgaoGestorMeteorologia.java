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
@Table(name = "orgaos_gestores_meteorologia")
@Getter @Setter @NoArgsConstructor
public class OrgaoGestorMeteorologia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sigla;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 160)
    private String emailCentral;

    @Column(length = 30)
    private String telefoneCentral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diretor_geral_id")
    private Usuario diretorGeral;

    @OneToMany(mappedBy = "orgaoGestor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SetorCoordenadoria> setores = new HashSet<>();

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
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative o OGM em vez de exclui-lo fisicamente"); }
}
