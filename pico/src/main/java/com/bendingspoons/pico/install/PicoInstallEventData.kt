package com.bendingspoons.pico.install

import com.bendingspoons.concierge.Concierge
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicoInstallEventData(
    @field:Json(name = "backup_persistent_id_status") val backupPersistentIdStatus: Concierge.CreationType,
    @field:Json(name = "non_backup_persistent_id_status") val nonBackupPersistentIdStatus: Concierge.CreationType,
    @field:Json(name = "new_app_version") val newAppVersion: String,
    @field:Json(name = "old_app_version") val oldAppVersion: String?,
    @field:Json(name = "old_bundle_version") val oldBundleVersion: String?
)
