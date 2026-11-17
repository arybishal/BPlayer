package com.pulse.player.di;

import android.content.Context;
import com.pulse.player.data.local.db.PulseDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvidePulseDatabaseFactory implements Factory<PulseDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvidePulseDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PulseDatabase get() {
    return providePulseDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvidePulseDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvidePulseDatabaseFactory(contextProvider);
  }

  public static PulseDatabase providePulseDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePulseDatabase(context));
  }
}
