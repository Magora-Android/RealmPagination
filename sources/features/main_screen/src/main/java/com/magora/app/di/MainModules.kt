package com.magora.app.di

import com.magora.app.VmMain
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import ru.terrakok.cicerone.Navigator

val mainModule = module {
    viewModel { (navigator: Navigator) ->
        VmMain(
            app = get(),
            router = get(),
            navHolder = get(),
            navigator = navigator
        )
    }
}

internal object MainModuleLoader {
    private var isLoaded = false

    fun load() {
        if (!isLoaded) {
            kotlin.runCatching {
                loadKoinModules(mainModule)
            }
            isLoaded = true
        }
    }
}