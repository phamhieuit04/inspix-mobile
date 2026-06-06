package com.example.inspixmobile.data.source.local.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

class SettingStore(
    private val context: Context
) {

    private object Keys {
        val HOME_LAYOUT = stringPreferencesKey("home_layout")
        val NAVBAR_LAYOUT = stringPreferencesKey("navbar_layout")
    }

    val homeLayout: Flow<HomeLayoutStyle> = context.settingDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            HomeLayoutStyle.valueOf(
                preferences[Keys.HOME_LAYOUT] ?: HomeLayoutStyle.Grid.name
            )
        }

    val navbarLayout: Flow<NavigationBarStyle> = context.settingDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            NavigationBarStyle.valueOf(
                preferences[Keys.NAVBAR_LAYOUT] ?: NavigationBarStyle.Floating.name
            )
        }
}