package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import br.edu.ucsal.clima.domain.entity.Sensor;
import br.edu.ucsal.clima.domain.entity.SoftwareColetaTransmissao;
import br.edu.ucsal.clima.domain.entity.SolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.entity.Usuario;
import br.edu.ucsal.clima.domain.enums.NomePerfil;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.StatusSolicitacaoInstalacao;
import br.edu.ucsal.clima.domain.enums.TipoLicencaSoftware;
import br.edu.ucsal.clima.domain.enums.TipoSensor;
import br.edu.ucsal.clima.dto.ReprovacaoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoInstalacaoRequest;
import br.edu.ucsal.clima.dto.SolicitacaoInstalacaoResponse;
import br.edu.ucsal.clima.repository.EstacaoMeteorologicaRepository;
import br.edu.ucsal.clima.repository.SensorRepository;
import br.edu.ucsal.clima.repository.SoftwareColetaTransmissaoRepository;
import br.edu.ucsal.clima.repository.SolicitacaoInstalacaoRepository;
import br.edu.ucsal.clima.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SolicitacaoInstalacaoService {

    private final SolicitacaoInstalacaoRepository solicitacaoRepository;
    private final EstacaoMeteorologicaRepository estacaoRepository;
    private final SensorRepository sensorRepository;
    private final SoftwareColetaTransmissaoRepository softwareRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<SolicitacaoInstalacaoResponse> listar(boolean apenasMinhas) {
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
    public List<SolicitacaoInstalacaoResponse> listarTodas() {
        return solicitacaoRepository.findAllByOrderByDataSolicitacaoDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoInstalacaoResponse buscarPorId(Long id) {
        return solicitacaoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de instalacao nao encontrada com o ID informado"));
    }

    @Transactional
    public SolicitacaoInstalacaoResponse criar(SolicitacaoInstalacaoRequest req) {
        Long estacaoId = AdminValidation.idObrigatorio(req.estacaoId(), "Estacao");
        String sensoresSoftwares = AdminValidation.textoObrigatorio(req.sensoresSoftwares(), "Sensores/Softwares");
        String objetivoUso = AdminValidation.textoObrigatorio(req.objetivoUso(), "Objetivo de uso");

        EstacaoMeteorologica estacao = estacaoRepository.findById(estacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada"));
        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Estacao meteorologica esta inativa");
        }

        Usuario solicitante = obterUsuarioAutenticadoObrigatorio();

        var solicitacao = new SolicitacaoInstalacao();
        solicitacao.setEstacao(estacao);
        solicitacao.setSolicitante(solicitante);
        solicitacao.setSensoresSoftwares(sensoresSoftwares);
        solicitacao.setUrlDownload(req.urlDownload() != null ? req.urlDownload().trim() : null);
        solicitacao.setTipoSoftware(req.tipoSoftware());
        solicitacao.setObjetivoUso(objetivoUso);
        solicitacao.setStatus(StatusSolicitacaoInstalacao.PENDENTE);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("CRIAR_SOLICITACAO_INSTALACAO", "SolicitacaoInstalacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId());
        return toResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoInstalacaoResponse cadastrar(SolicitacaoInstalacaoRequest req) {
        return criar(req);
    }

    @Transactional
    public SolicitacaoInstalacaoResponse atualizar(Long id, SolicitacaoInstalacaoRequest req) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de instalacao nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoInstalacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser editadas");
        }

        Long estacaoId = AdminValidation.idObrigatorio(req.estacaoId(), "Estacao");
        String sensoresSoftwares = AdminValidation.textoObrigatorio(req.sensoresSoftwares(), "Sensores/Softwares");
        String objetivoUso = AdminValidation.textoObrigatorio(req.objetivoUso(), "Objetivo de uso");

        EstacaoMeteorologica estacao = estacaoRepository.findById(estacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada"));
        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Estacao meteorologica esta inativa");
        }

        solicitacao.setEstacao(estacao);
        solicitacao.setSensoresSoftwares(sensoresSoftwares);
        solicitacao.setUrlDownload(req.urlDownload() != null ? req.urlDownload().trim() : null);
        solicitacao.setTipoSoftware(req.tipoSoftware());
        solicitacao.setObjetivoUso(objetivoUso);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("ATUALIZAR_SOLICITACAO_INSTALACAO", "SolicitacaoInstalacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId());
        return toResponse(solicitacao);
    }

    @Transactional
    public void inativar(Long id) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de instalacao nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoInstalacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser inativadas ou canceladas");
        }

        solicitacao.setStatus(StatusSolicitacaoInstalacao.INATIVO);
        solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("INATIVAR_SOLICITACAO_INSTALACAO", "SolicitacaoInstalacao", solicitacao.getId(),
                "Inativacao de solicitacao de instalacao pendente");
    }

    @Transactional
    public SolicitacaoInstalacaoResponse aprovar(Long id) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de instalacao nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoInstalacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser aprovadas");
        }

        EstacaoMeteorologica estacao = solicitacao.getEstacao();
        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Nao e possivel aprovar instalacao para uma estacao inativa");
        }

        solicitacao.setStatus(StatusSolicitacaoInstalacao.APROVADA);
        anexarSensorOuSoftwareNaEstacao(solicitacao, estacao);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("APROVAR_SOLICITACAO_INSTALACAO", "SolicitacaoInstalacao", solicitacao.getId(),
                "estacaoId=" + estacao.getId() + ", item=" + solicitacao.getSensoresSoftwares());
        return toResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoInstalacaoResponse reprovar(Long id, ReprovacaoRequest req) {
        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitacao de instalacao nao encontrada com o ID informado"));

        if (solicitacao.getStatus() != StatusSolicitacaoInstalacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitacoes com status PENDENTE podem ser reprovadas");
        }

        String justificativa = AdminValidation.textoObrigatorio(
                req != null ? req.justificativa() : null,
                "Justificativa de reprovacao"
        );

        solicitacao.setStatus(StatusSolicitacaoInstalacao.REPROVADA);
        solicitacao.setJustificativaReprovacao(justificativa);

        solicitacao = solicitacaoRepository.save(solicitacao);
        auditoriaService.registrar("REPROVAR_SOLICITACAO_INSTALACAO", "SolicitacaoInstalacao", solicitacao.getId(),
                "justificativa=" + justificativa);
        return toResponse(solicitacao);
    }

    private void anexarSensorOuSoftwareNaEstacao(SolicitacaoInstalacao solicitacao, EstacaoMeteorologica estacao) {
        String descricaoUpper = solicitacao.getSensoresSoftwares().toUpperCase(Locale.ROOT);
        boolean sensorAdicionado = false;

        for (TipoSensor tipo : TipoSensor.values()) {
            if (descricaoUpper.contains(tipo.name())) {
                var sensor = new Sensor();
                sensor.setTipo(tipo);
                sensor.setStatus(StatusEntidade.ATIVO);
                sensor.setEstacao(estacao);
                sensorRepository.save(sensor);
                sensorAdicionado = true;
            }
        }

        boolean possuiDadosSoftware = (solicitacao.getUrlDownload() != null && !solicitacao.getUrlDownload().isBlank())
                || solicitacao.getTipoSoftware() != null;

        if (possuiDadosSoftware || !sensorAdicionado) {
            var sw = new SoftwareColetaTransmissao();
            sw.setNome(solicitacao.getSensoresSoftwares().length() > 160
                    ? solicitacao.getSensoresSoftwares().substring(0, 160)
                    : solicitacao.getSensoresSoftwares());
            sw.setUrlDownload(solicitacao.getUrlDownload() != null && !solicitacao.getUrlDownload().isBlank()
                    ? solicitacao.getUrlDownload()
                    : "https://clima.ucsal.br/softwares/padrao");
            sw.setTipoSoftware(solicitacao.getTipoSoftware() != null
                    ? solicitacao.getTipoSoftware()
                    : TipoLicencaSoftware.SOFTWARE_LIVRE);
            sw.setObjetivoUso(solicitacao.getObjetivoUso());
            sw.setStatus(StatusEntidade.ATIVO);
            sw.setEstacao(estacao);
            softwareRepository.save(sw);
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

    private SolicitacaoInstalacaoResponse toResponse(SolicitacaoInstalacao s) {
        Long estacaoId = s.getEstacao() != null ? s.getEstacao().getId() : null;
        String estacaoSigla = s.getEstacao() != null ? s.getEstacao().getSigla() : null;
        String estacaoNome = s.getEstacao() != null ? s.getEstacao().getNome() : null;
        Long usuarioId = s.getSolicitante() != null ? s.getSolicitante().getId() : null;
        String nomeUsuario = s.getSolicitante() != null ? s.getSolicitante().getNomeCompleto() : null;

        return new SolicitacaoInstalacaoResponse(
                s.getId(),
                estacaoId,
                estacaoSigla,
                estacaoNome,
                usuarioId,
                nomeUsuario,
                s.getSensoresSoftwares(),
                s.getUrlDownload(),
                s.getTipoSoftware(),
                s.getObjetivoUso(),
                s.getDataSolicitacao(),
                s.getStatus(),
                s.getJustificativaReprovacao()
        );
    }
}
