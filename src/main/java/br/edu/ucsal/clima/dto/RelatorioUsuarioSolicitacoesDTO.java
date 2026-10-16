package br.edu.ucsal.clima.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RelatorioUsuarioSolicitacoesDTO(
        RelatorioEmissorDTO usuario,
        String nomeEmissor,
        String emailEmissor,
        LocalDateTime dataHoraEmissao,
        List<SolicitacaoUsoResponse> solicitacoesUso,
        List<SolicitacaoInstalacaoResponse> solicitacoesInstalacao
) { }
