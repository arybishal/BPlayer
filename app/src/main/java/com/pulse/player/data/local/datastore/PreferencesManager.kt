package com.pulse.player.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.pulseDataStore: DataStore<Preferences> by preferencesDataStore(name = "pulse_settings")
