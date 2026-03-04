package app.ghostfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ghostfit.data.model.ReferencePhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferencePhotoDao {

    @Query("SELECT * FROM reference_photo WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<ReferencePhoto>>

    @Query("SELECT * FROM reference_photo WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUser(userId: String): List<ReferencePhoto>

    @Query("SELECT COUNT(*) FROM reference_photo WHERE userId = :userId")
    suspend fun countByUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: ReferencePhoto)

    @Delete
    suspend fun delete(photo: ReferencePhoto)

    @Query("DELETE FROM reference_photo WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
