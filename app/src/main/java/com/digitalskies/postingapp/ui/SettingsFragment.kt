package com.digitalskies.postingapp.ui

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.work.*
import com.digitalskies.postingapp.*
import com.digitalskies.postingapp.R
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.databinding.FragmentSettingsBinding
import com.digitalskies.postingapp.utils.UploadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.concurrent.TimeUnit


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

        setUpPostingVideos()


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

    private fun setUpPostingVideos() {
        setUpPostingModeSpinner()

        setUpSearchTypeSpinner()

        lifecycleScope.launch {
            val postVideos=activity?.dataStore?.data?.first()?.get(booleanPreferencesKey(POST_VIDEOS))

            val tweetsQuery=activity?.dataStore?.data?.first()?.get(stringPreferencesKey("tweets_query"))

            val interval=activity?.dataStore?.data?.first()?.get(stringPreferencesKey("posting_interval"))

            binding.swPostVideos.isChecked=postVideos?:false

            binding.etQuery.setText(tweetsQuery)

            binding.etInterval.setText(interval)

            binding.etQuery.isEnabled= postVideos != true

            binding.etInterval.isEnabled=postVideos != true
        }

        binding.swPostVideos.setOnCheckedChangeListener { buttonView, isChecked ->


            binding.etQuery.isEnabled=!isChecked
            binding.etInterval.isEnabled=!isChecked
            binding.spinnerMode.isEnabled=!isChecked
            binding.spinnerSearchType.isEnabled=!isChecked
            lifecycleScope.launch {

                val tweetsQuery=binding.etQuery.text

                val interval=binding.etInterval.text
                activity?.dataStore?.edit {
                    it[booleanPreferencesKey(POST_VIDEOS)] = isChecked

                }
                if(!tweetsQuery.isNullOrBlank()){
                    activity?.dataStore?.edit {
                        it[stringPreferencesKey("tweets_query")] = tweetsQuery.toString()

                    }
                }
                if(!interval.isNullOrBlank()){
                    activity?.dataStore?.edit {
                        it[stringPreferencesKey("posting_interval")] = interval.toString()

                    }
                }

                if(isChecked){

                    if(!interval.isNullOrBlank()){
                        setUpUploadWork(interval.toString())
                    }
                    else{
                        setUpUploadWork()
                    }


                }
                else{
                    WorkManager.getInstance(requireActivity()).cancelAllWork()
                }

            }



        }
    }

    private fun setUpPostingModeSpinner() {
        binding.spinnerMode.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    activity?.dataStore?.edit {
                        it[intPreferencesKey("posting_mode")] = position

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

        }
    }

    private fun setUpSearchTypeSpinner() {
        binding.spinnerSearchType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    var searchType:String?=null
                    when (position) {
                        0 -> {
                            searchType="recent"
                        }
                        1 -> {
                            searchType="popular"
                        }
                        2 -> {
                            searchType="mixed"
                        }
                    }
                    activity?.dataStore?.edit {
                        it[stringPreferencesKey("result_type")] = searchType?:"recent"

                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

        }
    }

    private suspend fun setUpUploadWork(interval: String="120") {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val postingMode=activity?.dataStore?.data?.map { it[intPreferencesKey("posting_mode")] }
            ?.first()

        val notificationRequest: WorkRequest = if(postingMode==POSTING_MODE_ONETIME){
            OneTimeWorkRequest.Builder(UploadWorker::class.java)
                .setInitialDelay(Duration.ofMinutes(5))
                .setConstraints(constraints)
                .build()
        } else{
            PeriodicWorkRequest.Builder(UploadWorker::class.java, Duration.ofMinutes(interval.toLong()))
                .setInitialDelay(Duration.ofMinutes(5))
                .setConstraints(constraints)
                .build()
        }


        WorkManager.getInstance(requireActivity()).enqueue(notificationRequest)


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