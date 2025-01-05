package com.example.fit_life.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fit_life.data.models.Gym

class GymListViewModel : ViewModel() {

    // LiveData to hold the list of gyms
    private val _gyms = MutableLiveData<List<Gym>>()
    val gyms: LiveData<List<Gym>> get() = _gyms

    // Function to update the list of gyms
    fun setGyms(gyms: List<Gym>) {
        // Update the LiveData only if the new list is different from the current list
        if (_gyms.value != gyms) {
            _gyms.value = gyms
        }
    }
}
