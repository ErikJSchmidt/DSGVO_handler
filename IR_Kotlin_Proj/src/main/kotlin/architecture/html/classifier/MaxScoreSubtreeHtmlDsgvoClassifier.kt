package architecture.html.classifier

import architecture.Document
import architecture.Html
import architecture.datamodel.DsgvoAssistantConfig
import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.vectorizer.DocumentVectorizer
import architecture.html.handler.HtmlHandler

class MaxScoreSubtreeHtmlDsgvoClassifier(
    private val config: DsgvoAssistantConfig,
    private val htmlHandler: HtmlHandler,
    private val textVectorizer: DocumentVectorizer,
    private val documentVectorClassifier: DocumentDsgvoClassifier
): HtmlDsgvoClassifier {

    override fun containsDsgvo(html: Html): Boolean {
        val (jsoupDoc, nodesWithDsgvoScores) = htmlHandler.doWithSubtrees(
            html = html
        ) { subtreeText ->
            documentVectorClassifier.getDsgvoPredictionForDocument(subtreeText).isDsgvoScore
        }

        val dsgvoScores = nodesWithDsgvoScores.filter { nodeToScore ->
            !nodeToScore.second.isNaN()
        }.map { nodeDoublePair ->
            nodeDoublePair.second
        }

        if (dsgvoScores.isEmpty()) return false

        val maxSubtreeDSGVOProb = dsgvoScores.max()

        return maxSubtreeDSGVOProb > config.maxSubtreeProbabilityThreshold

    }

}