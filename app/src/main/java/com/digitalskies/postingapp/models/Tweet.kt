package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class Tweet(@SerializedName("status")val body:String)
