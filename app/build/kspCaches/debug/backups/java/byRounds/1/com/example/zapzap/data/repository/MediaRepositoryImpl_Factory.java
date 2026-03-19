package com.example.zapzap.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class MediaRepositoryImpl_Factory implements Factory<MediaRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<SupabaseClient> supabaseClientProvider;

  public MediaRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<SupabaseClient> supabaseClientProvider) {
    this.contextProvider = contextProvider;
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public MediaRepositoryImpl get() {
    return newInstance(contextProvider.get(), supabaseClientProvider.get());
  }

  public static MediaRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<SupabaseClient> supabaseClientProvider) {
    return new MediaRepositoryImpl_Factory(contextProvider, supabaseClientProvider);
  }

  public static MediaRepositoryImpl newInstance(Context context, SupabaseClient supabaseClient) {
    return new MediaRepositoryImpl(context, supabaseClient);
  }
}
