package com.pulse.player;

import com.pulse.player.data.local.db.dao.ErrorLogDao;
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
    "deprecation"
})
public final class PulseApplication_MembersInjector implements MembersInjector<PulseApplication> {
  private final Provider<ErrorLogDao> errorLogDaoProvider;

  public PulseApplication_MembersInjector(Provider<ErrorLogDao> errorLogDaoProvider) {
    this.errorLogDaoProvider = errorLogDaoProvider;
  }

  public static MembersInjector<PulseApplication> create(
      Provider<ErrorLogDao> errorLogDaoProvider) {
    return new PulseApplication_MembersInjector(errorLogDaoProvider);
  }

  @Override
  public void injectMembers(PulseApplication instance) {
    injectErrorLogDao(instance, errorLogDaoProvider.get());
  }

  @InjectedFieldSignature("com.pulse.player.PulseApplication.errorLogDao")
  public static void injectErrorLogDao(PulseApplication instance, ErrorLogDao errorLogDao) {
    instance.errorLogDao = errorLogDao;
  }
}
