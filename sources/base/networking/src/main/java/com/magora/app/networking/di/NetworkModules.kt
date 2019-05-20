package com.magora.app.networking.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.magora.app.networking.BuildConfig
import com.magora.app.networking.NetworkClientImpl
import com.magora.app.networking.RestClient
import com.magora.app.networking.UsersRepository
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.binds
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private const val TIMEOUT_CONNECTION_SECONDS = 15L
private const val TIMEOUT_READ_SECONDS = 15L
private const val TIMEOUT_WRITE_SECONDS = 15L

val networkModule = module {

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val authInterceptor = Interceptor { chain ->
        //        if (sessionRepository.isAuthorized()) {
//            val originalRequest = chain.request()
//            var modifiedRequest = originalRequest
//
//            val header = originalRequest.header(RestOptions.HEADER_KEY_MARKER)
//            if (header == RestOptions.HEADER_VALUE_MARKER_NON_AUTH) {
//                /*Do nothing*/
//            } else {
//                modifiedRequest = originalRequest.newBuilder()
//                    .header(RestOptions.HEADER_KEY_AUTH, RestOptions.HEADER_VALUE_BEARER + sessionRepository.sessionInfo().accessToken)
//                    .build()
//            }
//
//            chain.proceed(modifiedRequest)
//        } else {
        val originalRequest = chain.request()
        chain.proceed(originalRequest)
//        }
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .retryOnConnectionFailure(true)
        .connectTimeout(TIMEOUT_CONNECTION_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_WRITE_SECONDS, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.REST_API_ENDPOINT)
        .addConverterFactory(Json.nonstrict.asConverterFactory(MediaType.parse("application/json")!!))
        .client(client)
        .build()

    single { NetworkClientImpl(retrofit.create(RestClient::class.java)) } binds arrayOf(UsersRepository::class)
}