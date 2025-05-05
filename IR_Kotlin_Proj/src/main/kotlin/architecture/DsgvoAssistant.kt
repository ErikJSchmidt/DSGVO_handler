package architecture

import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.classifier.DocumentDsgvoClassifierLiblinearSVM
import architecture.datamodel.DsgvoAssistantConfig
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessorGermanBase
import architecture.document.vectorizer.DocumentVectorizer
import architecture.html.classifier.HtmlDsgvoClassifier
import architecture.html.classifier.MaxScoreSubtreeHtmlDsgvoClassifier
import architecture.html.cleaner.BaseHtmlDsgvoCleaner
import architecture.html.cleaner.HtmlDsgvoCleaner
import architecture.html.handler.BaseHtmlHandler
import architecture.html.handler.HtmlHandler

class DsgvoAssistant(
    config: DsgvoAssistantConfig = DsgvoAssistantConfig()
) {

    private val dataProvider = DataProvider()

    private val textPreprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    private val textVectorizer: DocumentVectorizer = TfIdfVectorizerCustom(
        textPreprocessor,
        precalculatedIdf = dataProvider.getPrecalculatedIdf().inverseDocumentFrequencies
    )

    private val documentClassifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierLiblinearSVM(
        textVectorizer,
        pretrainedModel = dataProvider.getPretrainedLiblinearModel()
    )

    private val htmlHandler: HtmlHandler = BaseHtmlHandler(config)

    private val htmlClassifier: HtmlDsgvoClassifier = MaxScoreSubtreeHtmlDsgvoClassifier(
        config,
        htmlHandler,
        textVectorizer,
        documentClassifier
    )

    private val htmlCleaner: HtmlDsgvoCleaner = BaseHtmlDsgvoCleaner(
        config,
        htmlHandler,
        textVectorizer,
        documentClassifier
    )

    /**
     * Takes a html and returns a prediction on whether the html belongs to class "contains-DSGVO text" (true) or class
     * "contains-no-DSGVO-text" (false).
     * */
    fun containsDsgvo(html: Html): Boolean {
        return htmlClassifier.containsDsgvo(html)
    }


    /**
     * Takes a html and predicts whether the html belongs to class "contains-DSGVO text" (true) or class
     * "contains-no-DSGVO-text" (false). If it belongs to "contains-DSGVO text" the HTML is cleaned by removing
     * elements from the HTML that correspond to DSGVO elements. Otherwise, the original HTMLis returned.
     * */
    fun removeDsgvo(html: Html): Html {
        return if (!containsDsgvo(html)) {
            html
        } else {
            htmlCleaner.removeDsgvo(html)
        }
    }

    /**
     * Takes a html and predicts whether the html belongs to class "contains-DSGVO text" (true) or class
     * "contains-no-DSGVO-text" (false). If it belongs to "contains-DSGVO text" the HTML of the DSGVO element is
     * extracted from the original HTML and gets returned. Otherwise, an empty String is returned.
     * */
    fun extractDsgvo(html: Html): Html {
        return if (!containsDsgvo(html)) {
            ""
        } else {
            htmlCleaner.extractDsgvo(html) ?: ""
        }
    }

}