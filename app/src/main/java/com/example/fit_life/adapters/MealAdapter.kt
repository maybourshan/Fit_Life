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
import com.example.fit_life.data.models.Meal

class MealAdapter(
    private val context: Context,
    private val onItemClick: (Meal) -> Unit,
    private val onEditClick: (Meal) -> Unit,
    private val onDeleteClick: (Meal) -> Unit
) : ListAdapter<Meal, MealAdapter.MealViewHolder>(MealDiffCallback()) {

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view, context)
    }

    // Called by RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = getItem(position)
        holder.bind(meal, onItemClick, onEditClick, onDeleteClick)
    }

    class MealViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.mealNameTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.mealCaloriesTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.mealTimeTextView)
        private val imageViewMeal: ImageView = itemView.findViewById(R.id.imageViewMeal)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        // Binds the data to the views in the ViewHolder.
        fun bind(meal: Meal, onItemClick: (Meal) -> Unit, onEditClick: (Meal) -> Unit, onDeleteClick: (Meal) -> Unit) {
            nameTextView.text = "${getLocalizedString(R.string.name)} ${meal.name}"
            caloriesTextView.text = "${getLocalizedString(R.string.calories_)} ${meal.calories}"
            dateTextView.text = "${getLocalizedString(R.string.date_)} ${meal.date}"

            if (!meal.imageUri.isNullOrEmpty()) {
                imageViewMeal.setImageURI(Uri.parse(meal.imageUri))
                imageViewMeal.visibility = View.VISIBLE
            } else {
                imageViewMeal.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(meal) }
            editButton.setOnClickListener { onEditClick(meal) }
            deleteButton.setOnClickListener { onDeleteClick(meal) }
        }

        // Retrieves the localized string based on the resource ID.
        private fun getLocalizedString(resId: Int): String {
            return context.getString(resId)
        }
    }
}

class MealDiffCallback : DiffUtil.ItemCallback<Meal>() {
    // Checks if two items have the same ID.
    override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the contents of two items are the same.
    override fun areContentsTheSame(oldItem: Meal, newItem: Meal): Boolean {
        return oldItem == newItem
    }
}