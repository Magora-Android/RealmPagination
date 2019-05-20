package com.magora.app.datastore.di

import com.magora.app.datastore.LocalUserStorage
import com.magora.app.datastore.UserDataStorage
import org.koin.dsl.binds
import org.koin.dsl.module

val dataStorageModule = module {
    single { LocalUserStorage() } binds arrayOf(UserDataStorage::class)
}