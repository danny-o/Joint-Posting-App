package com.digitalskies.postingapp.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digitalskies.postingapp.R
import com.digitalskies.postingapp.databinding.MainContainerBinding





const val LINK="LINK"

const val MEDIA_URI="media_uri"

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: MainContainerBinding


    lateinit var mainActivityViewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding= MainContainerBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)

        val bundle=Bundle()

        intent?.let {
            Log.d(MainActivity::class.java.simpleName,it.data.toString())


            if(it.type?.startsWith("text")==true){

                Log.d(MainActivity::class.java.simpleName,it.getStringExtra(Intent.EXTRA_TEXT).toString())
                bundle.putString(LINK,intent.getStringExtra(Intent.EXTRA_TEXT))
            }
            else if(it.type?.startsWith("image")==true||it.type?.startsWith("video")==true){

                val uri=intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri


                Log.d(MainActivity::class.java.simpleName,uri.toString())

                bundle.putString(MEDIA_URI,uri.toString())



            }
        }

       supportFragmentManager.beginTransaction()
           .replace(R.id.frame_layout,HomeFragment::class.java,bundle)
           .commit()


    }
    fun toast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }



    fun showLoading(){
        mainBinding.layoutProgressBar.isVisible=true
    }

    fun hideLoading(){
        mainBinding.layoutProgressBar.isVisible=false
    }
}