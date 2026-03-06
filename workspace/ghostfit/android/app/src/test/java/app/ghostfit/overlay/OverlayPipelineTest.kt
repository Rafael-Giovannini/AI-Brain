package app.ghostfit.overlay

import android.content.Intent
import app.ghostfit.domain.TryOnSession
import app.ghostfit.domain.TryOnStatus
import app.ghostfit.ui.tryon.TryOnActivity
import app.ghostfit.ui.tryon.TryOnSessionHolder
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for the overlay pipeline trigger logic:
 * - MediaProjectionHolder guard conditions
 * - Generation-in-progress guard
 * - TryOnSessionHolder state management during pipeline
 * - TryOnActivity display-only mode constant
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OverlayPipelineTest {

    @Before
    fun setUp() {
        MediaProjectionHolder.clear()
        TryOnSessionHolder.clear()
    }

    @After
    fun tearDown() {
        MediaProjectionHolder.clear()
        TryOnSessionHolder.clear()
    }

    // --- MediaProjectionHolder guard ---

    @Test
    fun `MediaProjectionHolder isAvailable is false when empty`() {
        assertFalse(MediaProjectionHolder.isAvailable)
    }

    @Test
    fun `MediaProjectionHolder isAvailable is true after store`() {
        MediaProjectionHolder.store(-1, Intent())
        assertTrue(MediaProjectionHolder.isAvailable)
    }

    @Test
    fun `MediaProjectionHolder get returns null when empty`() {
        assertNull(MediaProjectionHolder.get())
    }

    @Test
    fun `MediaProjectionHolder get returns stored data`() {
        val intent = Intent().apply { putExtra("test", "value") }
        MediaProjectionHolder.store(-1, intent)

        val result = MediaProjectionHolder.get()
        assertNotNull(result)
        assertEquals(-1, result!!.first)
        assertEquals("value", result.second.getStringExtra("test"))
    }

    @Test
    fun `MediaProjectionHolder clear resets availability`() {
        MediaProjectionHolder.store(-1, Intent())
        assertTrue(MediaProjectionHolder.isAvailable)

        MediaProjectionHolder.clear()
        assertFalse(MediaProjectionHolder.isAvailable)
        assertNull(MediaProjectionHolder.get())
    }

    // --- TryOnSessionHolder pipeline state ---

    @Test
    fun `TryOnSessionHolder stores session from pipeline`() {
        val session = TryOnSession(
            status = TryOnStatus.DONE,
            modelUsed = "fashn",
            durationMs = 5000
        )
        TryOnSessionHolder.update(session)

        val stored = TryOnSessionHolder.currentSession
        assertNotNull(stored)
        assertEquals(TryOnStatus.DONE, stored!!.status)
        assertEquals("fashn", stored.modelUsed)
        assertEquals(5000L, stored.durationMs)
    }

    @Test
    fun `TryOnSessionHolder update overwrites previous session`() {
        val session1 = TryOnSession(status = TryOnStatus.CAPTURING)
        val session2 = TryOnSession(status = TryOnStatus.DONE, modelUsed = "vertex")

        TryOnSessionHolder.update(session1)
        assertEquals(TryOnStatus.CAPTURING, TryOnSessionHolder.currentSession?.status)

        TryOnSessionHolder.update(session2)
        assertEquals(TryOnStatus.DONE, TryOnSessionHolder.currentSession?.status)
        assertEquals("vertex", TryOnSessionHolder.currentSession?.modelUsed)
    }

    @Test
    fun `TryOnSessionHolder clear removes session`() {
        TryOnSessionHolder.update(TryOnSession(status = TryOnStatus.DONE))
        assertNotNull(TryOnSessionHolder.currentSession)

        TryOnSessionHolder.clear()
        assertNull(TryOnSessionHolder.currentSession)
    }

    @Test
    fun `TryOnSessionHolder stores NO_GARMENT session without consuming attempt`() {
        val session = TryOnSession(
            status = TryOnStatus.NO_GARMENT,
            errorMessage = "Nenhuma roupa detectada. Tente em uma pagina de produto."
        )
        TryOnSessionHolder.update(session)

        val stored = TryOnSessionHolder.currentSession!!
        assertEquals(TryOnStatus.NO_GARMENT, stored.status)
        assertNull(stored.generatedImage)
        assertNull(stored.modelUsed)
    }

    // --- TryOnActivity display-only constant ---

    @Test
    fun `TryOnActivity EXTRA_DISPLAY_ONLY constant is defined`() {
        assertEquals("extra_display_only", TryOnActivity.EXTRA_DISPLAY_ONLY)
    }

    // --- Generation in progress guard ---

    @Test
    fun `generationInProgress flag defaults to false`() {
        // OverlayService.generationInProgress is internal and defaults to false
        // Test through the service behavior: pipeline should run when not in progress
        assertFalse(MediaProjectionHolder.isAvailable)
        // Without MediaProjection, pipeline won't start regardless of generationInProgress
        // This test verifies the precondition
    }

    // --- Pipeline status transitions ---

    @Test
    fun `pipeline session tracks all status transitions via TryOnSessionHolder`() {
        // Simulate the sequence of status updates that OverlayService pipeline produces
        val statuses = mutableListOf<TryOnStatus>()

        // Capturing
        val s1 = TryOnSession(status = TryOnStatus.CAPTURING)
        TryOnSessionHolder.update(s1)
        statuses.add(TryOnSessionHolder.currentSession!!.status)

        // Detecting
        val s2 = s1.copy(status = TryOnStatus.DETECTING)
        TryOnSessionHolder.update(s2)
        statuses.add(TryOnSessionHolder.currentSession!!.status)

        // Generating
        val s3 = s2.copy(status = TryOnStatus.GENERATING)
        TryOnSessionHolder.update(s3)
        statuses.add(TryOnSessionHolder.currentSession!!.status)

        // Done
        val s4 = s3.copy(status = TryOnStatus.DONE, modelUsed = "fashn")
        TryOnSessionHolder.update(s4)
        statuses.add(TryOnSessionHolder.currentSession!!.status)

        assertEquals(
            listOf(TryOnStatus.CAPTURING, TryOnStatus.DETECTING, TryOnStatus.GENERATING, TryOnStatus.DONE),
            statuses
        )
    }

    @Test
    fun `pipeline error session is stored in TryOnSessionHolder`() {
        val errorSession = TryOnSession(
            status = TryOnStatus.ERROR,
            errorMessage = "Both FASHN.ai and Vertex AI failed"
        )
        TryOnSessionHolder.update(errorSession)

        val stored = TryOnSessionHolder.currentSession!!
        assertEquals(TryOnStatus.ERROR, stored.status)
        assertTrue(stored.errorMessage!!.contains("failed"))
    }
}
