package com.example.inspixmobile.data.source.local.store

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inspixmobile.domain.model.Session
import com.example.inspixmobile.domain.model.Setting
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import com.example.inspixmobile.settingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingStore(
    private val context: Context
) {

    private object Keys {
        val HOME_LAYOUT = stringPreferencesKey("home_layout")
        val NAVBAR_LAYOUT = stringPreferencesKey("navbar_layout")
    }

    val setting: Flow<Setting> = context.settingDataStore.data
        .map { preferences ->
            Setting(
                homeLayout = HomeLayoutStyle.valueOf(
                    preferences[Keys.HOME_LAYOUT] ?: HomeLayoutStyle.Grid.name
                ),
                navbarLayout = NavigationBarStyle.valueOf(
                    preferences[Keys.NAVBAR_LAYOUT] ?: NavigationBarStyle.Floating.name
                )
            )
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

    suspend fun saveSetting(
        homeLayout: HomeLayoutStyle,
        navbarLayout: NavigationBarStyle
    ) {
        context.settingDataStore.edit { preferences ->
            preferences[Keys.HOME_LAYOUT] = homeLayout.name
            preferences[Keys.NAVBAR_LAYOUT] = navbarLayout.name
        }
    }

    suspend fun clearSetting() {
        context.settingDataStore.edit { preferences ->
            preferences.remove(Keys.HOME_LAYOUT)
            preferences.remove(Keys.NAVBAR_LAYOUT)
        }
    }
}