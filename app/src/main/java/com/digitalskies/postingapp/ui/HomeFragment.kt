package com.digitalskies.postingapp.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.digitalskies.postingapp.R
import com.digitalskies.postingapp.databinding.FragmentHomeBinding
import com.digitalskies.postingapp.utils.EventObserver
import com.digitalskies.postingapp.utils.FilePath
import com.digitalskies.postingapp.utils.OnEventChanged
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class HomeFragment:Fragment(){
    lateinit var fragmentHomeBinding: FragmentHomeBinding
    lateinit var mainActivityViewModel: MainActivityViewModel

    var fileUri:Uri?=null

    lateinit var file: File

    lateinit var fileSize:String

    var fileName:String?=null

    lateinit var fileType:String

    var characterCount:Int=0

    var transaction: FragmentTransaction?=null

    var linkedInUserSet=false

    var twitterUserSet=false

    var job: Job?=null

    private val REQUEST_PERMISSIONS =1

    private var mediaAttached=false

    private val UPLOAD_REQUEST = 2

    private val LOGIN = 2

    private var toPostOnTwitter=false

    private var toPostOnLinkedIn=false

    private var toPostOnFacebook=false

    private var toPostOnWhatsapp=false



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        fragmentHomeBinding= FragmentHomeBinding.inflate(
                requireActivity().layoutInflater,
                container,
                false
        )

        val view=fragmentHomeBinding.root

        mainActivityViewModel=ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)





        setUpPostBtn()

        setUpAttachMediaBtn()

        setUpCheckBoxes()

        setUpSettingsBtn()




        fileName?.let { fragmentHomeBinding.tvFileName.text=it }

        if(arguments!=null){
            if(arguments?.containsKey(MEDIA_URI)==true){

                mediaAttached=true


                fileUri= Uri.parse(arguments?.getString(MEDIA_URI))

                fileUri?.let { fileType= requireActivity().contentResolver.getType(it).toString() }



                getFileName()

            }
            else if(arguments?.containsKey(LINK)==true){

                fragmentHomeBinding.etMessage.setText(arguments?.getString(LINK))

                fragmentHomeBinding.tvCharacterCount.text=getString(R.string.characters,arguments?.getString(LINK)?.length)
            }
        }


        mainActivityViewModel.linkedInUserIsSet.asLiveData().observe(viewLifecycleOwner){linkedInUserSet->

           this.linkedInUserSet=linkedInUserSet
        }

        mainActivityViewModel.twitterUserIsSet.asLiveData().observe(viewLifecycleOwner){twitterUserSet->

            this.twitterUserSet=twitterUserSet
        }

        mainActivityViewModel.postingComplete.observe(viewLifecycleOwner,EventObserver{

            if(it==true){
             clearEntries()

            }


        })

        fragmentHomeBinding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                fragmentHomeBinding.tvCharacterCount.text = getString(R.string.characters,
                    s.toString().length)
            }

            override fun afterTextChanged(s: Editable?) {
                /* no-op */
            }


        })









        return view
    }

    private fun clearEntries() {
       fragmentHomeBinding.checkBoxWhatsapp.isChecked=false

       fragmentHomeBinding.checkBoxFacebook.isChecked=false

        fragmentHomeBinding.checkBoxTwitter.isChecked=false

        fragmentHomeBinding.checkBoxLinkedin.isChecked=false

        fragmentHomeBinding.etMessage.setText("")

        mediaAttached=false

        fileUri=null

        fragmentHomeBinding.tvFileName.isVisible=false

        toPostOnTwitter=false

        toPostOnFacebook=false

        toPostOnLinkedIn=false

        toPostOnWhatsapp=false

    }


    private fun setUpPostBtn() {
        fragmentHomeBinding.btnPost.setOnClickListener {

            if(fragmentHomeBinding.etMessage.text.isEmpty()){

                if(!mediaAttached){
                    return@setOnClickListener
                }



            }


            val bundle=Bundle()



            bundle.putString(MESSAGE, fragmentHomeBinding.etMessage.text.toString())


            val links=extractUrls(fragmentHomeBinding.etMessage.text.toString())

            if(links.isNotEmpty()){
                val link=links[0]

                bundle.putString(LINK,link)

            }


            bundle.putBoolean(POST_ON_TWITTER,toPostOnTwitter)

            bundle.putBoolean(POST_ON_FACEBOOK,toPostOnFacebook)

            bundle.putBoolean(POST_ON_LINKEDIN,toPostOnLinkedIn)

            bundle.putBoolean(POST_ON_WHATSAPP,toPostOnWhatsapp)

            if(mediaAttached){
                bundle.putString(MEDIA_URI,fileUri.toString())

                bundle.putString(MEDIA_TYPE,fileType)
            }

            transaction=null

           transaction= requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )



          transaction?.addToBackStack(null)
              ?.replace(R.id.frame_layout,PostingFragment::class.java,bundle,PostingFragment::class.java.simpleName)
              ?.commit()


        }
    }

    private fun setUpAttachMediaBtn() {
        fragmentHomeBinding.btnAttchImageOrVideo.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSIONS
                )

            }
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*")
                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Select picture"), UPLOAD_REQUEST)
        }
    }
    private fun setUpCheckBoxes() {
        fragmentHomeBinding.checkBoxTwitter.setOnCheckedChangeListener { buttonView, isChecked ->
            toPostOnTwitter=isChecked
            if(isChecked){

                       if(!twitterUserSet){
                           fragmentHomeBinding.checkBoxTwitter.isChecked=false
                           toPostOnTwitter=false
                           showDialog(
                                   getString(R.string.not_logged_in,"Twitter"),
                                   getString(R.string.login_now,"Twitter"),
                                   twitter
                           )


                       }


            }
        }
        fragmentHomeBinding.checkBoxLinkedin.setOnCheckedChangeListener { buttonView, isChecked ->
            toPostOnLinkedIn=isChecked
            if(isChecked){

                        if(!linkedInUserSet){
                            fragmentHomeBinding.checkBoxLinkedin.isChecked=false
                            toPostOnLinkedIn=false
                            showDialog(
                                    getString(R.string.not_logged_in,"LinkedIn"),
                                    getString(R.string.login_now,"LinkedIn"),
                                    linkedIn
                            )


                        }
                        else{
                            lifecycleScope.launch{
                                mainActivityViewModel.linkedInCredentialsExpired.collect{linkedInCredentialsExpired->
                                    linkedInCredentialsExpired?.let {
                                        if(linkedInCredentialsExpired){
                                            fragmentHomeBinding.checkBoxLinkedin.isChecked=false
                                            toPostOnLinkedIn=false
                                            showDialog(
                                                    getString(R.string.linkedin_credentials_expired),
                                                    getString(R.string.linked_credentials_expired_login_again),
                                                    linkedIn
                                            )
                                        }
                                    }


                                }
                            }

                        }


            }
        }
        fragmentHomeBinding.checkBoxFacebook.setOnCheckedChangeListener { buttonView, isChecked ->
            toPostOnFacebook=isChecked
            if(isChecked){
                if(!mediaAttached){

                    if(extractUrls(fragmentHomeBinding.etMessage.text.toString()).isEmpty()){
                        fragmentHomeBinding.checkBoxFacebook.isChecked=false

                        toPostOnFacebook=false

                        showDialog(
                                getString(R.string.attach_media_or_link),
                                getString(R.string.facebook_requires_media_or_link_included)
                        )
                    }

                }
            }
        }
        fragmentHomeBinding.checkBoxWhatsapp.setOnCheckedChangeListener { buttonView, isChecked ->

            toPostOnWhatsapp=isChecked
        }
    }

    private fun setUpSettingsBtn() {
        fragmentHomeBinding.ivSettings.setOnClickListener {

            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .addToBackStack(null)
                    .replace(R.id.frame_layout,SettingsFragment(),SettingsFragment::class.java.simpleName)
                    .commit()
        }


    }
    fun showDialog(title:String,message:String,loginWith:String?=null){

        val alertDialog=AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok
                ) { dialog, _ ->
                    dialog.dismiss()
                    if (loginWith!=null){
                        val bundle=Bundle()
                        bundle.putString(SIGN_IN_WITH, loginWith)
                        requireActivity().supportFragmentManager
                                .beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.frame_layout, AuthorizationFragment::class.java, bundle)
                                .commit()
                    }



                }

                if(loginWith!=null){
                   alertDialog
                           .setNegativeButton(android.R.string.cancel){ dialog, _ ->

                        dialog.dismiss()
                }



                }


                alertDialog.show()

    }



    fun extractUrls(input: String): List<String> {
        val urls: MutableList<String> = ArrayList()
        val words = input.split("\\s+").toTypedArray()
        var url:String


        val pattern: Pattern = Patterns.WEB_URL
        for (word in words) {
            if (pattern.matcher(word).find()) {
                url = if (!word.toLowerCase().contains("http://") && !word.toLowerCase().contains("https://")) {
                    "http://$word"

                } else{
                    word
                }


                urls.add(url)
            }
        }

        return urls
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPLOAD_REQUEST && resultCode == RESULT_OK) {
            Log.d(HomeFragment::class.java.simpleName, data?.data?.path.toString())
            data?.data?.let { uri ->

                fileUri = uri

                file = File(FilePath.getPath(requireActivity(), uri))



                Log.d(HomeFragment::class.java.simpleName, "file size is  ${file.length()}")

                Log.d(
                        HomeFragment::class.java.simpleName, "file path is ${
                    FilePath.getPath(
                            requireContext(),
                            uri
                    )
                }"
                )
                val contentResolver = requireContext().contentResolver




                val mimeType = contentResolver.getType(uri)

                if (mimeType?.startsWith("image") == false) {

                    if (!mimeType.startsWith("video")) {
                        Toast.makeText(
                                requireContext(),
                                "Attach an image or video",
                                Toast.LENGTH_SHORT
                        ).show()

                        return
                    }


                }
                fileType = mimeType!!

                mediaAttached = true

                getFileName()


            }


        } else if (requestCode == LOGIN && resultCode == RESULT_OK) {
            Log.d(HomeFragment::class.java.simpleName, "success")
        }


    }

    fun getFileName(){
        val contentResolver = requireContext().contentResolver


        val cursor = contentResolver.query(fileUri!!, null, null, null, null)


        cursor?.let {
            try {
                if (cursor.moveToFirst()) {
                    val fileSizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    val fileNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                    fileSize = cursor.getLong(fileSizeIndex).toString()

                  fileName = cursor.getString(fileNameIndex)



                    fragmentHomeBinding.tvFileName.text = getString(
                            R.string.file_name,
                            fileName
                    )

                    fragmentHomeBinding.tvFileName.visibility = View.VISIBLE

                    mediaAttached = true



                    Log.d(HomeFragment::class.java.simpleName, "$fileSize  and $fileName")
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }


        }

        cursor?.close()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
}