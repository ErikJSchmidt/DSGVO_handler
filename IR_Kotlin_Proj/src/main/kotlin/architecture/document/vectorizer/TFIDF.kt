package architecture.document.vectorizer

/**
 * From https://github.com/chathasphere/tf-idf/blob/master/architecture.tfidf.TFIDF.kt
 *
 * Implementation of the Term Frequency/Inverse Document Frequency algorithm. Tokenizes a corpus
 * (collection of documents) by assigning the architecture.tfidf.TFIDF statistic to each word, measuring its overall importance to the
 * corpus. A count of how often a word appears in its document (TF) is multiplied by a log-scaled inverse count of
 * how often the word appears in the corpus (IDF). This measures the "specificity" of a word. For instance, in a
 * typical English corpus, "the" and "a" will have a high TF, but their significance will be offset by a low IDF.
 *
 * The intuition of IDF follows Zipf's law, the observation that the distribution of terms in a natural language corpus
 * follows a "power law": a word's frequency ranking is inversely proportional to its frequency. A logarithm helps
 * smooth the distribution.
 *
 * This class "trains" on an initial corpus, a list of documents. Each document should be a list of words. Further
 * documents may be added to the model with a lower computational cost, with the caveat that new words will be
 * ignored (cast to "%UNCATEGORIZED%").
 *
 * @property knownWords a set of all recognized words in the corpus.
 */


class TFIDF {

    var knowWordsArray: Array<String>? = null
    var idf: Map<String, Double>? = null

    // Adds words from the training corpus to the known words set
    private fun findKnownWords(documents: List<List<String>>): Array<String> {
        val foundWords = mutableSetOf<String>()
        for (document in documents) {
            for (term in document) {
                foundWords.add(term)
            }
        }
        return foundWords.toTypedArray()
    }

    // Calculates the frequency of a terms in a document and returns a map
    private fun tf(document: List<String>, knownWordsArray: Array<String>): MutableMap<String, Double> {
        val docTf: MutableMap<String, Double> = knownWordsArray.map { word -> word to 0.0 }.toMap().toMutableMap()

        for (term in document) {
            docTf[term] =  docTf.getOrDefault(term, 0.0) + 1
        }
        return docTf
    }

    // Given a corpus, returns a map of words to their inverse document frequency
    private fun idf(documents: List<List<String>>): MutableMap<String, Double> {
        //First, calculates "document frequency" of words in the
        //n documents.
        val documentFrequencies = mutableMapOf<String, Double>()

        for (document in documents) {
            for (term in document) {
                documentFrequencies[term] = documentFrequencies.getOrDefault(term, 0.0) + 1
            }
        }
        //Next, calculate inverse document frequency
        val n = documents.size
        val idf: MutableMap<String, Double> = mutableMapOf()
        for ((term, docFrequency) in documentFrequencies) {
            idf[term] = Math.log(n / docFrequency) + 1
        }
        return idf
    }

    //Calculates architecture.tfidf.TFIDF for words in a document
    private fun tfIdf(tf: Map<String, Double>, idf: Map<String, Double>): Map<String, Double> {
        val tfIdf: MutableMap<String, Double> = mutableMapOf()
        for ((term, inverseFrequency) in idf) {
            tfIdf[term] = (tf[term]?: 0.0) * inverseFrequency
        }
        return tfIdf
    }

    fun initializeFromIdf(initializeIdf: Map<String, Double>){
        knowWordsArray = initializeIdf.map { word_to_idf -> word_to_idf.key }.toTypedArray()
        idf = initializeIdf
    }

    //Calculates TF-IDF for a training corpus (list of documents, each a list of words)
    fun fitDocuments(documents: List<List<String>>) {
        knowWordsArray = findKnownWords(documents)
        idf = idf(documents)
    }

    //Calculate TF-IDF for a new document
    fun vectorizeDocument(document: List<String>): Map<String, Double> {
        val cleanedDoc = document.filter { word -> knowWordsArray?.contains(word) == true}

        val tf = tf(cleanedDoc, knowWordsArray ?: throw Exception("Trying to use ${this.javaClass.name} to vectorize single document before fitting on corpus"))
        return tfIdf(tf, idf ?: throw Exception("Trying to use ${this.javaClass.name} to vectorize single document before fitting on corpus"))
    }

}