package architecture.document.vectorizer

import architecture.Document
import architecture.DocumentVector

/**
 * Vectorizer that can calculate vector representations of natural language text.
 * */
interface DocumentVectorizer {

    /**
     * Take a text passage/Document and calculate a vector representation of that text.
     * If needed, perform text preprocessing steps on the document before vectorization.
     *
     * @param document the text to vectorize
     *
     * @return the vector representation of the text as DoubleArray. The DocumentVector type alias is used to make clear
     * what this array of doubles represents.
     * */
    fun perprocessAndVectorizeDocument(document: Document): DocumentVector

    /**
     * Vectorizers often only consider a set list of words when calculating the vector of a text.
     *
     * @return the list of considered words or tokens
     * */
    fun getVocabulary(): List<String>?

    /**
     * Take two documents, calculate their vector representations and return some similarity or distance measure.
     *
     * @param documentA the first document
     * @param documentB the second document
     *
     * @return double value of the calculated measure
     * */
    fun getTextSimilarity(documentA: Document, documentB: Document): Double
}