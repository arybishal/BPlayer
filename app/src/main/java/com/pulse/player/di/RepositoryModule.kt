package com.pulse.player.di

import com.pulse.player.data.local.audio.AudioRecorder
import com.pulse.player.data.local.audio.MicrophoneRecorder
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.data.local.datastore.RecognitionPreferences
import com.pulse.player.data.repository.LibraryRepositoryImpl
import com.pulse.player.data.repository.LyricsRepositoryImpl
import com.pulse.player.data.repository.SongRecognitionRepositoryImpl
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.LyricsRepository
import com.pulse.player.domain.repository.SongRecognitionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(
        impl: LibraryRepositoryImpl
    ): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(
        impl: LyricsRepositoryImpl
    ): LyricsRepository

    @Binds
    @Singleton
    abstract fun bindSongRecognitionRepository(
        impl: SongRecognitionRepositoryImpl
    ): SongRecognitionRepository

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(
        impl: MicrophoneRecorder
    ): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindRecognitionPreferences(
        impl: PreferencesManager
    ): RecognitionPreferences
}
