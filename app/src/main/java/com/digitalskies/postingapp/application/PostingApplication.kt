package com.digitalskies.postingapp.application

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.digitalskies.postingapp.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PostingApplication:Application() {


    override fun onCreate() {
        super.onCreate()



        if(!getIfLinkedInClientIdAndSecretSet()){
            setLinkedInClientIdAndSecret()
        }
        if(!getIfTwitterApiKeyAndSecretSet()){
            setTwitterClientIdAndSecret()
        }
    }

    private fun setLinkedInClientIdAndSecret() {



        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this)

        sharedPreferences.edit().putString(LINKEDIN_CLIENT_SECRET,"a1oP5V1AJzkfQON0").apply()
        sharedPreferences.edit().putString(LINKEDIN_CLIENT_ID,"86lv9vqbo6dmwh").apply()
        sharedPreferences.edit().putBoolean(LINKEDIN_CLIENT_ID_AND_SECRET_SET,true).apply()


    }
    private fun setTwitterClientIdAndSecret() {



        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putString(TWITTER_CLIENT_SECRET,"WjkmnmQ9ARIo86ZbsumMuNBOZ4yPrXJGK38N0QSYy93Q4BMXYp").apply()
        sharedPreferences.edit().putString(TWITTER_API_KEY,"HksreL3zhC75C1E55CxLJr1XQ").apply()
        sharedPreferences.edit().putBoolean(TWITTER_API_KEY_AND_SECRET_SET,true).apply()


    }
    private fun getIfLinkedInClientIdAndSecretSet():Boolean{
        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this)

        return sharedPreferences.getBoolean(LINKEDIN_CLIENT_ID_AND_SECRET_SET,false)
    }
    private fun getIfTwitterApiKeyAndSecretSet():Boolean{
        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean(TWITTER_API_KEY_AND_SECRET_SET,false)
    }

}