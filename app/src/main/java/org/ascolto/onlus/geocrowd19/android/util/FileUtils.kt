package org.ascolto.onlus.geocrowd19.android.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object FileUtils {
    suspend fun download(link: String, path: String) = withContext(Dispatchers.IO) {
        URL(link).openStream().use { input ->
            val file = File(path)
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
}