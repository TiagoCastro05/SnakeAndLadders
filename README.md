# Jogo Cobras e Escadas - Documentação Técnica

## Visão Geral

Este projeto implementa um jogo multiplayer de Cobras e Escadas usando JavaFX para a interface gráfica e sockets para comunicação em rede. O sistema é baseado em arquitetura cliente-servidor, permitindo que múltiplos jogadores se conectem e joguem em tempo real.

## Arquitetura do Sistema

### Componentes Principais

1. **Servidor (`LobbyServidorController`)**
   - Gerencia conexões de clientes
   - Controla o estado do jogo
   - Distribui atualizações para todos os clientes
   - Gera tabuleiros aleatórios

2. **Cliente (`LobbyClienteController` + `TabuleiroController`)**
   - Interface gráfica do jogador
   - Comunicação com servidor via sockets
   - Renderização do tabuleiro e animações

3. **Modelo de Dados (`model` package)**
   - `Jogo`: Controla lógica principal do jogo
   - `Tabuleiro`: Representa o tabuleiro com cobras e escadas
   - `Jogador`: Dados do jogador
   - `Peca`: Representação da peça no tabuleiro
   - `Dado`: Lógica do dado
   - `Casas`: Representa cada casa do tabuleiro

## Classes Principais

### TabuleiroController

**Responsabilidades:**
- Gerenciamento da interface gráfica do tabuleiro
- Coordenação de animações (peças, dado, cobra/escada)
- Comunicação em tempo real com servidor
- Processamento de eventos de interface

**Métodos Principais:**
- `setSocket()`: Configura comunicação com servidor
- `desenharTabuleiro()`: Renderiza tabuleiro 10x10
- `processarEstado()`: Processa atualizações do servidor
- `animarMovimentoPeca()`: Anima movimento gradual de peças
- `animarMovimentoCobra()`: Anima movimento em curva para cobras
- `animarMovimentoReto()`: Anima movimento em linha reta para escadas

### LobbyServidorController

**Responsabilidades:**
- Aceitar conexões de clientes
- Gerenciar estado global do jogo
- Processar comandos dos clientes
- Distribuir atualizações de estado

### LobbyClienteController

**Responsabilidades:**
- Conectar ao servidor
- Transição para tela de jogo
- Configuração inicial do cliente

## Protocolo de Comunicação

### Mensagens Cliente → Servidor
- `ROLAR_DADO`: Solicita rolagem do dado
- `REINICIAR_SIM`: Vota sim para reiniciar jogo
- `REINICIAR_NAO`: Vota não para reiniciar jogo

### Mensagens Servidor → Cliente
- `START`: Inicia transição para tabuleiro
- `COBRAS:cabeca1-cauda1,cabeca2-cauda2,...`: Define posições das cobras
- `ESCADAS:base1-topo1,base2-topo2,...`: Define posições das escadas
- `ESTADO`: Seguido por dados do estado atual:
  - `vez:jogador1,jogador2,...`
  - `indiceVez:N`
  - `posicoes:pos1,pos2,...`
  - `vitorias:vit1,vit2,...`
  - `dado:N`
  - `status:mensagem...`
  - `fim:0|1`

## Recursos Visuais

### Animações
- **Dado**: Faces aleatórias antes do resultado final
- **Movimento normal**: Peça se move casa por casa
- **Cobra**: Movimento em curva suave para baixo
- **Escada**: Movimento em linha reta para cima
- **Mensagens**: Notificações temporárias no centro da tela

### Interface
- **Tabuleiro**: Grid 10x10 com numeração serpentina (1-100)
- **Peças**: Imagens diferenciadas por jogador
- **Cobras/Escadas**: Marcação visual com emojis e cores
- **Painel lateral**: Informações dos jogadores e placar
- **Setas**: Indicadores visuais das cobras e escadas

## Fluxo do Jogo

1. **Inicialização**
   - Servidor inicia e aguarda conexões
   - Clientes conectam e enviam nomes
   - Servidor aguarda mínimo de 2 jogadores

2. **Início da Partida**
   - Servidor gera tabuleiro aleatório
   - Envia comando START para clientes
   - Clientes transitam para tela de jogo
   - Servidor distribui configuração inicial

3. **Gameplay**
   - Jogador clica em "Rolar Dado"
   - Servidor processa movimento
   - Servidor distribui novo estado
   - Clientes animam mudanças
   - Próximo jogador (se dado ≠ 6)

4. **Fim de Jogo**
   - Jogador atinge casa 100
   - Diálogo de vitória aparece
   - Opção de reiniciar ou sair

## Estrutura de Arquivos

```
src/main/java/com/mycompany/snake_ladder/
├── App.java                    # Aplicação principal
├── AppCliente.java            # Launcher do cliente
├── AppServidor.java           # Launcher do servidor
├── controller/
│   ├── LobbyClienteController.java
│   ├── LobbyServidorController.java
│   └── TabuleiroController.java
└── model/
    ├── Casas.java
    ├── Comunica.java
    ├── Dado.java
    ├── Jogador.java
    ├── Jogo.java
    ├── Peca.java
    └── Tabuleiro.java

src/main/resources/com/mycompany/snake_ladder/
├── espera.fxml
├── lobby_cliente.fxml
├── lobby_servidor.fxml
├── tabuleiro.fxml
└── style.css

src/main/resources/imagens/
├── dado1.png ... dado6.png
├── peao.png
└── peao_azul.png
```

## Tecnologias Utilizadas

- **Java 11+**: Linguagem principal
- **JavaFX**: Interface gráfica e animações
- **Maven**: Gerenciamento de dependências
- **Sockets TCP**: Comunicação em rede
- **FXML**: Declaração de interfaces

## Configuração e Execução

### Pré-requisitos
- Java 11 ou superior
- JavaFX SDK
- Maven (opcional)

### Executando o Servidor
```bash
java -cp target/classes com.mycompany.snake_ladder.AppServidor
```

### Executando o Cliente
```bash
java -cp target/classes com.mycompany.snake_ladder.AppCliente
```

### Configuração de Rede
- **Porta padrão**: 12345
- **IP padrão**: localhost
- Configurável através da interface

## Características Técnicas

### Concorrência
- Thread separada para escuta de mensagens do servidor
- Platform.runLater() para atualizações de UI
- Sincronização de estado entre clientes

### Tratamento de Erros
- Validação de entrada de rede
- Fallbacks para animações falhadas
- Reconexão automática (implementação futura)

### Performance
- Animações otimizadas com PathTransition
- Redesenho eficiente do tabuleiro
- Comunicação assíncrona

## Limitações Conhecidas

1. **Número de Jogadores**: Limitado a 2 jogadores
2. **Reconexão**: Não há suporte para reconexão automática
3. **Persistência**: Estado do jogo não é persistido
4. **Validação**: Validação limitada de entrada do usuário

## Possíveis Melhorias

1. **Suporte a mais jogadores** (3-4 players)
2. **Sistema de chat** integrado
3. **Diferentes modos de jogo** (tempo limitado, obstáculos)
4. **Persistência** de ranking e estatísticas
5. **Reconexão automática** em caso de desconexão
6. **Interface responsiva** para diferentes resoluções
7. **Sons e efeitos** sonoros
8. **Tema personalizável** da interface

## Estrutura da Documentação

Esta documentação segue o padrão JavaDoc para métodos e classes, com comentários explicativos para:
- Propósito de cada método
- Parâmetros e tipos de retorno
- Comportamento esperado
- Efeitos colaterais

Para gerar a documentação JavaDoc completa, execute:
```bash
javadoc -d docs -sourcepath src/main/java -subpackages com.mycompany.snake_ladder
```