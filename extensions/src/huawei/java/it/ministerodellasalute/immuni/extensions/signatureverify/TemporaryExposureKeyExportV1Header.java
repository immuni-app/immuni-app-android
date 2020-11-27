/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ministerodellasalute.immuni.extensions.signatureverify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Encloses constant values related file format definition. */
public final class TemporaryExposureKeyExportV1Header {
    // See https://developers.google.com/android/exposure-notifications/exposure-key-file-format
    public static final ByteArrayValue HEADER =
            ByteArrayValue.from("EK Export v1    ".getBytes(UTF_8)); // right padded with whitespaces

    public static void readAndVerifyHeader(InputStream inputStream) throws IOException {
        byte[] header = new byte[HEADER.length];
        int totalBytesRead = 0;
        if (ContactTracingFeature.readFullMetadataHeader()) {
            int bytesRead = 0;
            while (totalBytesRead < HEADER.length && bytesRead != -1) {
                bytesRead = inputStream.read(header, totalBytesRead, HEADER.length - totalBytesRead);
                totalBytesRead += bytesRead;
            }
        } else {
            totalBytesRead = inputStream.read(header);
        }
        if (totalBytesRead != HEADER.length) {
            throw new IOException(
                    "Invalid header length (read "
                            + totalBytesRead
                            + " bytes, expected "
                            + HEADER.length
                            + " bytes)");
        }
        if (!Arrays.equals(header, HEADER.get())) {
            throw new IOException("Invalid header: " + Arrays.toString(header));
        }
    }

    private TemporaryExposureKeyExportV1Header() {}
}