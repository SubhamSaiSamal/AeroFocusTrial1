package com.aerofocus.app.data.repository;

import com.aerofocus.app.data.db.dao.AeroFocusDao;
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AeroFocusRepository_Factory implements Factory<AeroFocusRepository> {
  private final Provider<AeroFocusDao> daoProvider;

  public AeroFocusRepository_Factory(Provider<AeroFocusDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public AeroFocusRepository get() {
    return newInstance(daoProvider.get());
  }

  public static AeroFocusRepository_Factory create(Provider<AeroFocusDao> daoProvider) {
    return new AeroFocusRepository_Factory(daoProvider);
  }

  public static AeroFocusRepository newInstance(AeroFocusDao dao) {
    return new AeroFocusRepository(dao);
  }
}
