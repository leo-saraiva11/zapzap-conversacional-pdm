package com.example.zapzap.data.repository;

import android.content.Context;
import com.example.zapzap.data.local.dao.ConversationDao;
import com.example.zapzap.data.local.dao.MessageDao;
import com.example.zapzap.domain.network.FcmService;
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
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<FcmService> fcmServiceProvider;

  public ChatRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<FcmService> fcmServiceProvider) {
    this.contextProvider = contextProvider;
    this.firestoreProvider = firestoreProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.fcmServiceProvider = fcmServiceProvider;
  }

  @Override
  public ChatRepositoryImpl get() {
    return newInstance(contextProvider.get(), firestoreProvider.get(), messageDaoProvider.get(), conversationDaoProvider.get(), fcmServiceProvider.get());
  }

  public static ChatRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<FcmService> fcmServiceProvider) {
    return new ChatRepositoryImpl_Factory(contextProvider, firestoreProvider, messageDaoProvider, conversationDaoProvider, fcmServiceProvider);
  }

  public static ChatRepositoryImpl newInstance(Context context, FirebaseFirestore firestore,
      MessageDao messageDao, ConversationDao conversationDao, FcmService fcmService) {
    return new ChatRepositoryImpl(context, firestore, messageDao, conversationDao, fcmService);
  }
}
