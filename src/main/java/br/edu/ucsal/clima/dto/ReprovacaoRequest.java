package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReprovacaoRequest(
        @NotBlank @Size(max = 1000) String justificativa
) { }
