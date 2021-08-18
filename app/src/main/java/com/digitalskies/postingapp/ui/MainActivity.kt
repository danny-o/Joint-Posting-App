package com.digitalskies.postingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digitalskies.postingapp.R
import com.digitalskies.postingapp.databinding.MainContainerBinding



class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: MainContainerBinding


    lateinit var mainActivityViewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding= MainContainerBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)

       supportFragmentManager.beginTransaction()
           .replace(R.id.frame_layout,HomeFragment())
           .commit()


    }
    fun toast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showLoading(){
        mainBinding.layoutProgressBar.isVisible=true
    }

    fun hideLoading(){
        mainBinding.layoutProgressBar.isVisible=false
    }
}