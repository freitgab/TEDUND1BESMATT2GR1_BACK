package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import java.time.LocalDate;
import java.util.List;

public record OgmResponse(Long id, String sigla, String nome, String emailCentral,
                          String telefoneCentral, Long diretorGeralId, String diretorGeralNome,
                          List<SetorDTO> setores, LocalDate dataCadastro, StatusEntidade status) { }
