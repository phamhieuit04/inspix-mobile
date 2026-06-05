package com.example.inspixmobile

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import okhttp3.OkHttpClient

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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InspixMobileTheme {
                Graph()
            }
        }
    }
}