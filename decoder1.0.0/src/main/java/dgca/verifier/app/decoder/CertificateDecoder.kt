/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 5/10/21 11:28 PM
 */

package dgca.verifier.app.decoder

import dgca.verifier.app.decoder.model.GreenCertificate

/**
 * Represents error that might occur during decoding green certificate QR.
 */
sealed class CertificateDecodingError(val error: Throwable? = null) {
    class Base45DecodingError(error: Throwable) : CertificateDecodingError(error)
    class Base45DecompressionError(error: Throwable) : CertificateDecodingError(error)
    class CoseDataDecodingError(error: Throwable) : CertificateDecodingError(error)
    class GreenCertificateDecodingError(error: Throwable) : CertificateDecodingError(error)
    object EmptyGreenCertificate : CertificateDecodingError()
    class CertificateConversionError(error: Throwable) : CertificateDecodingError(error)
}

/**
 * Represents green certificate decoding result. If it's successful - it should return {@link Success},
 * specific descriptive error {@link Error}.
 */
sealed class CertificateDecodingResult {
    class Success(val greenCertificate: GreenCertificate) : CertificateDecodingResult()
    data class Error(val error: CertificateDecodingError) : CertificateDecodingResult()
}

/**
 * Provides ability to decode QR code representing green certificate to local model {@link CertificateModel}
 */
interface CertificateDecoder {

    /**
     * Returns success result with {@link CertificateModel} or descriptive error model.
     */
    fun decodeCertificate(qrCodeText: String): CertificateDecodingResult
}