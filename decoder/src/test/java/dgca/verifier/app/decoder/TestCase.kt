package dgca.verifier.app.decoder

import com.fasterxml.jackson.annotation.JsonProperty
import dgca.verifier.app.decoder.model.GreenCertificate

data class TestCase(

    @JsonProperty("JSON")
    val eudgc: GreenCertificate? = null,

    @JsonProperty("CBOR")
    val cborHex: String? = null,

    @JsonProperty("COSE")
    val coseHex: String? = null,

    @JsonProperty("COMPRESSED")
    val compressedHex: String? = null,

    @JsonProperty("BASE45")
    val base45: String? = null,

    @JsonProperty("PREFIX")
    val base45WithPrefix: String? = null,

    @JsonProperty("2DCODE")
    val qrCodePng: String? = null,

    @JsonProperty("TESTCTX")
    val context: TestContext,

    @JsonProperty("EXPECTEDRESULTS")
    val expectedResult: TestExpectedResults
)

data class TestContext(

    @JsonProperty("VERSION")
    val version: Int,

    @JsonProperty("SCHEMA")
    val schema: String,

    @JsonProperty("CERTIFICATE")
    val certificate: String?,

    @JsonProperty("VALIDATIONCLOCK")
    val validationClock: String?,

    @JsonProperty("DESCRIPTION")
    val description: String
)

data class TestExpectedResults(

    @JsonProperty("EXPECTEDVALIDOBJECT")
    val schemaGeneration: Boolean? = null,

    @JsonProperty("EXPECTEDSCHEMAVALIDATION")
    val schemaValidation: Boolean? = null,

    @JsonProperty("EXPECTEDENCODE")
    val encodeGeneration: Boolean? = null,

    @JsonProperty("EXPECTEDDECODE")
    val cborDecode: Boolean? = null,

    @JsonProperty("EXPECTEDVERIFY")
    val coseSignature: Boolean? = null,

    @JsonProperty("EXPECTEDUNPREFIX")
    val prefix: Boolean? = null,

    @JsonProperty("EXPECTEDVALIDJSON")
    val json: Boolean? = null,

    @JsonProperty("EXPECTEDCOMPRESSION")
    val compression: Boolean? = null,

    @JsonProperty("EXPECTEDB45DECODE")
    val base45Decode: Boolean? = null,

    @JsonProperty("EXPECTEDPICTUREDECODE")
    val qrDecode: Boolean? = null,

    @JsonProperty("EXPECTEDEXPIRATIONCHECK")
    val expirationCheck: Boolean? = null,

    @JsonProperty("EXPECTEDKEYUSAGE")
    val keyUsage: Boolean? = null
)
