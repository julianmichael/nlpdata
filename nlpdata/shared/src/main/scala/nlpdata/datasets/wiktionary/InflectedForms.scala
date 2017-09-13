package nlpdata.datasets.wiktionary

import nlpdata.util._
import nlpdata.util.LowerCaseStrings._

case class InflectedForms(
  stem: LowerCaseString,
  present: LowerCaseString,
  presentParticiple: LowerCaseString,
  past: LowerCaseString,
  pastParticiple: LowerCaseString
) extends (VerbForm => LowerCaseString) {

  def apply(form: VerbForm): LowerCaseString = form match {
    case Stem => stem
    case PresentSingular3rd => present
    case PresentParticiple => presentParticiple
    case Past => past
    case PastParticiple => pastParticiple
  }

  def allForms: List[LowerCaseString] = List(stem, present, presentParticiple, past, pastParticiple)
}

object InflectedForms {
  def fromStrings(stem: String, present: String, presentParticiple: String, past: String, pastParticiple: String) =
    InflectedForms(
      stem.lowerCase,
      present.lowerCase,
      presentParticiple.lowerCase,
      past.lowerCase,
      pastParticiple.lowerCase)

  val doForms = InflectedForms.fromStrings(
    "do", "does", "doing", "did", "done")

  val beSingularForms = InflectedForms.fromStrings(
    "be", "is", "being", "was", "been")

  val haveForms = InflectedForms.fromStrings(
    "have", "has", "having", "had", "had"
  )

}
