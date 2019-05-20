package com.magora.app.di

import com.magora.app.VmSplash
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.terrakok.cicerone.Navigator

val splashModule = module {
    viewModel { (navigator: Navigator) ->
        VmSplash(
            app = get(),
            router = get(),
            navHolder = get(),
            navigator = navigator,
            mainScreen = get(named("main"))
        )
    }
}

internal object SplashModuleLoader {
    private var isLoaded = false

    fun load() {
        if (!isLoaded) {
            kotlin.runCatching {
                loadKoinModules(splashModule)
            }
            isLoaded = true
        }
    }
}