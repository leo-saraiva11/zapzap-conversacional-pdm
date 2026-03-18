package com.example.zapzap.data.repository;

import android.content.Context;
import com.example.zapzap.data.local.dao.ContactDao;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class ContactRepositoryImpl_Factory implements Factory<ContactRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<ContactDao> contactDaoProvider;

  public ContactRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<ContactDao> contactDaoProvider) {
    this.contextProvider = contextProvider;
    this.firestoreProvider = firestoreProvider;
    this.contactDaoProvider = contactDaoProvider;
  }

  @Override
  public ContactRepositoryImpl get() {
    return newInstance(contextProvider.get(), firestoreProvider.get(), contactDaoProvider.get());
  }

  public static ContactRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<ContactDao> contactDaoProvider) {
    return new ContactRepositoryImpl_Factory(contextProvider, firestoreProvider, contactDaoProvider);
  }

  public static ContactRepositoryImpl newInstance(Context context, FirebaseFirestore firestore,
      ContactDao contactDao) {
    return new ContactRepositoryImpl(context, firestore, contactDao);
  }
}
