package com.bendingspoons.base.file

import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * File utility methods.
 */
object FileUtils {

    /**
     * Download a remote file and save it in a local path.
     *
     * @param url the url of the remote file
     * @param path the path of a local [File]
     */
    fun download(url: String, path: String) {
        URL(url).openStream().use { input ->
            val file = File(path)
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
}