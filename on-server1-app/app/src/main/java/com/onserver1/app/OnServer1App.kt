package com.onserver1.app

import android.app.Application
import android.content.Context
import com.onserver1.app.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OnServer1App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }
}
