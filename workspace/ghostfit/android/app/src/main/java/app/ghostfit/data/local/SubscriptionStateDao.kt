package app.ghostfit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ghostfit.data.model.SubscriptionState
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionStateDao {

    @Query("SELECT * FROM subscription_state WHERE userId = :userId LIMIT 1")
    suspend fun getByUser(userId: String): SubscriptionState?

    @Query("SELECT * FROM subscription_state WHERE userId = :userId LIMIT 1")
    fun observeByUser(userId: String): Flow<SubscriptionState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SubscriptionState)

    @Query("DELETE FROM subscription_state WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}
