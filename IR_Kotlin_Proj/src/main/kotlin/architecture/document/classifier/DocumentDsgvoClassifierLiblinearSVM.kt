package architecture.document.classifier

import architecture.Document
import architecture.DocumentVector
import architecture.datamodel.FitSvmTrainData
import architecture.document.vectorizer.DocumentVectorizer

import de.bwaldvogel.liblinear.FeatureNode
import de.bwaldvogel.liblinear.Linear
import de.bwaldvogel.liblinear.Model
import de.bwaldvogel.liblinear.Parameter
import de.bwaldvogel.liblinear.Problem
import de.bwaldvogel.liblinear.SolverType
import kotlin.io.path.Path

/**
 * The default values of the hyperparameters are the combination that achieved the highest true negative rate.
 * */
class DocumentDsgvoClassifierLiblinearSVM(
    private val documentVectorizer: DocumentVectorizer,
    private val fitSvmTrainData: FitSvmTrainData? = null,
    private val regularizationC: Double = 10.0,
    private val stoppingToleranceEps: Double = 0.0003,
    private val epsilonOfLossFunctionP: Double = 0.1,
    private val pretrainedModel: Model? = null
): DocumentDsgvoClassifier {

    private val fittedSVM: Model

    init {
        fittedSVM = if (pretrainedModel != null) {
            pretrainedModel
        } else if (fitSvmTrainData != null) {
            fitSVM(fitSvmTrainData)
        } else {
            throw Exception("Trying to initialize ${this.javaClass.name}, but both given arguments fitSvmTrainData and" +
                    "pretrainedModel are null. One of them must be given to initialize the SVM regression model.")
        }
    }

    private fun fitSVM(fitSvmTrainData: FitSvmTrainData): Model {
        val trainingProblem = getSvmProblem(fitSvmTrainData)

        val trainingParams = Parameter(
            // support vector regression is used instead of classification, because we need "probability" scores [-1;1]
            // in order to decide how much to remove from DOM tree. The classifier returns only labels (1 or -1)
            SolverType.L2R_L2LOSS_SVR,
            regularizationC,
            stoppingToleranceEps,
            epsilonOfLossFunctionP
        )

        return  Linear.train(trainingProblem, trainingParams)
    }

    private fun getSvmProblem(fitSvmTrainData: FitSvmTrainData): Problem {
        val docVectors = fitSvmTrainData.text.map { indexToDoc ->
            documentVectorizer.perprocessAndVectorizeDocument(indexToDoc.value)
        }

        val trainingProblem = Problem()
        trainingProblem.l = fitSvmTrainData.is_GDPR.size
        trainingProblem.n = docVectors[0].size
        trainingProblem.y = fitSvmTrainData.is_GDPR.map { isGDPR -> if (isGDPR.value == 1) 1.0 else -1.0 }.toDoubleArray()

        val documentVectorsAsSvmNodes: Array<Array<FeatureNode>> = Array(
            size = trainingProblem.l,
            init = { index -> documentVectorToFeatureNodes(docVectors[index]) }
        )
        trainingProblem.x = documentVectorsAsSvmNodes

        return trainingProblem
    }

    private fun documentVectorToFeatureNodes(documentVector: DocumentVector): Array<FeatureNode> {
        val documentSvmNodes: Array<FeatureNode> = Array(
            documentVector.size,
            { index ->
                FeatureNode(index + 1, documentVector[index])
            }
        )

        return documentSvmNodes
    }


    override fun getDsgvoPredictionForDocumentVector(documentVector: DocumentVector): DocumentDsgvoClassifier.Prediction {
        val probability = Linear.predict(fittedSVM, documentVectorToFeatureNodes(documentVector))
        val prediction = probability > 0.0

        return DocumentDsgvoClassifier.Prediction(
            probability,
            prediction
        )
    }

    override fun getDsgvoPredictionForDocument(document: Document): DocumentDsgvoClassifier.Prediction {

        val documentVector = this.documentVectorizer.perprocessAndVectorizeDocument(document)

        return getDsgvoPredictionForDocumentVector(documentVector)
    }

    fun saveModel(filePath: String){
        val path = Path(filePath)
        fittedSVM.save(path)
    }
}