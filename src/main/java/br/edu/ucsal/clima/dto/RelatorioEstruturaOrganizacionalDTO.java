package br.edu.ucsal.clima.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RelatorioEstruturaOrganizacionalDTO(
        RelatorioEmissorDTO administradorResponsavel,
        String nomeEmissor,
        String emailEmissor,
        LocalDateTime dataHoraEmissao,
        List<OgmResponse> ogms,
        List<DmcResponse> dmcs,
        List<RedeResponse> redes,
        List<RelatorioOgmItemDTO> estruturaConsolidada
) { }
