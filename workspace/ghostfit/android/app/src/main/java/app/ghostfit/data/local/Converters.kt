package app.ghostfit.data.local

import androidx.room.TypeConverter
import app.ghostfit.data.model.PlanType

class Converters {

    @TypeConverter
    fun fromPlanType(value: PlanType): String = value.name

    @TypeConverter
    fun toPlanType(value: String): PlanType = PlanType.valueOf(value)
}
