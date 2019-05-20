package com.magora.app.datastore

import androidx.annotation.UiThread
import com.magora.app.model.User
import com.magora.app.model.UserFields
import com.magora.app.model.UserList
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.kotlin.addChangeListener
import io.realm.kotlin.createObject
import io.realm.kotlin.isValid
import io.realm.kotlin.where
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface UserDataStorage {
    fun putUser(user: User)
    fun getUser(id: Int): User?
    suspend fun getUserAsync(id: Int): User?
    fun putUsers(users: Collection<User>)
    fun putUsersNextPage(users: Collection<User>)
    fun getUsers(): UserList

    suspend fun getUsersAsync(): UserList
}

class LocalUserStorage : UserDataStorage {
    private var realm: Realm? = null

    private fun ensureRealmCreated(): Realm {
        if (realm == null || realm!!.isClosed) {
            realm = Realm.getDefaultInstance()
        }
        return realm!!
    }

    override fun putUser(user: User) {
        withAsyncTransaction {
            insertOrUpdate(user)
        }
    }

    override fun getUser(id: Int): User? = with(ensureRealmCreated()) {
        where<User>().equalTo(UserFields.ID, id).findFirst()
    }

    @UiThread
    override suspend fun getUserAsync(id: Int): User? = suspendCancellableCoroutine { continuation ->
        with(ensureRealmCreated()) {
            val userWrapper = where<User>().equalTo(UserFields.ID, id).findFirstAsync()
            userWrapper.addChangeListener(RealmChangeListener { user ->
                if (user.isValid() == true) {
                    continuation.resume(user)
                }
            })
        }
    }

    override fun putUsers(users: Collection<User>) {
        withAsyncTransaction {
            var cached = where<UserList>().findFirst()
            if (cached != null) {
                cached.users.clear()
                cached.users.addAll(users)
            } else {
                cached = createObject()
                cached.users.addAll(users)
            }
            cached.lastUpdateTimestamp = System.currentTimeMillis()
        }
    }

    override fun putUsersNextPage(users: Collection<User>) {
        withAsyncTransaction {
            var cached = where<UserList>().findFirst()
            if (cached != null) {
                cached.users.addAll(users)
            } else {
                cached = createObject()
                cached.users.addAll(users)
            }
            cached.lastUpdateTimestamp = System.currentTimeMillis()
        }
    }

    override fun getUsers(): UserList = with(ensureRealmCreated()) {
        var users = where<UserList>().findFirst()
        if (users == null) {
            beginTransaction()
            users = createObject()
            commitTransaction()
        }
        users
    }

    override suspend fun getUsersAsync(): UserList = suspendCancellableCoroutine { continuation ->
        with(ensureRealmCreated()) {
            val usersWrapper = where<UserList>().findFirstAsync()
            usersWrapper.addChangeListener(RealmChangeListener { users ->
                if (users != null) {
                    if (users.isValid()) {
                        continuation.resume(users)
                    }
                } else {
                    continuation.resume(createObject())
                }
            })
        }
    }

    private fun withAsyncTransaction(block: Realm.() -> Unit) {
        Realm.getDefaultInstance().use { r ->
            r.executeTransactionAsync { realm ->
                realm.block()
            }
        }
    }
}