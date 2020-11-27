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

import java.util.Arrays;
import java.util.List;

/**
 * This class contains various constants and feature flags used through the exposure notifications
 * system. Many of these values should ideally be delivered in a way that they can be updated
 * without needing to push a new version of code. For example, the method
 * {@link #partnerPublicKeys()} returns the public keys of all currently active partners using
 * the system.
 * FIXME: Implement this as a deserialized object or some sort of key/value store.
 */
public class ContactTracingFeature {
    /**
     * File name of signature inside diagnosis keys zip
     */
    public static String diagnosisKeySignatureFileName() {
        return "export.sig";
    }

    /**
     * File name of key data binary inside diagnosis keys zip
     */
    public static String diagnosisKeyBinFileName() {
        return "export.bin";
    }

    /**
     * Calculates the hash of all keys when provideDiagnosisKeys is called.
     */
    public static boolean calculateDiagnosisKeyHash() {
        return true;
    }

    /**
     * Maximum permissible batch size group size of signature
     */
    public static int signatureMaxAllowedBatchGroupSize() {
        return 1_000_000;
    }

    /**
     * If enabled, signature verification would accept multiple whole batches
     */
    public static boolean enableMultipleWholeBatchSignatureVerification() {
        return true;
    }

    /**
     * Allow signature files to have signatures without a matching public key, or more matching
     * public keys than signatures, during verification.
     */
    public static boolean ignoreUnmatchedSignatures() {
        return true;
    }

    /**
     * A list of partner public keys for diagnosis key signature verification.
     */
    public static List<String> partnerPublicKeys() {
        return Arrays.asList(PUBLIC_KEYS);
    }

    /**
     * each list entry is a string formatted as:
     * <my.package.name>:<COSIGN_SET>|<COSIGN_SET>|...
     * <p>
     * COSIGN_SET:
     * <PUBLIC_KEY>&<PUBLIC_KEY>&...
     * <p>
     * PUBLIC_KEY:
     * <NAME>,<BASE64KEYVALUE>
     * <p>
     * NAME:
     * <KEY_ID>-<KEY_VERSION>
     * <p>
     * KEY_ID and KEY_VERSION:
     * arbitrary alphanumeric identifiers. Match verification_key_id
     * and verification_key_version in SignatureInfo fields of the
     * diagnosis export proto. Should definitely not contain any separator
     * character used in the flag definition. A name must be defined here, but
     * verification_key_id may be omitted in the proto if there is only a
     * single signature in the proto.
     * <p>
     * each co-signing set may include multiple key values, where all values
     * within a set are verified together. multiple co-signing sets may be
     * specified in order to support key rotations.
     * <p>
     */
    private static final String[] PUBLIC_KEYS = {
            "com.google.android.apps.exposurenotification:ExampleServer-v1,MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEUczyMAkfSeoU77Nmcb1G7t7xyGCAhQqMOIVDFLFas3J+elP7CiotovigCLWj706F07j1EPL27ThRzZl7Ha9uOA==|310-v1,MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE49JY6kekDgxj3Crm4y6kEHdfoKQFSNDM4mV9cgDb+e5nOAw0GeRoRThCu9/wX5wDT2QloFoOjl2pGZHI0f3C3w==",
            "it.ministerodellasalute.immuni:222-v1,MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEw4rImGfl1eU13nq2OdnHor73/BAaMuTVNiR7zShVj8LCTApq+f2oNRdcwoCt3U3oCKtZj473FuZp4Yx889ttWA==|222-v2,MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoYUk5kfvyUEOfumu1/jjIWORiROu5msDe8dpW6DBCtZ0CgNPz/LXUHaLe+hFx3NzxSREBh03Y99sjEhGTvqSmg=="
    };

    /**
     * If enabled, will read full header
     */
    public static boolean readFullMetadataHeader() {
        return true;
    }

    /**
     * The Settings.Global name for the BLE low latency scan window value.
     */
}
