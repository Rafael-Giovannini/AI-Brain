package app.ghostfit

import android.app.Application
import com.google.crypto.tink.aead.AeadConfig

class GhostFitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AeadConfig.register()
    }
}
