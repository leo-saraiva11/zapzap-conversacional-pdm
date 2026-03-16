# ZapZap - Aplicativo de Mensagens Instantâneas 💬

Aplicativo de mensagens instantâneas para Android, desenvolvido como Trabalho Prático 5 para a disciplina de **Programação para Dispositivos Móveis** da **Universidade Federal de Uberlândia (UFU)**.

## 👥 Equipe — Grupo 11

| Membro | Responsabilidades |
|--------|------------------|
| **André Noro Crivellenti** | Autenticação, segurança, criptografia, mensagens fixadas |
| **Leonardo Rodrigues Oliveira Saraiva** | Mensagens em tempo real, sincronização, status de entrega, filtro por palavra-chave |
| **Marcelo Gonçalves Alves** | Interface de chat (UI/UX), notificações push, emojis/stickers |
| **Gustavo Antônio Teixeira de Matos** | Grupos, contatos, envio de mídia, sensores (GPS/câmera/microfone) |

## 🚀 Funcionalidades

1. ✅ Cadastro e autenticação (email/senha, Google, telefone)
2. ✅ Listagem e busca de conversas
3. ✅ Mensagens em tempo real com sincronização
4. ✅ Status de mensagem (enviada, entregue, lida)
5. ✅ Notificações push (FCM)
6. ✅ Gerenciamento de contatos
7. ✅ Criação e gerenciamento de grupos
8. ✅ Envio de mídia (imagens, vídeos, áudios, arquivos)
9. ✅ Perfil do usuário com status
10. ✅ Interface de chat com bolhas e emojis
11. ✅ Sincronização offline com Room Database
12. ✅ Criptografia AES-GCM
13. ✅ Logout seguro

### Requisitos Especiais
- 📌 **Mensagens fixadas** (3 pts): fixar/desafixar mensagem no topo do chat
- 🔍 **Filtro por palavra-chave** (5 pts): busca com highlight dentro da conversa
- 📍 **GPS** (4 pts): enviar localização atual na conversa
- 📷 **Câmera** (4 pts): capturar e enviar foto direto do app
- 🎤 **Microfone** (4 pts): gravar e enviar mensagem de voz

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Kotlin | Linguagem principal |
| Jetpack Compose | UI declarativa (Material Design 3) |
| Firebase Auth | Autenticação |
| Cloud Firestore | Banco de dados + tempo real |
| Firebase Storage | Armazenamento de mídia |
| Firebase Cloud Messaging | Notificações push |
| Room Database | Cache offline |
| Hilt | Injeção de dependência |
| CameraX | Captura de fotos |
| Google Maps SDK | Localização |

## 📦 Arquitetura

**MVVM + Clean Architecture**

```
com.example.zapzap/
├── data/          # Entities, DAOs, Repositórios, Data Sources
├── domain/        # Modelos, Interfaces, Use Cases
├── ui/            # Telas, ViewModels, Componentes, Tema
├── navigation/    # Rotas e NavGraph
├── di/            # Módulos Hilt
├── service/       # FCM, Sincronização
└── util/          # Criptografia, Constantes
```

## 🚀 Como Executar

1. Clone o repositório
2. Abra no **Android Studio**
3. Configure o Firebase:
   - Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
   - Ative **Authentication** (Email/Senha, Google, Telefone)
   - Ative **Cloud Firestore**
   - Ative **Firebase Storage**
   - Ative **Cloud Messaging**
   - Baixe o `google-services.json` e coloque em `app/`
4. Configure o Google Maps:
   - Obtenha uma API key no [Google Cloud Console](https://console.cloud.google.com/)
   - Substitua `YOUR_GOOGLE_MAPS_API_KEY` no `AndroidManifest.xml`
5. Execute o app no emulador ou dispositivo (API 26+)

## 📄 Licença

Desenvolvido para fins acadêmicos — UFU 2025/2026
