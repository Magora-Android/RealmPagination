package com.magora.app.networking

import com.magora.app.model.User
import com.magora.app.networking.mock.rawUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Call
import retrofit2.await
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.HttpURLConnection
import java.net.InetAddress

interface RestClient {
    @GET("users/{login}")
    fun user(@Path("login") login: String): Call<User>

    @GET("users")
    fun users(@Query("since") page: Int, @Query("per_page") pageSize: Int): Call<List<User>>
}

interface UsersRepository {
    suspend fun user(login: String): User

    suspend fun users(page: Int, pageSize: Int): List<User>
}

class NetworkClientImpl(private val usersRestClient: RestClient) : UsersRepository {
    override suspend fun user(login: String): User =
        if (BuildConfig.FLAVOR.contains("mock", ignoreCase = true)) {
            mocked(rawUsers.mocked()) { usersRestClient.user(login).await() }
        } else {
            usersRestClient.user(login).await()
        }

    override suspend fun users(page: Int, pageSize: Int): List<User> =
        if (BuildConfig.FLAVOR.contains("mock", ignoreCase = true)) {
            mocked(rawUsers.mocked()) { usersRestClient.users(page, pageSize).await() }
        } else {
            usersRestClient.users(page, pageSize).await()
        }

    private fun String.mocked(code: Int = HttpURLConnection.HTTP_OK) = MockResponse().apply {
        setResponseCode(code)
        setBody(this@mocked)
    }

    private suspend inline fun <T> mocked(response: MockResponse, crossinline block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            MockWebServer().use { ws ->
                ws.start(InetAddress.getLocalHost(), 8888)
                ws.enqueue(response)
                val result = block()
                delay(800)
                ws.takeRequest()
                result
            }
        }
}