package architecture.document.vectorizer

import architecture.Document
import architecture.DocumentVector
import architecture.datamodel.FitTfIdfDocuments
import architecture.datamodel.PrecalculatedIdf
import architecture.document.preprocesser.TextPreprocessor
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.Charset
import kotlin.math.sqrt

class TfIdfVectorizerCustom(
    private val textPreprocessor: TextPreprocessor,
    fitTfIdfDocuments: FitTfIdfDocuments? = null,
    precalculatedIdf: Map<String, Double>? = null
): DocumentVectorizer {

    private var fittedVectorizer: TFIDF

    init {
        fittedVectorizer = if (precalculatedIdf != null) {
            initializeFromIdf(precalculatedIdf)
        } else if(fitTfIdfDocuments != null) {
            fitVectorizer(fitTfIdfDocuments)
        } else {
            throw Exception("Trying to initialize ${this.javaClass.name}, but both given arguments fitTfIdfDocuments and" +
                    "precalculatedIdf are null. One of them must be given to initialize the idf of the TfIdf vectorizer.")
        }
    }

    private fun initializeFromIdf(idf: Map<String, Double>): TFIDF {
        val tfidf = TFIDF()
        tfidf.initializeFromIdf(idf)
        return tfidf
    }

    private fun fitVectorizer(fitTfIdfDocuments: FitTfIdfDocuments): TFIDF {
        // preprocess list of documents => List of List of strings
        val preprocessedDocuments = fitTfIdfDocuments.documents.map { document -> textPreprocessor.preprocessDocument(document) }

        // fit TfIdf with preprocessed documents => vectorizer
        val tfidf = TFIDF()
        tfidf.fitDocuments(preprocessedDocuments)
        return tfidf
    }

    override fun perprocessAndVectorizeDocument(document: Document): DocumentVector {
        val preprocessedDocument = textPreprocessor.preprocessDocument(document)

        val vectorizedDoc = fittedVectorizer.vectorizeDocument(preprocessedDocument)
        val vector = vectorizedDoc.map { wordToTfIDF -> wordToTfIDF.value }

        // vectors are normalized to have L1 unit norm
        val summedUpTFIDFValues = vector.sum()
        val vectorNormalized = vector.map { value -> value/summedUpTFIDFValues }.toDoubleArray()

        return vectorNormalized
    }

    fun getInverseDocumentFrequencies(): Map<String, Double>?{
        return fittedVectorizer.idf
    }

    override fun getVocabulary(): List<String>? {
        return fittedVectorizer.knowWordsArray?.toList()
    }

    fun saveIdf(filepath: String){
        fittedVectorizer.idf?.let { idf ->
            val idfJsonString = Json.encodeToString(PrecalculatedIdf(idf))

            try {
                PrintWriter(FileWriter(filepath, Charset.defaultCharset()))
                    .use { it.write(idfJsonString) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * Preprocesses text, vectorizes them and calculates cosine similarity of two vectors.
     * */
    override fun getTextSimilarity(documentA: Document, documentB: Document): Double {
        val docAVec = perprocessAndVectorizeDocument(documentA)
        val docBVec = perprocessAndVectorizeDocument(documentB)

        val elementwiseProducts = mutableListOf<Double>()
        val aSquares = mutableListOf<Double>()
        val bSquares = mutableListOf<Double>()
        for ((a,b) in docAVec.zip(docBVec)) {
            elementwiseProducts.add(a * b)
            aSquares.add(a*a)
            bSquares.add(b*b)
        }
        val denominator = elementwiseProducts.sum()
        val fraction = sqrt(aSquares.sum()) * sqrt(bSquares.sum())

        return denominator/fraction


        return 0.0
    }
}