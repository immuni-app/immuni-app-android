package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class OracleSettings(
    @field:Json(name = "skip_paywall") var skipPaywall: Boolean = false,
    @field:Json(name = "__privacy_notice_url__") var privacyUrl: String? = null,
    @field:Json(name = "__tos_url__") var tosUrl: String? = null,
    @field:Json(name = "__is_free__") var isFree: Boolean = false,
    @field:Json(name = "__is_baseline__") var isBaseline: Boolean = false,
    @field:Json(name = "__tos_version__") var tosVersion: String? = null,
    @field:Json(name = "__privacy_notice_version__") var privacyVersion: String? = null,
    @field:Json(name = "_experiments_segments") var experimentsSegments: Map<String, Int> = mapOf(),
    @field:Json(name = "in_app_purchases") var inAppPurchases: List<String> = listOf(),
    @field:Json(name = "main_subscription_index") var mainSubscriptionIndex: Int = -1,
    @field:Json(name = "min_build_version") var minBuildVersion: Int = 0,
    // Review request settings

    // after how many soft triggers a review flow is evaluated for display
    @field:Json(name = "review_soft_trigger_factor") val softReviewTriggersFactor: Int = 0,
    // after how many hard triggers a review flow is evaluated for display. The hard trigger factor should be less than the soft trigger factor
    @field:Json(name = "review_hard_trigger_factor") val hardReviewTriggersFactor: Int = 0,
    // how many times should a review popup be shown at most to the same user in a given app version
    @field:Json(name = "review_max_requests_per_version") val maxReviewRequestsPerVersion: Int = 0,
    // the minimum time that we should allow between showing two review requests
    @field:Json(name = "review_min_time_between_requests") val minTimeBetweenReviewRequests: Int = 0,
    // Number by which the `softReviewTriggersFactor` should be divided in case no popup has ever been shown
    @field:Json(name = "review_first_soft_trigger_factor_divider") val firstSoftReviewTriggersFactorDivider: Int = 1
) {
    open fun availableProductsId(): List<String> {
        return inAppPurchases.filterIndexed { index, s -> (index == mainSubscriptionIndex) }
    }
}
