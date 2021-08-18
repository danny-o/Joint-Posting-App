package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class MediaUploadStatus(@SerializedName("media_id_string")var mediaID:String,
                             @SerializedName("processing_info") var processingInfo: ProcessingInfo?){


}


data class ProcessingInfo(@SerializedName("state")var state:String,
                          @SerializedName("check_after_secs")var checkAfter:Int
)