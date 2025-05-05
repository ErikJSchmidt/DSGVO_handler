package architecture.document.classifier

import architecture.Document
import architecture.DocumentVector

/**
 * Interface for classifiers that were trained to classify a vector that represents the plain text
 * of some document as either Dsgvo related or not.
 *
 * */
interface DocumentDsgvoClassifier {

    /**
     * Given a vector that represents some text and was derived by the same encoder/vectorizer as the documentVectors
     * the classifier was trained on this function produces a prediction, run classification and return a prediction for
     * that text. The prediction tells how much the text is DSGVO related
     *
     * @param documentVector a Double Array produced by some text to vector encoder
     *
     * @return a prediction consists of a truth label denoting which of the two classes (dsgvo/not dsgvo) the
     * documentVector belongs to.
     * */
    fun getDsgvoPredictionForDocumentVector(documentVector: DocumentVector): Prediction


    /**
     * Given a document/a text passage vectorize it and classify it as above.
     *
     * @param documentVector a Double Array produced by some text to vector encoder
     *
     * @return a prediction consists of a truth label denoting which of the two classes (dsgvo/not dsgvo) the
     * documentVector belongs to.
     * */
    fun getDsgvoPredictionForDocument(document: Document): Prediction


    data class Prediction(
        val isDsgvoScore:Double,
        val isDsgvoPrediction: Boolean
    )
}