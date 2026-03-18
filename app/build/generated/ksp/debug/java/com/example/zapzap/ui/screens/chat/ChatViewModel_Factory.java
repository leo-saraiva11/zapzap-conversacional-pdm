package com.example.zapzap.ui.screens.chat;

import com.example.zapzap.domain.repository.AuthRepository;
import com.example.zapzap.domain.repository.ChatRepository;
import com.example.zapzap.domain.repository.MediaRepository;
import com.example.zapzap.domain.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<MediaRepository> mediaRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ChatViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.mediaRepositoryProvider = mediaRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(chatRepositoryProvider.get(), authRepositoryProvider.get(), mediaRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ChatViewModel_Factory(chatRepositoryProvider, authRepositoryProvider, mediaRepositoryProvider, userRepositoryProvider);
  }

  public static ChatViewModel newInstance(ChatRepository chatRepository,
      AuthRepository authRepository, MediaRepository mediaRepository,
      UserRepository userRepository) {
    return new ChatViewModel(chatRepository, authRepository, mediaRepository, userRepository);
  }
}
