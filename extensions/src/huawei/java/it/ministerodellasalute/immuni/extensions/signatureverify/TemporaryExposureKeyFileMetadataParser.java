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

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.WireFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import it.ministerodellasalute.immuni.ExposureKeyExportProto.*;


/**
 * A parser class to parse metadata from exposure key export file.
 */
public class TemporaryExposureKeyFileMetadataParser {
    // An early returning option, should be disabled unless the correctness and robustness is proved
    // when enabled.
    private static final boolean EARLY_RETURN_ENABLED = false;

    /**
     * Parses the metadata (all the fields of {@link TemporaryExposureKeyExport} except the keys). It
     * provides a memory efficient way to get the metadata.
     */
    public static TemporaryExposureKeyExport parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    /**
     * See {@link #parse(File)}
     */
    public static TemporaryExposureKeyExport parse(InputStream inputStream) throws IOException {
        TemporaryExposureKeyExportV1Header.readAndVerifyHeader(inputStream);

        CodedInputStream codedStream = CodedInputStream.newInstance(inputStream);
        TemporaryExposureKeyExport.Builder builder = TemporaryExposureKeyExport.newBuilder();
        int nextTag = codedStream.readTag();
        boolean hasSeenKey = false;
        while (nextTag != 0) {
            switch (WireFormat.getTagFieldNumber(nextTag)) {
                case TemporaryExposureKeyExport.START_TIMESTAMP_FIELD_NUMBER:
                    builder.setStartTimestamp(codedStream.readFixed64());
                    break;
                case TemporaryExposureKeyExport.END_TIMESTAMP_FIELD_NUMBER:
                    builder.setEndTimestamp(codedStream.readFixed64());
                    break;
                case TemporaryExposureKeyExport.REGION_FIELD_NUMBER:
                    builder.setRegion(codedStream.readString());
                    break;
                case TemporaryExposureKeyExport.BATCH_NUM_FIELD_NUMBER:
                    builder.setBatchNum(codedStream.readInt32());
                    break;
                case TemporaryExposureKeyExport.BATCH_SIZE_FIELD_NUMBER:
                    builder.setBatchSize(codedStream.readInt32());
                    break;
                case TemporaryExposureKeyExport.SIGNATURE_INFOS_FIELD_NUMBER:
                    SignatureInfo.Builder signatureBuilder = SignatureInfo.newBuilder();
                    codedStream.readMessage(signatureBuilder, ExtensionRegistryLite.getEmptyRegistry());
                    builder.addSignatureInfos(signatureBuilder);
                    break;
                case TemporaryExposureKeyExport.KEYS_FIELD_NUMBER:
                    hasSeenKey = true;
                    // fall through to skip keys.
                default:
                    // Skip unknown fields.
                    codedStream.skipField(nextTag);
                    break;
            }
            if (EARLY_RETURN_ENABLED && isMetadataComplete(builder, hasSeenKey)) {
                break;
            }
            nextTag = codedStream.readTag();
        }
        return builder.build();
    }

    private static boolean isMetadataComplete(
            TemporaryExposureKeyExport.Builder builder, boolean hasSeenKey) {
        return hasSeenKey
                && builder.hasStartTimestamp()
                && builder.hasEndTimestamp()
                && builder.hasRegion()
                && builder.hasBatchNum()
                && builder.hasBatchSize()
                && builder.getSignatureInfosCount() > 0;
    }

    private TemporaryExposureKeyFileMetadataParser() {
    }
}