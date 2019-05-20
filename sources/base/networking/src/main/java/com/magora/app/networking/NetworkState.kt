package com.magora.app.networking

sealed class NetworkState {
    object Loading : NetworkState()
    object Success : NetworkState()
    class Error(val error: Throwable) : NetworkState()
}