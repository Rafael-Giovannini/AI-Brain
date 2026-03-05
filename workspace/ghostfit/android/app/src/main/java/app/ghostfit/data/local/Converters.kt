package app.ghostfit.data.local

import androidx.room.TypeConverter
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.PurchaseType

class Converters {

    @TypeConverter
    fun fromPlanType(value: PlanType): String = value.name

    @TypeConverter
    fun toPlanType(value: String): PlanType = PlanType.valueOf(value)

    @TypeConverter
    fun fromPurchaseType(value: PurchaseType): String = value.name

    @TypeConverter
    fun toPurchaseType(value: String): PurchaseType = PurchaseType.valueOf(value)
}
