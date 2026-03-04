package app.ghostfit

import android.app.Application
import com.google.crypto.tink.TinkConfig

class GhostFitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TinkConfig.register()
    }
}
