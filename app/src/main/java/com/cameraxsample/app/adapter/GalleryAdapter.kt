package com.cameraxsample.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cameraxsample.app.R
import kotlinx.android.synthetic.main.row_gallery.view.*
import java.io.File

class GalleryAdapter(val mediaList: MutableList<File>, val onItemClick: ((Int) -> Unit)) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_gallery, parent, false)
        return GalleryViewHolder(v)

    }

    override fun getItemCount(): Int {
        return mediaList.size
    }


    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = mediaList[position]

        Glide.with(holder.itemView.ivGalleryImage.context).load(item).into(holder.itemView.ivGalleryImage)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(position)
        }

    }

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}