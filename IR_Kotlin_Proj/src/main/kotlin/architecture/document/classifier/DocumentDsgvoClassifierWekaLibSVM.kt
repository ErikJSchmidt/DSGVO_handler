package architecture.document.classifier

import architecture.Document
import architecture.DocumentVector
import architecture.datamodel.FitSvmTrainData
import architecture.document.vectorizer.DocumentVectorizer
import libsvm.*

/**
 * For reference of how to use libsvm:
 * https://github.com/prestodb/presto/blob/master/presto-ml/src/main/java/com/facebook/presto/ml/AbstractSvmModel.java#L110
 * */
@Deprecated("This was a try to get the Weka Wrapper around the LibLinear SVM to work. Not running and not used.")
class DocumentDsgvoClassifierWekaLibSVM(
    private val documentVectorizer: DocumentVectorizer,
    private val fitSvmTrainData: FitSvmTrainData,
) : DocumentDsgvoClassifier {

    val fittedSVM: svm_model = fitSVM()

    private fun fitSVM(): svm_model {
        val svmProblem = getSvmProblem()
        val svmParams = svm_parameter().apply {
            svm_type = svm_parameter.C_SVC;
            kernel_type = svm_parameter.RBF; //overwritten svm_parameter.RBF
            degree = 3;
            gamma = 0.0;	// 1/num_features
            coef0 = 0.0;
            nu = 0.5;
            cache_size = 100.0;
            C = 1.0;
            eps = 1e-3;
            p = 0.1;
            shrinking = 1;
            probability = 1; //overwritten 0
            nr_weight = 0;
        }
        return svm.svm_train(svmProblem, svmParams)
    }

    private fun getSvmProblem(): svm_problem {
        val docVectors = fitSvmTrainData.text.map { indexToDoc ->
            documentVectorizer.perprocessAndVectorizeDocument(indexToDoc.value)
        }

        val svmProblem = svm_problem()
        svmProblem.l = fitSvmTrainData.is_GDPR.size
        svmProblem.y = fitSvmTrainData.is_GDPR.map { isGDPR -> if (isGDPR.value == 1) 1.0 else -1.0 }.toDoubleArray()

        val documentVectorsAsSvmNodes: Array<Array<svm_node>> = Array(
            size = svmProblem.l,
            init = { index -> documentVectorToSvmNodes(docVectors[index]) }
        )
        svmProblem.x = documentVectorsAsSvmNodes

        return svmProblem
    }

    private fun documentVectorToSvmNodes(documentVector: DocumentVector): Array<svm_node> {
        val documentSvmNodes: Array<svm_node> = Array(
            documentVector.size,
            { index ->
                svm_node().apply {
                    this.index = index
                    this.value = documentVector[index]
                }
            }
        )

        return documentSvmNodes
    }

    override fun getDsgvoPredictionForDocumentVector(documentVector: DocumentVector): DocumentDsgvoClassifier.Prediction {
        val probability = svm.svm_predict(fittedSVM, documentVectorToSvmNodes(documentVector))
        val prediction = probability > 0

        return DocumentDsgvoClassifier.Prediction(
            isDsgvoScore = probability,
            isDsgvoPrediction = prediction
        )
    }

    override fun getDsgvoPredictionForDocument(document: Document): DocumentDsgvoClassifier.Prediction {

        val documentVector = this.documentVectorizer.perprocessAndVectorizeDocument(document)

        return getDsgvoPredictionForDocumentVector(documentVector)
    }
}