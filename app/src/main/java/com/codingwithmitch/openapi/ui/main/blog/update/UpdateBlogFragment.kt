package com.codingwithmitch.openapi.ui.main.blog.update

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.main.blog.BaseBlogFragment
import com.codingwithmitch.openapi.util.*
import com.codingwithmitch.openapi.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.SOMETHING_WRONG_WITH_IMAGE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_update_blog.*
import javax.inject.Inject

class UpdateBlogFragment : BaseBlogFragment(R.layout.fragment_update_blog)
{

    @Inject
    lateinit var options: RequestOptions

    private val viewModel: UpdateBlogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()

        image_container.setOnClickListener {
            if(uiCommunicationListener.isStoragePermissionGranted()){
                pickFromGallery()
            }
        }

        viewModel.state.value?.let { state ->
            state.blogPost?.let { blogPost ->
                setBlogProperties(
                    blogPost.title,
                    blogPost.body,
                    blogPost.image.toUri()
                )
            }
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri){
        context?.let{
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showImageSelectionError(){
        viewModel.onTriggerEvent(UpdateBlogEvents.Error(
            stateMessage = StateMessage(
                response = Response(
                    message = SOMETHING_WRONG_WITH_IMAGE,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        ))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let{
                            launchImageCrop(uri)
                        }
                    }?: showImageSelectionError()
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: ${resultUri}")
                    viewModel.onTriggerEvent(UpdateBlogEvents.OnUpdateUri(resultUri))
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Log.d(TAG, "CROP: ERROR")
                    showImageSelectionError()
                }
            }
        }
    }

    fun subscribeObservers(){
        // TODO("Listen for if the BlogPost was updated. Then popBackStack()")
    }

    fun setBlogProperties(title: String?, body: String?, image: Uri?){
        image?.let {
            Glide.with(this)
                .setDefaultRequestOptions(options)
                .load(it)
                .into(blog_image)
        }
        blog_title.setText(title)
        blog_body.setText(body)
    }

    private fun saveChanges(){
        viewModel.onTriggerEvent(UpdateBlogEvents.Update)
        uiCommunicationListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // save any changes before rotate / go to background
    override fun onPause() {
        super.onPause()
        viewModel.onTriggerEvent(UpdateBlogEvents.OnUpdateTitle(blog_title.text.toString()))
        viewModel.onTriggerEvent(UpdateBlogEvents.OnUpdateBody(blog_body.text.toString()))
    }
}










