package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JustificativaInativacaoDTO(@NotBlank @Size(max = 500) String justificativa) { }
