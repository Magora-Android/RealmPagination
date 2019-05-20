package com.magora.app.usersList

import com.magora.app.networking.NetworkState

sealed class UsersResult {
    object Empty : UsersResult()
    class ContentProgress(val inProgress: Boolean) : UsersResult()
    class PaginationProgress(val inProgress: Boolean) : UsersResult()
    class PullToRefreshProgress(val inProgress: Boolean) : UsersResult()
    class SilentUpdateProgress(val inProgress: Boolean) : UsersResult()
    class Error(val error: Throwable) : UsersResult()
}

fun NetworkState?.toFeedContentViewState(): UsersResult =
    when (this) {
        null -> UsersResult.Empty
        is NetworkState.Loading -> UsersResult.ContentProgress(true)
        is NetworkState.Success -> UsersResult.ContentProgress(false)
        is NetworkState.Error -> UsersResult.Error(error)
    }

fun NetworkState?.toFeedPagingViewState(): UsersResult =
    when (this) {
        null -> UsersResult.Empty
        is NetworkState.Loading -> UsersResult.PaginationProgress(true)
        is NetworkState.Success -> UsersResult.PaginationProgress(false)
        is NetworkState.Error -> UsersResult.Error(error)
    }

fun NetworkState?.toFeedPtrViewState(): UsersResult =
    when (this) {
        null -> UsersResult.Empty
        is NetworkState.Loading -> UsersResult.PullToRefreshProgress(true)
        is NetworkState.Success -> UsersResult.PullToRefreshProgress(false)
        is NetworkState.Error -> UsersResult.Error(error)
    }

fun NetworkState?.toFeedSilentUpdateViewState(): UsersResult =
    when (this) {
        null -> UsersResult.Empty
        is NetworkState.Loading -> UsersResult.SilentUpdateProgress(true)
        is NetworkState.Success -> UsersResult.SilentUpdateProgress(false)
        is NetworkState.Error -> UsersResult.Error(error)
    }