package app.ghostfit.overlay

import android.app.Activity
import android.content.Intent
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for MediaProjectionHolder — the singleton that bridges
 * MediaProjection permission results between MainActivity and TryOnActivity.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MediaProjectionHolderTest {

    @After
    fun tearDown() {
        MediaProjectionHolder.clear()
    }

    @Test
    fun `isAvailable returns false when nothing stored`() {
        assertFalse(MediaProjectionHolder.isAvailable)
    }

    @Test
    fun `get returns null when nothing stored`() {
        assertNull(MediaProjectionHolder.get())
    }

    @Test
    fun `store makes isAvailable return true`() {
        val intent = Intent()
        MediaProjectionHolder.store(Activity.RESULT_OK, intent)

        assertTrue(MediaProjectionHolder.isAvailable)
    }

    @Test
    fun `get returns stored resultCode and data`() {
        val intent = Intent().apply { putExtra("test_key", "test_value") }
        MediaProjectionHolder.store(Activity.RESULT_OK, intent)

        val result = MediaProjectionHolder.get()
        assertNotNull(result)
        assertEquals(Activity.RESULT_OK, result!!.first)
        assertEquals("test_value", result.second.getStringExtra("test_key"))
    }

    @Test
    fun `clear resets to unavailable state`() {
        MediaProjectionHolder.store(Activity.RESULT_OK, Intent())
        assertTrue(MediaProjectionHolder.isAvailable)

        MediaProjectionHolder.clear()

        assertFalse(MediaProjectionHolder.isAvailable)
        assertNull(MediaProjectionHolder.get())
    }

    @Test
    fun `store overwrites previous values`() {
        val intent1 = Intent().apply { putExtra("key", "first") }
        val intent2 = Intent().apply { putExtra("key", "second") }

        MediaProjectionHolder.store(Activity.RESULT_OK, intent1)
        MediaProjectionHolder.store(Activity.RESULT_CANCELED, intent2)

        val result = MediaProjectionHolder.get()
        assertNotNull(result)
        assertEquals(Activity.RESULT_CANCELED, result!!.first)
        assertEquals("second", result.second.getStringExtra("key"))
    }
}
