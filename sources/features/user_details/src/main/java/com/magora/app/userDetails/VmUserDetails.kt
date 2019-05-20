package com.magora.app.userDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magora.app.datastore.LocalUserStorage
import com.magora.app.model.User
import io.realm.RealmObject
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router
import kotlin.coroutines.CoroutineContext

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

class VmUserDetails(
    app: Application,
    private val localStorage: LocalUserStorage,
    private val router: Router
) : AndroidViewModel(app), CoroutineScope {
    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> get() = _userLiveData

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    fun getUser(userId: Int) {
        launch {
            val user = localStorage.getUserAsync(userId)
            _userLiveData.value = RealmObject.getRealm(user!!).copyFromRealm(user)
        }
    }

    override fun onCleared() {
        coroutineContext.cancelChildren()
        super.onCleared()
    }
}