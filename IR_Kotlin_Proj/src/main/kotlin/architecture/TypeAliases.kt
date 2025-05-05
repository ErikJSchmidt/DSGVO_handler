package architecture

/**
 * A Double Array that is a numerical representation of text.
 * In this project text is encoded to vectors by DocumentVectorizer.
 * @see architecture.documentvectorizer.DocumentVectorizer
 * */
typealias DocumentVector = DoubleArray

typealias Document = String
typealias Html = String
typealias TokenizedDocument = List<String>