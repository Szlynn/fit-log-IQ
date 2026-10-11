package io.github.szlynn.fitlogiq.data.db

import androidx.room.TypeConverter
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.data.model.RunType

class Converters {
    @TypeConverter
    fun fromRunType(value: RunType): String = value.name

    @TypeConverter
    fun toRunType(value: String): RunType = RunType.valueOf(value)

    @TypeConverter
    fun fromDayStatus(value: DayStatus): String = value.name

    @TypeConverter
    fun toDayStatus(value: String): DayStatus = DayStatus.valueOf(value)
}
