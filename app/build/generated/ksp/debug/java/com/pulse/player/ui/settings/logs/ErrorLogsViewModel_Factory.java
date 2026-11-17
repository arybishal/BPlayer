package com.pulse.player.ui.settings.logs;

import com.pulse.player.data.local.db.dao.ErrorLogDao;
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
    "deprecation"
})
public final class ErrorLogsViewModel_Factory implements Factory<ErrorLogsViewModel> {
  private final Provider<ErrorLogDao> errorLogDaoProvider;

  public ErrorLogsViewModel_Factory(Provider<ErrorLogDao> errorLogDaoProvider) {
    this.errorLogDaoProvider = errorLogDaoProvider;
  }

  @Override
  public ErrorLogsViewModel get() {
    return newInstance(errorLogDaoProvider.get());
  }

  public static ErrorLogsViewModel_Factory create(Provider<ErrorLogDao> errorLogDaoProvider) {
    return new ErrorLogsViewModel_Factory(errorLogDaoProvider);
  }

  public static ErrorLogsViewModel newInstance(ErrorLogDao errorLogDao) {
    return new ErrorLogsViewModel(errorLogDao);
  }
}
