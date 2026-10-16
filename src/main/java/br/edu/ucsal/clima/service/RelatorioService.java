package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.dto.*;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final OrgaoGestorMeteorologiaService ogmService;
    private final DistritoMonitoramentoClimaticoService dmcService;
    private final RedeMonitoramentoService redeService;
    private final TecnicoMeteorologistaService tecnicoService;
    private final EstacaoMeteorologicaService estacaoService;
    private final SolicitacaoUsoService solicitacaoUsoService;
    private final SolicitacaoInstalacaoService solicitacaoInstalacaoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public RelatorioEstruturaOrganizacionalDTO emitirRelatorioEstruturaOrganizacional() {
        LocalDateTime agora = LocalDateTime.now();
        RelatorioEmissorDTO emissor = obterEmissorAtual(agora);

        List<OgmResponse> ogms = ogmService.listarTodos();
        List<DmcResponse> dmcs = dmcService.listarTodos();
        List<RedeResponse> redes = redeService.listarTodos();

        List<RelatorioOgmItemDTO> estrutura = ogms.stream().map(ogm -> {
            List<DmcResponse> distritosOgm = dmcs.stream()
                    .filter(d -> ogm.id().equals(d.orgaoGestorId()))
                    .toList();
            Set<Long> dmcIds = distritosOgm.stream().map(DmcResponse::id).collect(Collectors.toSet());
            List<RedeResponse> redesOgm = redes.stream()
                    .filter(r -> r.distritoId() != null && dmcIds.contains(r.distritoId()))
                    .toList();
            return new RelatorioOgmItemDTO(ogm, distritosOgm, redesOgm);
        }).toList();

        return new RelatorioEstruturaOrganizacionalDTO(
                emissor,
                emissor.nomeCompleto(),
                emissor.email(),
                agora,
                ogms,
                dmcs,
                redes,
                estrutura
        );
    }

    @Transactional(readOnly = true)
    public RelatorioTecnicosDTO emitirRelatorioTecnicos() {
        LocalDateTime agora = LocalDateTime.now();
        RelatorioEmissorDTO emissor = obterEmissorAtual(agora);
        List<TecnicoResponse> tecnicos = tecnicoService.listarTodos();

        return new RelatorioTecnicosDTO(
                emissor,
                emissor.nomeCompleto(),
                emissor.email(),
                agora,
                tecnicos
        );
    }

    @Transactional(readOnly = true)
    public RelatorioColaboradorGeralDTO emitirRelatorioColaboradorGeral() {
        LocalDateTime agora = LocalDateTime.now();
        RelatorioEmissorDTO emissor = obterEmissorAtual(agora);

        List<EstacaoResponse> estacoes = estacaoService.listarTodas();
        List<SolicitacaoUsoResponse> solicitacoesUso = solicitacaoUsoService.listarTodas();
        List<SolicitacaoInstalacaoResponse> solicitacoesInstalacao = solicitacaoInstalacaoService.listarTodas();

        return new RelatorioColaboradorGeralDTO(
                emissor,
                emissor.nomeCompleto(),
                emissor.email(),
                agora,
                estacoes,
                solicitacoesUso,
                solicitacoesInstalacao
        );
    }

    @Transactional(readOnly = true)
    public RelatorioUsuarioSolicitacoesDTO emitirRelatorioMinhasSolicitacoes() {
        LocalDateTime agora = LocalDateTime.now();
        RelatorioEmissorDTO emissor = obterEmissorAtual(agora);

        List<SolicitacaoUsoResponse> solicitacoesUso = solicitacaoUsoService.listar(true);
        List<SolicitacaoInstalacaoResponse> solicitacoesInstalacao = solicitacaoInstalacaoService.listar(true);

        return new RelatorioUsuarioSolicitacoesDTO(
                emissor,
                emissor.nomeCompleto(),
                emissor.email(),
                agora,
                solicitacoesUso,
                solicitacoesInstalacao
        );
    }

    private RelatorioEmissorDTO obterEmissorAtual(LocalDateTime agora) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "sistema@clima.gov.br";

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario != null) {
            return new RelatorioEmissorDTO(usuario.getId(), usuario.getNomeCompleto(), usuario.getEmail(), agora);
        }
        return new RelatorioEmissorDTO(null, email, email, agora);
    }
}
