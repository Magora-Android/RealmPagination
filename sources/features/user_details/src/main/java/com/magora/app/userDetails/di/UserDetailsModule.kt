package com.magora.app.userDetails.di

import com.magora.app.userDetails.VmUserDetails
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

val userDetailsUseCases = module {
    viewModel { VmUserDetails(app = get(), localStorage = get(), router = get()) }
}

internal object UserDetailsModuleLoader {
    private var isLoaded = false

    fun load() {
        if (!isLoaded) {
            kotlin.runCatching {
                loadKoinModules(userDetailsUseCases)
            }
            isLoaded = true
        }
    }
}