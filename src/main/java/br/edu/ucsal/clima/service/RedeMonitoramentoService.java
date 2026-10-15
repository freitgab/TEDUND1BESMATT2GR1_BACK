package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.DistritoMonitoramentoClimatico;
import br.edu.ucsal.clima.domain.entity.RedeMonitoramento;
import br.edu.ucsal.clima.domain.entity.TecnicoMeteorologista;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.dto.RedeRequest;
import br.edu.ucsal.clima.dto.RedeResponse;
import br.edu.ucsal.clima.repository.DistritoMonitoramentoClimaticoRepository;
import br.edu.ucsal.clima.repository.EstacaoMeteorologicaRepository;
import br.edu.ucsal.clima.repository.RedeMonitoramentoRepository;
import br.edu.ucsal.clima.repository.TecnicoMeteorologistaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedeMonitoramentoService {

    private final RedeMonitoramentoRepository redeRepository;
    private final DistritoMonitoramentoClimaticoRepository dmcRepository;
    private final TecnicoMeteorologistaRepository tecnicoRepository;
    private final EstacaoMeteorologicaRepository estacaoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<RedeResponse> listarTodos() {
        return redeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RedeResponse buscarPorId(Long id) {
        return redeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Rede nao encontrada com o ID informado"));
    }

    @Transactional
    public RedeResponse cadastrar(RedeRequest req) {
        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String descricao = AdminValidation.textoObrigatorio(req.descricao(), "Descricao");
        Long distritoId = AdminValidation.idObrigatorio(req.distritoId(), "Distrito");

        if (redeRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new IllegalStateException("Sigla ja cadastrada para outra Rede");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(distritoId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        TecnicoMeteorologista responsavel = null;
        if (req.meteorologistaResponsavelId() != null) {
            responsavel = tecnicoRepository.findById(req.meteorologistaResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Meteorologista responsavel nao encontrado"));
            AdminValidation.requireAtivo(responsavel.getStatus(), "Meteorologista responsavel");
        }

        if (req.tiposMonitoramento() == null || req.tiposMonitoramento().isEmpty()) {
            throw new IllegalArgumentException("Ao menos um tipo de monitoramento deve ser informado");
        }

        var rede = new RedeMonitoramento();
        rede.setSigla(sigla);
        rede.setDescricao(descricao);
        rede.setDistrito(dmc);
        rede.setTiposMonitoramento(new HashSet<>(req.tiposMonitoramento()));
        rede.setMeteorologistaResponsavel(responsavel);
        rede.setStatus(StatusEntidade.ATIVO);

        rede = redeRepository.save(rede);
        auditoriaService.registrar("CADASTRAR", "RedeMonitoramento", rede.getId(), "sigla=" + rede.getSigla());
        return toResponse(rede);
    }

    @Transactional
    public RedeResponse atualizar(Long id, RedeRequest req) {
        var rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rede nao encontrada com o ID informado"));

        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String descricao = AdminValidation.textoObrigatorio(req.descricao(), "Descricao");
        Long distritoId = AdminValidation.idObrigatorio(req.distritoId(), "Distrito");

        if (redeRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new IllegalStateException("Sigla ja cadastrada para outra Rede");
        }

        DistritoMonitoramentoClimatico dmc = dmcRepository.findById(distritoId)
                .orElseThrow(() -> new EntityNotFoundException("DMC nao encontrado"));
        AdminValidation.requireAtivo(dmc.getStatus(), "DMC");

        TecnicoMeteorologista responsavel = null;
        if (req.meteorologistaResponsavelId() != null) {
            responsavel = tecnicoRepository.findById(req.meteorologistaResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Meteorologista responsavel nao encontrado"));
            AdminValidation.requireAtivo(responsavel.getStatus(), "Meteorologista responsavel");
        }

        if (req.tiposMonitoramento() == null || req.tiposMonitoramento().isEmpty()) {
            throw new IllegalArgumentException("Ao menos um tipo de monitoramento deve ser informado");
        }

        rede.setSigla(sigla);
        rede.setDescricao(descricao);
        rede.setDistrito(dmc);
        rede.setTiposMonitoramento(new HashSet<>(req.tiposMonitoramento()));
        rede.setMeteorologistaResponsavel(responsavel);

        rede = redeRepository.save(rede);
        auditoriaService.registrar("ATUALIZAR", "RedeMonitoramento", rede.getId(), "sigla=" + rede.getSigla());
        return toResponse(rede);
    }

    @Transactional
    public void inativar(Long id) {
        var rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rede nao encontrada com o ID informado"));

        if (rede.getStatus() == StatusEntidade.INATIVO) {
            throw new IllegalStateException("Rede ja esta inativa");
        }

        if (estacaoRepository.existsByRede_IdAndStatusNot(id, StatusEstacao.INATIVO)) {
            throw new IllegalStateException("Nao e possivel inativar Rede com Estacoes Meteorologicas ativas vinculadas");
        }

        rede.setStatus(StatusEntidade.INATIVO);
        redeRepository.save(rede);
        auditoriaService.registrar("INATIVAR", "RedeMonitoramento", rede.getId(), "sigla=" + rede.getSigla());
    }

    private RedeResponse toResponse(RedeMonitoramento rede) {
        Long dmcId = rede.getDistrito() != null ? rede.getDistrito().getId() : null;
        String dmcNome = rede.getDistrito() != null ? rede.getDistrito().getNome() : null;
        Long respId = rede.getMeteorologistaResponsavel() != null ? rede.getMeteorologistaResponsavel().getId() : null;
        String respNome = rede.getMeteorologistaResponsavel() != null ? rede.getMeteorologistaResponsavel().getNomeCompleto() : null;

        return new RedeResponse(
                rede.getId(),
                rede.getSigla(),
                rede.getDescricao(),
                dmcId,
                dmcNome,
                rede.getTiposMonitoramento(),
                respId,
                respNome,
                rede.getDataCadastro(),
                rede.getStatus()
        );
    }
}
