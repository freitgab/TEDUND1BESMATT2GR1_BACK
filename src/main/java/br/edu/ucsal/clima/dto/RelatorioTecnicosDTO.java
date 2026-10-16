package br.edu.ucsal.clima.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RelatorioTecnicosDTO(
        RelatorioEmissorDTO administradorResponsavel,
        String nomeEmissor,
        String emailEmissor,
        LocalDateTime dataHoraEmissao,
        List<TecnicoResponse> tecnicos
) { }
