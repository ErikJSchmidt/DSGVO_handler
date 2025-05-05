package architecture.classifier

import architecture.DataProvider
import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.classifier.DocumentDsgvoClassifierSmileSVM
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Deprecated("Smile is not used by the DsgvoAssitant. Liblinear SVM worked better.")
class DocumentDsgvoClassifierSmileSVMTest{

    val dataProvider = DataProvider()

    val preprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    val idf = dataProvider.getPrecalculatedIdf()

    val vectorizer = TfIdfVectorizerCustom(
        preprocessor,
        precalculatedIdf = idf.inverseDocumentFrequencies
    )

    @Test
    fun testWithPositiveSample(){
        val classifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierSmileSVM(
            vectorizer,
            dataProvider.getTrainDataForSvmFitting()
        )

        val dsgvoText2TestSet = "  Diese Seite nutzt Website Tracking-Technologien von Dritten, um ihre Dienste anzubieten, stetig zu verbessern und Werbung entsprechend der Interessen der Nutzer anzuzeigen. Ich bin damit einverstanden und kann meine Einwilligung jederzeit mit Wirkung für die Zukunft widerrufen oder ändern.     Akzeptieren    Verweigern    mehr     Powered by               &"
        val dsgvoText2TestSetVector = vectorizer.perprocessAndVectorizeDocument(dsgvoText2TestSet)

        assertEquals(
            true,
            classifier.getDsgvoPredictionForDocumentVector(dsgvoText2TestSetVector).isDsgvoPrediction
        )
    }

    @Test
    fun testWithNegativeSample(){
        val classifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierSmileSVM(
            vectorizer,
            dataProvider.getTrainDataForSvmFitting()
        )

        val noDSGVOText = "Hat nichts damit zu tun. Text handelt von ganz anderem Inhalt. Von Reisen und Urlaub. Unternehmen"
        val noDSGVOTextVector = vectorizer.perprocessAndVectorizeDocument(noDSGVOText)
        assertEquals(
            false,
            classifier.getDsgvoPredictionForDocumentVector(noDSGVOTextVector).isDsgvoPrediction
        )

    }
}