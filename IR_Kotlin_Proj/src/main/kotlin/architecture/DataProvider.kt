package architecture

import architecture.datamodel.FitSvmTrainData
import architecture.datamodel.FitTfIdfDocuments
import architecture.datamodel.GermanStopwords
import architecture.datamodel.PrecalculatedIdf
import de.bwaldvogel.liblinear.Linear
import de.bwaldvogel.liblinear.Model
import kotlinx.serialization.json.Json
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel

class DataProvider {

    fun getTokenizerModel(): TokenizerME {
        // https://www.apache.org/dyn/closer.cgi/opennlp/models/ud-models-1.0/opennlp-de-ud-gsd-tokens-1.0-1.9.3.bin
        val modelIn = this.javaClass.classLoader.getResourceAsStream(TOKENIZER_MODEL_RESOURCE)
        val model = TokenizerModel(modelIn)
        return TokenizerME(model)
    }


    fun getDocumentsForTfIdfFitting(): FitTfIdfDocuments {
        val jsonString: String =
            this.javaClass.classLoader.getResource(FIT_TFIDF_DOCUMENTS_RESOURCE)?.readText() ?: throw Exception(
                "Did not find resource $FIT_TFIDF_DOCUMENTS_RESOURCE"
            )
        return Json.decodeFromString<FitTfIdfDocuments>(jsonString)
    }

    fun getGermanStopwords(): GermanStopwords {
        val jsonString: String =
            this.javaClass.classLoader.getResource(GERMAN_STOPWORDS_RESOURCE)?.readText() ?: throw Exception(
                "Did not find resource $GERMAN_STOPWORDS_RESOURCE"
            )
        return Json.decodeFromString<GermanStopwords>(jsonString)
    }


    fun getTrainDataForSvmFitting(): FitSvmTrainData {
        val jsonString: String =
            this.javaClass.classLoader.getResource(FIT_SVM_TRAINING_DATA_RESOURCE)?.readText() ?: throw Exception(
                "Did not find resource $FIT_SVM_TRAINING_DATA_RESOURCE"
            )
        return Json.decodeFromString<FitSvmTrainData>(jsonString)
    }

    fun getPrecalculatedIdf(): PrecalculatedIdf {
        val jsonString: String =
            this.javaClass.classLoader.getResource(PRECALCULATED_IDF_FOR_VECTORIZER)?.readText() ?: throw Exception(
                "Did not find resource $PRECALCULATED_IDF_FOR_VECTORIZER"
            )
        return Json.decodeFromString<PrecalculatedIdf>(jsonString)
    }

    fun getPretrainedLiblinearModel(): Model {
        val modelInputStream =
            this.javaClass.classLoader.getResource(PRETRAINED_LIBLINEAR_MODEL_L2R_L2LOSS_SVR)?.openStream() ?: throw Exception(
                "Did not find resource $PRETRAINED_LIBLINEAR_MODEL_L2R_L2LOSS_SVR"
            )

        return Linear.loadModel(modelInputStream.reader())
    }


    companion object {

        const val TOKENIZER_MODEL_RESOURCE = "models/opennlp-de-ud-gsd-tokens-1.0-1.9.3.bin"

        const val FIT_TFIDF_DOCUMENTS_RESOURCE = "data/fit_tfidf_documents.json"

        const val GERMAN_STOPWORDS_RESOURCE = "data/german_stopwords.json"

        const val FIT_SVM_TRAINING_DATA_RESOURCE = "data/fit_svm_training_data.json"

        const val PRECALCULATED_IDF_FOR_VECTORIZER = "data/precalculated_idf.json"

        const val PRETRAINED_LIBLINEAR_MODEL_L2R_L2LOSS_SVR = "data/pretrained_liblinear_model_l2r_l2loss_svr"
        }
}