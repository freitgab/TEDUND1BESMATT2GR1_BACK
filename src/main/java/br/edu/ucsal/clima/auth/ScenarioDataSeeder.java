package br.edu.ucsal.clima.auth;

import br.edu.ucsal.clima.domain.entity.*;
import br.edu.ucsal.clima.domain.enums.*;
import br.edu.ucsal.clima.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ScenarioDataSeeder implements CommandLineRunner {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrgaoGestorMeteorologiaRepository ogmRepository;
    private final DistritoMonitoramentoClimaticoRepository dmcRepository;
    private final RedeMonitoramentoRepository redeRepository;
    private final TecnicoMeteorologistaRepository tecnicoRepository;
    private final EstacaoMeteorologicaRepository estacaoRepository;
    private final SolicitacaoUsoEstacaoRepository solicitacaoUsoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.password}")
    private String senhaInicial;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Garantir usuarios diretores adicionais para os 3 OGMs (1 diretor unico por OGM)
        Usuario dirInmet = obterOuCriarUsuario("Administrador de Teste", "admin@clima.gov.br", NomePerfil.ADMINISTRADOR);
        Usuario dirCemaden = obterOuCriarUsuario("Gestor de Teste", "gestor@clima.gov.br", NomePerfil.ADMINISTRADOR);
        Usuario dirCptec = obterOuCriarUsuario("Diretor CPTEC/INPE", "diretor.cptec@clima.gov.br", NomePerfil.ADMINISTRADOR);
        Usuario usrTecnico = obterOuCriarUsuario("Tecnico de Teste", "tecnico@clima.gov.br", NomePerfil.COLABORADOR);
        Usuario usrPesquisador = obterOuCriarUsuario("Pesquisador de Teste", "pesquisador@clima.gov.br", NomePerfil.USUARIO);

        // 2. OGMs pre-cadastrados (Item 2.3 e 2.4 - deixando SIMBA nao cadastrado propositalmente)
        OrgaoGestorMeteorologia inmet = obterOuCriarOgm(
                "INMET",
                "Instituto Nacional de Meteorologia",
                "contato@inmet.gov.br",
                "(61) 2102-4700",
                dirInmet,
                "Coordenadoria de Estacoes Meteorologicas",
                "Carlos Eduardo Mendes"
        );

        OrgaoGestorMeteorologia cemaden = obterOuCriarOgm(
                "CEMADEN",
                "Centro Nacional de Monitoramento e Alertas de Desastres Naturais",
                "contato@cemaden.gov.br",
                "(12) 3205-0200",
                dirCemaden,
                "Coordenadoria de Alertas Hidrometeorologicos",
                "Fernanda Souza Rocha"
        );

        obterOuCriarOgm(
                "CPTEC/INPE",
                "Centro de Previsão de Tempo e Estudos Climáticos",
                "contato@cptec.inpe.br",
                "(12) 3186-8400",
                dirCptec,
                "Divisao de Modelagem Numerica",
                "Roberto Almeida Lima"
        );

        // 3. DMCs pre-cadastrados (Item 3.4 e 3.5 - deixando DMC-SEM e NEA-SP para cadastro ao vivo)
        DistritoMonitoramentoClimatico disme5 = obterOuCriarDmc(
                "5° Distrito de Meteorologia - Bahia (5° DISME)",
                "5disme.ba@inmet.gov.br",
                "(71) 3525-9800",
                inmet
        );

        obterOuCriarDmc(
                "Distrito de Monitoramento do Recôncavo (DMC-REC)",
                "dmc.rec@inmet.gov.br",
                "(75) 3621-4400",
                inmet
        );

        obterOuCriarDmc(
                "Distrito de Monitoramento do Oeste Baiano (DMC-OB)",
                "dmc.ob@inmet.gov.br",
                "(77) 3611-3200",
                inmet
        );

        obterOuCriarDmc(
                "Distrito de Monitoramento do Litoral Sul (DMC-LS)",
                "dmc.ls@inmet.gov.br",
                "(73) 3214-5100",
                inmet
        );

        DistritoMonitoramentoClimatico neaBa = obterOuCriarDmc(
                "Núcleo Estadual de Alertas da Bahia (NEA-BA)",
                "nea.ba@cemaden.gov.br",
                "(71) 3115-6000",
                cemaden
        );

        // 4. Vincular o tecnico tecnico@clima.gov.br ao 5° DISME
        TecnicoMeteorologista tecnico = obterOuCriarTecnico(
                "TEC-5DISME-001",
                usrTecnico.getNomeCompleto(),
                usrTecnico.getEmail(),
                "(71) 99888-1001",
                usrTecnico,
                disme5
        );

        if (disme5.getMeteorologistaChefe() == null) {
            disme5.setMeteorologistaChefe(tecnico);
            dmcRepository.save(disme5);
        }

        // 5. Redes de Monitoramento pre-cadastradas (Item 4.4)
        // Vinculadas ao 5° DISME:
        RedeMonitoramento reamRec = obterOuCriarRede(
                "REAM-REC",
                "Rede de Estações Automáticas do Recôncavo",
                disme5,
                Set.of("TEMPERATURA", "UMIDADE", "PRECIPITACAO", "VENTO"),
                tecnico
        );

        RedeMonitoramento rrml = obterOuCriarRede(
                "RRML",
                "Rede de Radares Meteorológicos do Litoral",
                disme5,
                Set.of("RADAR", "PRECIPITACAO", "VENTO"),
                tecnico
        );

        obterOuCriarRede(
                "RMCS",
                "Rede de Monitoramento de Chuvas do Semiárido",
                disme5,
                Set.of("PRECIPITACAO", "UMIDADE"),
                tecnico
        );

        // Vinculadas ao NEA-BA:
        obterOuCriarRede(
                "RAD",
                "Rede de Alerta de Deslizamentos",
                neaBa,
                Set.of("PRECIPITACAO", "UMIDADE"),
                null
        );

        obterOuCriarRede(
                "REC-CONV",
                "Rede de Estações Convencionais",
                neaBa,
                Set.of("TEMPERATURA", "PRESSAO", "PRECIPITACAO"),
                null
        );

        // 6. Duas Estacoes iniciais com status DISPONIVEL
        EstacaoMeteorologica estSalvador = obterOuCriarEstacao(
                "EST-SSA-OND",
                "Estação Automática Salvador - Ondina",
                TipoEstacao.AUTOMATICA,
                "Estação meteorológica automática localizada no campus de Ondina em Salvador/BA",
                12,
                -13.0016,
                -38.5078,
                reamRec,
                List.of(TipoSensor.TEMPERATURA, TipoSensor.UMIDADE, TipoSensor.PLUVIOMETRO),
                "ClimaSync Collector v2.1",
                "https://clima.gov.br/downloads/climasync-v2.1.tar.gz",
                TipoLicencaSoftware.SOFTWARE_LIVRE,
                "Coleta e transmissão automática de dados termopluviométricos",
                Set.of("TEMPERATURA", "UMIDADE", "PRECIPITACAO")
        );

        obterOuCriarEstacao(
                "RAD-LIT-NORTE",
                "Radar Meteorológico Litoral Norte",
                TipoEstacao.RADAR_METEOROLOGICO,
                "Radar meteorológico banda S para monitoramento severo do Litoral Norte da Bahia",
                8,
                -12.5253,
                -38.0031,
                rrml,
                List.of(TipoSensor.VENTO, TipoSensor.PLUVIOMETRO),
                "RadarScan Pro",
                "https://clima.gov.br/downloads/radarscan-pro",
                TipoLicencaSoftware.PROPRIETARIO,
                "Processamento volumétrico de refletividade e velocidade radial",
                Set.of("PRECIPITACAO", "VENTO")
        );

        // 7. Solicitacoes de Uso iniciais (1 APROVADA em 13/03/2026 07:00-09:30 e 1 PENDENTE)
        if (solicitacaoUsoRepository.count() == 0) {
            var solAprovada = new SolicitacaoUsoEstacao();
            solAprovada.setEstacao(estSalvador);
            solAprovada.setDistrito(disme5);
            solAprovada.setSolicitante(usrPesquisador);
            solAprovada.setNomeUsuario(usrPesquisador.getNomeCompleto());
            solAprovada.setDataUso(LocalDate.of(2026, 3, 13));
            solAprovada.setHoraInicio(LocalTime.of(7, 0));
            solAprovada.setHoraFim(LocalTime.of(9, 30));
            solAprovada.setStatus(StatusSolicitacaoUso.APROVADA);
            solicitacaoUsoRepository.save(solAprovada);

            var solPendente = new SolicitacaoUsoEstacao();
            solPendente.setEstacao(estSalvador);
            solPendente.setDistrito(disme5);
            solPendente.setSolicitante(usrPesquisador);
            solPendente.setNomeUsuario(usrPesquisador.getNomeCompleto());
            solPendente.setDataUso(LocalDate.of(2026, 3, 13));
            solPendente.setHoraInicio(LocalTime.of(8, 30));
            solPendente.setHoraFim(LocalTime.of(10, 0));
            solPendente.setStatus(StatusSolicitacaoUso.PENDENTE);
            solicitacaoUsoRepository.save(solPendente);
        }
    }

    private Usuario obterOuCriarUsuario(String nome, String email, NomePerfil nomePerfil) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Perfil perfil = perfilRepository.findByNome(nomePerfil).orElseThrow();
            var u = new Usuario();
            u.setNomeCompleto(nome);
            u.setEmail(email);
            u.setSenhaHash(passwordEncoder.encode(senhaInicial));
            u.setStatus(StatusEntidade.ATIVO);
            u.setPerfis(new HashSet<>(Set.of(perfil)));
            return usuarioRepository.save(u);
        });
    }

    private OrgaoGestorMeteorologia obterOuCriarOgm(
            String sigla,
            String nome,
            String email,
            String telefone,
            Usuario diretor,
            String nomeSetor,
            String responsavelSetor
    ) {
        return ogmRepository.findBySiglaIgnoreCase(sigla).orElseGet(() -> {
            var ogm = new OrgaoGestorMeteorologia();
            ogm.setSigla(sigla);
            ogm.setNome(nome);
            ogm.setEmailCentral(email);
            ogm.setTelefoneCentral(telefone);
            ogm.setDiretorGeral(diretor);
            ogm.setStatus(StatusEntidade.ATIVO);

            var setor = new SetorCoordenadoria();
            setor.setNome(nomeSetor);
            setor.setNomeacaoResponsavel(responsavelSetor);
            setor.setOrgaoGestor(ogm);
            ogm.getSetores().add(setor);

            return ogmRepository.save(ogm);
        });
    }

    private DistritoMonitoramentoClimatico obterOuCriarDmc(
            String nome,
            String email,
            String telefone,
            OrgaoGestorMeteorologia ogm
    ) {
        return dmcRepository.findByNomeIgnoreCase(nome).orElseGet(() -> {
            var dmc = new DistritoMonitoramentoClimatico();
            dmc.setNome(nome);
            dmc.setEmailCentral(email);
            dmc.setTelefoneCentral(telefone);
            dmc.setOrgaoGestor(ogm);
            dmc.setStatus(StatusEntidade.ATIVO);
            return dmcRepository.save(dmc);
        });
    }

    private TecnicoMeteorologista obterOuCriarTecnico(
            String matricula,
            String nomeCompleto,
            String email,
            String telefone,
            Usuario usuario,
            DistritoMonitoramentoClimatico dmc
    ) {
        return tecnicoRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            var t = new TecnicoMeteorologista();
            t.setMatricula(matricula);
            t.setNomeCompleto(nomeCompleto);
            t.setEmail(email);
            t.setTelefone(telefone);
            t.setUsuario(usuario);
            t.setDistrito(dmc);
            t.setStatus(StatusEntidade.ATIVO);
            return tecnicoRepository.save(t);
        });
    }

    private RedeMonitoramento obterOuCriarRede(
            String sigla,
            String descricao,
            DistritoMonitoramentoClimatico dmc,
            Set<String> tiposMonitoramento,
            TecnicoMeteorologista responsavel
    ) {
        return redeRepository.findBySiglaIgnoreCase(sigla).orElseGet(() -> {
            var rede = new RedeMonitoramento();
            rede.setSigla(sigla);
            rede.setDescricao(descricao);
            rede.setDistrito(dmc);
            rede.setTiposMonitoramento(new HashSet<>(tiposMonitoramento));
            rede.setMeteorologistaResponsavel(responsavel);
            rede.setStatus(StatusEntidade.ATIVO);
            return redeRepository.save(rede);
        });
    }

    private EstacaoMeteorologica obterOuCriarEstacao(
            String sigla,
            String nome,
            TipoEstacao tipo,
            String descricao,
            Integer capacidade,
            Double lat,
            Double lon,
            RedeMonitoramento rede,
            List<TipoSensor> tiposSensores,
            String nomeSoftware,
            String urlDownload,
            TipoLicencaSoftware tipoLicenca,
            String objetivoSoftware,
            Set<String> variaveis
    ) {
        return estacaoRepository.findBySiglaIgnoreCase(sigla).orElseGet(() -> {
            var est = new EstacaoMeteorologica();
            est.setSigla(sigla);
            est.setNome(nome);
            est.setTipoEstacao(tipo);
            est.setDescricaoComplementar(descricao);
            est.setCapacidadeMaximaSuportada(capacidade);
            est.setLatitude(lat);
            est.setLongitude(lon);
            est.setRede(rede);
            est.setStatus(StatusEstacao.DISPONIVEL);

            for (TipoSensor ts : tiposSensores) {
                var s = new Sensor();
                s.setTipo(ts);
                s.setStatus(StatusEntidade.ATIVO);
                s.setEstacao(est);
                est.getSensores().add(s);
            }

            var sw = new SoftwareColetaTransmissao();
            sw.setNome(nomeSoftware);
            sw.setUrlDownload(urlDownload);
            sw.setTipoSoftware(tipoLicenca);
            sw.setObjetivoUso(objetivoSoftware);
            sw.setVariaveisMonitoradas(new HashSet<>(variaveis));
            sw.setStatus(StatusEntidade.ATIVO);
            sw.setEstacao(est);
            est.getSoftwares().add(sw);

            return estacaoRepository.save(est);
        });
    }
}
