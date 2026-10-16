package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import br.edu.ucsal.clima.domain.entity.RedeMonitoramento;
import br.edu.ucsal.clima.domain.entity.SolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.entity.SolicitacaoUsoEstacao;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso;
import br.edu.ucsal.clima.domain.enums.TipoEstacao;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import br.edu.ucsal.clima.domain.enums.TipoSensor;
import br.edu.ucsal.clima.dto.EstacaoRequest;
import br.edu.ucsal.clima.dto.ReprovacaoRequest;
import br.edu.ucsal.clima.dto.SoftwareDTO;
import br.edu.ucsal.clima.dto.SolicitacaoUsoRequest;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.EstacaoMeteorologicaRepository;
import br.edu.ucsal.clima.repository.RedeMonitoramentoRepository;
import br.edu.ucsal.clima.repository.SensorRepository;
import br.edu.ucsal.clima.repository.SoftwareColetaTransmissaoRepository;
import br.edu.ucsal.clima.repository.SolicitacaoInstalacaoRepository;
import br.edu.ucsal.clima.repository.SolicitacaoUsoEstacaoRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperacionalServicesTest {

    @Mock
    private EstacaoMeteorologicaRepository estacaoRepository;

    @Mock
    private RedeMonitoramentoRepository redeRepository;

    @Mock
    private SolicitacaoUsoEstacaoRepository solicitacaoUsoRepository;

    @Mock
    private SolicitacaoInstalacaoRepository solicitacaoInstalacaoRepository;

    @Mock
    private DistritoMonitoramentoClimaticoRepository dmcRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SoftwareColetaTransmissaoRepository softwareRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EstacaoMeteorologicaService estacaoService;

    @InjectMocks
    private SolicitacaoUsoService solicitacaoUsoService;

    @InjectMocks
    private SolicitacaoInstalacaoService solicitacaoInstalacaoService;

    @Test
    @DisplayName("Estacao AUTOMATICA sem sensor ou software deve lancar IllegalArgumentException (HTTP 400)")
    void estacaoAutomaticaSemSensorOuSoftwareDeveFalhar() {
        var rede = new RedeMonitoramento();
        rede.setId(1L);
        rede.setStatus(StatusEntidade.ATIVO);

        when(estacaoRepository.existsBySiglaIgnoreCase("EST-01")).thenReturn(false);
        when(estacaoRepository.existsByNomeIgnoreCase("Estacao 01")).thenReturn(false);
        when(redeRepository.findById(1L)).thenReturn(Optional.of(rede));

        var req = new EstacaoRequest(
                "EST-01",
                "Estacao 01",
                TipoEstacao.AUTOMATICA,
                "Desc",
                5,
                -12.97,
                -38.51,
                1L,
                List.of(),
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> estacaoService.cadastrar(req));
        verify(estacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Estacao AUTOMATICA com sensores e software validos deve iniciar com status DISPONIVEL")
    void estacaoAutomaticaValidaDeveCadastrarDisponivel() {
        var rede = new RedeMonitoramento();
        rede.setId(1L);
        rede.setSigla("REDE-BA");
        rede.setStatus(StatusEntidade.ATIVO);

        when(estacaoRepository.existsBySiglaIgnoreCase("EST-02")).thenReturn(false);
        when(estacaoRepository.existsByNomeIgnoreCase("Estacao 02")).thenReturn(false);
        when(redeRepository.findById(1L)).thenReturn(Optional.of(rede));
        when(estacaoRepository.save(any(EstacaoMeteorologica.class))).thenAnswer(inv -> {
            EstacaoMeteorologica e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        var sw = new SoftwareDTO(
                null,
                "Collector v1",
                "https://download.ucsal.br/collector",
                TipoLicencaSoftware.SOFTWARE_LIVRE,
                "Coleta automatica",
                Set.of("TEMPERATURA", "UMIDADE")
        );

        var req = new EstacaoRequest(
                "EST-02",
                "Estacao 02",
                TipoEstacao.AUTOMATICA,
                "Estacao completa",
                10,
                -12.97,
                -38.51,
                1L,
                List.of(TipoSensor.TEMPERATURA, TipoSensor.UMIDADE),
                List.of(sw)
        );

        var resp = estacaoService.cadastrar(req);

        assertEquals(StatusEstacao.DISPONIVEL, resp.status());
        assertEquals(2, resp.sensores().size());
        assertEquals(1, resp.softwares().size());
        verify(auditoriaService).registrar(eq("CADASTRAR"), eq("EstacaoMeteorologica"), eq(10L), anyString());
    }

    @Test
    @DisplayName("Solicitacao de Uso: horaFim anterior ou igual a horaInicio deve lancar IllegalArgumentException")
    void solicitacaoUsoHorarioInvalidoDeveFalhar() {
        var req = new SolicitacaoUsoRequest(
                1L,
                1L,
                LocalDate.of(2026, 3, 13),
                LocalTime.of(9, 30),
                LocalTime.of(7, 0)
        );

        assertThrows(IllegalArgumentException.class, () -> solicitacaoUsoService.criar(req));
    }

    @Test
    @DisplayName("Solicitacao de Uso: aprovar com conflito de horario deve lancar IllegalStateException (HTTP 409)")
    void solicitacaoUsoAprovarComConflitoDeAgendaDeveBloquear() {
        var estacao = new EstacaoMeteorologica();
        estacao.setId(5L);
        estacao.setStatus(StatusEstacao.DISPONIVEL);

        var solicitacao = new SolicitacaoUsoEstacao();
        solicitacao.setId(100L);
        solicitacao.setEstacao(estacao);
        solicitacao.setDataUso(LocalDate.of(2026, 3, 13));
        solicitacao.setHoraInicio(LocalTime.of(8, 0));
        solicitacao.setHoraFim(LocalTime.of(10, 0));
        solicitacao.setStatus(StatusSolicitacaoUso.PENDENTE);

        when(solicitacaoUsoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoUsoRepository.existeConflitoHorario(
                5L,
                LocalDate.of(2026, 3, 13),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                100L
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> solicitacaoUsoService.aprovar(100L));
        assertEquals(StatusEstacao.DISPONIVEL, estacao.getStatus());
    }

    @Test
    @DisplayName("Solicitacao de Uso: aprovar deve mudar status para APROVADA e estacao para INDISPONIVEL; liberar retorna para DISPONIVEL")
    void solicitacaoUsoFluxoAprovarELiberarEstacao() {
        var estacao = new EstacaoMeteorologica();
        estacao.setId(5L);
        estacao.setSigla("EST-05");
        estacao.setNome("Estacao Sul");
        estacao.setStatus(StatusEstacao.DISPONIVEL);

        var solicitacao = new SolicitacaoUsoEstacao();
        solicitacao.setId(101L);
        solicitacao.setEstacao(estacao);
        solicitacao.setDataUso(LocalDate.of(2026, 3, 13));
        solicitacao.setHoraInicio(LocalTime.of(7, 0));
        solicitacao.setHoraFim(LocalTime.of(9, 30));
        solicitacao.setStatus(StatusSolicitacaoUso.PENDENTE);

        when(solicitacaoUsoRepository.findById(101L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoUsoRepository.existeConflitoHorario(anyLong(), any(), any(), any(), anyLong())).thenReturn(false);
        when(solicitacaoUsoRepository.save(any(SolicitacaoUsoEstacao.class))).thenAnswer(inv -> inv.getArgument(0));

        var aprovada = solicitacaoUsoService.aprovar(101L);
        assertEquals(StatusSolicitacaoUso.APROVADA, aprovada.status());
        assertEquals(StatusEstacao.INDISPONIVEL, estacao.getStatus());

        var finalizada = solicitacaoUsoService.liberarEstacao(101L);
        assertEquals(StatusSolicitacaoUso.FINALIZADA, finalizada.status());
        assertEquals(StatusEstacao.DISPONIVEL, estacao.getStatus());
        assertNotNull(finalizada.dataLiberacao());
    }

    @Test
    @DisplayName("Solicitacao de Uso: reprovar deve exigir justificativa e mudar status para REPROVADA")
    void solicitacaoUsoReprovarComJustificativa() {
        var solicitacao = new SolicitacaoUsoEstacao();
        solicitacao.setId(102L);
        solicitacao.setStatus(StatusSolicitacaoUso.PENDENTE);

        when(solicitacaoUsoRepository.findById(102L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoUsoRepository.save(any(SolicitacaoUsoEstacao.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = solicitacaoUsoService.reprovar(102L, new ReprovacaoRequest("Manutencao preventiva agendada"));

        assertEquals(StatusSolicitacaoUso.REPROVADA, resp.status());
        assertEquals("Manutencao preventiva agendada", resp.justificativaReprovacao());
    }

    @Test
    @DisplayName("Solicitacao de Instalacao: aprovar deve mudar status para APROVADA e anexar sensor/software na estacao")
    void solicitacaoInstalacaoAprovarDeveAnexarNaEstacao() {
        var estacao = new EstacaoMeteorologica();
        estacao.setId(7L);
        estacao.setSigla("EST-07");
        estacao.setStatus(StatusEstacao.DISPONIVEL);

        var solicitacao = new SolicitacaoInstalacao();
        solicitacao.setId(200L);
        solicitacao.setEstacao(estacao);
        solicitacao.setSensoresSoftwares("PLUVIOMETRO + Modulo PyRain");
        solicitacao.setUrlDownload("https://github.com/org/pyrain");
        solicitacao.setTipoSoftware(TipoLicencaSoftware.SOFTWARE_LIVRE);
        solicitacao.setObjetivoUso("Monitorar precipitacao intensa");
        solicitacao.setStatus(StatusSolicitacaoInstalacao.PENDENTE);

        when(solicitacaoInstalacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoInstalacaoRepository.save(any(SolicitacaoInstalacao.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = solicitacaoInstalacaoService.aprovar(200L);

        assertEquals(StatusSolicitacaoInstalacao.APROVADA, resp.status());
        verify(sensorRepository, atLeastOnce()).save(any());
        verify(softwareRepository, atLeastOnce()).save(any());
        verify(auditoriaService).registrar(eq("APROVAR_SOLICITACAO_INSTALACAO"), eq("SolicitacaoInstalacao"), eq(200L), anyString());
    }
}
