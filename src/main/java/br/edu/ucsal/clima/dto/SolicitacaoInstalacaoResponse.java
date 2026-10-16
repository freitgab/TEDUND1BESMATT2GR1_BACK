package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;

import java.time.LocalDateTime;

public record SolicitacaoInstalacaoResponse(
        Long id,
        Long estacaoId,
        String estacaoSigla,
        String estacaoNome,
        Long usuarioId,
        String nomeUsuario,
        String sensoresSoftwares,
        String urlDownload,
        TipoLicencaSoftware tipoSoftware,
        String objetivoUso,
        LocalDateTime dataSolicitacao,
        StatusSolicitacaoInstalacao status,
        String justificativaReprovacao
) { }
