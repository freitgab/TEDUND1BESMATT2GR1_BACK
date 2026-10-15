package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import java.time.LocalDate;

public record DmcResponse(Long id, String nome, String emailCentral, String telefoneCentral,
                          Long orgaoGestorId, String orgaoGestorNome,
                          Long meteorologistaChefeId, String meteorologistaChefeNome,
                          LocalDate dataCadastro, StatusEntidade status) { }
