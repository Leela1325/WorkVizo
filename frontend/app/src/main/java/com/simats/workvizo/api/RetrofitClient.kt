package com.simats.workvizo.api
import retrofit2.converter.scalars.ScalarsConverterFactory

import java.net.CookieManager
import java.net.CookiePolicy
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit




object RetrofitClient {

    private const val BASE_URL = "http://10.163.250.141/workvizo_backend/api/"

    // 🔥 COOKIE MANAGER (REQUIRED FOR PHP SESSIONS)
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    // 🔥 OKHTTP CLIENT WITH COOKIES + TIMEOUTS
    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager)) // ✅ IMPORTANT
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()


    // 🔥 SINGLE RETROFIT INSTANC
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
