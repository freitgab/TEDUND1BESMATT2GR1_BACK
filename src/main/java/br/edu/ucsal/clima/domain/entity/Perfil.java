package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.NomePerfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfis")
@Getter @Setter @NoArgsConstructor
public class Perfil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private NomePerfil nome;

    @Column(length = 255)
    private String descricao;
}
