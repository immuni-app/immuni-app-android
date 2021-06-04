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
 *  Created by Mykhailo Nester on 4/23/21 9:51 AM
 */

package dgca.verifier.app.decoder.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.JSON_SCHEMA_V1
import dgca.verifier.app.decoder.cwt.CwtHeaderKeys
import dgca.verifier.app.decoder.model.VerificationResult

class DefaultSchemaValidator : SchemaValidator {

    override fun validate(cbor: ByteArray, verificationResult: VerificationResult): Boolean {
        var isValid = false
        try {
            val map = CBORObject.DecodeFromBytes(cbor)
            val hcert = map[CwtHeaderKeys.HCERT.asCBOR()]
            val json = hcert[CBORObject.FromObject(1)].ToJSONString()

            val mapper = ObjectMapper()
            val schemaNode: JsonNode = mapper.readTree(JSON_SCHEMA_V1)
            val jsonNode: JsonNode = mapper.readTree(json)

            val factory = JsonSchemaFactory.byDefault()
            val schema: JsonSchema = factory.getJsonSchema(schemaNode)

            val report: ProcessingReport = schema.validate(jsonNode)

            isValid = report.isSuccess
            verificationResult.isSchemaValid = isValid

        } catch (ex: Exception) {
        }

        return isValid
    }
}