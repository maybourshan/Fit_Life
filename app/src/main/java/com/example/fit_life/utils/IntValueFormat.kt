package com.example.fit_life.utils

import com.github.mikephil.charting.formatter.ValueFormatter

class IntValueFormatter : ValueFormatter() {
    // Formats the float value to an integer string.
    override fun getFormattedValue(value: Float): String {
        return value.toInt().toString()
    }
}