package architecture.document.preprocesser

import architecture.Document
import architecture.TokenizedDocument

/**
 * Interface to text preprocessors, used to prepare a natural language text for vectorization.
 * */
interface TextPreprocessor {

    /**
     * Given a passage of text preprocess the text into a list of Strings.
     * Preprocessing might include removal of irrelevant words, formatting or similar.
     *
     * @param document the String/passage of text to preprocess
     *
     * @return the list of tokens the test was processed to. Type alias TokenizedDocument is used to clearly show what
     * this list of Strings represents.
     * */
    fun preprocessDocument(document: Document): TokenizedDocument
}