package org.immuni.android.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.immuni.android.models.HealthProfile
import org.immuni.android.util.fromJson

@Entity(tableName = "health_profile_table")
class HealthProfileEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var userId: String = "",
    var surveyTimeMillis: Long = 0,
    var healthProfileJson: String = ""
) {
    @Transient
    private var _healthProfile: HealthProfile? = null
    val healthProfile: HealthProfile
        get() {
            if (_healthProfile == null) {
                _healthProfile = fromJson(healthProfileJson)
            }
            return _healthProfile!!
        }
}
