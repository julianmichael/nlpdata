package nlpdata.util

// utilities related to Penn Treebank pos tags.
// add more here as needed
object PosTags {
  val nounPosTags = Set("NN", "NNS", "NNP", "NNPS")
  val adverbPosTags = Set("RB", "RBR", "RBS", "WRB") // not sure if we really want WRB. oh well
  val pluralPosTags = Set("NNS", "NNPS")
  val verbPosTags = Set("VB", "VBD", "VBG", "VBN", "VBP", "VBZ")
  val whPosTags = Set("WDT", "WP", "WP$", "WRB")

  val allPosTags = Set(
    "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS",
    "MD", // then nouns
    "PDT", "POS", "PRP", "PRP$", // then adverbs
    "RP", "SYM", "TO", "UH" // then verbs, then whs
  ) ++ nounPosTags ++ adverbPosTags ++ verbPosTags ++ whPosTags
}
