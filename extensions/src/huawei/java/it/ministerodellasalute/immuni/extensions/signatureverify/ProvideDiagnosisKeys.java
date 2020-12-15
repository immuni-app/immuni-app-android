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
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import it.ministerodellasalute.immuni.ExposureKeyExportProto.*;

/**
 * This class is a heavily modified version of the original code which accepts a list of signed
 * diagnosis key ZIP files that match the file format described at
 * https://developers.google.com/android/exposure-notifications/exposure-key-file-format.
 * <p>
 * This code demonstrates how to call the required functions to verify the file format is
 * correct and the keys have been properly signed.
 * <p>
 * The code has been greatly simplified from the original version, primarily in the way errors
 * are handled.
 */
public class ProvideDiagnosisKeys {
    private final Context context;

    public ProvideDiagnosisKeys(Context context) {
        this.context = context;
    }

    public boolean verify(String callingPackage, File signedKeyFile) {
        KeyFileSignature keyFileSignature = unzip(context, signedKeyFile);

        ArrayList<KeyFileSignature> signatures = Lists.newArrayList(keyFileSignature);
        Log.e("JK", "KeyFileSignature len is " + signatures.size());

        SignatureVerifier signatureVerifier = new SignatureVerifier();
        try {
            return signatureVerifier.verify(callingPackage, signatures);
        } catch (Exception e) {
            throw new RuntimeException("Verification failed", e);
        }
    }

    /**
     * Unzips a diagnosis keys zip file into an internal folder to allow for
     * verification of the signature and processing of the keys by {}
     * <p>
     * FIXME: This code is NOT threadsafe and is incomplete as it exists. Key files are extracted
     * to a folder and those keys + signatures are not cleaned up here (or anywhere else in the
     * provided snippets). After matching occurs, these files should be removed by whatever
     * system is running the key matching process.
     *
     * @param context           The context to use
     * @param diagnosisKeysFile The zip file
     * @return A {@link KeyFileSignature} that can be verified by {@link SignatureVerifier}
     */
    static KeyFileSignature unzip(Context context, File diagnosisKeysFile) {
        File targetFolder = new File(context.getFilesDir(), "en_diagnosis_keys");

        if (targetFolder.exists()) {
            deleteFile(targetFolder);
        }

        if (!targetFolder.mkdirs()) {
            throw new RuntimeException("Can't create folder for keys");
        }

        Log.e("JK", "targetFolder" + targetFolder.getAbsolutePath());

        ArrayList<KeyFileSignature> results = new ArrayList<>();
        byte[] buffer = new byte[2048];
        TEKSignatureList signatureList = null;
        File keyFile = null;

        try {
            // We use a ZipInputStream, rather than a ZipFile, to minimize the memory footprint.
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(diagnosisKeysFile));
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = getValidatedName(entry);
                Log.e("JK", "entryName:" + entryName);
                if (entryName.equals(ContactTracingFeature.diagnosisKeySignatureFileName())) {
                    if (signatureList != null) {
                        deleteFile(targetFolder);
                        throw new IllegalStateException("Archive contains multiple " + entryName);
                    }
                    signatureList =
                            TEKSignatureList.parseFrom(zipStream);
                } else if (entryName.equals(ContactTracingFeature.diagnosisKeyBinFileName())) {
                    if (keyFile != null) {
                        deleteFile(targetFolder);
                        throw new IllegalStateException("Archive contains multiple " + entryName);
                    }
                    keyFile = new File(targetFolder, "export.bin");

                    if (!keyFile.createNewFile()) {
                        deleteFile(targetFolder);
                        throw new RuntimeException(
                                "Can't create file " + keyFile.getAbsolutePath());
                    }
                    try (FileOutputStream output = new FileOutputStream(keyFile.getAbsolutePath())) {
                        int len;
                        while ((len = zipStream.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    }
                } else {
                    deleteFile(targetFolder);
                    throw new IllegalStateException("Invalid key file entry " + entryName);
                }
            }
        } catch (IOException e) {
            deleteFile(targetFolder);
            throw new RuntimeException("Failed to extract diagnosis keys from " +
                    diagnosisKeysFile.getAbsolutePath(), e);
        }

        if (keyFile == null || signatureList == null) {
            deleteFile(targetFolder);
            throw new RuntimeException("Invalid file content: " +
                    diagnosisKeysFile.getAbsolutePath());
        }
        //return KeyFileSignature.create(keyFile, signatureList);
        return new KeyFileSignature(keyFile, signatureList);
    }

    /**
     * Return the name of a ZipEntry after attempting to verifying that it does not exploit any
     * path traversal attacks.
     *
     * @throws ZipException if {@param zipEntry} contains any possible path traversal
     *                      characters.
     */
    private static String getValidatedName(ZipEntry zipEntry) throws ZipException {
        String name = zipEntry.getName();
        if (name.contains("..")) {
            // If the string does contain "..", break it down into its actual name
            // elements to ensure it actually contains ".." as a name, not just a
            // name like "foo..bar" or even "foo..", which should be fine.
            File file = new File(name);
            while (file != null) {
                if (file.getName().equals("..")) {
                    throw new ZipException("Illegal name: " + name);
                }
                file = file.getParentFile();
            }
        }
        return name;
    }

    private static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFile(child);
                }
            }
        }

        return file.delete();
    }

    public static class KeyFileSignature implements SignatureVerifier.KeyExport {
        private File keyFile;
        private TEKSignatureList signatureList;

        KeyFileSignature(@Nullable File keyFile, @Nullable TEKSignatureList signatureList) {
            this.keyFile = keyFile;
            this.signatureList = signatureList;
        }

        /**
         * The file of the diagnosis keys.
         */
        private File keyFile() {
            return keyFile;
        }

        /**
         * The signature list that was read from export.sig file.
         * @return
         */
        @Override
        public TEKSignatureList signatureList() {
            return signatureList;
        }

        @Override
        public InputStream openKeyInputStream() throws IOException {
            return new FileInputStream(keyFile());
        }
    }
}
