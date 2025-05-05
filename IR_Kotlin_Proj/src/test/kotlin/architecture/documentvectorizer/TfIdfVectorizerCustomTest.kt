package architecture.documentvectorizer

import architecture.DataProvider
import architecture.datamodel.FitTfIdfDocuments
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import org.junit.jupiter.api.Test

class TfIdfVectorizerCustomTest {

    private val dataProvider = DataProvider()

    private val preprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    @Test
    fun testFitting(){
        val sampleCorpus = listOf(
            "Klettern ist die Bewegung in der Vertikalen mithilfe der Beine, Arme und Hände entlang von Kletterrouten",
            "Klettern ist eine vom Menschen seit jeher angewandte Fortbewegungsart in der vertikalen Ebene.",
            "Im Mittelalter bekamen Felsen eine zunehmende strategische Bedeutung."
        )

        val vectorizerCustom = TfIdfVectorizerCustom(
            preprocessor,
            FitTfIdfDocuments(sampleCorpus)
        )

        // Word Klettern appears often, Mittelalter just once => klettern has higher df, so the idf of Klettern should be smaller
        val inverseDocumentFrequencies = vectorizerCustom.getInverseDocumentFrequencies()

        val tokenClimbing = preprocessor.preprocessDocument("Klettern")[0]
        val tokenMiddleage = preprocessor.preprocessDocument("Mittelalter")[0]

        val idfClimbing = inverseDocumentFrequencies?.get(tokenClimbing) ?: 0.0
        val idfMiddleAge = inverseDocumentFrequencies?.get(tokenMiddleage) ?: 0.0

        assert(idfClimbing < idfMiddleAge)

    }

    @Test
    fun testFittingOnSvmFittingSet(){
        val vectorizer = TfIdfVectorizerCustom(
            preprocessor,
            dataProvider.getDocumentsForTfIdfFitting()
        )

        println(vectorizer.getVocabulary()?.size)
        println(vectorizer.getInverseDocumentFrequencies())
    }

    @Test
    fun testSavingFittedIdf(){
        val vectorizer = TfIdfVectorizerCustom(
            preprocessor,
            dataProvider.getDocumentsForTfIdfFitting()
        )

        vectorizer.saveIdf("src/main/resources/data/precalculated_idf.json")
    }

    @Test
    fun testVectorizerFromPrecalculatedIdf(){
        val idf = dataProvider.getPrecalculatedIdf()

        val vectorizer = TfIdfVectorizerCustom(
            preprocessor,
            precalculatedIdf = idf.inverseDocumentFrequencies
        )

        println(vectorizer.getVocabulary()?.size)
        println(vectorizer.getInverseDocumentFrequencies())
    }
}