package com.pulse.player.data.remote.artwork

import com.pulse.player.data.remote.artwork.model.ArtworkCandidate
import com.pulse.player.data.remote.artwork.model.DeezerSearchResponse
import com.pulse.player.data.remote.artwork.model.ItunesSearchResponse
import com.pulse.player.util.logger.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkSearchService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val tag = "ArtworkSearchService"

    suspend fun searchArtwork(query: String): List<ArtworkCandidate> = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<ArtworkCandidate>()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        // 1. Query iTunes Search API (fast and high-res 600x600)
        try {
            val itunesUrl = "https://itunes.apple.com/search?term=$encodedQuery&entity=album&limit=8"
            val request = Request.Builder().url(itunesUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val parsed = json.decodeFromString<ItunesSearchResponse>(bodyStr)
                    for (item in parsed.results) {
                        val highRes = item.highResArtworkUrl
                        if (!highRes.isNullOrBlank()) {
                            candidates.add(
                                ArtworkCandidate(
                                    imageUrl = highRes,
                                    source = "Apple Music / iTunes",
                                    albumTitle = item.collectionName ?: "",
                                    artistName = item.artistName ?: ""
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "iTunes artwork search failed: ${e.message}")
        }

        // 2. Query Deezer Search API (cover_xl 1000x1000 / cover_big 500x500)
        try {
            val deezerUrl = "https://api.deezer.com/search/album?q=$encodedQuery&limit=8"
            val request = Request.Builder().url(deezerUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val parsed = json.decodeFromString<DeezerSearchResponse>(bodyStr)
                    for (item in parsed.data) {
                        val imgUrl = item.coverXl ?: item.coverBig
                        if (!imgUrl.isNullOrBlank()) {
                            candidates.add(
                                ArtworkCandidate(
                                    imageUrl = imgUrl,
                                    source = "Deezer",
                                    albumTitle = item.title ?: "",
                                    artistName = item.artist?.name ?: ""
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Deezer artwork search failed: ${e.message}")
        }

        // Deduplicate candidates by image URL
        candidates.distinctBy { it.imageUrl }
    }
}
