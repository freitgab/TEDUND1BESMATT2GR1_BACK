package br.edu.ucsal.clima.service;

import br.edu.ucsal.clima.domain.entity.EstacaoMeteorologica;
import br.edu.ucsal.clima.domain.entity.RedeMonitoramento;
import br.edu.ucsal.clima.domain.entity.Sensor;
import br.edu.ucsal.clima.domain.entity.SoftwareColetaTransmissao;
import br.edu.ucsal.clima.domain.enums.StatusEntidade;
import br.edu.ucsal.clima.domain.enums.StatusEstacao;
import br.edu.ucsal.clima.domain.enums.TipoEstacao;
import br.edu.ucsal.clima.domain.enums.TipoSensor;
import br.edu.ucsal.clima.dto.EstacaoRequest;
import br.edu.ucsal.clima.dto.EstacaoResponse;
import br.edu.ucsal.clima.dto.SoftwareDTO;
import br.edu.ucsal.clima.repository.EstacaoMeteorologicaRepository;
import br.edu.ucsal.clima.repository.RedeMonitoramentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstacaoMeteorologicaService {

    private final EstacaoMeteorologicaRepository estacaoRepository;
    private final RedeMonitoramentoRepository redeRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<EstacaoResponse> listarTodas() {
        return estacaoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EstacaoResponse> listarTodos() {
        return listarTodas();
    }

    @Transactional(readOnly = true)
    public EstacaoResponse buscarPorId(Long id) {
        return estacaoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada com o ID informado"));
    }

    @Transactional
    public EstacaoResponse cadastrar(EstacaoRequest req) {
        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long redeId = AdminValidation.idObrigatorio(req.redeMonitoramentoId(), "Rede de Monitoramento");

        if (req.tipoEstacao() == null) {
            throw new IllegalArgumentException("Tipo da estacao e obrigatorio");
        }
        if (req.capacidadeMaximaSuportada() == null || req.capacidadeMaximaSuportada() <= 0) {
            throw new IllegalArgumentException("Capacidade maxima suportada deve ser maior que zero");
        }
        if (req.latitude() == null || req.longitude() == null) {
            throw new IllegalArgumentException("Latitude e longitude sao obrigatorias");
        }

        if (estacaoRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new IllegalStateException("Sigla ja cadastrada para outra estacao meteorologica");
        }
        if (estacaoRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Nome ja cadastrado para outra estacao meteorologica");
        }

        RedeMonitoramento rede = redeRepository.findById(redeId)
                .orElseThrow(() -> new EntityNotFoundException("Rede de monitoramento nao encontrada"));
        AdminValidation.requireAtivo(rede.getStatus(), "Rede de Monitoramento");

        List<SoftwareDTO> softwaresNormalizados = normalizarSoftwares(req);
        validarEstacaoAutomaticaOuRadar(req.tipoEstacao(), req.sensores(), softwaresNormalizados);

        var estacao = new EstacaoMeteorologica();
        estacao.setSigla(sigla);
        estacao.setNome(nome);
        estacao.setTipoEstacao(req.tipoEstacao());
        estacao.setDescricaoComplementar(req.descricaoComplementar() != null ? req.descricaoComplementar().trim() : null);
        estacao.setCapacidadeMaximaSuportada(req.capacidadeMaximaSuportada());
        estacao.setLatitude(req.latitude());
        estacao.setLongitude(req.longitude());
        estacao.setRede(rede);
        estacao.setStatus(StatusEstacao.DISPONIVEL);

        mapearSensoresESoftwares(req.sensores(), softwaresNormalizados, estacao);

        estacao = estacaoRepository.save(estacao);
        auditoriaService.registrar("CADASTRAR", "EstacaoMeteorologica", estacao.getId(), "sigla=" + estacao.getSigla());
        return toResponse(estacao);
    }

    @Transactional
    public EstacaoResponse atualizar(Long id, EstacaoRequest req) {
        var estacao = estacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada com o ID informado"));

        String sigla = AdminValidation.textoObrigatorio(req.sigla(), "Sigla");
        String nome = AdminValidation.textoObrigatorio(req.nome(), "Nome");
        Long redeId = AdminValidation.idObrigatorio(req.redeMonitoramentoId(), "Rede de Monitoramento");

        if (req.tipoEstacao() == null) {
            throw new IllegalArgumentException("Tipo da estacao e obrigatorio");
        }
        if (req.capacidadeMaximaSuportada() == null || req.capacidadeMaximaSuportada() <= 0) {
            throw new IllegalArgumentException("Capacidade maxima suportada deve ser maior que zero");
        }
        if (req.latitude() == null || req.longitude() == null) {
            throw new IllegalArgumentException("Latitude e longitude sao obrigatorias");
        }

        if (estacaoRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new IllegalStateException("Sigla ja cadastrada para outra estacao meteorologica");
        }
        if (estacaoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new IllegalStateException("Nome ja cadastrado para outra estacao meteorologica");
        }

        RedeMonitoramento rede = redeRepository.findById(redeId)
                .orElseThrow(() -> new EntityNotFoundException("Rede de monitoramento nao encontrada"));
        AdminValidation.requireAtivo(rede.getStatus(), "Rede de Monitoramento");

        List<SoftwareDTO> softwaresNormalizados = normalizarSoftwares(req);
        validarEstacaoAutomaticaOuRadar(req.tipoEstacao(), req.sensores(), softwaresNormalizados);

        estacao.setSigla(sigla);
        estacao.setNome(nome);
        estacao.setTipoEstacao(req.tipoEstacao());
        estacao.setDescricaoComplementar(req.descricaoComplementar() != null ? req.descricaoComplementar().trim() : null);
        estacao.setCapacidadeMaximaSuportada(req.capacidadeMaximaSuportada());
        estacao.setLatitude(req.latitude());
        estacao.setLongitude(req.longitude());
        estacao.setRede(rede);

        estacao.getSensores().clear();
        estacao.getSoftwares().clear();
        mapearSensoresESoftwares(req.sensores(), softwaresNormalizados, estacao);

        estacao = estacaoRepository.save(estacao);
        auditoriaService.registrar("ATUALIZAR", "EstacaoMeteorologica", estacao.getId(), "sigla=" + estacao.getSigla());
        return toResponse(estacao);
    }

    @Transactional
    public void inativar(Long id) {
        var estacao = estacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estacao meteorologica nao encontrada com o ID informado"));

        if (estacao.getStatus() == StatusEstacao.INATIVO) {
            throw new IllegalStateException("Estacao meteorologica ja esta inativa");
        }

        estacao.setStatus(StatusEstacao.INATIVO);
        estacaoRepository.save(estacao);
        auditoriaService.registrar("INATIVAR", "EstacaoMeteorologica", estacao.getId(), "sigla=" + estacao.getSigla());
    }

    private List<SoftwareDTO> normalizarSoftwares(EstacaoRequest req) {
        List<SoftwareDTO> lista = new ArrayList<>();
        if (req.softwares() != null && !req.softwares().isEmpty()) {
            lista.addAll(req.softwares());
        } else if (req.urlDownload() != null || req.tipoSoftware() != null || req.objetivoUso() != null || req.variaveisMonitoradas() != null) {
            String nomeSw = (req.nomeSoftware() != null && !req.nomeSoftware().isBlank())
                    ? req.nomeSoftware().trim()
                    : "Software " + req.sigla();
            lista.add(new SoftwareDTO(
                    null,
                    nomeSw,
                    req.urlDownload(),
                    req.tipoSoftware(),
                    req.objetivoUso(),
                    req.variaveisMonitoradas()
            ));
        }
        return lista;
    }

    private void validarEstacaoAutomaticaOuRadar(TipoEstacao tipo, List<TipoSensor> sensores, List<SoftwareDTO> softwares) {
        if (tipo == TipoEstacao.AUTOMATICA || tipo == TipoEstacao.RADAR_METEOROLOGICO) {
            if (sensores == null || sensores.isEmpty()) {
                throw new IllegalArgumentException("Estacoes do tipo AUTOMATICA ou RADAR_METEOROLOGICO exigem ao menos 1 tipo de sensor");
            }
            if (softwares == null || softwares.isEmpty()) {
                throw new IllegalArgumentException("Estacoes do tipo AUTOMATICA ou RADAR_METEOROLOGICO exigem os dados do software de coleta/transmissao");
            }
            for (SoftwareDTO sw : softwares) {
                if (sw.urlDownload() == null || sw.urlDownload().isBlank()
                        || sw.tipoSoftware() == null
                        || sw.objetivoUso() == null || sw.objetivoUso().isBlank()
                        || sw.variaveisMonitoradas() == null || sw.variaveisMonitoradas().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Dados do software de coleta/transmissao incompletos: informe urlDownload, tipoSoftware, objetivoUso e variaveisMonitoradas");
                }
            }
        }
    }

    private void mapearSensoresESoftwares(List<TipoSensor> sensores, List<SoftwareDTO> softwares, EstacaoMeteorologica estacao) {
        if (sensores != null) {
            for (TipoSensor tipoSensor : sensores) {
                if (tipoSensor == null) continue;
                var sensor = new Sensor();
                sensor.setTipo(tipoSensor);
                sensor.setStatus(StatusEntidade.ATIVO);
                sensor.setEstacao(estacao);
                estacao.getSensores().add(sensor);
            }
        }
        if (softwares != null) {
            for (SoftwareDTO dto : softwares) {
                if (dto == null) continue;
                var sw = new SoftwareColetaTransmissao();
                sw.setNome(dto.nome() != null && !dto.nome().isBlank() ? dto.nome().trim() : "Software " + estacao.getSigla());
                sw.setUrlDownload(AdminValidation.textoObrigatorio(dto.urlDownload(), "URL de download do software"));
                if (dto.tipoSoftware() == null) {
                    throw new IllegalArgumentException("Tipo de licenca do software e obrigatorio");
                }
                sw.setTipoSoftware(dto.tipoSoftware());
                sw.setObjetivoUso(AdminValidation.textoObrigatorio(dto.objetivoUso(), "Objetivo de uso do software"));
                if (dto.variaveisMonitoradas() != null) {
                    sw.setVariaveisMonitoradas(new HashSet<>(dto.variaveisMonitoradas()));
                }
                sw.setStatus(StatusEntidade.ATIVO);
                sw.setEstacao(estacao);
                estacao.getSoftwares().add(sw);
            }
        }
    }

    private EstacaoResponse toResponse(EstacaoMeteorologica estacao) {
        Long redeId = estacao.getRede() != null ? estacao.getRede().getId() : null;
        String redeSigla = estacao.getRede() != null ? estacao.getRede().getSigla() : null;

        List<TipoSensor> sensores = estacao.getSensores().stream()
                .map(Sensor::getTipo)
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        List<SoftwareDTO> softwares = estacao.getSoftwares().stream()
                .map(sw -> new SoftwareDTO(
                        sw.getId(),
                        sw.getNome(),
                        sw.getUrlDownload(),
                        sw.getTipoSoftware(),
                        sw.getObjetivoUso(),
                        sw.getVariaveisMonitoradas()
                ))
                .toList();

        return new EstacaoResponse(
                estacao.getId(),
                estacao.getSigla(),
                estacao.getNome(),
                estacao.getTipoEstacao(),
                estacao.getDescricaoComplementar(),
                estacao.getCapacidadeMaximaSuportada(),
                estacao.getLatitude(),
                estacao.getLongitude(),
                redeId,
                redeSigla,
                sensores,
                softwares,
                estacao.getDataCadastro(),
                estacao.getStatus()
        );
    }
}
