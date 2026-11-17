package com.pulse.player.data.remote.lrclib

import com.pulse.player.data.remote.lrclib.model.LrclibResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrclibApi {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") durationSeconds: Int? = null
    ): Response<LrclibResponse>

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): Response<List<LrclibResponse>>
}
