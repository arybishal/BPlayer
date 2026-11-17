package com.pulse.player.di

import android.content.Context
import androidx.room.Room
import com.pulse.player.data.local.db.PulseDatabase
import com.pulse.player.data.local.db.dao.AlbumDao
import com.pulse.player.data.local.db.dao.ArtistDao
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.data.local.db.dao.GenreDao
import com.pulse.player.data.local.db.dao.LyricsDao
import com.pulse.player.data.local.db.dao.PlaylistDao
import com.pulse.player.data.local.db.dao.QueueDao
import com.pulse.player.data.local.db.dao.TrackDao
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
    fun provideTrackDao(database: PulseDatabase): TrackDao = database.trackDao()

    @Provides
    fun provideAlbumDao(database: PulseDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(database: PulseDatabase): ArtistDao = database.artistDao()

    @Provides
    fun provideGenreDao(database: PulseDatabase): GenreDao = database.genreDao()

    @Provides
    fun providePlaylistDao(database: PulseDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideQueueDao(database: PulseDatabase): QueueDao = database.queueDao()

    @Provides
    fun provideErrorLogDao(database: PulseDatabase): ErrorLogDao = database.errorLogDao()

    @Provides
    fun provideLyricsDao(database: PulseDatabase): LyricsDao = database.lyricsDao()
}
