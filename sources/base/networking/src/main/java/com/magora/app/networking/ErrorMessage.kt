package com.magora.app.networking

import android.content.Context
import java.io.IOException

open class DefaultErrorMessage(private val context: Context) {

    fun toMessage(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> "Unknown Error"
            else -> "Something bad happened"
        }
    }
}