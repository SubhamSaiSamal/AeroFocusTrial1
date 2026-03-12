package com.aerofocus.app.ui.viewmodel;

import android.app.Application;
import com.aerofocus.app.data.repository.AeroFocusRepository;
import com.aerofocus.app.service.AmbientAudioPlayer;
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TimerViewModel_Factory implements Factory<TimerViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<AeroFocusRepository> repositoryProvider;

  private final Provider<AmbientAudioPlayer> audioPlayerProvider;

  public TimerViewModel_Factory(Provider<Application> applicationProvider,
      Provider<AeroFocusRepository> repositoryProvider,
      Provider<AmbientAudioPlayer> audioPlayerProvider) {
    this.applicationProvider = applicationProvider;
    this.repositoryProvider = repositoryProvider;
    this.audioPlayerProvider = audioPlayerProvider;
  }

  @Override
  public TimerViewModel get() {
    return newInstance(applicationProvider.get(), repositoryProvider.get(), audioPlayerProvider.get());
  }

  public static TimerViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<AeroFocusRepository> repositoryProvider,
      Provider<AmbientAudioPlayer> audioPlayerProvider) {
    return new TimerViewModel_Factory(applicationProvider, repositoryProvider, audioPlayerProvider);
  }

  public static TimerViewModel newInstance(Application application, AeroFocusRepository repository,
      AmbientAudioPlayer audioPlayer) {
    return new TimerViewModel(application, repository, audioPlayer);
  }
}
