package com.surendramaran.yolov8_instancesegmentation.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8_instancesegmentation.databinding.ItemImageBinding

class ViewPagerAdapter(private val images: MutableList<Pair<Bitmap, Bitmap?>>) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(layoutInflater, parent, false)
        return ViewPagerHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ViewPagerHolder(private var itemImageBinding: ItemImageBinding) :
        RecyclerView.ViewHolder(itemImageBinding.root) {
        fun bind(images: Pair<Bitmap, Bitmap?>) {
            itemImageBinding.image.setImageBitmap(images.first)
            itemImageBinding.overlay.setImageBitmap(images.second)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateImages(newImages: List<Pair<Bitmap, Bitmap?>>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }
}