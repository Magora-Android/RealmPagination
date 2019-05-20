package com.magora.app.usersList.di

import ru.terrakok.cicerone.Screen

interface UserDetailsScreenProvider {

    fun get(userId: Int): Screen
}