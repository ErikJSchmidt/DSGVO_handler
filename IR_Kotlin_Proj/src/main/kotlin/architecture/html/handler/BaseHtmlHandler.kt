package architecture.html.handler

import architecture.Document
import architecture.Html
import architecture.datamodel.DsgvoAssistantConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

class BaseHtmlHandler(
    private val config: DsgvoAssistantConfig
): HtmlHandler {

    override fun <T> doWithSubtrees(
        html: Html, function: (Document) -> T
    ): Pair<org.jsoup.nodes.Document, List<Pair<Node, T>>> {

        val jsoupDoc = Jsoup.parse(html)

        val subtreeResults = mutableListOf<Pair<Node, T>>()

        jsoupDoc.traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                val subtreeText = getVisibleTextOfSubtree(node)

                if (isProperText(subtreeText)) {
                    val subtreeResult = function(subtreeText)
                    subtreeResults.add(node to subtreeResult)

                }
            }

            override fun tail(node: Node, depth: Int) {}
        })
        return jsoupDoc to subtreeResults
    }

    override fun moveUpDomTreeUntilNodeTextFulfills(html: Html, startingNode: Node, stopCondition: (Document) -> Boolean): Node{
        //iteratively move up to parent and check dif in dsgvo score
        var checkParent = true
        var highestLevelNode = startingNode
        var iterations = 0
        while (checkParent) {
            println("# of iteration: $iterations")
            checkParent = false
            iterations++
            highestLevelNode.parent()?.let { parent ->
                if (!stopCondition(getVisibleTextOfSubtree(parent)) &&
                    parent.nodeName() != "html" && parent.nodeName() != "body") {
                    checkParent = true
                    highestLevelNode = parent
                }
            }
        }
        return highestLevelNode

    }

    private fun isProperText(subtreeText: Document): Boolean {
        val rules = listOf<(String) -> Boolean>(
            // very short texts can easily be classified as DSGVO even if they are not, e.g. "Analyse" scores 0.4
            // split text at whitespaces, filter out all single characters and count remainder
            { text -> text.split(" ").filterNot { it.length < 2 }.size > config.minSubtreeTextLength }
        )
        return rules.all { rule -> rule(subtreeText) }
    }

    private fun getVisibleTextOfSubtree(subtreeRoot: Node): Document {
        var subtreeText = ""

        subtreeRoot.traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                (node as? TextNode)?.let { textNode ->
                    if (textNode.text() != null)
                        subtreeText += node.text()
                }
            }

            override fun tail(node: Node, depth: Int) {}
        })

        return subtreeText
    }

    override fun getVisibleTextOfHtml(html: Html): Document {
        val jsoupDoc = Jsoup.parse(html)

        return (jsoupDoc as? Node?)?.let { node ->
            getVisibleTextOfSubtree(node)
        } ?: ""
    }

}