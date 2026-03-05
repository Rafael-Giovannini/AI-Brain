package app.ghostfit

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.model.ReferencePhoto
import app.ghostfit.data.model.UserProfile
import app.ghostfit.ui.onboarding.LgpdConsentScreen
import app.ghostfit.ui.onboarding.PermissionScreen
import app.ghostfit.ui.onboarding.PhotoSelectScreen
import app.ghostfit.ui.onboarding.WelcomeScreen
import app.ghostfit.ui.theme.GhostFitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val userProfileDao = db.userProfileDao()
        val referencePhotoDao = db.referencePhotoDao()
        val photoStorage = PhotoStorage.getInstance(this)

        setContent {
            GhostFitTheme {
                val navController = rememberNavController()

                // Overlay permission state — re-checked on every ON_RESUME
                var hasOverlayPermission by rememberSaveable {
                    mutableStateOf(Settings.canDrawOverlays(this@MainActivity))
                }
                var hasPhotosPermission by rememberSaveable { mutableStateOf(false) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasOverlayPermission = Settings.canDrawOverlays(this@MainActivity)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                NavHost(navController = navController, startDestination = "welcome") {

                    composable("welcome") {
                        WelcomeScreen(
                            onGetStarted = {
                                navController.navigate("permissions") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("permissions") {
                        PermissionScreen(
                            hasOverlayPermission = hasOverlayPermission,
                            hasPhotosPermission = hasPhotosPermission,
                            onOverlayPermissionRequest = { },
                            onPhotosPermissionGranted = { hasPhotosPermission = true },
                            onAllPermissionsGranted = {
                                navController.navigate("lgpd") {
                                    popUpTo("permissions") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("lgpd") {
                        LgpdConsentScreen(
                            onConsentGranted = {
                                CoroutineScope(Dispatchers.IO).launch {
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
                                }
                                navController.navigate("photoSelect") {
                                    popUpTo("lgpd") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("photoSelect") {
                        PhotoSelectScreen(
                            onPhotosSelected = { bitmaps ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val profile = userProfileDao.getProfile() ?: return@launch
                                    bitmaps.forEach { bitmap ->
                                        val fileName = "ref_${System.currentTimeMillis()}.enc"
                                        val path = photoStorage.encrypt(bitmap, fileName)
                                        if (path != null) {
                                            referencePhotoDao.insert(
                                                ReferencePhoto(
                                                    userId = profile.id,
                                                    encryptedFilePath = path,
                                                    isBodyFullVisible = true
                                                )
                                            )
                                        }
                                    }
                                    userProfileDao.updateProfile(
                                        profile.copy(onboardingCompleted = true)
                                    )
                                }
                                // TODO(Phase-2): Navigate to main app / overlay setup
                            }
                        )
                    }
                }
            }
        }
    }
}
