package com.example.zapzap.data.repository;

import android.content.Context;
import com.example.zapzap.data.local.dao.UserDao;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.storage.Storage;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<Storage> supabaseStorageProvider;

  private final Provider<UserDao> userDaoProvider;

  public UserRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<Storage> supabaseStorageProvider,
      Provider<UserDao> userDaoProvider) {
    this.contextProvider = contextProvider;
    this.firestoreProvider = firestoreProvider;
    this.supabaseStorageProvider = supabaseStorageProvider;
    this.userDaoProvider = userDaoProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(contextProvider.get(), firestoreProvider.get(), supabaseStorageProvider.get(), userDaoProvider.get());
  }

  public static UserRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<Storage> supabaseStorageProvider,
      Provider<UserDao> userDaoProvider) {
    return new UserRepositoryImpl_Factory(contextProvider, firestoreProvider, supabaseStorageProvider, userDaoProvider);
  }

  public static UserRepositoryImpl newInstance(Context context, FirebaseFirestore firestore,
      Storage supabaseStorage, UserDao userDao) {
    return new UserRepositoryImpl(context, firestore, supabaseStorage, userDao);
  }
}
