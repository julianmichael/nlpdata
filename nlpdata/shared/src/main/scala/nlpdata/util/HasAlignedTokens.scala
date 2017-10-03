package nlpdata.util

import nlpdata.structure.AlignedToken

import simulacrum._
import scala.language.implicitConversions

/*
 * Typeclass for types that can be rendered as a list of Penn-Treebank style tokens.
 */
@typeclass trait HasAlignedTokens[-A] {
  /* Returns a vector of Penn Treebank style tokens. */
  @op("alignedTokens") def getAlignedTokens(a: A): Vector[AlignedToken]
}
