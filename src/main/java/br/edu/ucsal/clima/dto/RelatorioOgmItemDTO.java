package br.edu.ucsal.clima.dto;

import java.util.List;

public record RelatorioOgmItemDTO(
        OgmResponse ogm,
        List<DmcResponse> distritos,
        List<RedeResponse> redes
) { }
