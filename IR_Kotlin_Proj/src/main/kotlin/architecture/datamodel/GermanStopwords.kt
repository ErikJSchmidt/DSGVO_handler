package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class GermanStopwords(
    val stopwords: List<String>
)
