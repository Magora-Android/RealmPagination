package com.magora.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.magora.app.di.MainModuleLoader
import com.magora.core.BaseActivity
import com.magora.core.HasTransition
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

open class MainActivity : BaseActivity() {
    private val router: Router by inject()
    private val navigationHolder: NavigatorHolder by inject()
    private val usersScreen: Screen = get(named("usersList"))
    private val navigator by lazy(LazyThreadSafetyMode.NONE) { MyNavigator(this, android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        MainModuleLoader.load()
        super.onCreate(savedInstanceState)
        router.newRootScreen(usersScreen)
    }

    override fun onResumeFragments() {
        navigationHolder.setNavigator(navigator)
        super.onResumeFragments()
    }

    override fun onPause() {
        navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        router.exit()
    }

    class MyNavigator(activity: FragmentActivity, container: Int) : SupportAppNavigator(activity, container) {

        override fun setupFragmentTransaction(
            command: Command?,
            currentFragment: Fragment?,
            nextFragment: Fragment?,
            ft: FragmentTransaction
        ) {
            if (currentFragment is HasTransition) {
                ft.setReorderingAllowed(true)

                val sharedElements = currentFragment.getSharedElements()

                sharedElements.forEach { (name, view) ->
                    ft.addSharedElement(view, name)
                }
            }
        }
    }
}