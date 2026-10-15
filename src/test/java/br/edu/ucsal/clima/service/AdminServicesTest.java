package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.*;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.dto.JustificativaInativacaoDTO;
import br.edu.ucsal.clima.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServicesTest {

    @Mock
    private OrgaoGestorMeteorologiaRepository orgaoRepository;

    @Mock
    private DistritoMonitoramentoClimaticoRepository dmcRepository;

    @Mock
    private RedeMonitoramentoRepository redeRepository;

    @Mock
    private TecnicoMeteorologistaRepository tecnicoRepository;

    @Mock
    private EstacaoMeteorologicaRepository estacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private OrgaoGestorMeteorologiaService ogmService;

    @InjectMocks
    private DistritoMonitoramentoClimaticoService dmcService;

    @InjectMocks
    private RedeMonitoramentoService redeService;

    @InjectMocks
    private TecnicoMeteorologistaService tecnicoService;

    @Test
    @DisplayName("OGM: inativacao com sucesso deve alterar status para INATIVO e registrar auditoria")
    void ogmInativarComSucesso() {
        var ogm = new OrgaoGestorMeteorologia();
        ogm.setId(1L);
        ogm.setSigla("INMET");
        ogm.setStatus(StatusEntidade.ATIVO);

        when(orgaoRepository.findById(1L)).thenReturn(Optional.of(ogm));
        when(dmcRepository.existsByOrgaoGestor_IdAndStatus(1L, StatusEntidade.ATIVO)).thenReturn(false);

        ogmService.inativar(1L);

        assertEquals(StatusEntidade.INATIVO, ogm.getStatus());
        verify(orgaoRepository).save(ogm);
        verify(auditoriaService).registrar(eq("INATIVAR"), eq("OrgaoGestorMeteorologia"), eq(1L), anyString());
    }

    @Test
    @DisplayName("OGM: deve bloquear inativacao se houver distritos ativos vinculados")
    void ogmInativarComConflitoDeDistritos() {
        var ogm = new OrgaoGestorMeteorologia();
        ogm.setId(1L);
        ogm.setStatus(StatusEntidade.ATIVO);

        when(orgaoRepository.findById(1L)).thenReturn(Optional.of(ogm));
        when(dmcRepository.existsByOrgaoGestor_IdAndStatus(1L, StatusEntidade.ATIVO)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ogmService.inativar(1L));
        verify(orgaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("DMC: inativacao com sucesso deve alterar status para INATIVO e registrar auditoria")
    void dmcInativarComSucesso() {
        var dmc = new DistritoMonitoramentoClimatico();
        dmc.setId(10L);
        dmc.setNome("DMC Nordeste");
        dmc.setStatus(StatusEntidade.ATIVO);

        when(dmcRepository.findById(10L)).thenReturn(Optional.of(dmc));
        when(redeRepository.existsByDistrito_IdAndStatus(10L, StatusEntidade.ATIVO)).thenReturn(false);
        when(tecnicoRepository.existsByDistrito_IdAndStatus(10L, StatusEntidade.ATIVO)).thenReturn(false);

        dmcService.inativar(10L);

        assertEquals(StatusEntidade.INATIVO, dmc.getStatus());
        verify(dmcRepository).save(dmc);
        verify(auditoriaService).registrar(eq("INATIVAR"), eq("DistritoMonitoramentoClimatico"), eq(10L), anyString());
    }

    @Test
    @DisplayName("DMC: deve bloquear inativacao se houver redes ou tecnicos ativos vinculados")
    void dmcInativarComConflitoDeVinculos() {
        var dmc = new DistritoMonitoramentoClimatico();
        dmc.setId(10L);
        dmc.setStatus(StatusEntidade.ATIVO);

        when(dmcRepository.findById(10L)).thenReturn(Optional.of(dmc));
        when(redeRepository.existsByDistrito_IdAndStatus(10L, StatusEntidade.ATIVO)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> dmcService.inativar(10L));
        verify(dmcRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rede: inativacao com sucesso deve alterar status para INATIVO")
    void redeInativarComSucesso() {
        var rede = new RedeMonitoramento();
        rede.setId(20L);
        rede.setSigla("R-PLUVIO");
        rede.setStatus(StatusEntidade.ATIVO);

        when(redeRepository.findById(20L)).thenReturn(Optional.of(rede));
        when(estacaoRepository.existsByRede_IdAndStatusNot(20L, StatusEstacao.INATIVO)).thenReturn(false);

        redeService.inativar(20L);

        assertEquals(StatusEntidade.INATIVO, rede.getStatus());
        verify(redeRepository).save(rede);
        verify(auditoriaService).registrar(eq("INATIVAR"), eq("RedeMonitoramento"), eq(20L), anyString());
    }

    @Test
    @DisplayName("Rede: deve bloquear inativacao se houver estacoes ativas vinculadas")
    void redeInativarComConflitoDeEstacoes() {
        var rede = new RedeMonitoramento();
        rede.setId(20L);
        rede.setStatus(StatusEntidade.ATIVO);

        when(redeRepository.findById(20L)).thenReturn(Optional.of(rede));
        when(estacaoRepository.existsByRede_IdAndStatusNot(20L, StatusEstacao.INATIVO)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> redeService.inativar(20L));
        verify(redeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tecnico: inativar com justificativa deve alterar status do tecnico e usuario e registrar auditoria")
    void tecnicoInativarComJustificativa() {
        var usuario = new Usuario();
        usuario.setId(100L);
        usuario.setStatus(StatusEntidade.ATIVO);

        var tecnico = new TecnicoMeteorologista();
        tecnico.setId(50L);
        tecnico.setMatricula("TEC-001");
        tecnico.setStatus(StatusEntidade.ATIVO);
        tecnico.setUsuario(usuario);

        when(tecnicoRepository.findById(50L)).thenReturn(Optional.of(tecnico));

        tecnicoService.inativar(50L, new JustificativaInativacaoDTO("Desligamento solicitado pelo colaborador"));

        assertEquals(StatusEntidade.INATIVO, tecnico.getStatus());
        assertEquals("Desligamento solicitado pelo colaborador", tecnico.getMotivoInativacao());
        assertEquals(StatusEntidade.INATIVO, usuario.getStatus());
        verify(usuarioRepository).save(usuario);
        verify(tecnicoRepository).save(tecnico);
        verify(auditoriaService).registrar(eq("INATIVAR"), eq("TecnicoMeteorologista"), eq(50L), contains("Desligamento solicitado"));
    }
}
