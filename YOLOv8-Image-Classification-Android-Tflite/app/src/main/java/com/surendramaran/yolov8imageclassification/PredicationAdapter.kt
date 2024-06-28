package com.surendramaran.yolov8imageclassification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8imageclassification.databinding.ItemPredicationBinding

class PredicationAdapter : ListAdapter<Prediction, PredicationAdapter.ViewHolder>(DiffCallBack()){

    class DiffCallBack : DiffUtil.ItemCallback<Prediction>() {
        override fun areItemsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ItemPredicationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Prediction, position: Int) {
            binding.prediction = item
            binding.position = position
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPredicationBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}