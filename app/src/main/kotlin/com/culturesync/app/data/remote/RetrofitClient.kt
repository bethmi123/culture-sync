package com.culturesync.app.data.remote

import com.culturesync.app.BuildConfig
import com.culturesync.app.utils.Constants
import okhttp3.Interceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Dynamic Base URL support for live Settings override without killing Retrofit instance
    var dynamicBaseUrl: String = Constants.BASE_URL
        set(value) { field = if (value.endsWith("/")) value else "$value/" }

    private val hostInterceptor = Interceptor { chain ->
        var request = chain.request()
        val newHttpUrl = dynamicBaseUrl.toHttpUrlOrNull()
        if (newHttpUrl != null) {
            val newUrl = request.url.newBuilder()
                .scheme(newHttpUrl.scheme)
                .host(newHttpUrl.host)
                .port(newHttpUrl.port)
                .build()
            request = request.newBuilder().url(newUrl).build()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(hostInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(Constants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL) // Used purely as a dummy initializer placeholder
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
