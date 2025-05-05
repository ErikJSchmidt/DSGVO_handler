package architecture.html.cleaner

import architecture.Html

/**
 * The HtmlDsgvoCleaner interface defines methods for treating DSGVO related text in a HTML that is already known to
 * contain a DSGVO element. So after classifying a HTML as containing DSGVO text, e.g, with the HtmlDsgvoClassifer,
 * functionalities of this interface allow to remove or extract DSGVO related HTML.
 * */
interface HtmlDsgvoCleaner {

    /**
     * Given a whole HTML, remove the subtree from the HTML that is thought to correspond to a DSGVO element.
     *
     * @param html The HTML String to clean.
     *
     * @return The HTML string where the DSGVO subtree has been removed.
     * */
    fun removeDsgvo(html: Html): Html

    /**
     * Given a whole HTML, extract the subtree from the HTML that is thought to correspond to a DSGVO element.
     *
     * @param html The HTML String to extract the DSGVO subtree form.
     *
     * @return The HTML String of the DSGVO element.
     * */
    fun extractDsgvo(html: Html): Html?

    //fun removeAndExtractDsgvo(html: Html): Pair<Html?, Html>

}