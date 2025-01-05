package com.example.fit_life.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fit_life.R
import com.example.fit_life.data.models.Workout

class WorkoutAdapter(
    private val context: Context,
    private val onItemClick: (Workout) -> Unit,
    private val onEditClick: (Workout) -> Unit,
    private val onDeleteClick: (Workout) -> Unit
) : ListAdapter<Workout, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view, context)
    }

    // Called by RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = getItem(position)
        holder.bind(workout, onItemClick, onEditClick, onDeleteClick)
    }

    class WorkoutViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val typeTextView: TextView = itemView.findViewById(R.id.workoutTypeTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.workoutDurationTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.workoutCaloriesTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.workoutDateTextView)
        private val imageViewWorkout: ImageView = itemView.findViewById(R.id.imageViewWorkout)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        // Binds the data to the views in the ViewHolder.
        fun bind(workout: Workout, onItemClick: (Workout) -> Unit, onEditClick: (Workout) -> Unit, onDeleteClick: (Workout) -> Unit) {
            typeTextView.text = "${getLocalizedString(R.string.type)} ${workout.type}"
            durationTextView.text = "${getLocalizedString(R.string.duration_)} ${workout.duration}"
            caloriesTextView.text = "${getLocalizedString(R.string.calories_burned_)} ${workout.caloriesBurned}"
            dateTextView.text = "${getLocalizedString(R.string.date_)} ${workout.date}"

            if (!workout.imageUri.isNullOrEmpty()) {
                imageViewWorkout.setImageURI(Uri.parse(workout.imageUri))
                imageViewWorkout.visibility = View.VISIBLE
            } else {
                imageViewWorkout.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(workout) }
            editButton.setOnClickListener { onEditClick(workout) }
            deleteButton.setOnClickListener { onDeleteClick(workout) }
        }

        // Retrieves the localized string based on the resource ID.
        private fun getLocalizedString(resId: Int): String {
            return context.getString(resId)
        }
    }
}

class WorkoutDiffCallback : DiffUtil.ItemCallback<Workout>() {
    // Checks if two items have the same ID.
    override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the contents of two items are the same.
    override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
        return oldItem == newItem
    }
}