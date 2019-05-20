package com.magora.app.usersList.di

import com.magora.app.usersList.GetUserListUseCase
import com.magora.app.usersList.UsersListDataSourceFactory
import com.magora.app.usersList.VmUsersList
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

val usersListModule = module {
    viewModel {
        VmUsersList(
            app = get(),
            dsFactory = get(),
            localStorage = get(),
            router = get(),
            screenProvider = get()
        )
    }
}

val usersUseCaseModule = module {
    factory { GetUserListUseCase(usersRepository = get()) }
    single { UsersListDataSourceFactory(getUsersUseCase = get(), localStorage = get()) }
}

internal object UsersModuleLoader {
    private var isLoaded = false

    fun load() {
        if (!isLoaded) {
            kotlin.runCatching {
                loadKoinModules(usersListModule, usersUseCaseModule)
            }
            isLoaded = true
        }
    }
}