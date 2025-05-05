package architecture.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class TestSet(
    val rows: List<TestSetRow>
)

@Serializable
data class TestSetRow(
   val language: String,
   val url: String,
   val content: String,
   val page_source_html: String,
   val content_removed: String? = null,
   val page_source_cleaned_html: String? = null,
   val page_source_removed_html: String? = null,
   val contains_GDPR: Boolean
)

@Serializable
data class ProcessedTestSet(
   val rows: List<ProcessedTestSetRow>
)

@Serializable
data class ProcessedTestSetRow(
   val language: String,
   val url: String,
   val content: String,
   val page_source_html: String,
   val content_removed: String? = null,
   val page_source_cleaned_html: String? = null,
   val page_source_removed_html: String? = null,
   val contains_GDPR: Boolean,
   val content_removed_assistant: String? = null,
   val page_source_cleaned_assistant: String? = null
)