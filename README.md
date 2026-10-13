# TEDUND1BESMATT2GR1_BACK
Official repositotry involving the backend from SWA project, using JPA.

### Nível 1: Diagrama de Contexto
```mermaid
C4Context
    title Diagrama de Contexto - Sistema de Controle e Monitoramento do Clima

    %% Atores
    Person(admin, "Administrador", "Gerencia informações estruturais, cadastra OGMs, DMCs, Redes e gerencia técnicos e perfis.")
    Person(colab, "Colaborador", "Técnico meteorologista vinculado a um DMC. Gerencia estações e aprova/reprova requisições.")
    Person(usuario, "Usuário", "Pesquisador ou Produtor Rural. Solicita uso de estações e instalação de softwares/sensores.")

    %% Sistema Principal
    System(sistema_clima, "Sistema de Controle e Monitoramento do Clima", "Plataforma web para gestão de recursos meteorológicos, controle de infraestrutura climática e aprovação de reservas.")

    %% Relacionamentos
    Rel(admin, sistema_clima, "Gerencia OGM, DMC, Redes, Técnicos e Logs", "Acesso Web")
    Rel(colab, sistema_clima, "Cadastra/Inativa estações, julga solicitações de uso e instalação", "Acesso Web")
    Rel(usuario, sistema_clima, "Submete pedidos individuais de estações e libera as estações pós-uso", "Acesso Web")

C4Container
    title Diagrama de Contêineres - Sistema de Controle e Monitoramento do Clima

    %% Atores
    Person(atores, "Atores do Sistema", "Administrador, Colaborador ou Usuário Autenticado.")

    %% Boundary do Sistema
    System_Boundary(sistema_boundary, "Sistema de Controle e Monitoramento do Clima") {
        
        Container(frontend, "Front-End Application", "Angular", "Interface gráfica com usuário responsiva, centralizando toda a interação e visões adaptáveis por perfil.")
        
        Container(backend, "Back-End Application", "Java + Spring", "Processa o negócio, autorização e orquestração. Verifica solicitações, horários e concorrência.")
        
        ContainerDb(database, "Banco de Dados Relacional", "PostgreSQL", "Armazena dados de OGMs, DMCs, estações, solicitações, logs e credenciais do Spring Security.")
    }

    %% Relacionamentos
    Rel(atores, frontend, "Interage com as funcionalidades através de HTTPS", "JSON/HTTPS")
    Rel(frontend, backend, "Solicita dados e envia comandos (API REST)", "JSON/HTTPS")
    Rel(backend, database, "Lê e grava dados persistentes e de autorização", "JDBC/SQL")
    
    UpdateRelStyle(atores, frontend, $offsetX="-30")
    UpdateRelStyle(frontend, backend, $offsetX="-30")
    UpdateRelStyle(backend, database, $offsetX="-30")

C4Component
    title Diagrama de Componentes (Nível 3) - Back-End Application

    Container_Boundary(backend, "Back-End Application (Java + Spring)") {
        
        Component(security, "Security Component", "Spring Security", "Interceta as requisições do Front-End, valida as credenciais e aplica a autorização baseada nos perfis de Administrador, Colaborador e Usuário.")
        
        Component(api, "REST API Controllers", "Spring MVC / RestController", "Expõe os endpoints HTTPS para que o Front-End Angular possa consumir os dados do sistema.")
        
        Component(admin_service, "Administração Service", "Spring Service", "Contém a lógica para gerir OGMs, DMCs, Redes de Monitoramento e utilizadores (Técnicos).")
        
        Component(estacao_service, "Estação Service", "Spring Service", "Processa o registo, a edição e a inativação das estações meteorológicas, validando os seus status.")
        
        Component(solicitacao_service, "Solicitação Service", "Spring Service", "Verifica a concorrência de horários e processa as aprovações e reprovações de uso das estações e da instalação de sensores.")
        
        Component(relatorio_service, "Relatório Service", "Spring Service", "Consolida os dados de negócio e emite os relatórios de auditoria e de requisições de uso.")
        
        Component(repositorios, "Data Repositories", "Spring Data JPA", "Interfaces que abstraem as consultas e a persistência de todas as entidades no banco relacional.")
    }

    ContainerDb(database, "Banco de Dados", "PostgreSQL", "Armazena os dados de negócio e as credenciais.")
    Container(frontend, "Front-End Application", "Angular", "Interface do utilizador.")

    Rel(frontend, api, "Faz requisições JSON via HTTPS", "HTTPS")
    Rel(api, security, "Delega a validação de autenticação e permissões", "Filtro de Requisição")
    Rel(security, api, "Permite ou nega o acesso ao recurso solicitado")
    
    Rel(api, admin_service, "Encaminha requisições administrativas")
    Rel(api, estacao_service, "Encaminha requisições de estações")
    Rel(api, solicitacao_service, "Encaminha requisições de reservas")
    Rel(api, relatorio_service, "Encaminha requisições de relatórios")

    Rel(admin_service, repositorios, "Lê/Grava dados")
    Rel(estacao_service, repositorios, "Lê/Grava dados")
    Rel(solicitacao_service, repositorios, "Lê/Grava dados")
    Rel(relatorio_service, repositorios, "Consulta dados consolidados")

    Rel(repositorios, database, "Executa queries e procedimentos", "JDBC/SQL")
