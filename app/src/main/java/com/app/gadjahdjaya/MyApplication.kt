package com.app.gadjahdjaya

import android.app.Application
import com.midtrans.sdk.uikit.SdkUIFlowBuilder

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Midtrans SDK
        SdkUIFlowBuilder.init()
            .setContext(this)
            .setClientKey("SB-Mid-client-et53d4Nh947a6_8h") // Ganti dengan Client Key Anda
            .setMerchantBaseUrl("https://api.sandbox.midtrans.com/v2/") // URL Sandbox
            .enableLog(true) // Aktifkan log
            .buildSDK()
    }
}
