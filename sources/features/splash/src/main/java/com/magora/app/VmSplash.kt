package com.magora.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen

class VmSplash(
    app: Application,
    private val router: Router,
    private val navHolder: NavigatorHolder,
    private val navigator: Navigator,
    private val mainScreen: Screen
) : AndroidViewModel(app) {

    fun navigateNext() {
        router.replaceScreen(mainScreen)
    }

    fun onResume() {
        navHolder.setNavigator(navigator)
    }

    fun onPause() {
        navHolder.removeNavigator()
    }
}