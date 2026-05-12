package com.example.inspixmobile.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat

object SystemUtils {
    val isEmulator: Boolean
        get() = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))

    inline fun <reified T : Any> Context.getSystemServiceCompat(): T =
        ContextCompat.getSystemService(applicationContext, T::class.java)!!

    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_NETWORK_STATE])
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager: ConnectivityManager = context.getSystemServiceCompat()
            connectivityManager.activeNetworkInfo?.isConnected == true
        } catch (e: Exception) {
            false
        }
    }
}