package architecture.preprocessing

import architecture.DataProvider
import architecture.document.preprocesser.TextPreprocessorGermanBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextPreprocessorTest {

    private val dataProvider = DataProvider()

    private val preprocessor = TextPreprocessorGermanBase(
        dataProvider.getTokenizerModel(),
        dataProvider.getGermanStopwords()
    )

    @Test
    fun textPreprocess() {
        val sampleText = "123 Hier kommen drei stop Wörter: der, dritte, niemanden. Die sollten alle entfernt werden"
        val expectedCleanedTokenizedText = listOf("hier", "stop", "wort", "die", "entfernt")
        val preprocessedText = preprocessor.preprocessDocument(sampleText)
        print(preprocessedText)
        assertEquals(expectedCleanedTokenizedText, preprocessedText)
    }
}