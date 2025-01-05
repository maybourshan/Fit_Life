package com.example.fit_life.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fit_life.data.models.Gym
import com.example.fit_life.databinding.ItemGymBinding

class GymAdapter(private var gyms: List<Gym>) : RecyclerView.Adapter<GymAdapter.GymViewHolder>() {

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GymViewHolder {
        val binding = ItemGymBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GymViewHolder(binding)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: GymViewHolder, position: Int) {
        holder.bind(gyms[position])
    }

    // Returns the total number of items in the data set held by the adapter
    override fun getItemCount(): Int = gyms.size

    // Updates the list of gyms and notifies the adapter to refresh the views
    fun updateGyms(newGyms: List<Gym>) {
        gyms = newGyms
        notifyDataSetChanged()
    }

    // ViewHolder class to hold and bind the views for each gym item
    class GymViewHolder(private val binding: ItemGymBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(gym: Gym) {
            binding.gymName.text = gym.name          // Bind gym name to TextView
            binding.gymAddress.text = gym.address    // Bind gym address to TextView
        }
    }
}