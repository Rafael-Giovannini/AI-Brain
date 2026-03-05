package app.ghostfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.ghostfit.data.model.ReferencePhoto
import app.ghostfit.data.model.SubscriptionState
import app.ghostfit.data.model.UserProfile

@Database(
    entities = [UserProfile::class, ReferencePhoto::class, SubscriptionState::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun referencePhotoDao(): ReferencePhotoDao
    abstract fun subscriptionStateDao(): SubscriptionStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghostfit.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
