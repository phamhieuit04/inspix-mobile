package com.example.inspixmobile.data.source.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inspixmobile.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session"
)

class SessionStore(
    private val context: Context
) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_UUID = stringPreferencesKey("user_uuid")
    }

    val session: Flow<Session> = context.sessionDataStore.data
        .map { preferences ->
            Session(
                accessToken = preferences[Keys.ACCESS_TOKEN],
                userUuid = preferences[Keys.USER_UUID]
            )
        }

    val accessToken: Flow<String?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ACCESS_TOKEN]
        }

    val userUuid: Flow<String?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.USER_UUID]
        }

    suspend fun saveSession(
        accessToken: String,
        userUuid: String
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.USER_UUID] = userUuid
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.USER_UUID)
        }
    }
}