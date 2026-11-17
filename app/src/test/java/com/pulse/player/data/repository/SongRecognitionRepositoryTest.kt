package com.pulse.player.data.repository

import com.pulse.player.data.local.audio.AudioRecorder
import com.pulse.player.data.local.datastore.RecognitionPreferences
import com.pulse.player.data.local.mediastore.ScanResult
import com.pulse.player.data.remote.audd.AuddApi
import com.pulse.player.data.remote.audd.AuddAppleMusic
import com.pulse.player.data.remote.audd.AuddArtwork
import com.pulse.player.data.remote.audd.AuddError
import com.pulse.player.data.remote.audd.AuddResponse
import com.pulse.player.data.remote.audd.AuddResult
import com.pulse.player.data.remote.audd.AuddSpotify
import com.pulse.player.data.remote.audd.AuddSpotifyAlbum
import com.pulse.player.data.remote.audd.AuddSpotifyImage
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.AudioFolder
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.RecognitionState
import com.pulse.player.domain.repository.TrackSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongRecognitionRepositoryTest {

    private lateinit var fakeAudioRecorder: FakeAudioRecorder
    private lateinit var fakeAuddApi: FakeAuddApi
    private lateinit var fakePreferences: FakeRecognitionPreferences
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var repository: SongRecognitionRepositoryImpl

    private val sampleTrack = Track(
        id = "track_1",
        title = "Hotel California",
        artist = "Eagles",
        album = "Hotel California",
        durationMs = 391000,
        path = "/storage/music/hotel_california.mp3"
    )

    @Before
    fun setUp() {
        fakeAudioRecorder = FakeAudioRecorder()
        fakeAuddApi = FakeAuddApi()
        fakePreferences = FakeRecognitionPreferences("token_test_123")
        fakeLibraryRepository = FakeLibraryRepository(listOf(sampleTrack))

        repository = SongRecognitionRepositoryImpl(
            audioRecorder = fakeAudioRecorder,
            auddApi = fakeAuddApi,
            preferences = fakePreferences,
            libraryRepository = fakeLibraryRepository
        )
    }

    @Test
    fun `successful recognition matches local library track`() = runTest {
        fakeAuddApi.responseToReturn = AuddResponse(
            status = "success",
            result = AuddResult(
                title = "Hotel California",
                artist = "Eagles",
                album = "Hotel California",
                releaseDate = "1976-12-08",
                songLink = "https://lis.tn/HotelCalifornia",
                spotify = AuddSpotify(
                    album = AuddSpotifyAlbum(
                        images = listOf(AuddSpotifyImage(url = "https://spotify.com/img.jpg"))
                    )
                )
            )
        )

        val state = repository.recognizeFromWavBytes(ByteArray(100))

        assertTrue(state is RecognitionState.Success)
        val success = state as RecognitionState.Success
        assertEquals("Hotel California", success.song.title)
        assertEquals("Eagles", success.song.artist)
        assertNotNull(success.song.localTrackMatch)
        assertEquals("track_1", success.song.localTrackMatch?.id)
        assertEquals("https://spotify.com/img.jpg", success.song.artworkUrl)
    }

    @Test
    fun `successful recognition with no local match still returns success without local track`() = runTest {
        fakeAuddApi.responseToReturn = AuddResponse(
            status = "success",
            result = AuddResult(
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                releaseDate = "2020-03-20",
                appleMusic = AuddAppleMusic(
                    artwork = AuddArtwork(url = "https://apple.com/{w}x{h}.jpg")
                )
            )
        )

        val state = repository.recognizeFromWavBytes(ByteArray(100))

        assertTrue(state is RecognitionState.Success)
        val success = state as RecognitionState.Success
        assertEquals("Blinding Lights", success.song.title)
        assertNull(success.song.localTrackMatch)
        assertEquals("https://apple.com/600x600.jpg", success.song.artworkUrl)
    }

    @Test
    fun `null result from API produces NoMatch state`() = runTest {
        fakeAuddApi.responseToReturn = AuddResponse(
            status = "success",
            result = null
        )

        val state = repository.recognizeFromWavBytes(ByteArray(100))

        assertTrue(state is RecognitionState.NoMatch)
    }

    @Test
    fun `api error code 900 flags that an api key is required`() = runTest {
        fakeAuddApi.responseToReturn = AuddResponse(
            status = "error",
            error = AuddError(errorCode = 900, errorMessage = "Limit reached for test token")
        )

        val state = repository.recognizeFromWavBytes(ByteArray(100))

        assertTrue(state is RecognitionState.Error)
        val err = state as RecognitionState.Error
        assertTrue(err.needsApiKey)
    }

    @Test
    fun `microphone failure returns error state`() = runTest {
        fakeAudioRecorder.sampleToReturn = null

        val state = repository.recognizeFromMicrophone(6)

        assertTrue(state is RecognitionState.Error)
    }

    // Fakes
    private class FakeAudioRecorder : AudioRecorder {
        override val amplitudeFlow: StateFlow<Float> = MutableStateFlow(0f)
        override val isRecordingFlow: StateFlow<Boolean> = MutableStateFlow(false)
        var sampleToReturn: ByteArray? = ByteArray(100)

        override suspend fun recordSample(durationSeconds: Int): ByteArray? = sampleToReturn
    }

    private class FakeAuddApi : AuddApi {
        var responseToReturn = AuddResponse(status = "success")

        override suspend fun recognizeSong(
            apiToken: RequestBody,
            file: MultipartBody.Part,
            returnExtra: RequestBody?
        ): AuddResponse = responseToReturn
    }

    private class FakeRecognitionPreferences(var key: String = "token_123") : RecognitionPreferences {
        override val auddApiKeyFlow: Flow<String> = flowOf(key)
    }

    private class FakeLibraryRepository(var tracks: List<Track> = emptyList()) : LibraryRepository {
        override fun getTracks(sortOrder: TrackSortOrder): Flow<List<Track>> = flowOf(tracks)
        override fun getFavorites(): Flow<List<Track>> = flowOf(emptyList())
        override fun getAlbums(): Flow<List<Album>> = flowOf(emptyList())
        override fun getArtists(): Flow<List<Artist>> = flowOf(emptyList())
        override fun getFolders(): Flow<List<AudioFolder>> = flowOf(emptyList())
        override fun getPlaylists(): Flow<List<Playlist>> = flowOf(emptyList())
        override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> = flowOf(emptyList())
        override suspend fun createPlaylist(name: String): Long = 1L
        override suspend fun deletePlaylist(playlistId: Long) {}
        override suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {}
        override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {}
        override suspend fun deleteTrack(trackId: String) {}
        override fun getTracksInFolder(folderPath: String): Flow<List<Track>> = flowOf(emptyList())
        override fun getTracksForAlbum(albumId: Long): Flow<List<Track>> = flowOf(emptyList())
        override fun getTracksForArtist(artistId: Long): Flow<List<Track>> = flowOf(emptyList())
        override fun searchTracks(query: String): Flow<List<Track>> = flowOf(emptyList())
        override suspend fun toggleFavorite(trackId: String, isFavorite: Boolean) {}
        override suspend fun rescanLibrary(): ScanResult = ScanResult.Success(0, 0, 0, 0)
    }
}
