package com.pulse.player.di;

import com.pulse.player.data.local.db.PulseDatabase;
import com.pulse.player.data.local.db.dao.ErrorLogDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "deprecation"
})
public final class DatabaseModule_ProvideErrorLogDaoFactory implements Factory<ErrorLogDao> {
  private final Provider<PulseDatabase> databaseProvider;

  public DatabaseModule_ProvideErrorLogDaoFactory(Provider<PulseDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ErrorLogDao get() {
    return provideErrorLogDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideErrorLogDaoFactory create(
      Provider<PulseDatabase> databaseProvider) {
    return new DatabaseModule_ProvideErrorLogDaoFactory(databaseProvider);
  }

  public static ErrorLogDao provideErrorLogDao(PulseDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideErrorLogDao(database));
  }
}
