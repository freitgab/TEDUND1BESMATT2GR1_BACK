package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import br.edu.ucsal.clima.domain.entity.SolicitacaoUsoEstacao;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoUso;
import br.edu.ucsal.clima.dto.ReprovacaoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoUsoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoUsoResponse;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.EstacaoMeteorologicaRepository;
import br.edu.ucsal.clima.repository.SolicitacaoUsoEstacaoRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoUsoService {

    private final SolicitacaoUsoEstacaoRepository solicitacaoRepository;
    private final EstacaoMeteorologicaRepository estacaoRepository;
    private final DistritoMonitoramentoClimaticoRepository dmcRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<SolicitacaoUsoResponse> listar(boolean apenasMinhas) {
        Usuario autenticado = obterUsuarioAutenticadoOpcional();
        boolean somenteUsuarioComum = autenticado != null && autenticado.getPerfis().stream()
                .noneMatch(p -> p.getNome() == NomePerfil.COLABORADOR || p.getNome() == NomePerfil.ADMINISTRADOR);

        if ((apenasMinhas || somenteUsuarioComum) && autenticado != null) {
            return solicitacaoRepository.findBySolicitante_IdOrderByDataSolicitacaoDesc(autenticado.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return solicitacaoRepository.findAllByOrderByDataSolicitacaoDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoUsoResponse> listarTodas() {
        return solicitacaoRepository.findAllByOrderByDataSolicitacaoDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoUsoResponse buscarPorId(Long id) {
        return solicitacaoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));
    }

    @Transactional
    public SolicitacaoUsoResponse criar(SolicitacaoUsoRequest req) {
        validarHorario(req);
        Long estacaoId = AdminValidation.idObrigatorio(req.estacaoId(), "Estacao");
        Long dmcId = AdminValidation.idObrigatorio(req.dmcId(), "DMC");

        EstacaoMeteorologica estacao = estacaoRepository.findById(estacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada"));
        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Estacao meteorologica esta inativa");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(dmcId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        Usuario solicitante = obterUsuarioAutenticadoObrigatorio();

        var solicitacao = new SolicitacaoUsoEstacao();
        solicitacao.setEstacao(estacao);
        solicitacao.setDistrito(dmc);
        solicitacao.setDataUso(req.dataUso());
        solicitacao.setHoraInicio(req.horaInicio());
        solicitacao.setHoraFim(req.horaFim());
        solicitacao.setSolicitante(solicitante);
        solicitacao.setNomeUsuario(solicitante.getNomeCompleto());
        solicitacao.setStatus(StatusSolicitacaoUso.PENDENTE);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("CRIAR_SOLICITACAO_USO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId() + ", dataUso=" + solicitacao.getDataUso());
        return toResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoUsoResponse cadastrar(SolicitacaoUsoRequest req) {
        return criar(req);
    }

    @Transactional
    public SolicitacaoUsoResponse atualizar(Long id, SolicitacaoUsoRequest req) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoUso.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser editadas");
        }

        validarHorario(req);
        Long estacaoId = AdminValidation.idObrigatorio(req.estacaoId(), "Estacao");
        Long dmcId = AdminValidation.idObrigatorio(req.dmcId(), "DMC");

        EstacaoMeteorologica estacao = estacaoRepository.findById(estacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada"));
        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Estacao meteorologica esta inativa");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(dmcId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        solicitacao.setEstacao(estacao);
        solicitacao.setDistrito(dmc);
        solicitacao.setDataUso(req.dataUso());
        solicitacao.setHoraInicio(req.horaInicio());
        solicitacao.setHoraFim(req.horaFim());

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("ATUALIZAR_SOLICITACAO_USO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId());
        return toResponse(solicitacao);
    }

    @Transactional
    public void inativar(Long id) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoUso.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser inativadas ou canceladas");
        }

        solicitacao.setStatus(StatusSolicitacaoUso.INATIVO);
        solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("INATIVAR_SOLICITACAO_USO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "Inativacao de solicitacao pendente");
    }

    @Transactional
    public SolicitacaoUsoResponse aprovar(Long id) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoUso.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser aprovadas");
        }

        EstacaoMeteorologica estacao = solicitacao.getEstacao();
        if (estacao.getStatus() != StatusEstacao.DISPONIVEL) {
            throw new IllegalStateException("Apenas estacoes com status DISPONIVEL podem receber aprovacao de uso");
        }

        boolean conflito = solicitacaoRepository.existeConflitoHorario(
                estacao.getId(),
                solicitacao.getDataUso(),
                solicitacao.getHoraInicio(),
                solicitacao.getHoraFim(),
                solicitacao.getId()
        );
        if (conflito) {
            throw new IllegalStateException("Conflito de agenda: ja existe reserva aprovada para esta estacao no intervalo informado");
        }

        solicitacao.setStatus(StatusSolicitacaoUso.APROVADA);
        estacao.setStatus(StatusEstacao.INDISPONIVEL);

        estacaoRepository.save(estacao);
        solicitacao = solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar("APROVAR_SOLICITACAO_USO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId() + ", statusEstacao=INDISPONIVEL");
        return toResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoUsoResponse reprovar(Long id, ReprovacaoRequest req) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoUso.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser reprovadas");
        }

        String justificativa = AdminValidation.textoObrigatorio(
                req != null ? req.justificativa() : null,
                "Justificativa de reprovacao"
        );

        solicitacao.setStatus(StatusSolicitacaoUso.REPROVADA);
        solicitacao.setJustificativaReprovacao(justificativa);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("REPROVAR_SOLICITACAO_USO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "justificativa=" + justificativa);
        return toResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoUsoResponse liberarEstacao(Long id) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de uso nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoUso.APROVADA) {
            throw new IllegalStateException("Apenas solicitacoes com status APROVADA podem confirmar a liberacao da estacao");
        }

        EstacaoMeteorologica estacao = solicitacao.getEstacao();
        solicitacao.setStatus(StatusSolicitacaoUso.FINALIZADA);
        solicitacao.setDataLiberacao(LocalDateTime.now());
        estacao.setStatus(StatusEstacao.DISPONIVEL);

        estacaoRepository.save(estacao);
        solicitacao = solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar("LIBERAR_ESTACAO", "SolicitacaoUsoEstacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId() + ", statusEstacao=DISPONIVEL");
        return toResponse(solicitacao);
    }

    private void validarHorario(SolicitacaoUsoRequest req) {
        if (req.dataUso() == null) {
            throw new IllegalArgumentException("Data de uso e obrigatoria");
        }
        if (req.horaInicio() == null || req.horaFim() == null) {
            throw new IllegalArgumentException("Hora de inicio e hora de fim sao obrigatorias");
        }
        if (!req.horaFim().isAfter(req.horaInicio())) {
            throw new IllegalArgumentException("A hora de fim deve ser posterior a hora de inicio");
        }
    }

    private Usuario obterUsuarioAutenticadoObrigatorio() {
        Usuario usuario = obterUsuarioAutenticadoOpcional();
        if (usuario == null) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return usuario;
    }

    private Usuario obterUsuarioAutenticadoOpcional() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return usuarioRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    private SolicitacaoUsoResponse toResponse(SolicitacaoUsoEstacao s) {
        Long estacaoId = s.getEstacao() != null ? s.getEstacao().getId() : null;
        String estacaoSigla = s.getEstacao() != null ? s.getEstacao().getSigla() : null;
        String estacaoNome = s.getEstacao() != null ? s.getEstacao().getNome() : null;
        Long usuarioId = s.getSolicitante() != null ? s.getSolicitante().getId() : null;
        Long dmcId = s.getDistrito() != null ? s.getDistrito().getId() : null;
        String dmcNome = s.getDistrito() != null ? s.getDistrito().getNome() : null;

        return new SolicitacaoUsoResponse(
                s.getId(),
                estacaoId,
                estacaoSigla,
                estacaoNome,
                usuarioId,
                s.getNomeUsuario(),
                dmcId,
                dmcNome,
                s.getDataUso(),
                s.getHoraInicio(),
                s.getHoraFim(),
                s.getDataSolicitacao(),
                s.getStatus(),
                s.getJustificativaReprovacao(),
                s.getDataLiberacao()
        );
    }
}
