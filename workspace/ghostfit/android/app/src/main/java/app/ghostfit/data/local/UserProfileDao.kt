package app.ghostfit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET overlayPositionX = :x, overlayPositionY = :y")
    suspend fun updateOverlayPosition(x: Float, y: Float)

    @Query("UPDATE user_profile SET dailyTriesUsed = :tries, dailyTriesResetDate = :resetDate")
    suspend fun updateDailyTries(tries: Int, resetDate: String)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
