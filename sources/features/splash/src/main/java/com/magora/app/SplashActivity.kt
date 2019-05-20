package com.magora.app

import android.os.Bundle
import com.magora.app.di.SplashModuleLoader
import com.magora.core.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.terrakok.cicerone.android.support.SupportAppNavigator

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 *
 * @author Viktor Zemtsov
 */


class SplashActivity : BaseActivity() {
    private val viewModel: VmSplash by viewModel { parametersOf(SupportAppNavigator(this, android.R.id.content)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SplashModuleLoader.load()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        viewModel.onResume()
        viewModel.navigateNext()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }
}