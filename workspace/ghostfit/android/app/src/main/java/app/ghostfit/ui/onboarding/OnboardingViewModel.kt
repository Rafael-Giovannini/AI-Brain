package app.ghostfit.ui.onboarding

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.local.ReferencePhotoDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.ReferencePhoto
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userProfileDao: UserProfileDao,
    private val referencePhotoDao: ReferencePhotoDao,
    private val photoStorage: PhotoStorage
) : ViewModel() {

    fun grantLgpdConsent(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = userProfileDao.getProfile()
            if (existing != null) {
                userProfileDao.updateProfile(
                    existing.copy(
                        lgpdConsentGranted = true,
                        lgpdConsentTimestamp = System.currentTimeMillis()
                    )
                )
            } else {
                userProfileDao.insertProfile(
                    UserProfile(
                        lgpdConsentGranted = true,
                        lgpdConsentTimestamp = System.currentTimeMillis()
                    )
                )
            }
            launch(Dispatchers.Main) { onDone() }
        }
    }

    fun savePhotosAndCompleteOnboarding(bitmaps: List<Bitmap>, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = userProfileDao.getProfile() ?: return@launch
            // Encrypt all photos in parallel for speed
            val results = bitmaps.mapIndexed { index, bitmap ->
                async {
                    val fileName = "ref_${System.currentTimeMillis()}_$index.enc"
                    photoStorage.encrypt(bitmap, fileName)
                }
            }.awaitAll()

            results.filterNotNull().forEach { path ->
                // TODO: FR-001/US1-6 — substituir por validação real de corpo inteiro
                referencePhotoDao.insert(
                    ReferencePhoto(
                        userId = profile.id,
                        encryptedFilePath = path,
                        isBodyFullVisible = true // placeholder até validação real
                    )
                )
            }
            userProfileDao.updateProfile(
                profile.copy(onboardingCompleted = true)
            )
            launch(Dispatchers.Main) { onDone() }
        }
    }

    class Factory(
        private val userProfileDao: UserProfileDao,
        private val referencePhotoDao: ReferencePhotoDao,
        private val photoStorage: PhotoStorage
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(userProfileDao, referencePhotoDao, photoStorage) as T
        }
    }
}
