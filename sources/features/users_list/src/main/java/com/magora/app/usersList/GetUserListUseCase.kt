package com.magora.app.usersList

import com.magora.app.model.User
import com.magora.app.networking.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */
class GetUserListUseCase(private val usersRepository: UsersRepository) {

    suspend operator fun invoke(page: Int, pageSize: Int): List<User> = withContext(Dispatchers.IO) {
        usersRepository.users(page, pageSize)
    }
}