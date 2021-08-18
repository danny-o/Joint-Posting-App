package com.digitalskies.postingapp.utils

import android.app.Application
import android.util.Log
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.digitalskies.postingapp.LINKEDIN_ACCESS_TOKEN
import com.digitalskies.postingapp.LINKEDIN_ACCESS_TOKEN_EXPIRY_TIME
import com.digitalskies.postingapp.LINKEDIN_USER_SET
import com.digitalskies.postingapp.TWITTER_USER_SET
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.models.LinkedInAccessTokenResponse
import com.google.gson.Gson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*


fun checkTwitterLogin(application: Application): Flow<Boolean> {


        val twitterLoggedInPrefKey= booleanPreferencesKey(TWITTER_USER_SET)

        return application.applicationContext.dataStore.data.map { preferences->
            preferences[twitterLoggedInPrefKey]?:false
        }


    }

    fun checkLinkedInLogin(application: Application):Flow<Boolean>{
        val linkedInLoggedInPrefKey= booleanPreferencesKey(LINKEDIN_USER_SET)

        return application.applicationContext.dataStore.data.map { preferences->
            preferences[linkedInLoggedInPrefKey]?:false
        }
    }

    fun checkLinkedCredentialsExpired(application: Application):Flow<Boolean?>{

       return application.dataStore.data.map { preferences->


            val currentTimeInSecs=System.currentTimeMillis()/1000

           preferences[longPreferencesKey(LINKEDIN_ACCESS_TOKEN_EXPIRY_TIME)]?.let {
               if(it <currentTimeInSecs){


                   return@map true
               }
               else{
                   false
               }
           }



        }





    }
        fun getTwitterOauthSecret(application: Application):Flow<String?>{
           return application.dataStore.data.map { preferences->
            preferences[stringPreferencesKey("twitter_oauth_token")]


        }


        }
