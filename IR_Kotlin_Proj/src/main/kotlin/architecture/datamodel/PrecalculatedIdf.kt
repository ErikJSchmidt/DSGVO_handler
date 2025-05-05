package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class PrecalculatedIdf(
    val inverseDocumentFrequencies: Map<String, Double>
)
