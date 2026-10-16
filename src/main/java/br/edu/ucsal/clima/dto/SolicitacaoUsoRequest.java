package br.edu.ucsal.clima.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoUsoRequest(
        @NotNull Long estacaoId,
        @NotNull Long dmcId,
        @NotNull LocalDate dataUso,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim
) { }
