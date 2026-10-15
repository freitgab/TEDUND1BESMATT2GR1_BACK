package br.edu.ucsal.clima.domain.entity;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "tecnicos_meteorologistas")
@Getter @Setter @NoArgsConstructor
public class TecnicoMeteorologista {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String matricula;

    @Column(nullable = false, length = 160)
    private String nomeCompleto;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(nullable = false, updatable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntidade status;

    @Column(length = 500)
    private String motivoInativacao;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distrito_id", nullable = false)
    private DistritoMonitoramentoClimatico distrito;

    @PrePersist
    void inicializar() {
        if (dataCadastro == null) dataCadastro = LocalDate.now();
        if (status == null) status = StatusEntidade.ATIVO;
        validarPerfilColaborador();
    }

    @PreUpdate
    void validarPerfilColaborador() {
        if (status == StatusEntidade.ATIVO && (usuario == null || usuario.getPerfis().stream()
                .noneMatch(perfil -> perfil.getNome() == NomePerfil.COLABORADOR))) {
            throw new IllegalStateException("O tecnico deve estar vinculado a um usuario com perfil COLABORADOR");
        }
    }

    @PreRemove
    void impedirExclusaoFisica() { throw new IllegalStateException("Inative o tecnico em vez de exclui-lo fisicamente"); }

    public void inativarComJustificativa(String justificativa) {
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Justificativa de inativacao e obrigatoria");
        }
        this.status = StatusEntidade.INATIVO;
        this.motivoInativacao = justificativa.trim();
    }
}
