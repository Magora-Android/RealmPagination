package com.magora.app.userDetails

import com.magora.app.model.User
import com.magora.app.networking.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */
class GetUserUseCase(private val usersRepository: UsersRepository) {

    suspend operator fun invoke(login: String): User = withContext(Dispatchers.IO) {
        usersRepository.user(login)
    }
}