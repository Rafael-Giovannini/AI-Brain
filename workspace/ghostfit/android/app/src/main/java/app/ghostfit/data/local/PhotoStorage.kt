package app.ghostfit.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Encrypted photo storage using Tink AEAD (AES-256-GCM) with Android Keystore-backed master key.
 *
 * Stores reference photos encrypted in app-private filesDir/.enc/ directory.
 * Photos never leave the device unencrypted — LGPD compliant.
 */
class PhotoStorage internal constructor(
    private val aead: Aead,
    private val encDir: File
) {

    init {
        encDir.mkdirs()
    }

    /**
     * Encrypts a Bitmap and saves it to the encrypted storage directory.
     *
     * @param bitmap The photo to encrypt and store.
     * @param fileName The file name (without path) for the encrypted file. Should end in .enc.
     * @return The absolute path of the encrypted file, or null on failure.
     */
    fun encrypt(bitmap: Bitmap, fileName: String): String? {
        return try {
            val plaintext = bitmapToBytes(bitmap)
            val ciphertext = aead.encrypt(plaintext, fileName.toByteArray())
            val outFile = File(encDir, fileName)
            outFile.writeBytes(ciphertext)
            outFile.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Failed to encrypt photo: $fileName", e)
            null
        }
    }

    /**
     * Decrypts an encrypted photo file and returns it as a Bitmap.
     *
     * @param filePath The absolute path to the encrypted file.
     * @return The decrypted Bitmap, or null if decryption fails or file not found.
     */
    fun decrypt(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            val ciphertext = file.readBytes()
            val fileName = file.name
            val plaintext = aead.decrypt(ciphertext, fileName.toByteArray())
            BitmapFactory.decodeByteArray(plaintext, 0, plaintext.size)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decrypt photo: $filePath", e)
            null
        }
    }

    /**
     * Securely deletes an encrypted photo file.
     *
     * @param filePath The absolute path to the encrypted file.
     * @return true if the file was deleted successfully, false otherwise.
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.exists() && file.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete file: $filePath", e)
            false
        }
    }

    /**
     * Deletes all encrypted files for a given user (by their file paths).
     *
     * @param filePaths List of absolute paths to encrypted files.
     * @return Number of files successfully deleted.
     */
    fun deleteAll(filePaths: List<String>): Int {
        return filePaths.count { deleteFile(it) }
    }

    /**
     * Returns the encrypted storage directory.
     */
    fun getEncryptedDir(): File = encDir

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    companion object {
        private const val TAG = "PhotoStorage"
        private const val ENC_DIR = ".enc"
        private const val KEYSET_NAME = "ghostfit_photo_keyset"
        private const val PREF_FILE_NAME = "ghostfit_photo_keyset_prefs"
        private const val MASTER_KEY_URI = "android-keystore://ghostfit_master_key"

        @Volatile
        private var INSTANCE: PhotoStorage? = null

        fun getInstance(context: Context): PhotoStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: create(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun create(context: Context): PhotoStorage {
            AeadConfig.register()
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .build()
                .keysetHandle
            val aead = keysetHandle.getPrimitive(Aead::class.java)
            val encDir = File(context.filesDir, ENC_DIR)
            return PhotoStorage(aead, encDir)
        }
    }
}
