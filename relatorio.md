# Relatório Técnico - ZapZap (Grupo 11)

Este documento descreve as decisões de projeto, responsabilidades da equipe e o processo de desenvolvimento do aplicativo **ZapZap**.

## 1. Decisões Tomadas
*   **Arquitetura:** Optamos pelo padrão **MVVM (Model-View-ViewModel)** com **Clean Architecture**. Isso permitiu que a lógica de sensores (Câmera/GPS) ficasse isolada no repositório, enquanto a UI (Jetpack Compose) apenas observa as mudanças de estado.
*   **Banco de Dados:** Combinamos o **Cloud Firestore** para a verdade única do chat com o **Room Database** como persistência local. Isso garante que o app carregue as conversas offline instantaneamente, satisfazendo os requisitos de sincronização.
*   **Segurança:** Implementamos criptografia ponta-a-ponta em nível de aplicação (AES/TLS) para todas as mensagens de texto antes delas chegarem ao banco de dados Firestore.

## 2. Papéis dos Membros
*   **André Noro Crivellenti:** Responsável por todo o fluxo de Autenticação (cadastro, login, recuperação) e a estrutura base de criptografia das conversas.
*   **Leonardo Oliveira Saraiva:** Focado no motor de Mensagens em Tempo Real e o sistema de sincronização (offline/online) e status de leitura/entrega (checks).
*   **Marcelo Gonçalves Alves:** Especialista em Interface (UI/UX), criando o visual "ZapZap Premium", sistema de Notificações Locais/Push e integração de Emojis.
*   **Gustavo Antônio de Matos:** Integrador de Sensores (Câmera para fotos/vídeos, GPS para localização e Microfone para áudio) e do gerenciamento de Grupos e Contatos.

## 3. Uso de Sensores e Recursos do Aparelho
*   **Câmera:** Implementada através da API `ActivityResultContracts.TakePicture()` e `CaptureVideo()`. O app abre a câmera nativa, salva o arquivo temporário e envia para o Firebase Storage.
*   **Microfone:** Utilizamos a classe `MediaRecorder` para capturar áudio do usuário, encodando-o em formato AAC em um container MPEG_4 para compatibilidade universal entre dispositivos Android.
*   **GPS:** Integrado com a biblioteca `Google Play Services - Location`. O app solicita permissão e utiliza o `FusedLocationProviderClient` para capturar as coordenadas (Latitude/Longitude) atuais com alta precisão e formatá-las em um link interativo do Google Maps na conversa.

## 4. Principais Dificuldades Enfrentadas
*   **Sincronização Offline:** Garantir que as mensagens enviadas sem rede fossem "enfileiradas" corretamente e enviadas ao servidor assim que a conexão voltava, mantendo a ordem dos timestamps.
*   **Preview de Mídia:** A renderização de vídeos em tempo real dentro do chat exigiu o uso de `AndroidView` para integrar o `VideoView` nativo com o Jetpack Compose, superando limitações de componentes puramente declarativos.
*   **Variedade de Formatos de Telefone:** A importação de contatos exigiu um algoritmo de normalização robusto para casar números formatados na agenda do telefone (ex: `(11) 99999-9999`) com os registros no Firebase.

## 5. Conclusão
O projeto atingiu 100% dos requisitos fundamentais e especiais propostos para a disciplina de PDM, entregando um sistema funcional, seguro e visualmente polido pronto para uso final.
