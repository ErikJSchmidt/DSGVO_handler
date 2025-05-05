package architecture.document.preprocesser

import architecture.Document
import architecture.TokenizedDocument
import architecture.datamodel.GermanStopwords
import opennlp.tools.stemmer.snowball.SnowballStemmer
import opennlp.tools.tokenize.TokenizerME

class TextPreprocessorGermanBase(
    private val tokenizer: TokenizerME,
    private val germanStopwords: GermanStopwords
): TextPreprocessor {

    override fun preprocessDocument(document: Document): TokenizedDocument {
        var tokens = tokenizer.tokenize(document).toList()

        // Remove stuff that can not be encoded in UTF-8
        tokens = tokens.filter { token -> token.all { character -> character.isLetter() } }

        // Remove german stopwords that do not add much value to TfIdf vectors when added to vocab
        tokens = tokens.filter { token -> germanStopwords.stopwords.contains(token).not() }

        // stem words
        tokens = tokens.map { token -> SnowballStemmer(SnowballStemmer.ALGORITHM.GERMAN).stem(token).toString()}

        // Change all to lower case
        tokens = tokens.map { token -> token.lowercase() }

        return tokens
    }
}