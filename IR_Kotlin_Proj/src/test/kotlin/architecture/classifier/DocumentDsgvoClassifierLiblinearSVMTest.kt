package architecture.classifier

import architecture.DataProvider
import architecture.document.classifier.DocumentDsgvoClassifier
import architecture.document.classifier.DocumentDsgvoClassifierLiblinearSVM
import architecture.document.vectorizer.DocumentVectorizer
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DocumentDsgvoClassifierLiblinearSVMTest{


    val dataProvider = DataProvider()

    val preprocessor: TextPreprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    val vectorizer: DocumentVectorizer = TfIdfVectorizerCustom(
        preprocessor,
        dataProvider.getDocumentsForTfIdfFitting()
    )

    @Test
    fun testWithPositiveSample(){
        val classifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierLiblinearSVM(
            vectorizer,
            dataProvider.getTrainDataForSvmFitting()
        )

        val dsgvoText2TestSet = "  Diese Seite nutzt Website Tracking-Technologien von Dritten, um ihre Dienste anzubieten, stetig zu verbessern und Werbung entsprechend der Interessen der Nutzer anzuzeigen. Ich bin damit einverstanden und kann meine Einwilligung jederzeit mit Wirkung für die Zukunft widerrufen oder ändern.     Akzeptieren    Verweigern    mehr     Powered by               &"
        val dsgvoText2TestSetVector = vectorizer.perprocessAndVectorizeDocument(dsgvoText2TestSet)

        val prediction = classifier.getDsgvoPredictionForDocumentVector(dsgvoText2TestSetVector)
        print(prediction)
        assertEquals(
            true,
            prediction.isDsgvoPrediction
        )
    }

    @Test
    fun testWithNegativeSample(){
        val classifier: DocumentDsgvoClassifier = DocumentDsgvoClassifierLiblinearSVM(
            vectorizer,
            dataProvider.getTrainDataForSvmFitting()
        )

        val noDSGVOText = "Hat nichts damit zu tun. Text handelt von ganz anderem Inhalt. Von Reisen und Urlaub. Unternehmen"
        val noDSGVOTextVector = vectorizer.perprocessAndVectorizeDocument(noDSGVOText)

        val prediction = classifier.getDsgvoPredictionForDocumentVector(noDSGVOTextVector)
        print(prediction)
        assertEquals(
            false,
            classifier.getDsgvoPredictionForDocumentVector(noDSGVOTextVector).isDsgvoPrediction
        )

    }

    @Test
    fun testSavingFittedSVM(){
        val classifier = DocumentDsgvoClassifierLiblinearSVM(
            vectorizer,
            dataProvider.getTrainDataForSvmFitting(),
            regularizationC = 10.0,
            stoppingToleranceEps = 0.0003,
            epsilonOfLossFunctionP = 0.1,
        )

        classifier.saveModel("src/main/resources/data/pretrained_liblinear_model_l2r_l2loss_svr")
    }

    @Test
    fun testSVMFromPrecalculatedIdf(){
        val pretrainedModel = dataProvider.getPretrainedLiblinearModel()

        val classifier = DocumentDsgvoClassifierLiblinearSVM(
            vectorizer,
            pretrainedModel = pretrainedModel
        )

        val noDSGVOText = "Hat nichts damit zu tun. Text handelt von ganz anderem Inhalt. Von Reisen und Urlaub. Unternehmen"
        val noDSGVOTextVector = vectorizer.perprocessAndVectorizeDocument(noDSGVOText)

        val prediction = classifier.getDsgvoPredictionForDocumentVector(noDSGVOTextVector)
        print(prediction)
        assertEquals(
            false,
            classifier.getDsgvoPredictionForDocumentVector(noDSGVOTextVector).isDsgvoPrediction
        )
    }

}