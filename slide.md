# Prompt para Criação de Slides (Gamma.app) - Projeto ZapZap (Grupo 11)

**Prompt:** "Crie uma apresentação profissional de 7 slides sobre o desenvolvimento de um aplicativo mobile Android em Kotlin chamado 'ZapZap'. Siga estas instruções rigorosamente:"

---

### Slide 1: Título e Equipe
*   **Título Principal:** ZapZap - Aplicativo de Mensagens Instantâneas (Grupo 11)
*   **Subtítulo:** Disciplina: Programação para Dispositivos Móveis - UFU
*   **Equipe:** André Noro Crivellenti (Auth & Segurança), Leonardo Rodrigues Oliveira Saraiva (Mensagens & Sinc.), Marcelo Gonçalves Alves (UI/UX & Notif.), Gustavo Antônio Teixeira de Matos (Grupos & Sensores).

### Slide 2: Visão Geral e Funcionalidades Core
*   **Tópicos:** Autenticação completa (Auth), Mensagens em Tempo Real (Firestore), Notificações Push, e Sincronização Offline (Room).
*   **Destaque:** Arquitetura MVVM + Clean para escalabilidade.

### Slide 3: Integração Sensorial (Câmera, GPS e Microfone)
*   **Descrição:** Uso nativo de recursos do smartphone.
*   **Código Exemplo (Frisar para auditório):**
```kotlin
// Captura de Vídeo Nativo no App
val videoLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CaptureVideo()
) { success -> if(success) viewModel.onVideoCaptured(uri) }
```
*   **Recursos:** `FusedLocationProviderClient` para GPS e `MediaRecorder` para mensagens de voz.

### Slide 4: Dificuldades Encontradas no Desenvolvimento
*   **Tópicos:**
    1.  **Sincronização Offline:** Enviar mensagens sem rede e atualizar o servidor via Room Database.
    2.  **Renderização Multimídia:** Integrar `VideoView` nativo dentro das colunas dinâmicas do Jetpack Compose.
    3.  **Normalização de Contatos:** Casar formatos de números de telefone heterogêneos na agenda.

### Slide 5: Observações sobre o Uso de LLMs (Antigravity/Gemini)
*   **Ferramenta usada:** Antigravity (Powered by Google Gemini).
*   **Exemplos de Prompts Críticos:** 
    *   "Melhore toda a UI/UX com o visual do WhatsApp usando Jetpack Compose."
    *   "Implemente a prévia de vídeo gravado para não ficar com tela preta."
*   **Opinião do Grupo:** As LLMs aceleraram em 5x o desenvolvimento da UI e facilitaram o debug de integrações complexas como o Firebase Storage.

### Slide 6: Demonstração ao Vivo (Preparação)
*   **Conteúdo:** Roteiro rápido: Envio de Mensagem de Texto -> Captura de Foto/Vídeo -> Envio de Localização GPS -> Notificação Push no celular espelhado.
*   **Nota:** Dois membros do grupo demonstrarão em smartphones reais.

### Slide 7: Conclusão e Futuros Incrementos
*   **Tópicos:** Segurança de dados via criptografia AES, 100% dos requisitos atingidos e projeto pronto para release .apk.
*   **Agradecimentos:** Aberto a perguntas da banca e colegas.

---
**Regra Visual para o Gamma:** Utilize um tema moderno, escuro e vibrante, preferencialmente tons de verde e tons foscos (estilo WhatsApp). Todas as imagens de código devem ser nítidas e legíveis.
