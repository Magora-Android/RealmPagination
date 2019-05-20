package com.magora.app.model

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

@Parcelize
@Serializable
@RealmClass
open class User : Parcelable, RealmModel {
    @PrimaryKey
    @SerialName("id") var id: Int = 0
    @SerialName("login") var login: String? = null
    @SerialName("avatar_url") var avatarUrl: String? = null
    @SerialName("url") var url: String? = null
    @SerialName("html_url") var htmlUrl: String? = null
    @SerialName("repos_url") var reposUrl: String? = null
}

@RealmClass
open class UserList : RealmModel {
    lateinit var users: RealmList<User>
    var lastUpdateTimestamp: Long = 0
}

