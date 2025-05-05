package architecture.html.cleaner

import architecture.Document
import architecture.Html
import architecture.datamodel.DsgvoAssistantConfig
import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.vectorizer.DocumentVectorizer
import architecture.html.handler.HtmlHandler
import org.jsoup.nodes.Node

class BaseHtmlDsgvoCleaner(
    private val config: DsgvoAssistantConfig,
    private val htmlHandler: HtmlHandler,
    private val textVectorizer: DocumentVectorizer,
    private val documentVectorClassifier: DocumentDsgvoClassifier
) : HtmlDsgvoCleaner {

    override fun removeDsgvo(html: Html): Html {
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

        if (dsgvoScores.isEmpty()) return html

        val maxSubtreeDsgvoScore = dsgvoScores.max()

        val dsgvoSubtreeRoot =
            nodesWithDsgvoScores.find { it.second == maxSubtreeDsgvoScore }?.first?.let { maxScoreNode ->
                getRootOfDsgvoSubtree(html, maxScoreNode)
            } ?: return html

        dsgvoSubtreeRoot.remove()

        return jsoupDoc.toString()
    }

    override fun extractDsgvo(html: Html): Html? {
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

        if (dsgvoScores.isEmpty()) return null

        val maxSubtreeDsgvoScore = dsgvoScores.max()

        val dsgvoSubtreeRoot =
            nodesWithDsgvoScores.find { it.second == maxSubtreeDsgvoScore }?.first?.let { maxScoreNode ->
                getRootOfDsgvoSubtree(html, maxScoreNode)
            } ?: return null

        return dsgvoSubtreeRoot.toString()
    }


    private fun getRootOfDsgvoSubtree(
        html: Html,
        maxScoreNode: Node
    ): Node {
        val dsgvoSubtreeRoot = htmlHandler.moveUpDomTreeUntilNodeTextFulfills(
            html = html,
            startingNode = maxScoreNode,
            stopCondition = { subtreeText -> subtreeUndercutsDsgvoThreshold(subtreeText) }
        )

        return dsgvoSubtreeRoot
    }

    private fun subtreeUndercutsDsgvoThreshold(subtreeText: Document): Boolean {
        val parentScore = documentVectorClassifier.getDsgvoPredictionForDocument(subtreeText).isDsgvoScore
        return parentScore < config.upwardsPropagationThreshold
    }
}