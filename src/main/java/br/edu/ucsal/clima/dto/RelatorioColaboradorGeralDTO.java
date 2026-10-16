package br.edu.ucsal.clima.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RelatorioColaboradorGeralDTO(
        RelatorioEmissorDTO colaboradorEmissor,
        String nomeEmissor,
        String emailEmissor,
        LocalDateTime dataHoraEmissao,
        List<EstacaoResponse> estacoes,
        List<SolicitacaoUsoResponse> solicitacoesUso,
        List<SolicitacaoInstalacaoResponse> solicitacoesInstalacao
) { }
