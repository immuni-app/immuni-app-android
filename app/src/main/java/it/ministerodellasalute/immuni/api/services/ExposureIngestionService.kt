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

package it.ministerodellasalute.immuni.api.services

import com.squareup.moshi.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Exposure Ingestion Service API.
 */
interface ExposureIngestionService {
    interface RequestWithPadding {
        val padding: String
    }

    // region: Validate Otp
    @JsonClass(generateAdapter = true)
    data class ValidateOtpRequest(
        @field:Json(name = "padding") override val padding: String = ""
    ) : RequestWithPadding

    @POST("/v1/ingestion/check-otp")
    suspend fun validateOtp(
        @Header("Authorization") authorization: String,
        @Header("Immuni-Dummy-Data") isDummyData: Int,
        @Body body: ValidateOtpRequest = ValidateOtpRequest()
    ): Response<ResponseBody>
    // endregion

    // region: Upload Teks
    @JsonClass(generateAdapter = true)
    data class UploadTeksRequest(
        @field:Json(name = "teks") val teks: List<TemporaryExposureKey>,
        @field:Json(name = "province") val province: Province,
        @field:Json(name = "exposure_detection_summaries") val exposureSummaries: List<ExposureSummary>,
        @field:Json(name = "padding") override val padding: String = ""
    ) : RequestWithPadding

    @POST("/v1/ingestion/upload")
    suspend fun uploadTeks(
        @Header("Authorization") authorization: String,
        @Header("Immuni-Client-Clock") systemTime: Int,
        @Header("Immuni-Dummy-Data") isDummyData: Int,
        @Body body: UploadTeksRequest
    ): Response<ResponseBody>
    // endregion

    // region: Models
    @JsonClass(generateAdapter = true)
    data class TemporaryExposureKey(
        @field:Json(name = "key_data") val keyData: String,
        @field:Json(name = "rolling_start_number") val rollingStartIntervalNumber: Int,
        @field:Json(name = "rolling_period") val rollingPeriod: Int
    )

    @JsonClass(generateAdapter = true)
    data class ExposureSummary(
        @field:Json(name = "date") val date: String,
        @field:Json(name = "matched_key_count") val matchedKeyCount: Int,
        @field:Json(name = "days_since_last_exposure") val daysSinceLastExposure: Int,
        @field:Json(name = "attenuation_durations") val attenuationDurations: List<Int>,
        @field:Json(name = "maximum_risk_score") val maximumRiskScore: Int,
        @field:Json(name = "exposure_info") val exposureInfo: List<ExposureInformation>
    )

    @JsonClass(generateAdapter = true)
    data class ExposureInformation(
        @field:Json(name = "date") val date: String,
        @field:Json(name = "duration") val duration: Int,
        @field:Json(name = "attenuation_value") val attenuationValue: Int,
        @field:Json(name = "attenuation_durations") val attenuationDurations: List<Int>,
        @field:Json(name = "transmission_risk_level") val transmissionRiskLevel: Int,
        @field:Json(name = "total_risk_score") val totalRiskScore: Int
    )

    /**
     * Regions in Italy
     */
    enum class Region(val region: String) {
        abruzzo("Abruzzo"),
        basilicata("Basilicata"),
        calabria("Calabria"),
        campania("Campania"),
        emiliaRomagna("Emilia-Romagna"),
        friuliVeneziaGiulia("Friuli-Venezia Giulia"),
        lazio("Lazio"),
        liguria("Liguria"),
        lombardia("Lombardia"),
        marche("Marche"),
        molise("Molise"),
        piemonte("Piemonte"),
        puglia("Puglia"),
        sardegna("Sardegna"),
        sicilia("Sicilia"),
        toscana("Toscana"),
        trentinoAltoAdige("Trentino-Alto Adige"),
        umbria("Umbria"),
        valleAosta("Valle d'Aosta"),
        veneto("Veneto");

        fun provinces(): List<Province> {
            return when (this) {
                abruzzo -> listOf(
                    Province.chieti,
                    Province.lAquila,
                    Province.pescara,
                    Province.teramo
                )
                basilicata -> listOf(
                    Province.matera,
                    Province.potenza
                )
                calabria -> listOf(
                    Province.catanzaro,
                    Province.cosenza,
                    Province.crotone,
                    Province.reggioCalabria,
                    Province.viboValentia
                )
                campania -> listOf(
                    Province.avellino,
                    Province.benevento,
                    Province.caserta,
                    Province.napoli,
                    Province.salerno
                )
                emiliaRomagna -> listOf(
                    Province.bologna,
                    Province.ferrara,
                    Province.forliCesena,
                    Province.modena,
                    Province.parma,
                    Province.piacenza,
                    Province.ravenna,
                    Province.reggioEmilia,
                    Province.rimini
                )
                friuliVeneziaGiulia -> listOf(
                    Province.udine,
                    Province.gorizia,
                    Province.pordenone,
                    Province.trieste
                )
                lazio -> listOf(
                    Province.frosinone,
                    Province.latina,
                    Province.rieti,
                    Province.roma,
                    Province.viterbo
                )
                liguria -> listOf(
                    Province.genova,
                    Province.imperia,
                    Province.laSpezia,
                    Province.savona
                )
                lombardia -> listOf(
                    Province.bergamo,
                    Province.brescia,
                    Province.como,
                    Province.cremona,
                    Province.lecco,
                    Province.lodi,
                    Province.mantova,
                    Province.milano,
                    Province.monzaEBrianza,
                    Province.pavia,
                    Province.sondrio,
                    Province.varese
                )
                marche -> listOf(
                    Province.ancona,
                    Province.ascoliPiceno,
                    Province.fermo,
                    Province.macerata,
                    Province.pesaroEUrbino
                )
                piemonte -> listOf(
                    Province.alessandria,
                    Province.asti,
                    Province.biella,
                    Province.cuneo,
                    Province.novara,
                    Province.torino,
                    Province.vercelli,
                    Province.verbania
                )
                molise -> listOf(
                    Province.campobasso,
                    Province.isernia
                )
                puglia -> listOf(
                    Province.bari,
                    Province.brindisi,
                    Province.lecce,
                    Province.foggia,
                    Province.taranto,
                    Province.barletta
                )
                sardegna -> listOf(
                    Province.cagliari,
                    Province.nuoro,
                    Province.oristano,
                    Province.sassari,
                    Province.sudSardegna
                )
                sicilia -> listOf(
                    Province.agrigento,
                    Province.caltanissetta,
                    Province.catania,
                    Province.enna,
                    Province.messina,
                    Province.palermo,
                    Province.ragusa,
                    Province.siracusa,
                    Province.trapani
                )
                toscana -> listOf(
                    Province.arezzo,
                    Province.firenze,
                    Province.grosseto,
                    Province.livorno,
                    Province.lucca,
                    Province.massaECarrara,
                    Province.pisa,
                    Province.pistoia,
                    Province.prato,
                    Province.siena
                )
                umbria -> listOf(
                    Province.perugia,
                    Province.terni
                )
                trentinoAltoAdige -> listOf(
                    Province.bolzano,
                    Province.trento
                )
                valleAosta -> listOf(
                    Province.aosta
                )
                veneto -> listOf(
                    Province.belluno,
                    Province.padova,
                    Province.rovigo,
                    Province.treviso,
                    Province.venezia,
                    Province.verona,
                    Province.vicenza
                )
            }
        }
    }

    enum class Province(val code: String, val fullName: String) {
        @Json(name = "AG") agrigento("AG", "Agrigento"),
        @Json(name = "AL") alessandria("AL", "Alessandria"),
        @Json(name = "AN") ancona("AN", "Ancona"),
        @Json(name = "AO") aosta("AO", "Aosta"),
        @Json(name = "AR") arezzo("AR", "Arezzo"),
        @Json(name = "AP") ascoliPiceno("AP", "Ascoli Piceno"),
        @Json(name = "AT") asti("AT", "Asti"),
        @Json(name = "AV") avellino("AV", "Avellino"),
        @Json(name = "BA") bari("BA", "Bari"),
        @Json(name = "BT") barletta("BT", "Barletta-Andria-Trani"),
        @Json(name = "BL") belluno("BL", "Belluno"),
        @Json(name = "BN") benevento("BN", "Benevento"),
        @Json(name = "BG") bergamo("BG", "Bergamo"),
        @Json(name = "BI") biella("BI", "Biella"),
        @Json(name = "BO") bologna("BO", "Bologna"),
        @Json(name = "BZ") bolzano("BZ", "Bolzano"),
        @Json(name = "BS") brescia("BS", "Brescia"),
        @Json(name = "BR") brindisi("BR", "Brindisi"),
        @Json(name = "CA") cagliari("CA", "Cagliari"),
        @Json(name = "CL") caltanissetta("CL", "Caltanissetta"),
        @Json(name = "CB") campobasso("CB", "Campobasso"),
        @Json(name = "CE") caserta("CE", "Caserta"),
        @Json(name = "CT") catania("CT", "Catania"),
        @Json(name = "CZ") catanzaro("CZ", "Catanzaro"),
        @Json(name = "CH") chieti("CH", "Chieti"),
        @Json(name = "CO") como("CO", "Como"),
        @Json(name = "CS") cosenza("CS", "Cosenza"),
        @Json(name = "CR") cremona("CR", "Cremona"),
        @Json(name = "KR") crotone("KR", "Crotone"),
        @Json(name = "CN") cuneo("CN", "Cuneo"),
        @Json(name = "EN") enna("EN", "Enna"),
        @Json(name = "FM") fermo("FM", "Fermo"),
        @Json(name = "FE") ferrara("FE", "Ferrara"),
        @Json(name = "FI") firenze("FI", "Firenze"),
        @Json(name = "FG") foggia("FG", "Foggia"),
        @Json(name = "FC") forliCesena("FC", "Forlì Cesena"),
        @Json(name = "FR") frosinone("FR", "Frosinone"),
        @Json(name = "GE") genova("GE", "Genova"),
        @Json(name = "GO") gorizia("GO", "Gorizia"),
        @Json(name = "GR") grosseto("GR", "Grosseto"),
        @Json(name = "IM") imperia("IM", "Imperia"),
        @Json(name = "IS") isernia("IS", "Isernia"),
        @Json(name = "AQ") lAquila("AQ", "L\'Aquila"),
        @Json(name = "SP") laSpezia("SP", "La Spezia"),
        @Json(name = "LT") latina("LT", "Latina"),
        @Json(name = "LE") lecce("LE", "Lecce"),
        @Json(name = "LC") lecco("LC", "Lecco"),
        @Json(name = "LI") livorno("LI", "Livorno"),
        @Json(name = "LO") lodi("LO", "Lodi"),
        @Json(name = "LU") lucca("LU", "Lucca"),
        @Json(name = "MC") macerata("MC", "Macerata"),
        @Json(name = "MN") mantova("MN", "Mantova"),
        @Json(name = "MS") massaECarrara("MS", "Massa Carrara"),
        @Json(name = "MT") matera("MT", "Matera"),
        @Json(name = "ME") messina("ME", "Messina"),
        @Json(name = "MI") milano("MI", "Milano"),
        @Json(name = "MO") modena("MO", "Modena"),
        @Json(name = "MB") monzaEBrianza("MB", "Monza e Brianza"),
        @Json(name = "NA") napoli("NA", "Napoli"),
        @Json(name = "NO") novara("NO", "Novara"),
        @Json(name = "NU") nuoro("NU", "Nuoro"),
        @Json(name = "OR") oristano("OR", "Oristano"),
        @Json(name = "PD") padova("PD", "Padova"),
        @Json(name = "PA") palermo("PA", "Palermo"),
        @Json(name = "PR") parma("PR", "Parma"),
        @Json(name = "PV") pavia("PV", "Pavia"),
        @Json(name = "PG") perugia("PG", "Perugia"),
        @Json(name = "PU") pesaroEUrbino("PU", "Pesaro e Urbino"),
        @Json(name = "PE") pescara("PE", "Pescara"),
        @Json(name = "PC") piacenza("PC", "Piacenza"),
        @Json(name = "PI") pisa("PI", "Pisa"),
        @Json(name = "PT") pistoia("PT", "Pistoia"),
        @Json(name = "PN") pordenone("PN", "Pordenone"),
        @Json(name = "PZ") potenza("PZ", "Potenza"),
        @Json(name = "PO") prato("PO", "Prato"),
        @Json(name = "RG") ragusa("RG", "Ragusa"),
        @Json(name = "RA") ravenna("RA", "Ravenna"),
        @Json(name = "RC") reggioCalabria("RC", "Reggio Calabria"),
        @Json(name = "RE") reggioEmilia("RE", "Reggio Emilia"),
        @Json(name = "RI") rieti("RI", "Rieti"),
        @Json(name = "RN") rimini("RN", "Rimini"),
        @Json(name = "RM") roma("RM", "Roma"),
        @Json(name = "RO") rovigo("RO", "Rovigo"),
        @Json(name = "SA") salerno("SA", "Salerno"),
        @Json(name = "SS") sassari("SS", "Sassari"),
        @Json(name = "SV") savona("SV", "Savona"),
        @Json(name = "SI") siena("SI", "Siena"),
        @Json(name = "SR") siracusa("SR", "Siracusa"),
        @Json(name = "SO") sondrio("SO", "Sondrio"),
        @Json(name = "SU") sudSardegna("SU", "Sud Sardegna"),
        @Json(name = "TA") taranto("TA", "Taranto"),
        @Json(name = "TE") teramo("TE", "Teramo"),
        @Json(name = "TR") terni("TR", "Terni"),
        @Json(name = "TO") torino("TO", "Torino"),
        @Json(name = "TP") trapani("TP", "Trapani"),
        @Json(name = "TN") trento("TN", "Trento"),
        @Json(name = "TV") treviso("TV", "Treviso"),
        @Json(name = "TS") trieste("TS", "Trieste"),
        @Json(name = "UD") udine("UD", "Udine"),
        @Json(name = "VA") varese("VA", "Varese"),
        @Json(name = "VE") venezia("VE", "Venezia"),
        @Json(name = "VB") verbania("VB", "Verbania-Cusio-Ossola"),
        @Json(name = "VC") vercelli("VC", "Vercelli"),
        @Json(name = "VR") verona("VR", "Verona"),
        @Json(name = "VV") viboValentia("VV", "Vibo Valentia"),
        @Json(name = "VI") vicenza("VI", "Vicenza"),
        @Json(name = "VT") viterbo("VT", "Viterbo");

        companion object {
            fun fromCode(code: String) = values().first { province -> province.code == code }
        }

        class MoshiAdapter : JsonAdapter<Province>() {
            override fun fromJson(reader: JsonReader): Province? {
                return fromCode(
                    reader.nextString()
                )
            }

            override fun toJson(writer: JsonWriter, value: Province?) {
                writer.value(value?.code)
            }
        }
    }
    // endregion
}
