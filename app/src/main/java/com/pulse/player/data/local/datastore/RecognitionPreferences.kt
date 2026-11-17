package com.pulse.player.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface RecognitionPreferences {
    val auddApiKeyFlow: Flow<String>
}
