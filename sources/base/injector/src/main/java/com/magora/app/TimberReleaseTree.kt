package com.magora.app

import android.util.Log
import timber.log.Timber

/**
 * Developed by Magora Team (magora-systems.com)
 * 2017
 *
 * @author Viktor Zemtsov
 */

class TimberReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.VERBOSE, Log.DEBUG -> {

            }
            Log.INFO, Log.WARN, Log.ERROR -> {

            }
            else -> {
                // Ignoring
                // For example: Crashlytics.logException(t);
            }
        }
    }
}
