package com.aerofocus.app.di;

import com.aerofocus.app.data.db.AeroFocusDatabase;
import com.aerofocus.app.data.db.dao.AeroFocusDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDaoFactory implements Factory<AeroFocusDao> {
  private final Provider<AeroFocusDatabase> databaseProvider;

  public AppModule_ProvideDaoFactory(Provider<AeroFocusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AeroFocusDao get() {
    return provideDao(databaseProvider.get());
  }

  public static AppModule_ProvideDaoFactory create(Provider<AeroFocusDatabase> databaseProvider) {
    return new AppModule_ProvideDaoFactory(databaseProvider);
  }

  public static AeroFocusDao provideDao(AeroFocusDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDao(database));
  }
}
