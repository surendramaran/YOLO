package com.surendramaran.yolov8imageclassification.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8imageclassification.Prediction
import com.surendramaran.yolov8imageclassification.databinding.ItemPredicationBinding

@SuppressLint("NotifyDataSetChanged")
class PredicationAdapter : RecyclerView.Adapter<PredicationAdapter.ViewHolder>(){

    private var predictions: List<Prediction> = emptyList()

    override fun getItemCount() = predictions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(predictions[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    fun setData(newData: List<Prediction>) {
        predictions = newData
        notifyDataSetChanged()
    }

    fun clear() {
        predictions = emptyList()
        notifyDataSetChanged()
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