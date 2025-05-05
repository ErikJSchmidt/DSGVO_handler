package architecture.html.classifier

import architecture.DataProvider
import architecture.datamodel.DsgvoAssistantConfig
import architecture.datamodel.TestSet
import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.classifier.DocumentDsgvoClassifierLiblinearSVM
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import architecture.document.vectorizer.DocumentVectorizer
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.html.handler.BaseHtmlHandler
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File

class MaxScoreSubtreeHtmlDsgvoClassifierTest{

    private val dataProvider = DataProvider()

    private val textPreprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    private val textVectorizer: DocumentVectorizer = TfIdfVectorizerCustom(
        textPreprocessor,
        dataProvider.getDocumentsForTfIdfFitting()
    )

    private val config = DsgvoAssistantConfig()

    private val htmlHandler = BaseHtmlHandler(config = config)

    private val documentClassifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierLiblinearSVM(
        textVectorizer,
        pretrainedModel = dataProvider.getPretrainedLiblinearModel()
    )

    private val classifier = MaxScoreSubtreeHtmlDsgvoClassifier(
        DsgvoAssistantConfig(),
        htmlHandler,
        textVectorizer,
        documentClassifier
    )

    @Test
    fun testPositiveSample() {
        val positiveSampleHTML = File("src/test/resources/data/page_source_with_DSGVO.html").readText()

        assert(classifier.containsDsgvo(positiveSampleHTML))
    }

    @Test
    fun testNegativeSample() {
        val negativeSampleHTML = File("src/test/resources/data/page_source_no_DSGVO.html").readText()

        assert(!classifier.containsDsgvo(negativeSampleHTML))

    }

    @Test
    fun testContainsDSGVOAccuracy() {
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
            val prediction = classifier.containsDsgvo(row.page_source_html)
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

}