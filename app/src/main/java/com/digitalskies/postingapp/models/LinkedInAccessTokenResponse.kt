package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class LinkedInAccessTokenResponse(@SerializedName("access_token")var accessToken:String,
                                       @SerializedName("expires_in")var expiryTime:Int)
