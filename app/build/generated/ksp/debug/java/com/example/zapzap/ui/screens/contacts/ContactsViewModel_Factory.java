package com.example.zapzap.ui.screens.contacts;

import com.example.zapzap.domain.repository.AuthRepository;
import com.example.zapzap.domain.repository.ChatRepository;
import com.example.zapzap.domain.repository.ContactRepository;
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
public final class ContactsViewModel_Factory implements Factory<ContactsViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public ContactsViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ContactsViewModel get() {
    return newInstance(contactRepositoryProvider.get(), chatRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static ContactsViewModel_Factory create(
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new ContactsViewModel_Factory(contactRepositoryProvider, chatRepositoryProvider, authRepositoryProvider);
  }

  public static ContactsViewModel newInstance(ContactRepository contactRepository,
      ChatRepository chatRepository, AuthRepository authRepository) {
    return new ContactsViewModel(contactRepository, chatRepository, authRepository);
  }
}
