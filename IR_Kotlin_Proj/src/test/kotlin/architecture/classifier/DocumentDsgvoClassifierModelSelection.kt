package architecture.classifier

import architecture.DataProvider
import architecture.datamodel.FitSvmTrainData
import architecture.datamodel.GridSearchResult
import architecture.datamodel.GridSearchResults
import architecture.document.classifier.DocumentDsgvoClassifierLiblinearSVM
import architecture.document.vectorizer.TfIdfVectorizerCustom
import architecture.document.preprocesser.TextPreprocessor
import architecture.document.preprocesser.TextPreprocessorGermanBase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File

/**
 * This test class contains tests for finding the best hyperparameters for the SVM used to distinguish DSGVO text from
 * none-DSGVO text.
 * */
class DocumentDsgvoClassifierModelSelection {

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

    private fun getSVMTrainDataSplits(): List<List<SplitEntry>> {
        val trainData = dataProvider.getTrainDataForSvmFitting()
        val indices = (0 until trainData.text.size).toList().shuffled()
        println("nr of smaple indices ${indices.size}")
        val indicesOfSplits = indices.chunked((indices.size/4.9).toInt())

        val splits = indicesOfSplits.map { splitIndicies ->
            splitIndicies.map { index ->
                trainData.text[index] to trainData.is_GDPR[index]
                SplitEntry(
                    index,
                    trainData.text[index] ?: "",
                    trainData.is_GDPR[index]?: -1
                )
            }
        }

        return splits
    }

    data class SplitEntry(
        val originalIndex: Int,
        val text: String,
        val label: Int
    )

    @Test
    fun testCrossValidation(){
        val splits = getSVMTrainDataSplits()
        println("Nr of splits ${splits.size}")
        splits.forEachIndexed { index, pairs ->
            println("Split $index has entries ${pairs.size}")
        }
        val accuracies = mutableListOf<Double>()
        val truePositiveRates = mutableListOf<Double>()
        val trueNegativeRates = mutableListOf<Double>()

        for (i in splits.indices){
            println("\neval on split $i")
            //split i for testing, rest for training
            val trainSplitNumbers = mutableListOf(0,1,2,3,4).apply { remove(i) }

            // join texts and labels of splits
            val fitTfIdfDocuments = mutableListOf<String>() // should be the whole documents, not just dsgvo snippets of text
            val trainDocuments = mutableListOf<String>()
            val trainLabels = mutableListOf<Int>()
            for (j in trainSplitNumbers){
                splits[j].forEach { splitEntry ->
                    fitTfIdfDocuments.add(dataProvider.getDocumentsForTfIdfFitting().documents[splitEntry.originalIndex])
                    trainDocuments.add(splitEntry.text )
                    trainLabels.add(splitEntry.label)}
            }

            println("Number of TfIdf fitting docs ${trainDocuments.size}")

            val svmTrainData = FitSvmTrainData(
                text = trainDocuments.mapIndexed { index, doc ->
                    index to doc }.toMap().toMutableMap(),
                is_GDPR = trainLabels.mapIndexed { index, label ->
                    index to label }.toMap().toMutableMap()
            )

            println("Nr of known words")
            println(vectorizer.getInverseDocumentFrequencies()?.size)

            val classifier = DocumentDsgvoClassifierLiblinearSVM(
                vectorizer,
                svmTrainData
            )

            // test classifer on split i
            val predictions = mutableListOf<Boolean>()
            println("documents in test split ${splits[i].size}")
            splits[i].forEach {
                val documentVector = vectorizer.perprocessAndVectorizeDocument(it.text)
                val pred = classifier.getDsgvoPredictionForDocumentVector(documentVector)
                predictions.add(pred.isDsgvoPrediction)
            }
            var correct = 0.0
            var wrong = 0.0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                val pred = predictions[index]
                if (actual == pred){
                    correct += 1
                } else {
                    wrong += 1
                }
            }
            val accuracy = correct/(correct + wrong)
            accuracies.add(accuracy)
            println("accuracy $accuracy")

            var positives = 0
            var negatives = 0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                when(actual) {
                    true -> positives++
                    false -> negatives++
                }
            }
            println(println("positives ${positives}, negatives ${negatives}"))

            var truePositive = 0.0
            var falsePositive = 0.0
            var trueNegative = 0.0
            var falseNegative = 0.0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                val pred = predictions[index]
                when(actual to pred) {
                    true to true -> truePositive++
                    false to true -> falsePositive++
                    false to false -> trueNegative++
                    true to false -> falseNegative++
                }
            }
            val truePositiveRate = truePositive/(truePositive + falseNegative)
            truePositiveRates.add(truePositiveRate)
            val trueNegativeRate = trueNegative/(trueNegative + falsePositive)
            trueNegativeRates.add(trueNegativeRate)
            println(println("truePositiveRate ${truePositiveRate}, trueNegativeRate ${trueNegativeRate}"))
        }
        val crossValAvgAccuracy = accuracies.sum()/accuracies.size
        val crossValAvgTruePositiveRate = truePositiveRates.sum()/truePositiveRates.size
        val crossValAvgTrueNegativeRate = trueNegativeRates.sum()/trueNegativeRates.size
        println(println("\n\navg accruacy ${crossValAvgAccuracy}, avg truePositiveRate ${crossValAvgTruePositiveRate}, " +
                "avg trueNegativeRate ${crossValAvgTrueNegativeRate}"))
    }

    /**
     * returns list [avg accuracy, avg tpr, avg tnr]
     * */
    private fun crossValidation(c: Double, eps: Double, p: Double): List<Double>{
        val splits = getSVMTrainDataSplits()
        val accuracies = mutableListOf<Double>()
        val truePositiveRates = mutableListOf<Double>()
        val trueNegativeRates = mutableListOf<Double>()

        for (i in splits.indices){
            //split i for testing, rest for training
            val trainSplitNumbers = mutableListOf(0,1,2,3,4).apply { remove(i) }

            // join texts and labels of splits
            val fitTfIdfDocuments = mutableListOf<String>() // should be the whole documents, not just dsgvo snippets of text
            val trainDocuments = mutableListOf<String>()
            val trainLabels = mutableListOf<Int>()
            for (j in trainSplitNumbers){
                splits[j].forEach { splitEntry ->
                    fitTfIdfDocuments.add(dataProvider.getDocumentsForTfIdfFitting().documents[splitEntry.originalIndex])
                    trainDocuments.add(splitEntry.text )
                    trainLabels.add(splitEntry.label)}
            }

            val svmTrainData = FitSvmTrainData(
                text = trainDocuments.mapIndexed { index, doc ->
                    index to doc }.toMap().toMutableMap(),
                is_GDPR = trainLabels.mapIndexed { index, label ->
                    index to label }.toMap().toMutableMap()
            )

            val classifier = DocumentDsgvoClassifierLiblinearSVM(
                vectorizer,
                svmTrainData,
                c,
                eps,
                p
            )

            // test classifier on split i
            val predictions = mutableListOf<Boolean>()
           splits[i].forEach {
                val documentVector = vectorizer.perprocessAndVectorizeDocument(it.text)
                val pred = classifier.getDsgvoPredictionForDocumentVector(documentVector)
                predictions.add(pred.isDsgvoPrediction)
            }
            var correct = 0.0
            var wrong = 0.0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                val pred = predictions[index]
                if (actual == pred){
                    correct += 1
                } else {
                    wrong += 1
                }
            }
            val accuracy = correct/(correct + wrong)
            accuracies.add(accuracy)

            var positives = 0
            var negatives = 0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                when(actual) {
                    true -> positives++
                    false -> negatives++
                }
            }
            var truePositive = 0.0
            var falsePositive = 0.0
            var trueNegative = 0.0
            var falseNegative = 0.0
            splits[i].forEachIndexed { index, splitEntry ->
                val actual = splitEntry.label == 1
                val pred = predictions[index]
                when(actual to pred) {
                    true to true -> truePositive++
                    false to true -> falsePositive++
                    false to false -> trueNegative++
                    true to false -> falseNegative++
                }
            }
            val truePositiveRate = truePositive/(truePositive + falseNegative)
            truePositiveRates.add(truePositiveRate)
            val trueNegativeRate = trueNegative/(trueNegative + falsePositive)
            trueNegativeRates.add(trueNegativeRate)
        }
        val crossValAvgAccuracy = accuracies.sum()/accuracies.size
        val crossValAvgTruePositiveRate = truePositiveRates.sum()/truePositiveRates.size
        val crossValAvgTrueNegativeRate = trueNegativeRates.sum()/trueNegativeRates.size

        return listOf(crossValAvgAccuracy, crossValAvgTruePositiveRate, crossValAvgTrueNegativeRate)
    }

    /**
     * The Liblinear SVM has three major hyper-parameters. Here we define seven values for each parameter and record
     * accuracy, true positive rate and true negative rate for each of the 7*7*7 combinations
     * */
    @Test
    fun gridSearchHyperParameters(){
        val candidatesC = listOf(0.01,0.03, 0.1, 0.3, 1.0, 3.0, 10.0)
        val candidatesEps = listOf(0.0001, 0.0003, 0.001, 0.003, 0.01,0.03, 0.1)
        val candidatesP = listOf(0.001, 0.003, 0.01,0.03, 0.1, 0.3, 1.0)

        val results = mutableListOf<GridSearchResult>()

        var count = 0
        for (c in candidatesC){
            for (eps in candidatesEps){
                for (p in candidatesP){
                    val crossValResult = crossValidation(c,eps,p)
                    val result = GridSearchResult(
                        c, eps, p, crossValResult[0], crossValResult[1], crossValResult[2]
                    )
                    results.add(result)
                    count++
                    println("\n\n$count\n\n")
                }
            }
        }
        val gridSearchResults = GridSearchResults(results)
        val jsonString = Json.encodeToString(gridSearchResults)

        File("src/test/resources/modelselection/grid_search_svm_params_results.json").writeText(jsonString)
    }

    /**
     * In grid search three metrics were documented: Accuracy, true positive rate and true negative rate.
     * For this project tnr is picked as the most important:
     *  - tnr = tn/(tn + fp)
     *  - We do not want false positives, that is text that is not DSGVO related should not be classified as DSGVO related
     *  - On the other hand false negatives are not to bad. If we classify some DSGVO text as not DSGVO it will stay in
     *      the index.
     * */
    @Test
    fun findBestHyperParameterCombination(){
        val jsonString = File("src/test/resources/modelselection/grid_search_svm_params_results.json").readText()
        val gridSearchResults = Json.decodeFromString<GridSearchResults>(jsonString)
        val results = gridSearchResults.results
        var maxTnr = 0.0
        var maxTnrIndex = -1
        for (i in results.indices) {
            results[i].let { res ->
                if (res.trueNegativeRate > maxTnr) {
                    maxTnr = res.trueNegativeRate
                    maxTnrIndex = i
                }
            }
        }

        println("Highest tnr at index $maxTnrIndex:")
        println(results[maxTnrIndex].toString())
    }

    @Test
    fun testBestHyperParameterCombination(){
        val crossValResult = crossValidation(10.0, 0.0003, 0.1)

        println("avg accuracy ${crossValResult[0]}, avg tpr ${crossValResult[1]}, avg tnr ${crossValResult[2]}")
    }
}