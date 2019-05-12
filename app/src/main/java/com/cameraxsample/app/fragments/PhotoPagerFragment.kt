package com.cameraxsample.app.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cameraxsample.app.BuildConfig

import com.cameraxsample.app.R
import com.cameraxsample.app.utils.padWithDisplayCutout
import com.cameraxsample.app.utils.showImmersive
import java.io.File


const val KEY_ROOT_DIRECTORY = "root_folder"
const val SELECTED_POSITION = "selected_position"
val EXTENSION_WHITELIST = arrayOf("JPG")

class PhotoPagerFragment internal constructor() : Fragment() {
    private var position: Int = 0
    private lateinit var rootDirectory: File
    private lateinit var mediaList: MutableList<File>
    private lateinit var mediaViewPager: ViewPager

    inner class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retainInstance = true

        arguments?.let {
            position = it.getInt(SELECTED_POSITION)
            rootDirectory = File(it.getString(KEY_ROOT_DIRECTORY))

            mediaList = rootDirectory.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
            }.sorted().reversed().toMutableList()

            mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
                offscreenPageLimit = 2
                adapter = MediaPagerAdapter(childFragmentManager)
            }
            mediaViewPager.currentItem = position
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            fragmentManager?.popBackStack()
        }

        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {
            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->
                val appContext = requireContext().applicationContext

                val intent = Intent().apply {
                    val mediaType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(mediaFile.extension)
                    val uri = FileProvider.getUriForFile(
                            appContext, BuildConfig.APPLICATION_ID + ".provider", mediaFile)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mediaType
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }

        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {
            val context = requireContext()
            AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_dialog))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                            mediaFile.delete()

                            mediaList.removeAt(mediaViewPager.currentItem)
                            mediaViewPager.adapter?.notifyDataSetChanged()

                            if (mediaList.isEmpty()) {
                                fragmentManager?.popBackStack()
                            }
                        }
                    }

                    .setNegativeButton(android.R.string.no, null)
                    .create().showImmersive()
        }
    }
}