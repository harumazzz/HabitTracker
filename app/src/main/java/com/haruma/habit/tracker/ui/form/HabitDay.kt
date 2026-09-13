package com.haruma.habit.tracker.ui.form

import androidx.annotation.StringRes
import com.haruma.habit.tracker.R
import java.time.DayOfWeek

enum class HabitDay(
    val dayOfWeek: DayOfWeek,
    val code: String,
    @StringRes val stringResId: Int,
    @StringRes val labelResId: Int,
) {
    MONDAY(DayOfWeek.MONDAY, "MON", R.string.day_mon_short, R.string.day_mon_label),
    TUESDAY(DayOfWeek.TUESDAY, "TUE", R.string.day_tue_short, R.string.day_tue_label),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "WED", R.string.day_wed_short, R.string.day_wed_label),
    THURSDAY(DayOfWeek.THURSDAY, "THU", R.string.day_thu_short, R.string.day_thu_label),
    FRIDAY(DayOfWeek.FRIDAY, "FRI", R.string.day_fri_short, R.string.day_fri_label),
    SATURDAY(DayOfWeek.SATURDAY, "SAT", R.string.day_sat_short, R.string.day_sat_label),
    SUNDAY(DayOfWeek.SUNDAY, "SUN", R.string.day_sun_short, R.string.day_sun_label);

    companion object {
        fun fromCode(code: String): HabitDay? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        fun fromDayOfWeek(dayOfWeek: DayOfWeek): HabitDay =
            entries.first { it.dayOfWeek == dayOfWeek }
    }
}
