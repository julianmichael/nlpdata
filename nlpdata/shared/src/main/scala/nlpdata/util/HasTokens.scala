package nlpdata.util

import simulacrum._
import scala.language.implicitConversions

// scaladoc doesn't work at some use sites, probably because of a problem in macro paradise
// TODO fix

/*
 * Typeclass for types that can be rendered as a list of Penn-Treebank style tokens.
 */
@typeclass trait HasTokens[-A] {
  /* Returns a vector of Penn Treebank style tokens. */
  @op("tokens") def getTokens(a: A): Vector[String]
}
