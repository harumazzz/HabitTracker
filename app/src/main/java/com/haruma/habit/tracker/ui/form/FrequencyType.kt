package com.haruma.habit.tracker.ui.form

import androidx.annotation.StringRes
import com.haruma.habit.tracker.R

enum class FrequencyType(@param:StringRes val titleResId: Int) {
    DAILY(R.string.form_freq_daily),
    WEEKLY(R.string.form_freq_weekly),
    SPECIFIC(R.string.form_freq_specific);

    companion object {
        fun fromString(value: String): FrequencyType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DAILY
    }
}
