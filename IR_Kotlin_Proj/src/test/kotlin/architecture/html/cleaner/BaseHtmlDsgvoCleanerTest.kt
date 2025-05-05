package architecture.html.cleaner

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

class BaseHtmlDsgvoCleanerTest{

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

    private val htmlCleaner: HtmlDsgvoCleaner = BaseHtmlDsgvoCleaner(
        DsgvoAssistantConfig(),
        htmlHandler,
        textVectorizer,
        documentClassifier
    )

    @Test
    fun extractDsgvoTextFromHtml() {
        val sampleNumber = 0

        val testSetJsonString = File("src/test/resources/data/test_dataset.json").readText()
        val testSet = Json.decodeFromString<TestSet>(testSetJsonString)

        val sample = testSet.rows[sampleNumber]
        println(sample.url)
        println(sample.contains_GDPR)

        val html = sample.page_source_html
        val assistantRemovedHtml = htmlCleaner.extractDsgvo(html)
        val assistantRemovedText = assistantRemovedHtml?.let { assistantHtml ->
            htmlHandler.getVisibleTextOfHtml(assistantHtml) } ?: ""

        println("HtmlCleaner:")
        println(assistantRemovedText)
        println("# of chars: ${assistantRemovedText.length ?: 0}")
        println("Manual")
        val manuallyRemovedText = sample.content_removed ?: ""
        println(manuallyRemovedText)
        println("# of chars: ${manuallyRemovedText.length}")
        val similarity = textVectorizer.getTextSimilarity(assistantRemovedText, manuallyRemovedText)
        println("similarity of removed texts: $similarity")
    }

    @Test
    fun removeDsgvoTextFromHtml() {
        val sampleNumber = 0

        val testSetJsonString = File("src/test/resources/data/test_dataset.json").readText()
        val testSet = Json.decodeFromString<TestSet>(testSetJsonString)

        val sample = testSet.rows[sampleNumber]
        println(sample.url)
        println(sample.contains_GDPR)

        val html = sample.page_source_html
        val assistantCleanedHtml = htmlCleaner.removeDsgvo(html)

        println("HtmlCleaner:")
        println(assistantCleanedHtml)
        println("# of chars: ${assistantCleanedHtml.length}")
        println("Manual")
        val manuallyCleanedHtml = sample.page_source_cleaned_html ?: ""
        println(manuallyCleanedHtml)
        println("# of chars: ${manuallyCleanedHtml.length}")
        val similarity = textVectorizer.getTextSimilarity(assistantCleanedHtml, manuallyCleanedHtml)
        println("similarity of vectorized htmls: $similarity")
    }

}