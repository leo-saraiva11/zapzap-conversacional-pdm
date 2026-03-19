package com.example.zapzap;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.zapzap.data.local.AppDatabase;
import com.example.zapzap.data.local.dao.ContactDao;
import com.example.zapzap.data.local.dao.ConversationDao;
import com.example.zapzap.data.local.dao.MessageDao;
import com.example.zapzap.data.local.dao.UserDao;
import com.example.zapzap.data.remote.FcmServiceImpl;
import com.example.zapzap.data.repository.AuthRepositoryImpl;
import com.example.zapzap.data.repository.ChatRepositoryImpl;
import com.example.zapzap.data.repository.ContactRepositoryImpl;
import com.example.zapzap.data.repository.GroupRepositoryImpl;
import com.example.zapzap.data.repository.MediaRepositoryImpl;
import com.example.zapzap.data.repository.UserRepositoryImpl;
import com.example.zapzap.di.DatabaseModule_ProvideContactDaoFactory;
import com.example.zapzap.di.DatabaseModule_ProvideConversationDaoFactory;
import com.example.zapzap.di.DatabaseModule_ProvideDatabaseFactory;
import com.example.zapzap.di.DatabaseModule_ProvideMessageDaoFactory;
import com.example.zapzap.di.DatabaseModule_ProvideUserDaoFactory;
import com.example.zapzap.di.FirebaseModule_ProvideFirebaseAuthFactory;
import com.example.zapzap.di.FirebaseModule_ProvideFirebaseFirestoreFactory;
import com.example.zapzap.di.NetworkModule_ProvideOkHttpClientFactory;
import com.example.zapzap.di.SupabaseModule_ProvideSupabaseClientFactory;
import com.example.zapzap.di.SupabaseModule_ProvideSupabaseStorageFactory;
import com.example.zapzap.domain.network.FcmService;
import com.example.zapzap.ui.screens.auth.AuthViewModel;
import com.example.zapzap.ui.screens.auth.AuthViewModel_HiltModules;
import com.example.zapzap.ui.screens.chat.ChatViewModel;
import com.example.zapzap.ui.screens.chat.ChatViewModel_HiltModules;
import com.example.zapzap.ui.screens.contacts.ContactsViewModel;
import com.example.zapzap.ui.screens.contacts.ContactsViewModel_HiltModules;
import com.example.zapzap.ui.screens.conversations.ConversationsViewModel;
import com.example.zapzap.ui.screens.conversations.ConversationsViewModel_HiltModules;
import com.example.zapzap.ui.screens.groups.GroupViewModel;
import com.example.zapzap.ui.screens.groups.GroupViewModel_HiltModules;
import com.example.zapzap.ui.screens.profile.ProfileViewModel;
import com.example.zapzap.ui.screens.profile.ProfileViewModel_HiltModules;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.storage.Storage;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class DaggerZapZapApplication_HiltComponents_SingletonC {
  private DaggerZapZapApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public ZapZapApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements ZapZapApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements ZapZapApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements ZapZapApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements ZapZapApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements ZapZapApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements ZapZapApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements ZapZapApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public ZapZapApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends ZapZapApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends ZapZapApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends ZapZapApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends ZapZapApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(6).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_auth_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_chat_ChatViewModel, ChatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_contacts_ContactsViewModel, ContactsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_conversations_ConversationsViewModel, ConversationsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_groups_GroupViewModel, GroupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_zapzap_ui_screens_profile_ProfileViewModel = "com.example.zapzap.ui.screens.profile.ProfileViewModel";

      static String com_example_zapzap_ui_screens_auth_AuthViewModel = "com.example.zapzap.ui.screens.auth.AuthViewModel";

      static String com_example_zapzap_ui_screens_chat_ChatViewModel = "com.example.zapzap.ui.screens.chat.ChatViewModel";

      static String com_example_zapzap_ui_screens_contacts_ContactsViewModel = "com.example.zapzap.ui.screens.contacts.ContactsViewModel";

      static String com_example_zapzap_ui_screens_conversations_ConversationsViewModel = "com.example.zapzap.ui.screens.conversations.ConversationsViewModel";

      static String com_example_zapzap_ui_screens_groups_GroupViewModel = "com.example.zapzap.ui.screens.groups.GroupViewModel";

      @KeepFieldType
      ProfileViewModel com_example_zapzap_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      AuthViewModel com_example_zapzap_ui_screens_auth_AuthViewModel2;

      @KeepFieldType
      ChatViewModel com_example_zapzap_ui_screens_chat_ChatViewModel2;

      @KeepFieldType
      ContactsViewModel com_example_zapzap_ui_screens_contacts_ContactsViewModel2;

      @KeepFieldType
      ConversationsViewModel com_example_zapzap_ui_screens_conversations_ConversationsViewModel2;

      @KeepFieldType
      GroupViewModel com_example_zapzap_ui_screens_groups_GroupViewModel2;
    }
  }

  private static final class ViewModelCImpl extends ZapZapApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<ChatViewModel> chatViewModelProvider;

    private Provider<ContactsViewModel> contactsViewModelProvider;

    private Provider<ConversationsViewModel> conversationsViewModelProvider;

    private Provider<GroupViewModel> groupViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.contactsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.conversationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.groupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(6).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_auth_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_chat_ChatViewModel, ((Provider) chatViewModelProvider)).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_contacts_ContactsViewModel, ((Provider) contactsViewModelProvider)).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_conversations_ConversationsViewModel, ((Provider) conversationsViewModelProvider)).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_groups_GroupViewModel, ((Provider) groupViewModelProvider)).put(LazyClassKeyProvider.com_example_zapzap_ui_screens_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_zapzap_ui_screens_auth_AuthViewModel = "com.example.zapzap.ui.screens.auth.AuthViewModel";

      static String com_example_zapzap_ui_screens_conversations_ConversationsViewModel = "com.example.zapzap.ui.screens.conversations.ConversationsViewModel";

      static String com_example_zapzap_ui_screens_contacts_ContactsViewModel = "com.example.zapzap.ui.screens.contacts.ContactsViewModel";

      static String com_example_zapzap_ui_screens_profile_ProfileViewModel = "com.example.zapzap.ui.screens.profile.ProfileViewModel";

      static String com_example_zapzap_ui_screens_groups_GroupViewModel = "com.example.zapzap.ui.screens.groups.GroupViewModel";

      static String com_example_zapzap_ui_screens_chat_ChatViewModel = "com.example.zapzap.ui.screens.chat.ChatViewModel";

      @KeepFieldType
      AuthViewModel com_example_zapzap_ui_screens_auth_AuthViewModel2;

      @KeepFieldType
      ConversationsViewModel com_example_zapzap_ui_screens_conversations_ConversationsViewModel2;

      @KeepFieldType
      ContactsViewModel com_example_zapzap_ui_screens_contacts_ContactsViewModel2;

      @KeepFieldType
      ProfileViewModel com_example_zapzap_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      GroupViewModel com_example_zapzap_ui_screens_groups_GroupViewModel2;

      @KeepFieldType
      ChatViewModel com_example_zapzap_ui_screens_chat_ChatViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.zapzap.ui.screens.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 1: // com.example.zapzap.ui.screens.chat.ChatViewModel 
          return (T) new ChatViewModel(singletonCImpl.chatRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.mediaRepositoryImplProvider.get(), singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.groupRepositoryImplProvider.get());

          case 2: // com.example.zapzap.ui.screens.contacts.ContactsViewModel 
          return (T) new ContactsViewModel(singletonCImpl.contactRepositoryImplProvider.get(), singletonCImpl.chatRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 3: // com.example.zapzap.ui.screens.conversations.ConversationsViewModel 
          return (T) new ConversationsViewModel(singletonCImpl.chatRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 4: // com.example.zapzap.ui.screens.groups.GroupViewModel 
          return (T) new GroupViewModel(singletonCImpl.groupRepositoryImplProvider.get(), singletonCImpl.contactRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.mediaRepositoryImplProvider.get());

          case 5: // com.example.zapzap.ui.screens.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends ZapZapApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends ZapZapApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends ZapZapApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<FirebaseAuth> provideFirebaseAuthProvider;

    private Provider<FirebaseFirestore> provideFirebaseFirestoreProvider;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<FcmServiceImpl> fcmServiceImplProvider;

    private Provider<FcmService> bindFcmServiceProvider;

    private Provider<ChatRepositoryImpl> chatRepositoryImplProvider;

    private Provider<SupabaseClient> provideSupabaseClientProvider;

    private Provider<MediaRepositoryImpl> mediaRepositoryImplProvider;

    private Provider<Storage> provideSupabaseStorageProvider;

    private Provider<UserRepositoryImpl> userRepositoryImplProvider;

    private Provider<GroupRepositoryImpl> groupRepositoryImplProvider;

    private Provider<ContactRepositoryImpl> contactRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private UserDao userDao() {
      return DatabaseModule_ProvideUserDaoFactory.provideUserDao(provideDatabaseProvider.get());
    }

    private MessageDao messageDao() {
      return DatabaseModule_ProvideMessageDaoFactory.provideMessageDao(provideDatabaseProvider.get());
    }

    private ConversationDao conversationDao() {
      return DatabaseModule_ProvideConversationDaoFactory.provideConversationDao(provideDatabaseProvider.get());
    }

    private ContactDao contactDao() {
      return DatabaseModule_ProvideContactDaoFactory.provideContactDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideFirebaseAuthProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseAuth>(singletonCImpl, 1));
      this.provideFirebaseFirestoreProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseFirestore>(singletonCImpl, 2));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 3));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 0));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 6));
      this.fcmServiceImplProvider = new SwitchingProvider<>(singletonCImpl, 5);
      this.bindFcmServiceProvider = DoubleCheck.provider((Provider) fcmServiceImplProvider);
      this.chatRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ChatRepositoryImpl>(singletonCImpl, 4));
      this.provideSupabaseClientProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseClient>(singletonCImpl, 8));
      this.mediaRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MediaRepositoryImpl>(singletonCImpl, 7));
      this.provideSupabaseStorageProvider = DoubleCheck.provider(new SwitchingProvider<Storage>(singletonCImpl, 10));
      this.userRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<UserRepositoryImpl>(singletonCImpl, 9));
      this.groupRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<GroupRepositoryImpl>(singletonCImpl, 11));
      this.contactRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ContactRepositoryImpl>(singletonCImpl, 12));
    }

    @Override
    public void injectZapZapApplication(ZapZapApplication zapZapApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.zapzap.data.repository.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.userDao(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.google.firebase.auth.FirebaseAuth 
          return (T) FirebaseModule_ProvideFirebaseAuthFactory.provideFirebaseAuth();

          case 2: // com.google.firebase.firestore.FirebaseFirestore 
          return (T) FirebaseModule_ProvideFirebaseFirestoreFactory.provideFirebaseFirestore();

          case 3: // com.example.zapzap.data.local.AppDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.example.zapzap.data.repository.ChatRepositoryImpl 
          return (T) new ChatRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.messageDao(), singletonCImpl.conversationDao(), singletonCImpl.bindFcmServiceProvider.get());

          case 5: // com.example.zapzap.data.remote.FcmServiceImpl 
          return (T) new FcmServiceImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideOkHttpClientProvider.get());

          case 6: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 7: // com.example.zapzap.data.repository.MediaRepositoryImpl 
          return (T) new MediaRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideSupabaseClientProvider.get());

          case 8: // io.github.jan.supabase.SupabaseClient 
          return (T) SupabaseModule_ProvideSupabaseClientFactory.provideSupabaseClient();

          case 9: // com.example.zapzap.data.repository.UserRepositoryImpl 
          return (T) new UserRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideSupabaseStorageProvider.get(), singletonCImpl.userDao());

          case 10: // io.github.jan.supabase.storage.Storage 
          return (T) SupabaseModule_ProvideSupabaseStorageFactory.provideSupabaseStorage(singletonCImpl.provideSupabaseClientProvider.get());

          case 11: // com.example.zapzap.data.repository.GroupRepositoryImpl 
          return (T) new GroupRepositoryImpl(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.bindFcmServiceProvider.get());

          case 12: // com.example.zapzap.data.repository.ContactRepositoryImpl 
          return (T) new ContactRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.contactDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
