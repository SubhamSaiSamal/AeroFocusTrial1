package com.aerofocus.app.service;

import com.aerofocus.app.data.repository.AeroFocusRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class FocusEnforcerService_MembersInjector implements MembersInjector<FocusEnforcerService> {
  private final Provider<AeroFocusRepository> repositoryProvider;

  public FocusEnforcerService_MembersInjector(Provider<AeroFocusRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<FocusEnforcerService> create(
      Provider<AeroFocusRepository> repositoryProvider) {
    return new FocusEnforcerService_MembersInjector(repositoryProvider);
  }

  @Override
  public void injectMembers(FocusEnforcerService instance) {
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.aerofocus.app.service.FocusEnforcerService.repository")
  public static void injectRepository(FocusEnforcerService instance,
      AeroFocusRepository repository) {
    instance.repository = repository;
  }
}
