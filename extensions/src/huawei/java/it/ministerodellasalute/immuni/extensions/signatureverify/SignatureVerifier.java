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

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.ministerodellasalute.immuni.ExposureKeyExportProto.*;

/**
 * Verified signatures of diagnosis key files, given a raw input stream to the file, and a metadata
 * proto object.
 */
public class SignatureVerifier {

    /**
     * The key export format for verification purpose.
     */
    public interface KeyExport {

        /**
         * The signature list that was read from export.sig file.
         * @return
         */
        TEKSignatureList signatureList();

        /**
         * Opens input stream for diagnosis key file. Caller of this method is responsible for closing
         * it.
         */
        InputStream openKeyInputStream() throws IOException;
    }

    public static final String KEY_VERSION_JOINER = "-";

    // map of supported signature algorithms - add as necessary
    private static final ImmutableMap<String, String> OID_TO_NAME_MAP =
            ImmutableMap.of(
                    "1.2.840.10045.4.3.2", "SHA256withECDSA",
                    "1.2.840.10045.4.3.4", "SHA512withECDSA");

    private static final int STREAMING_BYTE_BUFFER_SIZE = 4096;

    @Nullable
    private byte[] keyFilesHash;
    @Nullable
    private String debugPublicKey;

    /**
     * Performs verification that the key exports (really {@link KeyFileSignature})s provided
     * have been properly signed by the PHA key that matches the app's package. It also
     * performs matching for co-signatures in order to support keys signed by multiple PHAs.
     *
     * @param callingPackage The calling package -- that is, the package name of the health
     *                       authority app calling this code.
     * @param exports        List of KeyFileSignature extracted by
     *                       {@link ProvideDiagnosisKeys#unzip(Context, File)}
     * @return {@code true} if the files were properly signed, {@code false} otherwise
     */
    public boolean verify(String callingPackage, List<? extends KeyExport> exports)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException,
            SignatureException, ExecutionException, InterruptedException {
        Preconditions.checkArgument(!exports.isEmpty());

        // all chunks in batch must be present to verify
        BitSet batchChunks = new BitSet(exports.size());

        // b/157995134 would enable multiple whole batches and thus would need a map to verify.
        Map<TemporaryExposureKeyExport, BitSet> batchChunkMap = new HashMap<>();

        // all timestamps in a batch should match
        long startTime = Long.MIN_VALUE;
        long endTime = Long.MIN_VALUE;

        // retrieve public keys - same keys will be used throughout
        PartnerPublicKeySets partnerKeySet = getKeysForPackage(callingPackage, debugPublicKey);
        if (partnerKeySet.cosignSets.isEmpty()) {
            throw new SignatureException("no public key found for package " + callingPackage);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        //MessageDigest keyHash = MessageDigest.getInstance("SHA-256");

        for (KeyExport export : exports) {
            TEKSignatureList tekSignatures = export.signatureList();
            if (tekSignatures.getSignaturesCount() == 0) {
                throw new SignatureException("data is unsigned");
            }

            // create list of verifications for each rotation. only one verification within the rotation
            // has to verify successfully. within each cosigned set, all keys must verify successfully for
            // the set to verify.
            List<Verification> setVerifications = new ArrayList<>(partnerKeySet.cosignSets.size());
            for (CosignSet cosignSet : partnerKeySet.cosignSets) {
                setVerifications.add(createCosignatureVerifications(tekSignatures, cosignSet));
            }

            // read bytes, and simultaneously update all signatures and divert them to another thread
            // where we will be reconstructing the metadata from those bytes
            TemporaryExposureKeyExport keyMetadata;
            try (PipedInputStream in = new PipedInputStream(STREAMING_BYTE_BUFFER_SIZE)) {
                Future<TemporaryExposureKeyExport> protoDecodeFuture;
                try (PipedOutputStream out = new PipedOutputStream(in);
                     InputStream is = export.openKeyInputStream()) {
                    protoDecodeFuture =
                            executor.submit(() -> TemporaryExposureKeyFileMetadataParser.parse(in));

                    // read stream and divert to secondary pipe and every signature
                    byte[] buffer = new byte[STREAMING_BYTE_BUFFER_SIZE];
                    int len = is.read(buffer);
                    while (len != -1) {
                        out.write(buffer, 0, len);
//                        if (ContactTracingFeature.calculateDiagnosisKeyHash()) {
//                            keyHash.update(buffer, 0, len);
//                        }
                        for (Verification verification : setVerifications) {
                            verification.update(buffer, 0, len);
                        }
                        len = is.read(buffer);
                    }
                }

                // finalize signature verification - only one set needs to match
                boolean matched = false;
                for (Verification verification : setVerifications) {
                    if (verification.verify()) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                //    Log.log.atWarning().log("The necessary signatures did not verify");
                    return false;
                }

                // retrieve metadata generated on other thread, and only now can we close the input stream
                keyMetadata = protoDecodeFuture.get();
            }

            if (keyMetadata.getBatchSize() > ContactTracingFeature.signatureMaxAllowedBatchGroupSize()) {
                throw new UnsupportedOperationException(
                        "Signature batch size is "
                                + keyMetadata.getBatchSize()
                                + ". It must not be greater than "
                                + ContactTracingFeature.signatureMaxAllowedBatchGroupSize()
                                + " for "
                                + createExposureKeyString(keyMetadata));
            }

            // verify start/end times...
            // When enableMultipleWholeBatchSignatureVerification is enabled, this is always verified as
            // start and end timestamps are present in the key.
            if (!ContactTracingFeature.enableMultipleWholeBatchSignatureVerification()) {
                if (startTime == Long.MIN_VALUE && endTime == Long.MIN_VALUE) {
                    startTime = keyMetadata.getStartTimestamp();
                    endTime = keyMetadata.getEndTimestamp();
                } else {
                    if (keyMetadata.getStartTimestamp() != startTime
                            || keyMetadata.getEndTimestamp() != endTime) {
                        throw new IOException(
                                "exposure key start/end times ("
                                        + keyMetadata.getStartTimestamp()
                                        + "/"
                                        + keyMetadata.getEndTimestamp()
                                        + ") do not match ("
                                        + startTime
                                        + "/"
                                        + endTime
                                        + ") for "
                                        + createExposureKeyString(keyMetadata));
                    }
                }
            }

            // verify metadata signature count...
            if (tekSignatures.getSignaturesCount() != keyMetadata.getSignatureInfosCount()) {
                throw new SignatureException(
                        "expected "
                                + keyMetadata.getSignatureInfosCount()
                                + " signatures, but found "
                                + tekSignatures.getSignaturesCount()
                                + " for "
                                + createExposureKeyString(keyMetadata));
            }

            // key for collecting all files belonging to the same batch
            TemporaryExposureKeyExport keyMetadataForMap =
                    keyMetadata.toBuilder().clearBatchNum().build();

            // verify batches, if fields are set
            if (keyMetadata.hasBatchSize()) {
                if (!keyMetadata.hasBatchNum()) {
                    throw new SignatureException(
                            "key metadata specifies batch size but not batch number for "
                                    + createExposureKeyString(keyMetadata));
                }

                // batch number is ordinal 1 based
                if (keyMetadata.getBatchNum() < 1) {
                    throw new SignatureException(
                            "batch number should be one or greater for " + createExposureKeyString(keyMetadata));
                }

                if (keyMetadata.getBatchNum() > keyMetadata.getBatchSize()) {
                    throw new SignatureException(
                            "batch number should not be greater than batch size for "
                                    + createExposureKeyString(keyMetadata));
                }
                if (ContactTracingFeature.enableMultipleWholeBatchSignatureVerification()) {
                    if (!batchChunkMap.containsKey(keyMetadataForMap)) {
                        batchChunkMap.put(keyMetadataForMap, new BitSet(keyMetadata.getBatchSize()));
                    }
                    // batch number is ordinal 1 based
                    batchChunkMap.get(keyMetadataForMap).set(keyMetadata.getBatchNum() - 1);

                } else {
                    if (keyMetadata.getBatchSize() != exports.size()) {
                        throw new SignatureException(
                                "signature batch size ("
                                        + keyMetadata.getBatchSize()
                                        + " does not match actual batch size ("
                                        + exports.size()
                                        + ") for "
                                        + createExposureKeyString(keyMetadata));
                    }
                    batchChunks.set(keyMetadata.getBatchNum() - 1);
                }

            } else {
                if (ContactTracingFeature.enableMultipleWholeBatchSignatureVerification()) {
                    if (!batchChunkMap.containsKey(keyMetadataForMap)) {
                        batchChunkMap.put(keyMetadataForMap, new BitSet(keyMetadata.getBatchSize()));
                    }
                    batchChunkMap.get(keyMetadataForMap).set(0);
                } else {
                    batchChunks.set(0);
                }
            }
        }

        // verify all chunks seen
        if (!ContactTracingFeature.enableMultipleWholeBatchSignatureVerification()
                && batchChunks.nextClearBit(0) < exports.size()) {
            throw new IOException(
                    "missing some chunks out of " + exports.size() + " chunks expected in batch");
        } else {
            SignatureException exceptions = null;
            for (Map.Entry<TemporaryExposureKeyExport, BitSet> entry : batchChunkMap.entrySet()) {
                TemporaryExposureKeyExport keyMetaData = entry.getKey();
                if (keyMetaData.hasBatchSize()
                        && keyMetaData.getBatchSize() != entry.getValue().cardinality()) {
                    SignatureException e =
                            new SignatureException(
                                    "Signature batch size ("
                                            + keyMetaData.getBatchSize()
                                            + " does not match actual batch size ("
                                            + entry.getValue().cardinality()
                                            + ") for batch: "
                                            + createExposureKeyString(keyMetaData));
                    if (exceptions == null) {
                        exceptions = e;
                    } else {
                        exceptions.addSuppressed(e);
                    }
                }
            }
            if (exceptions != null) {
                throw exceptions;
            }
        }

//        if (ContactTracingFeature.calculateDiagnosisKeyHash()) {
//            keyFilesHash = keyHash.digest();
//        }
     //   Log.log.atInfo().log("Signature verification succeeded");
        return true;
    }

//    /**
//     * Gets the SHA-256 hash of the key files. If not available, returns an empty array.
//     */
//    public byte[] getKeyFilesHash() {
//        if (keyFilesHash == null) {
//            return new byte[0];
//        }
//        return keyFilesHash;
//    }

    public void setDebugPublicKey(@Nullable String debugPublicKey) {
        this.debugPublicKey = debugPublicKey;
    }

    private static final Splitter PUBLIC_KEY_SPLITTER = Splitter.on(':').limit(2);
    private static final Splitter ROTATION_LIST_SPLITTER = Splitter.on('|');

    private static final Splitter.MapSplitter COSIGNING_LIST_SPLITTER =
            Splitter.on('&').withKeyValueSeparator(',');

    // returns a list of potential keysets where only one keyset within the list must verify - this
    // supports key rotation. within a keyset, all keys within the set must verify - this support
    // cosigning
    private static PartnerPublicKeySets getKeysForPackage(
            String packageName, @Nullable String debugPublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // This is the list of all the flag-configured public signing keys, one element for each
        // partner's keys.
        List<String> publicKeyList = ContactTracingFeature.partnerPublicKeys();
//        Log.log
//                .atFine()
//                .log("getKeysForPackage: package name: %s, debug: %s, partner public keys flag: %s",
//                        packageName, debugPublicKey, TextUtils.join("\n", publicKeyList));

        // Start with an empty set of keys.
        PartnerPublicKeySets partnerKeySets = new PartnerPublicKeySets(packageName);

        // Parse the flag-defined keys.
        for (String publicKeyInfo : publicKeyList) {
            PartnerPublicKeySets candidateKeySet = parsePublicKeyFlag(publicKeyInfo);
            if (candidateKeySet.packageName.equals(packageName)) {
//                Log.log
//                        .atInfo()
//                        .log("Found flag-defined keys %s for package %s", publicKeyInfo, packageName);
                partnerKeySets.cosignSets.addAll(candidateKeySet.cosignSets);
            }
        }

        // One "extra" public key definition can be set via debug settings. If it is set, and its
        // package is the one we're working with here, we add it as an additional cosign set so that
        // it has an "additive" effect. That is: if the given keyfile's signature passes the debug
        // key's verification, we accept the file, otherwise we continue evaluating the flag-defined
        // public keys.
        if (debugPublicKey != null) {
       //     Log.log.atInfo().log("Debug key set: %s", debugPublicKey);
            try {
                PartnerPublicKeySets debugKeySet = parsePublicKeyFlag(debugPublicKey);
                if (debugKeySet.packageName.equals(packageName)) {
           //         Log.log.atInfo().log("Adding debug key set to partner's sets: %s", packageName);
                    // If there are no flag-defined keys for the package name we're interested, that's OK.
                    // partnerKeySet will be an empty set here, which is fine; we add the debug keys and
                    // continue.
                    partnerKeySets.cosignSets.addAll(debugKeySet.cosignSets);
                }
            } catch (InvalidKeySpecException e) {
//                Log.log
//                        .atWarning()
//                        .withCause(e)
//                        .log("Custom diagnosis key signature fields of debug mode is invalid and " +
//                                "will be ignored! ");
            }
        }

        return partnerKeySets;
    }

    /**
     * Parse the public key flag value for one partner, e.g.
     * "com.my.package:keyid-v1,<keybytes>&otherid-v1,<otherbytes>|keyid-v2,<morebytes>"
     */
    private static PartnerPublicKeySets parsePublicKeyFlag(String flagValue)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<String> parts = PUBLIC_KEY_SPLITTER.splitToList(flagValue);
        String partnerPackageName = parts.get(0);
        PartnerPublicKeySets partnerKeys = new PartnerPublicKeySets(partnerPackageName);

        List<String> partnerKeyConfigs = ROTATION_LIST_SPLITTER.splitToList(parts.get(1));
        for (String cosignSetConfig : partnerKeyConfigs) {
            // Split each cosign config into keys mapped by their name.
            Map<String, String> cosignKeys = COSIGNING_LIST_SPLITTER.split(cosignSetConfig);
            CosignSet cosignSet = new CosignSet();
            for (Map.Entry<String, String> cosignKeyEntry : cosignKeys.entrySet()) {
                byte[] publicKeyBytes = Base64.decode(cosignKeyEntry.getValue(), Base64.DEFAULT);
                cosignSet.keys.put(
                        cosignKeyEntry.getKey(),
                        KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKeyBytes)));
            }
            partnerKeys.cosignSets.add(cosignSet);
        }

        return partnerKeys;
    }

    private static Verification createCosignatureVerifications(
            TEKSignatureList tekSignatures, CosignSet cosignSet)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Preconditions.checkArgument(tekSignatures.getSignaturesCount() > 0);
        Preconditions.checkArgument(!cosignSet.keys.isEmpty());

        // if there are more public keys than signatures in the set, the signatures cannot verify
        if (tekSignatures.getSignaturesCount() < cosignSet.keys.size()) {
            return new FailedVerification();
        }

        // enforce that all public keys are matched
        HashSet<String> matchedPublicKeyNames = new HashSet<>();

        // for each signature...
        ImmutableList.Builder<Verification> cosignatureVerificationsBuilder =
                ImmutableList.builderWithExpectedSize(tekSignatures.getSignaturesCount());
        for (TEKSignature tekSignature : tekSignatures.getSignaturesList()) {
            SignatureInfo info = tekSignature.getSignatureInfo();

            // get correct public key to match
            String publicKeyName;
            PublicKey publicKey = null;
            if (info.hasVerificationKeyId()) {
                publicKeyName =
                        info.getVerificationKeyId() + KEY_VERSION_JOINER + info.getVerificationKeyVersion();
                if (cosignSet.keys.containsKey(publicKeyName)) {
                    publicKey = cosignSet.keys.get(publicKeyName);
                }
            } else {
                if (tekSignatures.getSignaturesCount() > 1) {
                    throw new SignatureException(
                            "signature in cosigning group does not specify verification_key_id");
                }
                if (ContactTracingFeature.ignoreUnmatchedSignatures()) {
                    if (cosignSet.keys.size() > 1) {
                        return new FailedVerification();
                    }
                } else {
                    Preconditions.checkState(cosignSet.keys.size() == 1);
                }
                Map.Entry<String, PublicKey> entry = cosignSet.keys.entrySet().iterator().next();
                publicKeyName = entry.getKey();
                publicKey = entry.getValue();
            }

            // if no matching public key found, move on to the next key, otherwise mark key as seen
            if (publicKey == null) {
                if (ContactTracingFeature.ignoreUnmatchedSignatures()) {
                    continue;
                } else {
              //      Log.log.atWarning().log("a verification failed due to an unmatched signature (strict)");
                    // if one key in the set cannot be matched, the entire set will fail anyways
                    return new FailedVerification();
                }
            } else if (ContactTracingFeature.ignoreUnmatchedSignatures()) {
                matchedPublicKeyNames.add(publicKeyName);
            }

            // create verifier for public key and add to the list
            String signatureAlgorithmName = OID_TO_NAME_MAP.get(info.getSignatureAlgorithm());
            if (signatureAlgorithmName == null) {
                throw new NoSuchAlgorithmException(
                        "unsupported signature OID: " + info.getSignatureAlgorithm());
            }
            Signature signatureAttempt = Signature.getInstance(signatureAlgorithmName);
            signatureAttempt.initVerify(publicKey);
            Log.e("JK", "--------------" + tekSignature.getSignature().toString());
            cosignatureVerificationsBuilder.add(
                    new SignatureVerification(signatureAttempt, tekSignature.getSignature().toByteArray()));
        }

        // check all public keys are being verified with none left out
        ImmutableList<Verification> verifications = cosignatureVerificationsBuilder.build();
        if (verifications.size() < cosignSet.keys.size()) {
            if (ContactTracingFeature.ignoreUnmatchedSignatures()) {
//                Log.log
//                        .atInfo()
//                        .log(
//                                "More public keys in this cosign set than signatures/verifications in this keyfile"
//                                        + " (%d > %d). Continuing with other cosign sets.",
//                                cosignSet.keys.size(), verifications.size());
                // Failing this cosign set allows us to continue with other cosign sets.
                return new FailedVerification();
            } else {
                // Throwing an exception stops us from attempting signature verification with other cosign
                // sets.
                throw new SignatureException(
                        "expected at least "
                                + cosignSet.keys.size()
                                + " signatures with matching ids, found only "
                                + verifications.size());
            }
        }
        if (ContactTracingFeature.ignoreUnmatchedSignatures()
                && matchedPublicKeyNames.size() != cosignSet.keys.size()) {
//            Log.log
//                    .atInfo()
//                    .log(
//                            "Unequal number of signatures/verifications in this keyfile vs number of public keys"
//                                    + " in this cosign set. %d != %d. Continuing with other cosign sets.",
//                            matchedPublicKeyNames.size(), cosignSet.keys.size());
            // Failing this cosign set (as opposed to throwing an exception) allows us to continue with
            // other cosign sets.
            return new FailedVerification();
        }

        return new CosignatureVerification(verifications);
    }

    private static String createExposureKeyString(TemporaryExposureKeyExport keyMetadata) {
        return "[StartTimestamp="
                + keyMetadata.getStartTimestamp()
                + "; EndTimestamp="
                + keyMetadata.getEndTimestamp()
                + "; Region="
                + keyMetadata.getRegion()
                + "; BatchNum="
                + keyMetadata.getBatchNum()
                + "; BatchSize="
                + keyMetadata.getBatchSize()
                + "]";
    }

    private interface Verification {

        void update(byte[] data, int off, int len) throws SignatureException;

        boolean verify() throws SignatureException;
    }

    /**
     * Individual verification that will immediately fail. Not necessarily indicative that all
     * signature verifications for a file have failed.
     */
    private static class FailedVerification implements Verification {

        @Override
        public void update(byte[] data, int off, int len) {
        }

        @Override
        public boolean verify() {
            return false;
        }
    }

    private static class CosignatureVerification implements Verification {

        private final ImmutableList<Verification> cosignatures;

        private CosignatureVerification(List<Verification> cosignatures) {
            Preconditions.checkArgument(!cosignatures.isEmpty());
            this.cosignatures = ImmutableList.copyOf(cosignatures);
        }

        @Override
        public void update(byte[] data, int off, int len) throws SignatureException {
            for (Verification verification : cosignatures) {
                verification.update(data, off, len);
            }
        }

        @Override
        public boolean verify() throws SignatureException {
            for (Verification verification : cosignatures) {
                if (!verification.verify()) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class SignatureVerification implements Verification {

        private final Signature signature;
        private final byte[] comparison;

        private SignatureVerification(Signature signature, byte[] comparison) {
            this.signature = signature;
            this.comparison = comparison;
        }

        @Override
        public void update(byte[] data, int off, int len) throws SignatureException {
            signature.update(data, off, len);
        }

        @Override
        public boolean verify() throws SignatureException {
            return signature.verify(comparison);
        }
    }

    /**
     * Value class to hold all the public signing keys for one partner's keyfiles.
     *
     * <p>The partner is identified by their app package name, and may have multiple possible sets of
     * keys, called cosign sets. Among a given partner's cosign sets, at least one must successfully
     * verify a keyfile's signature for the keyfile to pass.
     *
     * <p>It is supported for there to be more than one flag value with the same package name, in
     * which case we will concat the lists of CosignSets for each flag so that if any of them pass,
     * the keyfile passes.
     *
     * <p>This is additionally true for public keys supplied via the debug settings: If one is
     * supplied for the same package as flag-defined keys, we'll add the debug key to the list of
     * CosignSets and accept keyfiles which pass its verification.
     */
    private static class PartnerPublicKeySets {

        private final String packageName;
        private final List<CosignSet> cosignSets = new ArrayList<>();

        private PartnerPublicKeySets(String packageName) {
            this.packageName = packageName;
        }
    }

    /**
     * A set of keys that together must all successfully verify a keyfile's signature for the keyfile
     * to pass.
     */
    private static class CosignSet {

        private final Map<String, PublicKey> keys = new HashMap<>();
    }
}
