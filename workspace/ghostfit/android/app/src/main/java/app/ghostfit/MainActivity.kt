package app.ghostfit

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.domain.BillingManager
import app.ghostfit.overlay.MediaProjectionHolder
import app.ghostfit.overlay.OverlayService
import app.ghostfit.ui.onboarding.LgpdConsentScreen
import app.ghostfit.ui.onboarding.OnboardingViewModel
import app.ghostfit.ui.onboarding.PermissionScreen
import app.ghostfit.ui.onboarding.PhotoSelectScreen
import app.ghostfit.ui.onboarding.WelcomeScreen
import app.ghostfit.ui.subscription.UpgradeScreen
import app.ghostfit.ui.theme.GhostFitTheme
import com.android.billingclient.api.BillingClient

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            MediaProjectionHolder.store(result.resultCode, result.data!!)
        }
        // Start overlay regardless — it works without projection, just can't capture
        OverlayService.start(this)
    }

    private fun requestMediaProjectionAndStartOverlay() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val userProfileDao = db.userProfileDao()
        val referencePhotoDao = db.referencePhotoDao()
        val subscriptionStateDao = db.subscriptionStateDao()
        val photoStorage = PhotoStorage.getInstance(this)

        val billingClient = BillingClient.newBuilder(this)
            .setListener { result, purchases -> billingManager.onPurchasesUpdated(result, purchases) }
            .enablePendingPurchases()
            .build()
        billingManager = BillingManager(billingClient, subscriptionStateDao, userProfileDao)
        billingManager.connect()

        setContent {
            GhostFitTheme {
                val navController = rememberNavController()
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.Factory(
                        userProfileDao, referencePhotoDao, photoStorage
                    )
                )

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
                                onboardingViewModel.grantLgpdConsent {
                                    navController.navigate("photoSelect") {
                                        popUpTo("lgpd") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("photoSelect") {
                        PhotoSelectScreen(
                            onPhotosSelected = { bitmaps ->
                                onboardingViewModel.savePhotosAndCompleteOnboarding(bitmaps) {
                                    // Request MediaProjection, then start overlay
                                    requestMediaProjectionAndStartOverlay()
                                }
                            }
                        )
                    }

                    composable("upgrade") {
                        UpgradeScreen(
                            productDetailsFlow = billingManager.productDetails,
                            purchaseEventFlow = billingManager.purchaseEvent,
                            onPurchase = { productId ->
                                billingManager.launchPurchaseFlow(this@MainActivity, productId)
                            },
                            onPurchaseEventConsumed = { billingManager.consumePurchaseEvent() },
                            onClose = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
