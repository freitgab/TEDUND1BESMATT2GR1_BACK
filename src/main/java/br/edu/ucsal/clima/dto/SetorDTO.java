package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorDTO(Long id,
                       @NotBlank @Size(max = 160) String nome,
                       @NotBlank @Size(max = 160) String nomeacaoResponsavel) { }
