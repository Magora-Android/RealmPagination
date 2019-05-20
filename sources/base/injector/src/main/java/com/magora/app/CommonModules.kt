package com.magora.app

import com.magora.app.usersList.di.UserDetailsScreenProvider
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.terrakok.cicerone.Cicerone

val navigationModule = module {
    val cicerone = Cicerone.create()
    single { cicerone.router }
    single { cicerone.navigatorHolder }
}

val screenModule = module {
    factory(named("main")) { MainScreen() }
    factory(named("usersList")) { UsersListScreen() }
    single { UserDetailsScreenProviderImpl() } bind UserDetailsScreenProvider::class
}