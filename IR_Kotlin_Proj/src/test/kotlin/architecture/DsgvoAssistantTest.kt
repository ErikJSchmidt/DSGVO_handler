package architecture

import architecture.datamodel.DsgvoAssistantConfig
import architecture.datamodel.ProcessedTestSet
import architecture.datamodel.ProcessedTestSetRow
import architecture.datamodel.TestSet
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import architecture.html.handler.BaseHtmlHandler
import architecture.html.handler.HtmlHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.Charset

class DsgvoAssistantTest {

    private val dataProvider = DataProvider()

    private val textPreprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    private val textVectorizer: TfIdfVectorizerCustom = TfIdfVectorizerCustom(
        textPreprocessor,
        dataProvider.getDocumentsForTfIdfFitting()
    )

    private val config = DsgvoAssistantConfig()

    val htmlHandler: HtmlHandler = BaseHtmlHandler(config = config)

    val assitant = DsgvoAssistant(config = config)

    @Test
    fun testPositiveSample() {
        val positiveSampleHTML = File("src/test/resources/data/page_source_with_DSGVO.html").readText()

        assert(assitant.containsDsgvo(positiveSampleHTML))
    }

    @Test
    fun testNegativeSample() {
        val negativeSampleHTML = File("src/test/resources/data/page_source_no_DSGVO.html").readText()

        assert(!assitant.containsDsgvo(negativeSampleHTML))

    }

    @Test
    fun testAccuracyOnTestSet() {
        val testSetJsonString = File("src/test/resources/data/test_dataset.json").readText()

        val testSet = Json.decodeFromString<TestSet>(testSetJsonString)

        var truePositive = 0.0
        var falsePositive = 0.0
        var trueNegative = 0.0
        var falseNegative = 0.0

        var count = 0
        for (row in testSet.rows) {
            println(count)
            count++
            val prediction = assitant.containsDsgvo(row.page_source_html)
            val actual = row.contains_GDPR
            println("actual: $actual")
            when (actual to prediction) {
                true to true -> truePositive++
                false to true -> {
                    falsePositive++
                }

                false to false -> trueNegative++
                true to false -> falseNegative++
            }
        }

        val accuracy = (truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative)
        val truePositiveRate = truePositive / (truePositive + falseNegative)
        val trueNegativeRate = trueNegative / (trueNegative + falsePositive)

        println("accuracy $accuracy, tpr $truePositiveRate, tnr $trueNegativeRate")
    }

    @Test
    fun getDsgvoTextFromHtml() {
        val sampleNumber = 10

        val testSetJsonString = File("src/test/resources/data/test_dataset.json").readText()
        val testSet = Json.decodeFromString<TestSet>(testSetJsonString)

        val sample = testSet.rows[sampleNumber]
        println(sample.url)
        println(sample.contains_GDPR)

        val html = sample.page_source_html
        val assistantRemovedHtml = assitant.extractDsgvo(html)

        val assistantRemovedText = htmlHandler.getVisibleTextOfHtml(assistantRemovedHtml)

        println("Assistant:")
        println(assistantRemovedText)
        println("# of chars: ${assistantRemovedText.length}")
        println("Manual")
        val manuallyRemovedText = sample.content_removed ?: ""
        println(manuallyRemovedText)
        println("# of chars: ${manuallyRemovedText.length}")
        val similarity = textVectorizer.getTextSimilarity(assistantRemovedText, manuallyRemovedText)
        println("similarity of removed texts: $similarity")
    }

    /**
     * Runs removeDsgvo and extractDsgvo on each of the 50 test samples and stores the extracted html as well as the
     * cleaned HTML in new columns that are added to the test dataset table.
     *
     * Stores the test dataset together with the results to a json file.
     * */
    @Test
    fun runAssistantFunctionalitiesOnTestSet() {
        val testSetJsonString = File("src/test/resources/data/test_dataset.json").readText()
        val testSet = Json.decodeFromString<TestSet>(testSetJsonString)

        val resultRows = mutableListOf<ProcessedTestSetRow>()

        for ((count, sample) in testSet.rows.withIndex()) {
            println(count)
            println(sample.url)
            println(sample.contains_GDPR)

            val html = sample.page_source_html

            val assistantRemovedHtml = assitant.extractDsgvo(html)
            val assistantRemovedText = htmlHandler.getVisibleTextOfHtml(assistantRemovedHtml)

            val assistantCleanedHtml = assitant.removeDsgvo(html)

            resultRows.add(
                ProcessedTestSetRow(
                    language = sample.language,
                    url = sample.url,
                    content = sample.content,
                    page_source_html = sample.page_source_html,
                    content_removed = sample.content_removed,
                    page_source_cleaned_html = sample.page_source_cleaned_html,
                    page_source_removed_html = sample.page_source_removed_html,
                    contains_GDPR = sample.contains_GDPR,
                    content_removed_assistant = assistantRemovedText,
                    page_source_cleaned_assistant = assistantCleanedHtml
                )
            )
        }

        val resultJsonString = Json.encodeToString(ProcessedTestSet(
            resultRows
        ))

        val filepath = "src/test/resources/data/processed_test_set_20221230_2.json"

        try {
            PrintWriter(FileWriter(filepath, Charset.defaultCharset()))
                .use { it.write(resultJsonString) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}