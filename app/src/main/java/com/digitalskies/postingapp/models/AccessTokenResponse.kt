package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(@SerializedName("access_token")val accessToken:String)