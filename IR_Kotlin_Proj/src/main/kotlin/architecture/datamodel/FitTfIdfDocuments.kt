package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class FitTfIdfDocuments(
    val documents: List<String>
)