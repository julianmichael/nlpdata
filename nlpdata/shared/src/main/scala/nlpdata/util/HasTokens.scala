package nlpdata.util

/** Typeclass for types that can be rendered as a list of Penn-Treebank style tokens.
  */
trait HasTokens[-A] {
  /** Returns a vector of Penn Treebank style tokens. */
  def getTokens(a: A): Vector[String]
}
