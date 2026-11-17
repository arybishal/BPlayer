package com.pulse.player.di

import android.content.Context
import androidx.room.Room
import com.pulse.player.data.local.db.PulseDatabase
import com.pulse.player.data.local.db.dao.ErrorLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePulseDatabase(
        @ApplicationContext context: Context
    ): PulseDatabase {
        return Room.databaseBuilder(
            context,
            PulseDatabase::class.java,
            PulseDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideErrorLogDao(database: PulseDatabase): ErrorLogDao {
        return database.errorLogDao()
    }
}
