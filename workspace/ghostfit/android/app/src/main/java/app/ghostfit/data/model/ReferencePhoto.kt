package app.ghostfit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reference_photo",
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
data class ReferencePhoto(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val encryptedFilePath: String,
    val displayName: String? = null,
    val isBodyFullVisible: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
