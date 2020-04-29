package org.immuni.android.analytics.install

import org.immuni.android.ids.Ids
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicoInstallEventData(
    @field:Json(name = "backup_persistent_id_status") val backupPersistentIdStatus: Ids.CreationType,
    @field:Json(name = "non_backup_persistent_id_status") val nonBackupPersistentIdStatus: Ids.CreationType,
    @field:Json(name = "new_app_version") val newAppVersion: String,
    @field:Json(name = "old_app_version") val oldAppVersion: String?,
    @field:Json(name = "old_bundle_version") val oldBundleVersion: String?
)
