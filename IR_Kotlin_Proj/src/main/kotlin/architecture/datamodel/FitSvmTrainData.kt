package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class FitSvmTrainData(
    val text: MutableMap<Int, String>,
    val is_GDPR: MutableMap<Int, Int>
)
