package com.digitalskies.postingapp.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object RestClient {


    const val LINKEDIN_BASE_URL = "https://www.linkedin.com/oauth/v2/"

    const val LINKEDIN_PROFILE_API = "https://api.linkedin.com/v2/"

    const val TWITTER_BASE_URL="https://api.twitter.com"

    const val AUTHORIZATION="Authorization"

    const val TWITTER_AUTHORIZATION="authorization"


    const val BASIC="Basic"

    const val BEARER="Bearer "



    const val LINKEDIN_CLIENT_ID = "86lv9vqbo6dmwh"

    const val AUTH_VERIFIER=""

    const val API_KEY = "b2001f141c034d3ebf1425f0818f39d5"

    fun getPostingApi(): LinkedInApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClientBuilder = OkHttpClient.Builder()

        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)

        okHttpClientBuilder.readTimeout(15, TimeUnit.SECONDS)

        okHttpClientBuilder.connectTimeout(15, TimeUnit.SECONDS)


        return Retrofit.Builder()
                .baseUrl(LINKEDIN_BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LinkedInApi::class.java)
    }
    fun  getLinkedInProfileApi():LinkedInApi{
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClientBuilder = OkHttpClient.Builder()

        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)

        okHttpClientBuilder.readTimeout(15, TimeUnit.SECONDS)

        okHttpClientBuilder.connectTimeout(15, TimeUnit.SECONDS)




        return Retrofit.Builder()
            .baseUrl(LINKEDIN_PROFILE_API)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LinkedInApi::class.java)
    }

    fun  getTwitterApi():TwitterApi{
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClientBuilder = OkHttpClient.Builder()

        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)

        okHttpClientBuilder.addInterceptor(Interceptor { chain ->
            val timeout=chain.request().header("connection_timeout")?.toInt()


            timeout?.let {
                chain.withConnectTimeout(timeout,TimeUnit.MINUTES)
            }
            chain.proceed(chain.request())
        })

        okHttpClientBuilder.readTimeout(15, TimeUnit.SECONDS)

        okHttpClientBuilder.connectTimeout(15, TimeUnit.SECONDS)


        return Retrofit.Builder()
                .baseUrl(TWITTER_BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TwitterApi::class.java)
    }






}