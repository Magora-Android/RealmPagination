package com.magora.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

class VmMain(
    app: Application,
    private val router: Router,
    private val navHolder: NavigatorHolder,
    private val navigator: Navigator
) : AndroidViewModel(app) {

    fun onResume() {
        navHolder.setNavigator(navigator)
    }

    fun onPause() {
        navHolder.removeNavigator()
    }
}