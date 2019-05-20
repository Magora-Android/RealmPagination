package com.magora.app

import android.app.Application
import com.magora.app.datastore.di.dataStorageModule
import com.magora.app.networking.di.networkModule
import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

class CommonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
        Realm.init(this)
        startKoin {
            androidContext(this@CommonApplication)
            modules(
                navigationModule,
                screenModule,
                networkModule,
                dataStorageModule
            )
        }
    }

    private fun initTimber() {
        val tree = if (BuildConfig.DEBUG) Timber.DebugTree() else TimberReleaseTree()
        Timber.plant(tree)
    }
}