package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SolicitacaoUsoResponse(
        Long id,
        Long estacaoId,
        String estacaoSigla,
        String estacaoNome,
        Long usuarioId,
        String nomeUsuario,
        Long dmcId,
        String dmcNome,
        LocalDate dataUso,
        LocalTime horaInicio,
        LocalTime horaFim,
        LocalDateTime dataSolicitacao,
        StatusSolicitacaoUso status,
        String justificativaReprovacao,
        LocalDateTime dataLiberacao
) { }
