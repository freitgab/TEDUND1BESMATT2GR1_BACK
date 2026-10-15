package br.edu.ucsal.clima.dto;

import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import java.time.LocalDate;
import java.util.Set;

public record RedeResponse(Long id, String sigla, String descricao,
                           Long distritoId, String distritoNome, Set<String> tiposMonitoramento,
                           Long meteorologistaResponsavelId, String meteorologistaResponsavelNome,
                           LocalDate dataCadastro, StatusEntidade status) { }
