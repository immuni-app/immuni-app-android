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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import it.ministerodellasalute.immuni.api.services.Language.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Configuration Settings Service API.
 */
interface ConfigurationSettingsService {
    @GET("v1/settings?platform=android")
    suspend fun settings(@Query("build") build: Long): Response<ConfigurationSettings>

    @GET
    suspend fun faqs(@Url url: String): Response<Faqs>
}

@JsonClass(generateAdapter = true)
data class ConfigurationSettings(
    @field:Json(name = "minimum_build_version") val minimumBuildVersion: Int,
    @field:Json(name = "faq_url") val faqUrls: Map<String, String>,
    @field:Json(name = "tou_url") val termsOfUseUrls: Map<String, String>,
    @field:Json(name = "pn_url") val privacyNoticeUrls: Map<String, String>,
    @field:Json(name = "exposure_configuration") val exposureConfiguration: ExposureConfiguration,
    @field:Json(name = "service_not_active_notification_period") val serviceNotActiveNotificationPeriod: Int,
    @field:Json(name = "onboarding_not_completed_notification_period") val onboardingNotCompletedNotificationPeriod: Int,
    @field:Json(name = "required_update_notification_period") val requiredUpdateNotificationPeriod: Int,
    @field:Json(name = "risk_reminder_notification_period") val riskReminderNotificationPeriod: Int,
    @field:Json(name = "exposure_info_minimum_risk_score") val exposureInfoMinimumRiskScore: Int,
    @field:Json(name = "exposure_detection_period") val exposureDetectionPeriod: Int,
    @field:Json(name = "dummy_teks_average_opportunity_waiting_time") val dummyTeksAverageOpportunityWaitingTime: Int,
    @field:Json(name = "dummy_teks_average_request_waiting_time") val dummyTeksAverageRequestWaitingTime: Int,
    @field:Json(name = "dummy_teks_request_probabilities") val dummyTeksRequestProbabilities: List<Double>,
    @field:Json(name = "teks_max_summary_count") val teksMaxSummaryCount: Int,
    @field:Json(name = "teks_max_info_count") val teksMaxInfoCount: Int,
    @field:Json(name = "teks_packet_size") val teksPacketSize: Int,
    @field:Json(name = "operational_info_with_exposure_sampling_rate") val operationalInfoWithExposureSamplingRate: Double,
    @field:Json(name = "operational_info_without_exposure_sampling_rate") val operationalInfoWithoutExposureSamplingRate: Double,
    @field:Json(name = "dummy_analytics_waiting_time") val dummyAnalyticsWaitingTime: Int

)

@JsonClass(generateAdapter = true)
data class ExposureConfiguration(
    @field:Json(name = "attenuation_thresholds") val attenuationThresholds: List<Int>,
    @field:Json(name = "attenuation_bucket_scores") val attenuationScores: List<Int>,
    @field:Json(name = "days_since_last_exposure_bucket_scores") val daysSinceLastExposureScores: List<Int>,
    @field:Json(name = "duration_bucket_scores") val durationScores: List<Int>,
    @field:Json(name = "transmission_risk_bucket_scores") val transmissionRiskScores: List<Int>,
    @field:Json(name = "minimum_risk_score") val minimumRiskScore: Int
)

@JsonClass(generateAdapter = true)
data class Faqs(
    @field:Json(name = "faqs") val faqs: List<Faq>
)

@JsonClass(generateAdapter = true)
data class Faq(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "content") val content: String
)

enum class Language(val code: String) {
    @Json(name = "en")
    EN("en"),

    @Json(name = "it")
    IT("it"),

    @Json(name = "de")
    DE("de"),

    @Json(name = "fr")
    FR("fr"),

    @Json(name = "es")
    ES("es");

    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: EN
    }
}

private fun languageMap(map: (Language) -> String): Map<String, String> {
    return mapOf(*Language.values().map { language ->
        language.code to map(language)
    }.toTypedArray())
}

val defaultSettings = ConfigurationSettings(
    minimumBuildVersion = 0,
    faqUrls = languageMap { "https://get.immuni.gov.it/docs/faq-${it.code}.json" },
    termsOfUseUrls = languageMap { "https://get.immuni.gov.it/docs/app-tou-${it.code}.html" },
    privacyNoticeUrls = languageMap { "https://get.immuni.gov.it/docs/app-pn-${it.code}.html" },

    exposureConfiguration = ExposureConfiguration(
        attenuationThresholds = listOf(50, 70),
        attenuationScores = listOf(0, 0, 3, 5, 7, 7, 7, 7),
        daysSinceLastExposureScores = listOf(1, 1, 1, 1, 1, 1, 1, 1),
        durationScores = listOf(0, 0, 0, 3, 5, 5, 5, 7),
        transmissionRiskScores = listOf(1, 1, 1, 1, 1, 1, 1, 1),
        minimumRiskScore = 1
    ),
    exposureInfoMinimumRiskScore = 20,
    exposureDetectionPeriod = 60 * 60 * 4, // 4 hours
    serviceNotActiveNotificationPeriod = 60 * 60 * 24, // 1 day
    onboardingNotCompletedNotificationPeriod = 60 * 60 * 24, // 1 day
    requiredUpdateNotificationPeriod = 60 * 60 * 24, // 1 day
    riskReminderNotificationPeriod = 60 * 60 * 24, // 1 day
    dummyTeksAverageOpportunityWaitingTime = 60 * 24 * 60 * 60, // 60 days
    dummyTeksAverageRequestWaitingTime = 10,
    dummyTeksRequestProbabilities = listOf(0.95, 0.1),
    teksMaxSummaryCount = 6 * 14,
    teksMaxInfoCount = 600,
    teksPacketSize = 110_000,
    operationalInfoWithExposureSamplingRate = 1.0,
    operationalInfoWithoutExposureSamplingRate = 0.6,
    dummyAnalyticsWaitingTime = 2_592_000
)

val defaultFaqs = mapOf(
    IT to listOf(
        Faq(
            title = "Cos'è Immuni?",
            content = "Immuni è un'app creata per aiutarci a combattere le epidemie, a partire da quella del COVID-19:\n\n • L'app si propone di avvertire gli utenti potenzialmente contagiati il prima possibile, anche quando sono asintomatici.\n\n • Questi utenti possono poi isolarsi per evitare di contagiare altri. Questo minimizza la diffusione del virus e, allo stesso tempo, velocizza il ritorno a una vita normale per la maggior parte della popolazione.\n\n • Venendo informati tempestivamente, gli utenti possono anche contattare il proprio medico di medicina generale prima e ridurre così il rischio di complicanze."
        ),
        Faq(
            title = "Come funziona il sistema di notifiche di esposizione di Immuni?",
            content = "Il sistema di notifiche di esposizione di Immuni mira ad avvertire gli utenti quando sono stati esposti a un utente potenzialmente contagioso.\n\nIl sistema è basato sulla tecnologia Bluetooth Low Energy e non utilizza dati di geolocalizzazione di alcun genere, inclusi quelli del GPS. L'app non raccoglie e non è in grado di ottenere alcun dato identificativo dell'utente, quali nome, cognome, data di nascita, indirizzo, numero di telefono o indirizzo email. Immuni riesce quindi a determinare che un contatto fra due utenti è avvenuto, ma non chi siano effettivamente i due utenti o dove si siano incontrati.\n\nDi seguito, una spiegazione semplificata di come funziona il sistema. Consideriamo l'esempio di Alice e Marco, due ipotetici utenti.\n\nUna volta installata da Alice, l'app fa sì che il suo smartphone emetta continuativamente un segnale Bluetooth Low Energy che include un codice casuale. Lo stesso vale per Marco. Quando Alice si avvicina a Marco, gli smartphone dei due utenti registrano nella propria memoria il codice casuale dell'altro, tenendo quindi traccia di quel contatto. Registrano anche quanto è durato il contatto e a che distanza erano i due smartphone approssimativamente.\n\nI codici sono generati del tutto casualmente, senza contenere alcuna informazione sul dispositivo o l'utente. Inoltre, sono modificati diverse volte ogni ora, in modo da proteggere ulteriormente la privacy degli utenti.\n\nSupponiamo che, successivamente, Marco risulti positivo al SARS-CoV-2. Con l'aiuto di un operatore sanitario, Marco potrà caricare su un server delle chiavi crittografiche dalle quali è possibile derivare i suoi codici casuali.\n\nPer ogni utente, l'app scarica periodicamente dal server le nuove chiavi crittografiche inviate dagli utenti che sono risultati positivi al virus. L'app usa queste chiavi per derivare i loro codici casuali e controlla se qualcuno di quei codici corrisponde a quelli registrati nella memoria dello smartphone nei giorni precedenti. In questo caso, l'app di Alice troverà il codice casuale di Marco, verificherà se la durata e la distanza del contatto siano state tali da aver potuto causare un contagio e, se sì, avvertirà Alice."
        ),
        Faq(
            title = "L'app traccia i miei spostamenti?",
            content = "No. Il sistema di notifiche di esposizione di Immuni si basa sulla tecnologia Bluetooth Low Energy e non raccoglie dati di geolocalizzazione di alcun genere, inclusi quelli del GPS. Immuni non è in grado di sapere dove vai o chi incontri."
        ),
        Faq(
            title = "Come viene tutelata la mia privacy?",
            content = "Durante l'intero processo di design e sviluppo di Immuni, abbiamo posto grande attenzione sulla tutela della tua privacy.\n\nEccoti una lista di alcune delle misure con cui Immuni protegge i tuoi dati:\n\n • L'app non raccoglie alcun dato che consentirebbe di risalire alla tua identità. Per esempio, non ti chiede e non è in grado di ottenere il tuo nome, cognome, data di nascita, indirizzo, numero di telefono o indirizzo email.\n\n • L'app non raccoglie alcun dato di geolocalizzazione, inclusi i dati del GPS. I tuoi spostamenti non sono tracciati in alcun modo.\n\n • Il codice Bluetooth Low Energy trasmesso dall'app è generato in maniera casuale e non contiene alcuna informazione riguardo al tuo smartphone, né su di te. Inoltre, questo codice cambia svariate volte ogni ora, per tutelare ancora meglio la tua privacy.\n\n • I dati salvati sul tuo smartphone sono cifrati.\n\n • Le connessioni tra l'app e il server sono cifrate.\n\n • Tutti i dati, siano essi salvati sul dispositivo o sul server, saranno cancellati non appena non saranno più necessari e in ogni caso non oltre il 31 dicembre 2020.\n\n • È il Ministero della Salute il soggetto che raccoglie i tuoi dati. I dati verranno usati solo per contenere l'epidemia del COVID-19 o per la ricerca scientifica.\n\n • I dati sono salvati su server in Italia e gestiti da soggetti pubblici."
        ),
        Faq(
            title = "Il codice è open source?",
            content = "Sì, il codice è open source e disponibile su GitHub. La licenza è la GNU Affero General Public License version 3."
        ),
        Faq(
            title = "Perché Immuni è importante?",
            content = "Tutti noi desideriamo ridurre la diffusione dell'epidemia, ridurre il rischio alla salute dei nostri cari e tornare al più presto a una vita normale.\n\nImmuni gioca un ruolo importante nella realizzazione di questi obiettivi. Grazie al sistema di notifiche di esposizione, l'app permette di avvertire rapidamente gli utenti che sono stati in prossimità di un utente contagioso, suggerendo l'isolamento e di contattare il proprio medico di medicina generale. Tutto questo è di cruciale importanza per minimizzare il numero di contagi e assicurarsi che gli utenti possano ricevere le giuste attenzioni mediche il prima possibile, minimizzando il rischio di complicanze."
        ),
        Faq(
            title = "È proprio necessario che tutti usino l'app? Cosa succede se non viene usata da un numero sufficiente di persone?",
            content = "Più persone usano Immuni, più l'app può essere efficace. Infatti, maggiore è la diffusione di Immuni, più sono i potenziali contagiati che l'app riesce ad avvertire e che possono quindi isolarsi, aiutando a contenere l'epidemia e ad accelerare il ritorno alla normalità.\n\nTuttavia, anche se la diffusione di Immuni fosse limitata, l'app potrà comunque contribuire a rallentare l'epidemia, specialmente in combinazione alle altre misure implementate dal governo. Questo rallentamento, anche se minimo, ridurrà la pressione sul Servizio Sanitario Nazionale, permettendo a più pazienti di ricevere cure appropriate e potenzialmente salvando molte vite. Nel frattempo, la ricerca scientifica avanza verso un possibile vaccino."
        ),
        Faq(
            title = "Come posso controllare se sto usando l'app correttamente?",
            content = "Fare un uso scorretto dell'app rende Immuni molto meno efficace e aumenta il rischio per te, per i tuoi cari e per tutta la comunità.\n\nPer accertarti che tu stia usando l'app come previsto, basterà aprirla e controllare che nella sezione Home ci sia scritto “Servizio attivo”. In caso contrario, premi sul tasto “Riattiva Immuni” e segui le istruzioni per riportare l'app a funzionare correttamente.\n\nQualche altro consiglio importante per assicurarti che Immuni possa essere efficace:\n\n • Quando esci di casa, porta sempre con te lo smartphone sul quale hai installato l'app.\n\n • Non disabilitare il Bluetooth (salvo quando stai dormendo, se lo desideri).\n\n • Non disinstallare l'app.\n\n • È di vitale importanza che, quando l'app ti manda una notifica, tu la legga, apra l'app e segua le indicazioni fornite. Per esempio, se l'app ti chiede di aggiornarla, per favore fallo. Se ti suggerisce di isolarti e di chiamare il tuo medico di medicina generale, è fondamentale che tu lo faccia immediatamente."
        ),
        Faq(
            title = "L'app fa diagnosi mediche o fornisce consigli medici?",
            content = "Immuni non fa e non può fare diagnosi. Sulla base dello storico della tua esposizione a utenti potenzialmente contagiosi, Immuni elabora alcune raccomandazioni su come è necessario comportarsi. Ma l'app non è un dispositivo medico e non può in alcun caso sostituire un medico."
        ),
        Faq(
            title = "Quali dispositivi e sistemi operativi sono supportati?",
            content = "Al momento, Immuni supporta gli smartphone con Bluetooth Low Energy e una versione di iOS pari o superiore alla 13.5 o di Android pari o superiore alla 6 (Marshmallow, API 23). Per gli smartphone Android, è anche richiesta una versione di Google Play Services pari o superiore alla 20.18.13.\n\nSe il tuo dispositivo lo consente, ti invitiamo ad aggiornare il sistema operativo a una versione che permetta l'uso di Immuni. Nel caso tu abbia uno smartphone Android, assicurati anche di aggiornare Google Play Services.\n\nSiamo consci dell'importanza di supportare il maggior numero di dispositivi possibile. I requisiti descritti sopra sono imposti dalle tecnologie per le notifiche di esposizione messe a disposizione da Apple e Google, che non sono disponibili per versioni precedenti di iOS, Android e Google Play Services."
        ),
        Faq(
            title = "Le istruzioni fornite dall'app sono attendibili?",
            content = "Le raccomandazioni fornite dall'app dipendono dalla durata della tua esposizione a utenti potenzialmente contagiosi e dalla distanza fra il tuo smartphone e quello di questi utenti durante l'esposizione.\n\nSi tratta di un numero limitato di informazioni, peraltro mai perfette, in quanto il segnale Bluetooth Low Energy è influenzato da vari fattori di disturbo. Quindi, la valutazione non sarà sempre impeccabile. Per esempio, se l'app ti raccomanda di isolarti, non significa che sicuramente hai il SARS-CoV-2. Significa piuttosto che, sulla base delle informazioni a disposizione dell'app, l'isolamento è la cosa più sicura da fare per te e per chi ti sta accanto.\n\nÈ quindi importante che tu segua le indicazioni fornite dall'app, per il bene tuo, dei tuoi cari e della comunità. Non esitare a consultare il tuo medico di medicina generale in caso l'app ti avverta di un possibile contagio."
        ),
        Faq(
            title = "Non ho uno smartphone compatibile con Immuni. Cosa posso fare?",
            content = "Senza uno smartphone compatibile, per il momento purtroppo non puoi usare Immuni.\n\nSiamo consci dell'importanza di consentire al maggior numero di persone possibile di usare Immuni. Comunicheremo prontamente eventuali novità in questo senso."
        ),
        Faq(
            title = "L'app scaricherà la batteria del mio smartphone?",
            content = "Non dovresti notare una differenza sostanziale nella durata della tua batteria. Immuni infatti usa il Bluetooth Low Energy, una tecnologia creata per essere particolarmente efficiente in termini di risparmio energetico.\n\nTuttavia, anche se pensi che la batteria del tuo smartphone si sia scaricata un po' più velocemente del solito, per favore continua a usare l'app in modo corretto. Il tuo contributo è importante perché Immuni sia efficace nell'aiutarci a combattere l'epidemia e tornare al più presto a una vita normale."
        ),
        Faq(
            title = "Dove posso scaricare Immuni? Quali dispositivi e sistemi operativi sono supportati?",
            content = "Da dove puoi scaricare Immuni dipende dallo smartphone che usi. Non tutti i dispositivi sono supportati. Scegli la versione compatibile col tuo smartphone.\n" +
                "\n" +
                "App Store\n" +
                "Puoi scaricare Immuni dall’App Store e usarla correttamente se il tuo iPhone ha iOS versione 13.5 o superiore. Aggiorna iOS all’ultima versione disponibile prima di effettuare il download di Immuni.\n" +
                "\n" +
                "I modelli di iPhone che supportano iOS 13.5 sono i seguenti: 11, 11 Pro, 11 Pro Max, Xr, Xs, Xs Max, X, SE (2nd generation), 8, 8 Plus, 7, 7 Plus, 6s, 6s Plus, SE (1st generation). Purtroppo, non potrai usare Immuni se il tuo modello di iPhone non permette l’aggiornamento di iOS a una versione pari o superiore alla 13.5.\n" +
                "\n" +
                "Google Play\n" +
                "Puoi scaricare Immuni da Google Play e usarla correttamente se il tuo smartphone Android verifica tutti e tre i seguenti requisiti:\n" +
                " • Bluetooth Low Energy\n" +
                " • Android versione 6 (Marshmallow, API 23) o superiore\n" +
                " • Google Play Services versione 20.18.13 o superiore\n" +
                "Aggiorna Android e Google Play Services all’ultima versione disponibile prima di effettuare il download di Immuni.\n" +
                "\n" +
                "Purtroppo, non potrai usare Immuni se il tuo modello di smartphone Android non ha il Bluetooth Low Energy o non permette l’aggiornamento di Android e di Google Play Services alle versioni minime indicate.\n" +
                "\n" +
                "AppGallery\n" +
                "Stiamo lavorando per permettere di scaricare Immuni anche da AppGallery al più presto. Questo consentirà ai possessori di alcuni modelli di smartphone Huawei di usare Immuni.\n"
        ),
        Faq(
            title = "I minori possono usare l'app?",
            content = "Devi avere almeno 14 anni per usare Immuni. Se hai almeno 14 anni ma meno di 18, per usare l'app devi avere il permesso di almeno uno dei tuoi genitori o di chi esercita la tua rappresentanza legale."
        ),
        Faq(
            title = "Posso accedere al mio profilo da dispositivi diversi?",
            content = "No. Con Immuni non crei un profilo come in tante altre app. Pertanto, se installi l'app su un nuovo dispositivo, non c'è modo per Immuni di riconoscere che sei sempre tu."
        ),
        Faq(
            title = "Immuni è gestito dal governo?",
            content = "Sì. Immuni è l'app di notifiche di esposizione del governo italiano, sviluppata dal Commissario Straordinario per l'Emergenza COVID-19 in collaborazione con il Ministero della Salute e il Ministero per l'Innovazione Tecnologica e la Digitalizzazione.\n\nPer Immuni, il governo italiano si avvale di una licenza perpetua e irrevocabile su tutto il codice, le grafiche, i testi e la documentazione concessa a titolo gratuito da Bending Spoons S.p.A.\n\nSotto il coordinamento del Ministero della Salute e con il supporto del Dipartimento per l'Innovazione Tecnologica e la Digitalizzazione, lavorano al progetto le società a controllo pubblico SoGEI S.p.A. e PagoPA S.p.A. insieme a Bending Spoons S.p.A., che continua a fornire un servizio di documentazione, design e sviluppo software, sempre a titolo completamente gratuito e senza autorità decisionale o accesso ai dati degli utenti."
        ),
        Faq(
            title = "Bisogna pagare per usare Immuni?",
            content = "No. Immuni è un'app completamente gratuita."
        ),
        Faq(
            title = "Posso decidere di non usare l'app?",
            content = "Immuni è uno strumento importante nella lotta a questa terribile epidemia e ciascun utente ne aumenta l'efficacia complessiva. Per questo ti consigliamo vivamente di installare l'app, usarla correttamente e incoraggiare parenti e amici a fare lo stesso. Tuttavia, non sei obbligato a usarla. La decisione spetta soltanto a te."
        ),
        Faq(
            title = "Immuni dice che potrei essere a rischio, ma io mi sento bene. Cosa devo fare?",
            content = "Ti suggeriamo vivamente di seguire tutte le raccomandazioni di Immuni. Ci sono molte persone asintomatiche che hanno diffuso il virus senza rendersene conto. Uno dei punti di forza di Immuni è proprio la capacità di avvertire queste persone. Per favore, fai la tua parte seguendo le raccomandazioni, anche se pensi di non essere contagioso."
        ),
        Faq(
            title = "Sono stato in un luogo o con una persona che vorrei rimanessero privati. Immuni mette a repentaglio la mia privacy?",
            content = "No. Il sistema è basato sulla tecnologia Bluetooth Low Energy e non utilizza dati di geolocalizzazione di alcun genere, inclusi quelli del GPS. I codici che gli smartphone si scambiano sono generati in maniera casuale e cambiano svariate volte ogni ora. Di conseguenza, l'app non può determinare dove sia avvenuto un contatto né coloro che vi hanno preso parte. La tua privacy è tutelata."
        ),
        Faq(
            title = "Devo fare una registrazione con indirizzo email e password?",
            content = "No. L'app non raccoglie alcun dato che consentirebbe di risalire alla tua identità. Per esempio, non ti chiede e non è in grado di ottenere il tuo nome, cognome, data di nascita, indirizzo, numero di telefono o indirizzo email."
        ),
        Faq(
            title = "Devo tenere l'app aperta per farla funzionare correttamente? Posso usare altre app?",
            content = "Immuni funziona in background. L'importante è che il tuo smartphone sia acceso e che il Bluetooth sia attivo. Puoi anche chiudere l'app manualmente—fintanto che la tieni installata, non ci sono problemi. Puoi usare tranquillamente altre app, come fai di solito."
        ),
        Faq(
            title = "Il Bluetooth del mio smartphone deve essere sempre attivo?",
            content = "Il sistema di notifiche di esposizione si basa su Bluetooth Low Energy. È necessario, quindi, che il Bluetooth sia sempre attivo affinché il sistema possa rilevare i tuoi contatti con gli altri utenti. Resti ovviamente libero di attivare o disattivare il Bluetooth quando preferisci."
        ),
        Faq(
            title = "Tengo spesso il mio smartphone in modalità aereo. Posso continuare a farlo?",
            content = "Sì, l'importante è che tu mantenga il Bluetooth attivo. In questo modo, Immuni continuerà a funzionare come previsto."
        ),
        Faq(
            title = "Quanto traffico dati consuma Immuni?",
            content = "Molto poco. Ogni giorno, l'app scarica le nuove chiavi crittografiche dei dispositivi degli utenti positivi al SARS-CoV-2 per controllare se sei stato esposto a loro ed eventualmente avvertirti. Puoi aspettarti che questa operazione consumi fino a qualche megabyte di traffico dati al giorno, più o meno come caricare una pagina di un sito con qualche foto."
        ),
        Faq(
            title = "L'app mi ha suggerito di fare un aggiornamento. Cosa succede se non lo faccio?",
            content = "Gli aggiornamenti sono volti a migliorare l'efficacia del sistema, anche correggendo potenziali difetti critici. Pertanto, è importante aggiornare Immuni quando è disponibile una nuova versione. Se l'aggiornamento è ritenuto necessario, l'app ti manderà una notifica. Tuttavia, la scelta se aggiornare o meno l'app sta a te."
        ),
        Faq(
            title = "Posso usare l'app senza connessione a Internet?",
            content = "Immuni non richiede una connessione a Internet continuativa. Tuttavia, l'app ha bisogno di connettersi almeno una volta al giorno per scaricare le informazioni necessarie a controllare se sei stato esposto a utenti potenzialmente contagiosi. Pertanto, assicurati che il tuo smartphone sia connesso a Internet almeno una volta al giorno."
        ),
        Faq(
            title = "Immuni condivide o vende i miei dati?",
            content = "I dati sono controllati dal Ministero della Salute. In nessun caso i tuoi dati verranno venduti o usati per qualsivoglia scopo commerciale, inclusa la profilazione a fini pubblicitari. Il progetto non ha alcun fine di lucro, ma nasce unicamente per aiutare a far fronte all'epidemia. Non è esclusa la condivisione di dati al fine di favorire la ricerca scientifica, ma solo previa completa anonimizzazione e aggregazione degli stessi."
        ),
        Faq(
            title = "Posso cambiare la lingua dell'app?",
            content = "Le lingue attualmente supportate dall'app sono l'italiano, l'inglese, il tedesco, il francese e lo spagnolo. L'app usa la stessa lingua che hai impostato sul tuo smartphone, se disponibile, altrimenti l'inglese. Perciò per cambiare la lingua dell'app dovrai cambiare la lingua del tuo dispositivo."
        )
    ),
    EN to listOf(
        Faq(
            title = "What is Immuni?",
            content = "Immuni is an app that helps us fight epidemics—starting with COVID-19:\n\n • The app aims to notify users at risk of carrying the virus as early as possible—even when they are asymptomatic.\n\n • These users can then self-isolate to avoid infecting others. This minimises the spread of the virus, while speeding up a return to normal life for most people.\n\n • By being alerted early, these users can also contact their general practitioner promptly and lower the risk of serious consequences."
        ),
        Faq(
            title = "How does Immuni's exposure notification system work?",
            content = "Immuni's exposure notification system aims to alert users when they have been exposed to a potentially infectious user.\n\nThe system is based on Bluetooth Low Energy technology and doesn't use any kind of geolocalisation whatsoever, including GPS data. The app doesn't (and can't) collect any data that would identify the user, such as their name, date of birth, address, telephone number, or email address. Therefore, Immuni is able to determine that contact has taken place between two users without knowing who those users are and where the contact occurred.\n\nHere is a simplified explanation of how the system works. Let's consider the example of two hypothetical users, Alice and Marco:\n\nAlice installs the Immuni app. Her smartphone starts sending a Bluetooth Low Energy signal that contains a random code. It does this on a continuous basis. The same goes for Marco. When Alice and Marco are in close proximity, their smartphones mutually store each other's random code, taking note of that event. Their phones also note how long the event lasted and the approximate distance between the two devices.\n\nThe codes are generated randomly, and they don't contain any information about the user or their device. They also change several times each hour, protecting user privacy even more.\n\nLet's suppose that Marco later tests positive for SARS-CoV-2. Thanks to the help of a healthcare operator, Marco is able to transfer some cryptographic keys to a server. From these keys, it is possible to derive Marco's random codes.\n\nFor each user, the app regularly downloads all the new cryptographic keys sent to the server by those users who tested positive for the virus. The app uses these keys to derive their random codes and checks if any correspond to those stored in the device memory from previous days. As such, Alice's app will find Marco's random code, it will check the length and the distance of the contact to evaluate the risk of an infection, and, if necessary, it will notify Alice."
        ),
        Faq(
            title = "Does the app track my location?",
            content = "No. Immuni's exposure notification system is based on Bluetooth Low Energy and doesn't collect any geolocation data, including GPS data. Immuni doesn't (and can't) know where you go or who you meet."
        ),
        Faq(
            title = "How's my privacy protected?",
            content = "Throughout the entirety of Immuni's design and development, we have placed enormous focus on privacy protection.\n\nHere's a list of some of the measures Immuni uses to protect your data:\n\n • The app doesn't collect any data that could lead to it knowing your identity. For example, it doesn't ask for (and can't obtain) your name, date of birth, address, telephone number, or email address.\n\n • The app doesn't collect any geolocation data, including GPS data. Your movements aren't tracked in any shape or form.\n\n • The Bluetooth Low Energy code broadcast by the app is generated completely randomly and doesn't contain any information about you or your device. This code changes several times each hour, protecting your privacy even more.\n\n • All Immuni data stored on your smartphone is encrypted.\n\n • All connections between the app and the server are encrypted.\n\n • All data, whether stored on the device or on the server, is deleted when no longer relevant, and certainly no later than December 31, 2020.\n\n • The Ministry of Health is the body that collects your data. The data is used solely with the aim of containing the COVID-19 epidemic or for scientific research.\n\n • The data is stored on servers located in Italy and managed by public bodies."
        ),
        Faq(
            title = "Is the code open source?",
            content = "Yes, the code is open-source and is available on GitHub. This is the licence: GNU Affero General Public License version 3."
        ),
        Faq(
            title = "Why is Immuni important?",
            content = "Everybody wants to reduce the spread of the epidemic, minimise the risk to our loved ones, and return to a normal life.\n\nImmuni plays an important role in achieving these goals. Thanks to its exposure notification system, the app makes it possible to quickly alert those who may have been exposed to a potentially infectious user, suggesting actions like self-isolation and calling a general practitioner. Such measures are critical in minimising the number of infections and ensuring that those affected can receive prompt, suitable medical attention—which, in turn, reduces the risk of complications."
        ),
        Faq(
            title = "Is it really necessary that everybody uses the app? What happens if not enough people do so?",
            content = "Immuni gets increasingly effective as more people use the app. The greater the uptake of Immuni, the higher the number of potentially infected users the app can notify. These users can then self-isolate, helping to contain the epidemic and speeding up the return to a normal life.\n\nHowever, even if the uptake of Immuni ends up being limited, the app will still contribute to slowing down the epidemic—especially in combination with the other measures implemented by the government. Any slowdown would relieve pressure on the National Healthcare Service and help a higher proportion of patients receive proper care—potentially saving many lives. A slowdown would also buy time for the scientific community, as it strives to create a vaccine."
        ),
        Faq(
            title = "What should I do to make sure I'm using the app correctly?",
            content = "Failing to use the app correctly makes Immuni much less effective and increases the risk to you, your loved ones, and the community.\n\nTo make sure you are using the app as intended, simply open it and check that 'Service active' is written in the Home section. If it's not, tap on 'Reactivate Immuni' and follow the instructions to make the app work correctly.\n\nSome other important suggestions to ensure that Immuni is effective:\n\n • When you leave your house, always bring your smartphone with Immuni installed.\n\n • Don't disable Bluetooth (except when you're sleeping, if you prefer).\n\n • Don't uninstall the app.\n\n • When the app sends you a notification, it's vital that you read it, open the app, and follow all the instructions provided. For example, if the app asks you to upgrade it, please do so. If the app recommends that you self-isolate and call your general practitioner, it's crucial that you do so right away."
        ),
        Faq(
            title = "Does this app make medical diagnoses or provide medical advice?",
            content = "Immuni does not and cannot diagnose. Based on your history of exposure to potentially contagious users, it makes recommendations about what to do next. But the app is not a medical device, and it's certainly not a substitute for a doctor."
        ),
        Faq(
            title = "What devices and operating systems are supported?",
            content = "Immuni currently supports smartphones with Bluetooth Low Energy and running iOS 13.5 or above or Android 6 (Marshmallow, API 23) or above. Android devices must also run Google Play 20.18.13 or above.\n\nIf possible, we invite you to update the operating system to a version that enables you to use Immuni. If you have an Android device, make sure you also update Google Play Services.\n\nWe're aware of the importance of supporting the highest possible number of devices. The prerequisites mentioned above are imposed by the exposure notification technologies provided by Apple and Google, which are not available for previous versions of iOS, Android, and Google Play Services."
        ),
        Faq(
            title = "Are the instructions that the app provides reliable?",
            content = "The app's recommendations depend on the duration of your exposure to potentially contagious users and on the distance between your smartphones during such exposure.\n\nThis information is limited and can never be perfect, as Bluetooth Low Energy signals are impacted by various disruptive factors. As such, the app's assessments won't always be flawless. If the app recommends that you self-isolate, it doesn't mean you definitely have SARS-CoV-2. It just means that, based on the information the app has available, isolating yourself is the safest thing to do for yourself and those around you.\n\nTherefore, it's crucial that you follow all the instructions provided by the app—for your own good, as well as that of your loved ones and everyone else. Please don't hesitate to get in touch with your general practitioner if the app notifies you of a possible infection."
        ),
        Faq(
            title = "I don't have a smartphone compatible with Immuni—what should I do?",
            content = "Without a compatible smartphone, it's not possible to use Immuni at the moment.\n\nWe're aware how important it is that the highest possible number of people can use Immuni. If there is any news on this topic, rest assured that we'll share it promptly."
        ),
        Faq(
            title = "Is the app going to drain my smartphone's battery?",
            content = "You shouldn't notice a difference in terms of battery life. Immuni uses Bluetooth Low Energy, a technology designed to be particularly energy efficient.\n\nHowever, even if you think your smartphone's battery is being drained a touch faster than usual, please keep using the app the right way. Your contribution is an important part of making Immuni effective at helping us fight the epidemic and returning to normal as soon as possible."
        ),
        Faq(
            title = "Where can I download Immuni? What devices and operating systems are supported?",
            content = "Where you can download Immuni from depends on the smartphone you use. Please note that not all devices are supported. Choose the version compatible with your smartphone.\n" +
                "\n" +
                "App Store\n" +
                "You can download Immuni from the App Store and use it correctly if your iPhone supports running iOS version 13.5 or above. Please update iOS to the latest available version before downloading Immuni.\n" +
                "\n" +
                "The following iPhone models support iOS 13.5: 11, 11 Pro, 11 Pro Max, Xr, Xs, Xs Max, X, SE (2nd generation), 8, 8 Plus, 7, 7 Plus, 6s, 6s Plus, SE (1st generation). Unfortunately, you won’t be able to use Immuni if your iPhone model doesn’t support running iOS version 13.5 or above.\n" +
                "\n" +
                "Google Play\n" +
                "You can download Immuni from Google Play and use it correctly if your Android smartphone fulfils all three of the following requirements:\n" +
                " • Bluetooth Low Energy\n" +
                " • Android version 6 (Marshmallow, API 23) or above\n" +
                " • Google Play Services version 20.18.13 or above\n" +
                "Please update Android and Google Play Services to the latest available version before downloading Immuni.\n" +
                "\n" +
                "Unfortunately, you won’t be able to use Immuni if your model of Android smartphone doesn’t feature Bluetooth Low Energy or doesn’t support the minimum Android and Google Play Services versions indicated above.\n" +
                "\n" +
                "AppGallery\n" +
                "We’re working to make Immuni available for download from AppGallery as soon as possible. This will enable the users of certain Huawei smartphone models to use Immuni.\n"
        ),
        Faq(
            title = "Can minors use the app?",
            content = "You must be at least 14 years old to use Immuni. If you're between 14 and 18, you must have the authorisation of at least one of your parents or of your legal guardian."
        ),
        Faq(
            title = "Can I access my profile from multiple devices?",
            content = "No. With Immuni, you don't create a profile like you're used to doing with many other apps. Therefore, if you install the app on a new device, there's no way for Immuni to understand that you're the same user."
        ),
        Faq(
            title = "Is Immuni run by the government?",
            content = "Yes. Immuni is the exposure notification app of the Italian government, developed by the Extraordinary Commissioner for the COVID-19 Emergency, in collaboration with the Ministry of Health and the Ministry for Innovation Technology and Digitalization.\n\nFor Immuni, the government has a perpetual and irrevocable licence for the complete code, design, copy, and documentation, granted for free by Bending Spoons S.p.A.\n\nUnder the coordination of the Ministry of Health, and with the support of the Innovation Technology and Digitalization Department, SoGEI S.p.A. and PagoPA S.p.A—publicly controlled companies—are working on the project, together with Bending Spoons S.p.A. The latter continues to provide documentation, design, and software development services free of charge, without decision-making authority or access to user data."
        ),
        Faq(
            title = "Do I need to pay to use Immuni?",
            content = "No. Immuni is completely free of charge."
        ),
        Faq(
            title = "Can I choose not to use the app?",
            content = "Immuni is a valuable tool in the fight against this horrendous epidemic, and every single user increases its overall effectiveness. We strongly urge you to install Immuni, use it correctly, and encourage your friends and loved ones to do likewise. However, you are not compelled to use it. It is entirely your choice."
        ),
        Faq(
            title = "Immuni tells me I may be at risk, but I feel fine. What should I do?",
            content = "We urge you to follow Immuni's guidance in full. There are many asymptomatic people who have unknowingly spread the disease. One of Immuni's biggest strengths is in notifying these people. Please, play your part by following all the recommendations, even if you doubt that you're contagious."
        ),
        Faq(
            title = "I was somewhere or with someone that I would like to keep private. Does Immuni compromise this?",
            content = "No. The system is based on Bluetooth Low Energy technology, which doesn't use any geolocation data whatsoever, including GPS data. The codes broadcast by the smartphones are randomly generated and change multiple times each hour. Therefore, the app can't tell where any contact with a potentially contagious user took place, nor the identities of those involved. As such, your privacy is protected."
        ),
        Faq(
            title = "Do I need to sign up with my email address and password?",
            content = "No. The app doesn't collect any data that would make it possible to know your identity. For example, it doesn't ask for (and can't obtain) your name, date of birth, address, telephone number, or email address."
        ),
        Faq(
            title = "Does Immuni need to be in the foreground to work? Can I use other apps?",
            content = "Immuni operates in the background, so the important thing is that your smartphone is turned on and Bluetooth is active. You can also terminate the app manually and use other apps as normal—as long as you have it installed on your device, you're all set."
        ),
        Faq(
            title = "Do I need to keep my smartphone's Bluetooth turned on all the time?",
            content = "The exposure notification system is based on Bluetooth Low Energy, so Bluetooth must always be active for the system to detect your contact with other users. However, it is your choice and you are free to turn it on or off as you like."
        ),
        Faq(
            title = "I often keep my phone on airplane mode. Is this OK?",
            content = "Yes, as long as you still keep Bluetooth active. That way, Immuni will continue to work as intended."
        ),
        Faq(
            title = "How much data traffic does Immuni consume?",
            content = "Very little. Every day, the app downloads the new cryptographic keys of SARS-CoV-2-positive users' devices. It does this to check if you have been exposed to them, and it notifies you if required. You can expect this to consume up to a few megabytes of your data allowance every day—roughly similar to loading one page of a site with a few photos."
        ),
        Faq(
            title = "The app prompted me to update it: what happens if I don't do so?",
            content = "The updates aim to improve the effectiveness of the system, including fixing potentially critical issues. Therefore, it's important to update your Immuni app when a new version becomes available. If the update is deemed necessary, the app will send you a notification. However, the decision of whether or not to update the app is ultimately up to you."
        ),
        Faq(
            title = "Can I use the app without Internet connection?",
            content = "Immuni doesn't require a continuous Internet connection. However, the app does need to connect to the Internet at least once a day. This is so that the app can download the information necessary to check if you've been exposed to potentially contagious users. Therefore, please make sure your smartphone is connected to the Internet at least once a day."
        ),
        Faq(
            title = "Does Immuni share or sell my data?",
            content = "All data is controlled by the Ministry of Health. In no case will your data be sold or used for commercial reasons, including profiling for advertising purposes. This is a non-profit project that stems only from the desire to help defeat the epidemic. Data may be shared to facilitate scientific research, but only after its complete anonymisation and aggregation."
        ),
        Faq(
            title = "Can I change the language of the app?",
            content = "The languages currently supported are Italian, English, German, French, and Spanish. The app uses the same language that's set on your smartphone, where available. Otherwise, it uses English. Therefore, to change the language of the app, you'll need to change the language of your device."
        )
    )
)
