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
                _healthProfile = _healthProfile?.copy(
                    surveyAnswers = fixSerializationAliasingOfIntsToDoubles(
                        _healthProfile!!.surveyAnswers
                    )
                )
            }
            return _healthProfile!!
        }
}

// If you serialize a Map<Something, Any> to json, where Any only contains Ints, it gets
// deserialized as Map<Something, Any>, where Any contains Doubles. Let's fix that
fun fixSerializationAliasingOfIntsToDoubles(surveyAnswers: Map<String, Any>): Map<String, Any> {
    return surveyAnswers.mapValues {
        (it.value as List<Any>).map { answerIndexes ->
            when (answerIndexes) {
                is Double -> answerIndexes.toInt()
                is List<*> -> answerIndexes.map { answerIndex ->
                    (answerIndex as Double).toInt()
                }
                else -> answerIndexes
            }
        }
    }
}
