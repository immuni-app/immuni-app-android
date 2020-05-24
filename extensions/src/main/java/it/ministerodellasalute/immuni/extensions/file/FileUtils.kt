/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.file

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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

fun File.copyInputStream(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}
