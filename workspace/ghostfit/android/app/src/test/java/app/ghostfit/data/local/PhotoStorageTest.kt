package app.ghostfit.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Unit tests for PhotoStorage encryption/decryption.
 *
 * Uses Robolectric for Bitmap support. The internal constructor allows
 * injecting a test Aead (plain Tink keyset) instead of Android Keystore.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhotoStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var aead: Aead
    private lateinit var encDir: File
    private lateinit var storage: PhotoStorage

    @Before
    fun setUp() {
        AeadConfig.register()
        val keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
        aead = keysetHandle.getPrimitive(Aead::class.java)
        encDir = tempFolder.newFolder(".enc")
        storage = PhotoStorage(aead, encDir)
    }

    @After
    fun tearDown() {
        tempFolder.delete()
    }

    @Test
    fun `encrypt returns file path on success`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val result = storage.encrypt(bitmap, "test_photo.enc")

        assertNotNull(result)
        assertTrue(result!!.endsWith("test_photo.enc"))
        assertTrue(File(result).exists())
    }

    @Test
    fun `encrypted file is not readable as plain image`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val path = storage.encrypt(bitmap, "test_photo.enc")!!

        val rawBytes = File(path).readBytes()
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
        assertNull("Encrypted data should not decode as a valid bitmap", decoded)
    }

    @Test
    fun `decrypt returns bitmap for valid encrypted file`() {
        val original = Bitmap.createBitmap(50, 75, Bitmap.Config.ARGB_8888)
        val path = storage.encrypt(original, "roundtrip.enc")!!

        val decrypted = storage.decrypt(path)

        assertNotNull(decrypted)
        assertEquals(original.width, decrypted!!.width)
        assertEquals(original.height, decrypted.height)
    }

    @Test
    fun `decrypt returns null for nonexistent file`() {
        val result = storage.decrypt("/nonexistent/path/photo.enc")
        assertNull(result)
    }

    @Test
    fun `decrypt returns null for tampered file`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val path = storage.encrypt(bitmap, "tampered.enc")!!

        val file = File(path)
        val bytes = file.readBytes()
        if (bytes.size > 10) {
            bytes[10] = (bytes[10].toInt() xor 0xFF).toByte()
        }
        file.writeBytes(bytes)

        val result = storage.decrypt(path)
        assertNull("Tampered file should fail decryption", result)
    }

    @Test
    fun `deleteFile removes the file`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val path = storage.encrypt(bitmap, "to_delete.enc")!!

        assertTrue(File(path).exists())
        val deleted = storage.deleteFile(path)
        assertTrue(deleted)
        assertFalse(File(path).exists())
    }

    @Test
    fun `deleteFile returns false for nonexistent file`() {
        val result = storage.deleteFile("/nonexistent/file.enc")
        assertFalse(result)
    }

    @Test
    fun `deleteAll removes multiple files and returns count`() {
        val bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val path1 = storage.encrypt(bmp, "file1.enc")!!
        val path2 = storage.encrypt(bmp, "file2.enc")!!
        val path3 = storage.encrypt(bmp, "file3.enc")!!

        val count = storage.deleteAll(listOf(path1, path2, path3))
        assertEquals(3, count)
        assertFalse(File(path1).exists())
        assertFalse(File(path2).exists())
        assertFalse(File(path3).exists())
    }

    @Test
    fun `deleteAll handles mix of existing and nonexistent files`() {
        val bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val path1 = storage.encrypt(bmp, "exists.enc")!!

        val count = storage.deleteAll(listOf(path1, "/fake/path.enc"))
        assertEquals(1, count)
    }

    @Test
    fun `encrypt uses associated data based on filename`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val path = storage.encrypt(bitmap, "associated.enc")!!

        val decrypted = storage.decrypt(path)
        assertNotNull(decrypted)

        // Rename the file — associated data mismatch should cause decryption failure
        val renamedFile = File(encDir, "renamed.enc")
        File(path).renameTo(renamedFile)
        val result = storage.decrypt(renamedFile.absolutePath)
        assertNull("Renamed file should fail due to associated data mismatch", result)
    }

    @Test
    fun `getEncryptedDir returns the enc directory`() {
        val dir = storage.getEncryptedDir()
        assertTrue(dir.exists())
        assertTrue(dir.isDirectory)
        assertEquals(".enc", dir.name)
    }
}
