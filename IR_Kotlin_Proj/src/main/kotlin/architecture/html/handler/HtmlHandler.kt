package architecture.html.handler

import architecture.Document
import architecture.Html
import org.jsoup.Jsoup
import org.jsoup.nodes.Node

/**
 * A handler responsible for all html dom navigation and manipulation needed in this project.
 * */
interface HtmlHandler {

    /**
     *  A function that takes a HTML as String input and carries out a given function on the text of each subtree in th
     *  DOM. It returns a map of JSoup nodes paired with the result of their subtree text being fed into the function.
     *  Also returns the whole parsed JSoup of the given HTML.
     *
     *  @param html The html String to parse to tree and traverse over.
     *  @param function A function to carry out with each subtree text.
     *
     *  @return Returns the whole DOM as JSoup Document and a list of the considered JSoup nodes and their function
     *  results
     * */
    fun <T>doWithSubtrees(html: Html, function: (Document) -> T): Pair<org.jsoup.nodes.Document, List<Pair<Node, T>>>

    /**
     * Takes a HTML, a start node and a stop condition. From the start node check if the stop condition is fulfilled by
     * the parent node. If not go to the parent node and do again. If the parent fulfills the condition, then the
     * current node is returned as the node highest up in the tree hierarchy that still passes the condition.
     *
     * @param html The html String to parse as DOM tree
     * @param startingNode A JSoup node from on of the nodes in the html to start at
     * @param stopCondition A text to perfrom on the parent nodes subtree text before deciding wheter to move on.
     *
     * @return The node highest up the tree that still does no fulfill the stopCondition
     * * */
    fun moveUpDomTreeUntilNodeTextFulfills(html: Html, startingNode: Node, stopCondition: (Document) -> Boolean): Node

    /**
     * Extract text from HTML that would be visible to website visitors.
     *
     * @param html The html to extract text from
     *
     * @return The extracted subtree text.
     * */
    fun getVisibleTextOfHtml(html: Html): Document

}