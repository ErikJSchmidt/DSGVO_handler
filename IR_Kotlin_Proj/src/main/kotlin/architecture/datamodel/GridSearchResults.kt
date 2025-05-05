package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class GridSearchResults(
    val results: List<GridSearchResult>
)

@Serializable
data class GridSearchResult(
    val c: Double,
    val eps: Double,
    val p: Double,
    val accuracy: Double,
    val truePositiveRate: Double,
    val trueNegativeRate: Double
)
