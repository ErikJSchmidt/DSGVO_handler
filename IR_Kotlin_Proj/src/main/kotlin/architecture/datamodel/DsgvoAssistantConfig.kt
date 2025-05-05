package architecture.datamodel

data class DsgvoAssistantConfig(
    /**
     * The minimal score some of the subtrees of a HTML must score in the SVM so that the HTML is classified as
     * containing DSGVO text
     * */
    val maxSubtreeProbabilityThreshold: Double = 0.2,

    /**
     * The minimal score a parent of a DSGVO classified subtree must score so that this parent and its subtree is now
     * removed as DSGVO all together
     * */
    val upwardsPropagationThreshold: Double = 0.1,

    /**
     * The minimal number of whitespace separated words the text of a subtree must contain to be tested. Testing too
     * short texts leads to many false positive classifications, e.g. the text "Analyse" with just one word scores very
     * high, but could appear on any none DSGVO containing website.
     * */
    val minSubtreeTextLength: Double = 10.0
)
