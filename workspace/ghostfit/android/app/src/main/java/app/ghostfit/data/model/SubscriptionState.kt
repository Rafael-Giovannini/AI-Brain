package app.ghostfit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PurchaseType { NONE, PACK, MONTHLY }

@Entity(
    tableName = "subscription_state",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SubscriptionState(
    @PrimaryKey
    val userId: String,
    val purchaseToken: String? = null,
    val productId: String? = null,
    val purchaseType: PurchaseType = PurchaseType.NONE,
    val isActive: Boolean = false,
    val expiresAt: Long? = null,
    val acknowledgedAt: Long? = null
)
