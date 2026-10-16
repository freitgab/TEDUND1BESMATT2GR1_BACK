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
@Transactional(readOnly = true)
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
                    .filter(d -> ogm.id() != null && ogm.id().equals(d.orgaoGestorId()))
                    .toList();
            Set<Long> dmcIds = distritosOgm.stream()
                    .map(DmcResponse::id)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
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
        String principalName = (auth != null && auth.getName() != null && !auth.getName().isBlank()
                && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "sistema@clima.gov.br";

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principalName).orElse(null);
        if (usuario != null) {
            String nome = usuario.getNomeCompleto() != null ? usuario.getNomeCompleto() : principalName;
            String email = usuario.getEmail() != null ? usuario.getEmail() : principalName;
            return new RelatorioEmissorDTO(usuario.getId(), nome, email, agora);
        }
        return new RelatorioEmissorDTO(null, principalName, principalName, agora);
    }
}
