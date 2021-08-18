package com.digitalskies.postingapp.utils

import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class CustomRequestBody(val mediaType: String, val chunkNumber: Int, val offset: Int, val file: File) :RequestBody(){


    override fun contentType(): MediaType? {
       return mediaType.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {

        val inputStream=FileInputStream(file)

        val buffer=ByteArray(BUFFER_SIZE)


        try {

                val read = inputStream.read(buffer)
                val toRead=file.length()-(BUFFER_SIZE*(chunkNumber+1))

            Log.d(CustomRequestBody::class.java.simpleName,"To read $toRead")
                sink.write(buffer, offset, toRead.toInt())

        }
        catch (e: Exception){
            e.printStackTrace()
        }


    }

    override fun contentLength(): Long {
        if(chunkNumber==0){
            return BUFFER_SIZE.toLong()
        }
        else{
            return file.length()-(BUFFER_SIZE*(chunkNumber+1))
        }

    }




    companion object{

        const val BUFFER_SIZE=4096
    }
}