package app.ghostfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class PlanType { FREE, PREMIUM }

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val lgpdConsentGranted: Boolean = false,
    val lgpdConsentTimestamp: Long? = null,
    val onboardingCompleted: Boolean = false,
    val planType: PlanType = PlanType.FREE,
    val dailyTriesUsed: Int = 0,
    val dailyTriesResetDate: String? = null,
    val bonusTries: Int = 0,
    val overlayPositionX: Float = 0f,
    val overlayPositionY: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)
