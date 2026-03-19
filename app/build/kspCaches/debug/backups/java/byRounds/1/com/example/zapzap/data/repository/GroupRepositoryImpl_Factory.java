package com.example.zapzap.data.repository;

import com.example.zapzap.domain.network.FcmService;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GroupRepositoryImpl_Factory implements Factory<GroupRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FcmService> fcmServiceProvider;

  public GroupRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FcmService> fcmServiceProvider) {
    this.firestoreProvider = firestoreProvider;
    this.fcmServiceProvider = fcmServiceProvider;
  }

  @Override
  public GroupRepositoryImpl get() {
    return newInstance(firestoreProvider.get(), fcmServiceProvider.get());
  }

  public static GroupRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FcmService> fcmServiceProvider) {
    return new GroupRepositoryImpl_Factory(firestoreProvider, fcmServiceProvider);
  }

  public static GroupRepositoryImpl newInstance(FirebaseFirestore firestore,
      FcmService fcmService) {
    return new GroupRepositoryImpl(firestore, fcmService);
  }
}
