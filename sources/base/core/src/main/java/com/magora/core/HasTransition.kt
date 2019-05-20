package com.magora.core

import android.view.View

interface HasTransition {

    fun getSharedElements(): List<Pair<String, View>>
}