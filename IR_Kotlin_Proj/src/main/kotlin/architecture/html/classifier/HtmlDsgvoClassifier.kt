package architecture.html.classifier

import architecture.Html

/**
 * A classifier for determining whether a html contains a DSGVO element or if it is free of DSGVO text.
 * */
interface HtmlDsgvoClassifier {

    /**
     * Takes a html and returns a prediction on whether the html belongs to class "contains-DSGVO text" (true) or class
     * "contains-no-DSGVO-text" (false)
     *
     * @param html The html to classify.
     *
     * @return true if html is classified as "contains-DSGVO text".
     * */
    fun containsDsgvo(html: Html): Boolean

}