package com.example.financeapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financeapp.domain.model.PlannerState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.plannerDataStore: DataStore<Preferences> by preferencesDataStore(name = "planner")

class PlannerPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val state: Flow<PlannerState> = context.plannerDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            preferences[PlannerKeys.State]
                ?.let { encoded -> runCatching { json.decodeFromString<PlannerState>(encoded) }.getOrNull() }
                ?: PlannerState()
        }

    suspend fun update(transform: (PlannerState) -> PlannerState) {
        context.plannerDataStore.edit { preferences ->
            val current = preferences[PlannerKeys.State]
                ?.let { encoded -> runCatching { json.decodeFromString<PlannerState>(encoded) }.getOrNull() }
                ?: PlannerState()
            preferences[PlannerKeys.State] = json.encodeToString(transform(current))
        }
    }

    private object PlannerKeys {
        val State = stringPreferencesKey("planner_state")
    }
}
