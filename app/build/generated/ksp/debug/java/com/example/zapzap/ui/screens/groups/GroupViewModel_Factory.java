package com.example.zapzap.ui.screens.groups;

import com.example.zapzap.domain.repository.AuthRepository;
import com.example.zapzap.domain.repository.ContactRepository;
import com.example.zapzap.domain.repository.GroupRepository;
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
public final class GroupViewModel_Factory implements Factory<GroupViewModel> {
  private final Provider<GroupRepository> groupRepositoryProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public GroupViewModel_Factory(Provider<GroupRepository> groupRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.groupRepositoryProvider = groupRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public GroupViewModel get() {
    return newInstance(groupRepositoryProvider.get(), contactRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static GroupViewModel_Factory create(Provider<GroupRepository> groupRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new GroupViewModel_Factory(groupRepositoryProvider, contactRepositoryProvider, authRepositoryProvider);
  }

  public static GroupViewModel newInstance(GroupRepository groupRepository,
      ContactRepository contactRepository, AuthRepository authRepository) {
    return new GroupViewModel(groupRepository, contactRepository, authRepository);
  }
}
