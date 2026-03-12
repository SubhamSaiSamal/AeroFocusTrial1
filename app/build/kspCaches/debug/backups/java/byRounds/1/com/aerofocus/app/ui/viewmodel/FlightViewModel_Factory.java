package com.aerofocus.app.ui.viewmodel;

import com.aerofocus.app.data.repository.AeroFocusRepository;
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
public final class FlightViewModel_Factory implements Factory<FlightViewModel> {
  private final Provider<AeroFocusRepository> repositoryProvider;

  public FlightViewModel_Factory(Provider<AeroFocusRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public FlightViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static FlightViewModel_Factory create(Provider<AeroFocusRepository> repositoryProvider) {
    return new FlightViewModel_Factory(repositoryProvider);
  }

  public static FlightViewModel newInstance(AeroFocusRepository repository) {
    return new FlightViewModel(repository);
  }
}
