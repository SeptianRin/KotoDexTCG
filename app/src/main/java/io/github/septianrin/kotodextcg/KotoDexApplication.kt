package io.github.septianrin.kotodextcg

import android.app.Application
import io.github.septianrin.kotodextcg.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class KotoDexApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@KotoDexApplication)
            modules(appModule)
        }
    }
}