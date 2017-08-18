package nlpdata.datasets.wiktionary

import nlpdata.util._
import nlpdata.util.LowerCaseStrings._

case class InflectedForms(
  stem: LowerCaseString,
  present: LowerCaseString,
  presentParticiple: LowerCaseString,
  past: LowerCaseString,
  pastParticiple: LowerCaseString) {
  def allForms: List[LowerCaseString] = List(stem, present, presentParticiple, past, pastParticiple)
}
