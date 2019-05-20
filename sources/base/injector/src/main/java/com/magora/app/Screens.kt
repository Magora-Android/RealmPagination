package com.magora.app

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.magora.app.userDetails.UserDetailsFragment
import com.magora.app.usersList.FragmentUserList
import com.magora.app.usersList.di.UserDetailsScreenProvider
import ru.terrakok.cicerone.android.support.SupportAppScreen

class MainScreen : SupportAppScreen() {
    override fun getActivityIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java)
}

class UsersListScreen : SupportAppScreen() {
    override fun getFragment(): Fragment = FragmentUserList.newInstance()
}

class UserDetailsScreenProviderImpl : UserDetailsScreenProvider {
    override fun get(userId: Int) = object : SupportAppScreen() {
        override fun getFragment() = UserDetailsFragment.newInstance(userId)
    }
}