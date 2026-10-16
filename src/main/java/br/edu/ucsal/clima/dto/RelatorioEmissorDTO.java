package br.edu.ucsal.clima.dto;

import java.time.LocalDateTime;

public record RelatorioEmissorDTO(
        Long id,
        String nomeCompleto,
        String email,
        LocalDateTime dataHoraEmissao
) { }
