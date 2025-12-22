package com.thewintershadow.thoughtsmith.repository

import android.content.Context
import android.net.Uri
import com.thewintershadow.thoughtsmith.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Service for handling file operations, particularly saving journal entries.
 *
 * This service manages file I/O operations for exporting journal entries to the
 * device's file system. It uses Android's Storage Access Framework (SAF) through
 * URIs to provide secure, user-controlled file access.
 *
 * Features:
 * - Secure file access through Android SAF
 * - Asynchronous file operations using Coroutines
 * - Proper error handling and logging
 * - Support for user-selected file locations
 * - Automatic file stream management
 *
 * The service respects Android's scoped storage requirements and allows users
 * to choose where their journal entries are saved through the system file picker.
 *
 * @param context Android context for accessing content resolver
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
class FileStorageService(
    private val context: Context,
) {
    /**
     * Save a journal entry to a user-selected file location.
     *
     * This function writes the provided content to a file specified by the URI.
     * The URI is typically obtained through Android's file picker (CREATE_DOCUMENT intent)
     * which allows users to choose the save location and filename.
     *
     * The operation is performed on the IO dispatcher to avoid blocking the UI thread,
     * and includes proper resource management with automatic stream closure.
     *
     * File Format:
     * - Content is saved as UTF-8 encoded text
     * - Typically markdown format for journal entries
     * - User controls file extension through picker
     *
     * @param uri The destination URI from Storage Access Framework
     * @param content The journal entry content to save
     *
     * @return Result containing the file path string on success, or exception on failure
     *
     * @throws Exception Various I/O exceptions including permission denied, disk full, etc.
     */
    suspend fun saveJournalEntryToUri(
        uri: Uri,
        content: String,
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                AppLogger.info("FileStorageService", "Saving journal entry to URI: $uri")

                context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    outputStream.write(content.toByteArray())
                } ?: throw Exception("Failed to open output stream")

                val filePath = uri.toString()
                AppLogger.info("FileStorageService", "Journal entry saved successfully to: $filePath")

                Result.success(filePath)
            } catch (e: Exception) {
                AppLogger.error("FileStorageService", "Failed to save journal entry", e)
                Result.failure(e)
            }
        }
}
