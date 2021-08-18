package com.digitalskies.postingapp.ui

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.digitalskies.postingapp.*
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.databinding.FragmentSettingsBinding
import com.digitalskies.postingapp.utils.Event
import com.digitalskies.postingapp.utils.EventObserver
import com.digitalskies.postingapp.utils.OnEventChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment: Fragment() {

    lateinit var binding: FragmentSettingsBinding

    lateinit var mainActivityViewModel: MainActivityViewModel




    var twitterUserInfoSet=true

    var linkedInUserInfoSet=true

    var linkedinUserSignedIn=false

    var twitterUserSignedIn=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {




        binding= FragmentSettingsBinding.inflate(inflater,container,false)

        val view= binding.root

        mainActivityViewModel= ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)


        //mainActivityViewModel.clearResponseMessages()


        mainActivityViewModel.linkedInUserIsSet.asLiveData().observe(viewLifecycleOwner){linkedInUserSet->

            showLoading()
            linkedinUserSignedIn=linkedInUserSet




            binding.switchLinkedinSignedIn.isChecked=linkedInUserSet

            if(linkedInUserSet==true){

                updateLinkedInUserInfo()


            }
            else if(linkedInUserSet==false){
                binding.switchLinkedinSignedIn.text=getString(R.string.signed_out)
                binding.tvLinkedinEmail.visibility=View.GONE
                binding.tvLinkedinName.visibility=View.GONE
            }

            setUpLinkedInSwitchBtn()
            hideLoading()
        }
        mainActivityViewModel.twitterUserIsSet.asLiveData().observe(viewLifecycleOwner){twitterUserSet->

            twitterUserSignedIn=twitterUserSet



            binding.switchTwitterSignedIn.isChecked=twitterUserSet



            if(twitterUserSet){


                updateTwitterUserInfo()
            }
            else{
                binding.switchTwitterSignedIn.text=getString(R.string.signed_out)
                binding.tvTwitterName.visibility=View.GONE

            }
            setUpTwitterSwitchBtn()
        }


        return view
    }


    private fun setUpLinkedInSwitchBtn() {

        binding.switchLinkedinSignedIn.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){

                Log.d(SettingsFragment::class.java.simpleName,"is checked")
                if (linkedinUserSignedIn){
                    return@setOnCheckedChangeListener
                }

                Handler(requireActivity().mainLooper)
                        .postDelayed(

                                {
                                    val bundle=Bundle()
                                    bundle.putString(SIGN_IN_WITH, linkedIn)
                                    requireActivity().supportFragmentManager
                                            .beginTransaction()
                                            .setCustomAnimations(
                                                    R.anim.slide_in_right,
                                                    R.anim.slide_out_left,
                                                    R.anim.slide_in_left,
                                                    R.anim.slide_out_right
                                            )
                                            .addToBackStack(null)
                                            .replace(R.id.frame_layout, AuthorizationFragment::class.java, bundle)
                                            .commit()

                                },500
                        )


            }
            else{
                linkedinUserSignedIn=false


                val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(requireContext())

                sharedPreferences.edit().remove(LINKEDIN_ID).apply()

                sharedPreferences.edit().remove(LINKEDIN_ACCESS_TOKEN).apply()

                sharedPreferences.edit().remove(LINKEDIN_EMAIL).apply()

                sharedPreferences.edit().remove(LINKEDIN_USER_NAME).apply()

                sharedPreferences.edit().remove(LINKEDIN_ACCESS_TOKEN_EXPIRY_TIME).apply()

                lifecycleScope.launch(Dispatchers.IO) {
                    requireActivity().dataStore.edit { settings->
                        withContext(Dispatchers.Main){
                            binding.tvLinkedinEmail.visibility=View.GONE

                            binding.tvLinkedinName.visibility=View.GONE
                        }


                        settings.remove(booleanPreferencesKey(LINKEDIN_USER_SET))




                    }
                }


            }

        }
    }

    private fun setUpTwitterSwitchBtn() {
        binding.switchTwitterSignedIn.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked){

                if(twitterUserSignedIn){
                    return@setOnCheckedChangeListener
                }
                val bundle=Bundle()
                bundle.putString(SIGN_IN_WITH, twitter)

                Handler(requireActivity().mainLooper).postDelayed(

                        {
                            requireActivity().supportFragmentManager
                                    .beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left,
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                    )
                                    .addToBackStack(null)
                                    .replace(R.id.frame_layout, AuthorizationFragment::class.java, bundle)
                                    .commit()
                        }
                        ,500

                )

            }
            else{

                val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(requireContext())

                sharedPreferences.edit().remove(TWITTER_SCREEN_NAME).apply()

                sharedPreferences.edit().remove(TWITTER_OAUTH_TOKEN).apply()

                sharedPreferences.edit().remove(TWITTER_OAUTH_TOKEN_SECRET).apply()
                lifecycleScope.launch(Dispatchers.IO) {
                    requireActivity().dataStore.edit { settings->
                        withContext(Dispatchers.Main){
                            binding.tvTwitterName.visibility=View.GONE


                        }

                        settings.remove(booleanPreferencesKey(TWITTER_USER_SET))




                    }
                }


            }

        }
    }

    private fun updateTwitterUserInfo() {
        showLoading()
        lifecycleScope.launch(Dispatchers.Default) {
           val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(requireContext())

           val twitterUserName=sharedPreferences.getString(TWITTER_SCREEN_NAME,"")

            withContext(Dispatchers.Main){

                twitterUserSignedIn=true

                binding.tvTwitterName.isVisible=true

                binding.switchTwitterSignedIn.text=getString(R.string.signed_in)

                binding.switchTwitterSignedIn.isChecked=true

                binding.tvTwitterName.text=getString(R.string.name,twitterUserName)

                hideLoading()
            }



        }
    }

    private fun updateLinkedInUserInfo() {

        showLoading()
        lifecycleScope.launch(Dispatchers.Default) {


            val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(requireContext())

            val linkedInUserName=sharedPreferences.getString(LINKEDIN_USER_NAME,"")

            val linkedInEmail=sharedPreferences.getString(LINKEDIN_EMAIL,"")

            linkedinUserSignedIn=true

            withContext(Dispatchers.Main){
                binding.switchLinkedinSignedIn.text=getString(R.string.signed_in)

                binding.switchLinkedinSignedIn.isChecked=true



                binding.tvLinkedinName.text=getString(R.string.name,linkedInUserName)

                binding.tvLinkedinEmail.visibility=View.VISIBLE
                binding.tvLinkedinName.visibility=View.VISIBLE


                binding.tvLinkedinEmail.text=getString(R.string.email,linkedInEmail)

                //setUpLinkedInSwitchBtn()


                hideLoading()

            }



        }
    }
    fun showLoading(){
        binding.settingsProgressBar.visibility=View.VISIBLE
    }
    fun hideLoading(){
        if(twitterUserInfoSet&&linkedInUserInfoSet){
            binding.settingsProgressBar.visibility=View.GONE
        }

    }

}