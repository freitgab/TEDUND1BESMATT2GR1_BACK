package br.edu.ucsal.clima.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "setores_coordenadorias")
@Getter @Setter @NoArgsConstructor
public class SetorCoordenadoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, length = 160)
    private String nomeacaoResponsavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orgao_gestor_id", nullable = false)
    private OrgaoGestorMeteorologia orgaoGestor;
}
