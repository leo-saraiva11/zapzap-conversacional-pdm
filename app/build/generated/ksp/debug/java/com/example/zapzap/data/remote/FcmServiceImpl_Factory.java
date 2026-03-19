package com.example.zapzap.data.remote;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata
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
public final class FcmServiceImpl_Factory implements Factory<FcmServiceImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<OkHttpClient> httpClientProvider;

  public FcmServiceImpl_Factory(Provider<Context> contextProvider,
      Provider<OkHttpClient> httpClientProvider) {
    this.contextProvider = contextProvider;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public FcmServiceImpl get() {
    return newInstance(contextProvider.get(), httpClientProvider.get());
  }

  public static FcmServiceImpl_Factory create(Provider<Context> contextProvider,
      Provider<OkHttpClient> httpClientProvider) {
    return new FcmServiceImpl_Factory(contextProvider, httpClientProvider);
  }

  public static FcmServiceImpl newInstance(Context context, OkHttpClient httpClient) {
    return new FcmServiceImpl(context, httpClient);
  }
}
