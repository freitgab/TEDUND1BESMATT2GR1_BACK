package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String senhaHash;

    @Column(length = 30)
    private String telefone;

    @Column(nullable = false, updatable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntidade status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_perfis", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "perfil_id"), uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "perfil_id"}))
    private Set<Perfil> perfis = new HashSet<>();

    @PrePersist
    void inicializar() {
        if (dataCadastro == null) dataCadastro = LocalDate.now();
        if (status == null) status = StatusEntidade.ATIVO;
    }

    @PreRemove
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative o usuario em vez de exclui-lo fisicamente"); }
}
