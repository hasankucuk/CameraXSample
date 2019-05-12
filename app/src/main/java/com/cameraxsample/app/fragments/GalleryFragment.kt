package com.cameraxsample.app.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.cameraxsample.app.R
import com.cameraxsample.app.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File

class GalleryFragment : Fragment() {

    private lateinit var rootDirectory: File
    private lateinit var mediaList: MutableList<File>


    private lateinit var galleryAdapter: GalleryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retainInstance = true


        arguments?.let {
            rootDirectory = File(it.getString(KEY_ROOT_DIRECTORY))


            mediaList = rootDirectory.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
            }.sorted().reversed().toMutableList()

            recyclerGallery.layoutManager = GridLayoutManager(requireContext(), 3)
            galleryAdapter = GalleryAdapter(mediaList) { position ->
                val arguments = Bundle().apply {
                    putInt(SELECTED_POSITION, position)
                    putString(KEY_ROOT_DIRECTORY, rootDirectory.absolutePath)
                }
                Navigation.findNavController(requireActivity(), R.id.fragmentContainer)
                        .navigate(R.id.action_gallery_to_photo_pager, arguments)
            }
            recyclerGallery.adapter = galleryAdapter


        }

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            fragmentManager?.popBackStack()
        }

    }
}
