# TEDUND1BESMATT2GR1_BACK
Official repositotry involving the backend from SWA project, using JPA.

### Nível 1: Diagrama de Contexto
```mermaid
C4Context
    title Diagrama de Contexto - Sistema de Controle e Monitoramento do Clima

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")

    Person(admin, "Administrador", "Gerencia informações estruturais,<br>cadastra OGMs, DMCs, Redes<br>e gerencia perfis.")
    Person(colab, "Colaborador", "Técnico meteorologista de um DMC.<br>Gerencia estações e<br>julga requisições.")
    Person(usuario, "Usuário", "Pesquisador/Produtor Rural.<br>Solicita uso de estações e<br>instalação de sensores.")

    System(sistema_clima, "Sistema de Controle e Monitoramento", "Plataforma web para gestão<br>de recursos meteorológicos<br>e aprovação de reservas.")

    Rel(admin, sistema_clima, "Gerencia OGM, DMC, Redes", "Web")
    Rel(colab, sistema_clima, "Cadastra/Inativa estações", "Web")
    Rel(usuario, sistema_clima, "Submete e libera estações", "Web")
```

### Nível 2: Diagrama de Contêineres
```mermaid
C4Container
    title Diagrama de Contêineres - Sistema de Controle e Monitoramento do Clima

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")

    Person(atores, "Atores do Sistema", "Administrador, Colaborador<br>ou Usuário Autenticado.")

    System_Boundary(sistema_boundary, "Sistema de Controle e Monitoramento") {
        
        Container(frontend, "Front-End Application", "Angular", "Interface gráfica responsiva,<br>centralizando a interação.")
        Container(backend, "Back-End Application", "Java + Spring", "Processa o negócio e autorização.<br>Verifica concorrência.")
        ContainerDb(database, "Banco de Dados Relacional", "PostgreSQL", "Armazena dados das estações,<br>solicitações e credenciais.")
    }

    Rel(atores, frontend, "Interage com o sistema", "HTTPS")
    Rel(frontend, backend, "Solicita dados/comandos", "JSON/HTTPS")
    Rel(backend, database, "Lê e grava dados", "JDBC/SQL")
```

### Nível 3: Diagrama de Componentes (Back-End)
```mermaid
C4Component
    title Diagrama de Componentes (Nível 3) - Back-End Application

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="2")

    Container_Boundary(backend, "Back-End Application (Java + Spring)") {
        
        Component(security, "Security Component", "Spring Security", "Valida credenciais e<br>aplica autorização.")
        Component(api, "REST API Controllers", "Spring MVC", "Expõe os endpoints<br>HTTPS para o Front-End.")
        Component(admin_service, "Administração Service", "Spring Service", "Gere OGMs, DMCs,<br>Redes e Técnicos.")
        Component(estacao_service, "Estação Service", "Spring Service", "Processa o registo e<br>inativação de estações.")
        Component(solicitacao_service, "Solicitação Service", "Spring Service", "Verifica concorrência<br>de horários e reservas.")
        Component(relatorio_service, "Relatório Service", "Spring Service", "Emite os relatórios<br>de auditoria.")
        Component(repositorios, "Data Repositories", "Spring Data JPA", "Abstrai a persistência<br>no banco relacional.")
    }

    ContainerDb(database, "Banco de Dados", "PostgreSQL", "Armazena os dados.")
    Container(frontend, "Front-End Application", "Angular", "Interface do utilizador.")

    Rel(frontend, api, "Faz requisições", "JSON/HTTPS")
    Rel(api, security, "Valida acesso", "Filtro")
    Rel(security, api, "Permite acesso", "Filtro")
    
    Rel(api, admin_service, "Encaminha", "Método")
    Rel(api, estacao_service, "Encaminha", "Método")
    Rel(api, solicitacao_service, "Encaminha", "Método")
    Rel(api, relatorio_service, "Encaminha", "Método")

    Rel(repositorios, database, "Executa queries", "JDBC/SQL")
```
