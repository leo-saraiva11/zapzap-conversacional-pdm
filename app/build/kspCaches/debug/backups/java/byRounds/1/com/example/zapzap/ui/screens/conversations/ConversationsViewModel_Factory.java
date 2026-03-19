package com.example.zapzap.ui.screens.conversations;

import com.example.zapzap.domain.repository.AuthRepository;
import com.example.zapzap.domain.repository.ChatRepository;
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
public final class ConversationsViewModel_Factory implements Factory<ConversationsViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public ConversationsViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ConversationsViewModel get() {
    return newInstance(chatRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static ConversationsViewModel_Factory create(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new ConversationsViewModel_Factory(chatRepositoryProvider, authRepositoryProvider);
  }

  public static ConversationsViewModel newInstance(ChatRepository chatRepository,
      AuthRepository authRepository) {
    return new ConversationsViewModel(chatRepository, authRepository);
  }
}
