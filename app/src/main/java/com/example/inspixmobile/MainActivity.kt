package com.example.inspixmobile

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.inspixmobile.core.di.jsonModule
import com.example.inspixmobile.core.di.networkModule
import com.example.inspixmobile.core.di.repositoryModule
import com.example.inspixmobile.core.di.viewModelModule
import com.example.inspixmobile.core.di.databaseModule
import com.example.inspixmobile.presentation.navigation.Graph
import com.example.inspixmobile.presentation.theme.InspixMobileTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.inspixmobile.core.di.storeModule
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.data.source.local.store.SettingStore
import com.example.inspixmobile.domain.model.Session
import com.example.inspixmobile.domain.model.Setting
import com.example.inspixmobile.presentation.viewmodel.SplashViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session"
)

val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                jsonModule,
                databaseModule,
                repositoryModule,
                viewModelModule,
                storeModule
            )
        }

        val okHttpClient = OkHttpClient.Builder().build()
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(OkHttpNetworkFetcherFactory(okHttpClient))
                }
                .build()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashViewModel: SplashViewModel by viewModel()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }

        enableEdgeToEdge()
        setContent {
            InspixMobileTheme {
                val sessionStore = koinInject<SessionStore>()
                val settingStore = koinInject<SettingStore>()

                val currentSession by sessionStore.session.collectAsState(
                    initial = Session()
                )

                val currentSetting by settingStore.setting.collectAsState(
                    initial = Setting()
                )

                Graph(
                    currentSetting = currentSetting,
                    currentSession = currentSession
                )
            }
        }
    }
}