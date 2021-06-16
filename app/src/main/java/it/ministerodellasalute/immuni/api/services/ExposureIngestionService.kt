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
import java.util.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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

    @JsonClass(generateAdapter = true)
    data class ValidateCunRequest(
        @field:Json(name = "padding") override val padding: String = "",
        @field:Json(name = "last_his_number") val healthInsuranceCard: String,
        @field:Json(name = "symptoms_started_on") val symptomOnsetDate: String?
    ) : RequestWithPadding

    @POST("/v1/ingestion/check-cun")
    suspend fun validateCun(
        @Header("Authorization") authorization: String,
        @Header("Immuni-Dummy-Data") isDummyData: Int,
        @Body body: ValidateCunRequest
    ): Response<ResponseBody>

    // endregion

    // region: Upload Teks
    @JsonClass(generateAdapter = true)
    data class UploadTeksRequest(
        @field:Json(name = "teks") val teks: List<TemporaryExposureKey>,
        @field:Json(name = "province") val province: Province,
        @field:Json(name = "exposure_detection_summaries") val exposureSummaries: List<ExposureSummary>,
        @field:Json(name = "padding") override val padding: String = "",
        @field:Json(name = "countries_of_interest") val countries: List<String>
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
        trentinoAltoAdige("Trentino-Alto Adige/Südtirol"),
        umbria("Umbria"),
        valleAosta("Valle d'Aosta"),
        veneto("Veneto"),
        abroad("");

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
                abroad -> listOf(
                    Province.abroad
                )
            }
        }
    }

    enum class Province(val code: String, val fullName: String) {
        agrigento("AG", "Agrigento"),
        alessandria("AL", "Alessandria"),
        ancona("AN", "Ancona"),
        aosta("AO", "Aosta"),
        arezzo("AR", "Arezzo"),
        ascoliPiceno("AP", "Ascoli Piceno"),
        asti("AT", "Asti"),
        avellino("AV", "Avellino"),
        bari("BA", "Bari"),
        barletta("BT", "Barletta-Andria-Trani"),
        belluno("BL", "Belluno"),
        benevento("BN", "Benevento"),
        bergamo("BG", "Bergamo"),
        biella("BI", "Biella"),
        bologna("BO", "Bologna"),
        bolzano("BZ", "Bolzano/Bozen"),
        brescia("BS", "Brescia"),
        brindisi("BR", "Brindisi"),
        cagliari("CA", "Cagliari"),
        caltanissetta("CL", "Caltanissetta"),
        campobasso("CB", "Campobasso"),
        caserta("CE", "Caserta"),
        catania("CT", "Catania"),
        catanzaro("CZ", "Catanzaro"),
        chieti("CH", "Chieti"),
        como("CO", "Como"),
        cosenza("CS", "Cosenza"),
        cremona("CR", "Cremona"),
        crotone("KR", "Crotone"),
        cuneo("CN", "Cuneo"),
        enna("EN", "Enna"),
        fermo("FM", "Fermo"),
        ferrara("FE", "Ferrara"),
        firenze("FI", "Firenze"),
        foggia("FG", "Foggia"),
        forliCesena("FC", "Forlì Cesena"),
        frosinone("FR", "Frosinone"),
        genova("GE", "Genova"),
        gorizia("GO", "Gorizia"),
        grosseto("GR", "Grosseto"),
        imperia("IM", "Imperia"),
        isernia("IS", "Isernia"),
        lAquila("AQ", "L\'Aquila"),
        laSpezia("SP", "La Spezia"),
        latina("LT", "Latina"),
        lecce("LE", "Lecce"),
        lecco("LC", "Lecco"),
        livorno("LI", "Livorno"),
        lodi("LO", "Lodi"),
        lucca("LU", "Lucca"),
        macerata("MC", "Macerata"),
        mantova("MN", "Mantova"),
        massaECarrara("MS", "Massa Carrara"),
        matera("MT", "Matera"),
        messina("ME", "Messina"),
        milano("MI", "Milano"),
        modena("MO", "Modena"),
        monzaEBrianza("MB", "Monza e Brianza"),
        napoli("NA", "Napoli"),
        novara("NO", "Novara"),
        nuoro("NU", "Nuoro"),
        oristano("OR", "Oristano"),
        padova("PD", "Padova"),
        palermo("PA", "Palermo"),
        parma("PR", "Parma"),
        pavia("PV", "Pavia"),
        perugia("PG", "Perugia"),
        pesaroEUrbino("PU", "Pesaro e Urbino"),
        pescara("PE", "Pescara"),
        piacenza("PC", "Piacenza"),
        pisa("PI", "Pisa"),
        pistoia("PT", "Pistoia"),
        pordenone("PN", "Pordenone"),
        potenza("PZ", "Potenza"),
        prato("PO", "Prato"),
        ragusa("RG", "Ragusa"),
        ravenna("RA", "Ravenna"),
        reggioCalabria("RC", "Reggio Calabria"),
        reggioEmilia("RE", "Reggio Emilia"),
        rieti("RI", "Rieti"),
        rimini("RN", "Rimini"),
        roma("RM", "Roma"),
        rovigo("RO", "Rovigo"),
        salerno("SA", "Salerno"),
        sassari("SS", "Sassari"),
        savona("SV", "Savona"),
        siena("SI", "Siena"),
        siracusa("SR", "Siracusa"),
        sondrio("SO", "Sondrio"),
        sudSardegna("SU", "Sud Sardegna"),
        taranto("TA", "Taranto"),
        teramo("TE", "Teramo"),
        terni("TR", "Terni"),
        torino("TO", "Torino"),
        trapani("TP", "Trapani"),
        trento("TN", "Trento"),
        treviso("TV", "Treviso"),
        trieste("TS", "Trieste"),
        udine("UD", "Udine"),
        varese("VA", "Varese"),
        venezia("VE", "Venezia"),
        verbania("VB", "Verbania-Cusio-Ossola"),
        vercelli("VC", "Vercelli"),
        verona("VR", "Verona"),
        viboValentia("VV", "Vibo Valentia"),
        vicenza("VI", "Vicenza"),
        viterbo("VT", "Viterbo"),
        abroad("EX", "");

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
