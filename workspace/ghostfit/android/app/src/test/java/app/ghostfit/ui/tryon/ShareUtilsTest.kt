package app.ghostfit.ui.tryon

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareUtilsTest {

    private val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    @Test
    fun `shareTryOnImage creates Intent with ACTION_SEND`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shareTryOnImage(app, testBitmap)

        val shadow = Shadows.shadowOf(app)
        val chooserIntent = shadow.nextStartedActivity
        assertNotNull("Expected an activity to be started", chooserIntent)

        val shareIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull("Expected EXTRA_INTENT in chooser", shareIntent)
        assertEquals(Intent.ACTION_SEND, shareIntent!!.action)
    }

    @Test
    fun `shareTryOnImage includes GhostFit branding text`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shareTryOnImage(app, testBitmap)

        val shadow = Shadows.shadowOf(app)
        val chooserIntent = shadow.nextStartedActivity
        assertNotNull(chooserIntent)

        val shareIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        val text = shareIntent?.getStringExtra(Intent.EXTRA_TEXT)
        assertNotNull("Expected branding text", text)
        assertTrue("Expected GhostFit in branding text", text!!.contains("GhostFit"))
    }

    @Test
    fun `shareTryOnImage sets image_jpeg MIME type`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shareTryOnImage(app, testBitmap)

        val shadow = Shadows.shadowOf(app)
        val chooserIntent = shadow.nextStartedActivity
        assertNotNull(chooserIntent)

        val shareIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals("image/jpeg", shareIntent?.type)
    }

    @Test
    fun `shareTryOnImage handles null context gracefully`() {
        val mockContext = mock<Context>()
        whenever(mockContext.cacheDir).thenThrow(RuntimeException("context unavailable"))

        // Should not throw — function catches all exceptions
        shareTryOnImage(mockContext, testBitmap)
    }
}
