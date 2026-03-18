package com.example.zapzap.data.repository;

import com.example.zapzap.data.local.dao.UserDao;
import com.google.firebase.auth.FirebaseAuth;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<UserDao> userDaoProvider;

  public AuthRepositoryImpl_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<UserDao> userDaoProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
    this.userDaoProvider = userDaoProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authProvider.get(), firestoreProvider.get(), userDaoProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<UserDao> userDaoProvider) {
    return new AuthRepositoryImpl_Factory(authProvider, firestoreProvider, userDaoProvider);
  }

  public static AuthRepositoryImpl newInstance(FirebaseAuth auth, FirebaseFirestore firestore,
      UserDao userDao) {
    return new AuthRepositoryImpl(auth, firestore, userDao);
  }
}
