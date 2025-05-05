package architecture.document.classifier

import architecture.Document
import architecture.DocumentVector
import architecture.datamodel.FitSvmTrainData
import architecture.document.vectorizer.DocumentVectorizer
import smile.classification.SVM
import smile.classification.svm
import smile.math.kernel.GaussianKernel

@Deprecated("This was a try to get the Smile SVM to work. Not running and not used.")
class DocumentDsgvoClassifierSmileSVM(
    private val documentVectorizer: DocumentVectorizer,
    private val fitSvmTrainData: FitSvmTrainData,
): DocumentDsgvoClassifier {

    /**
     * The SVM produces a "probability" for the seen text being DSGVO related between 1 and -1.
     * This probability is mapped to a boolean prediction label by checking if the probability is higher than the
     * threshold. So a threshold closer to 1 means more conservative judgment, that is less texts get classified as
     * DSGVO related
     * */
    var confidenceThreshold: Double = 0.0
    /**
     * The gaussianKernalSigma is a hyperparameter of the SVM model
     * */
    val gaussianKernelSigma = 0.3
    /**
     * The regularizationC is a hyperparameter of the SVM model
     * */
    val regularizationC = 1.0

    private val fittedSvm: SVM<DoubleArray> = fitSvm()

    private fun fitSvm(): SVM<DoubleArray> {

        val docVectors = fitSvmTrainData.text.map { indexToDoc ->
            documentVectorizer.perprocessAndVectorizeDocument(indexToDoc.value)
        }

        // in the training set json 0 stands for negative label and 1 for positive. Here I need to map negative to -1
        val labels = fitSvmTrainData.is_GDPR.map { indexToLabel ->
            if (indexToLabel.value == 1) { 1 } else { -1 }
        }.toIntArray()

        return svm(
            x = Array<DoubleArray>(docVectors.size, { index -> docVectors[index] }),
            y = labels,
            kernel = GaussianKernel(gaussianKernelSigma),
            C = regularizationC

        )
    }

    override fun getDsgvoPredictionForDocumentVector(documentVector: DocumentVector): DocumentDsgvoClassifier.Prediction {
        val probability = fittedSvm.score(documentVector)
        val predictionLabel = probability > confidenceThreshold

        return DocumentDsgvoClassifier.Prediction(probability, predictionLabel)
    }

    override fun getDsgvoPredictionForDocument(document: Document): DocumentDsgvoClassifier.Prediction {
        val documentVector = this.documentVectorizer.perprocessAndVectorizeDocument(document)

        return getDsgvoPredictionForDocumentVector(documentVector)
    }

}