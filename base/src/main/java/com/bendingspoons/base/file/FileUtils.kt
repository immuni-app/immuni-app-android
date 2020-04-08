package com.bendingspoons.base.file

import java.io.File
import java.io.FileOutputStream
import java.net.URL

object FileUtils {
    fun download(link: String, path: String) {
        URL(link).openStream().use { input ->
            val file = File(path)
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
}