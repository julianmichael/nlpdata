package nlpdata.util

import scala.language.implicitConversions

/* Welcome to the new world.
 * The world of ad-hoc refinement types requiring nothing more from the user than a single method call.
 * NO MORE WILL YOU BE UNCERTAIN, ON THE FIRST LINE OF YOUR METHOD,
 * WHETHER THE STRING WAS GUARANTEED TO BE LOWERCASE.
 * FOR YOU HAVE GUARANTEED IT ALREADY IN THE TYPE SYSTEM.
 * This is your weapon. This is your LowerCaseString.
 * Wield it with pride.
 * NOTE: there are projects to help you do refinement typing...but they seem a bit heavier weight for client code...idk
 * Anyway, don't try to read the code just below. The point is that you can write:
 * import nlpdata.util.LowerCaseStrings._
 * and then you get the _.lowerCase method on strings, which yields a LowerCaseString,
 * as well as an implicit conversion from LowerCaseString back to String.
 * In addition, certain uses of existing methods on String will preserve LowerCaseString (as of now, just +);
 * if you want there to be more, feel free to let me (Julian) know and I can add them here.
 * I know it seems like weird extra complication, but honestly I was already having bugs from not lowercasing strings,
 * despite sprinkling calls to .toLowerCase around so much that the code had gotten noticeably harder to read.
 */
sealed trait LowerCaseStringCapsule0 {
  type LowerCaseString
  sealed trait LowerCaseStringOps {
    def lowerCase(s: String): LowerCaseString
    def +(s1: LowerCaseString, s2: LowerCaseString): LowerCaseString
    def contains(s1: LowerCaseString, s2: LowerCaseString): Boolean
    def startsWith(s1: LowerCaseString, s2: LowerCaseString): Boolean
    def endsWith(s1: LowerCaseString, s2: LowerCaseString): Boolean
    def substring(s: LowerCaseString, beginIndex: Int): LowerCaseString
    def substring(s: LowerCaseString, beginIndex: Int, endIndex: Int): LowerCaseString
  }
  val LowerCaseStringOpsImpl: LowerCaseStringOps
  implicit def lowerCaseStringToString(lcs: LowerCaseString): String
}
sealed trait LowerCaseStringCapsule extends LowerCaseStringCapsule0 {
  implicit def wrapLowerCaseString(lcs: LowerCaseString): LowerCaseStringWrapper
  implicit def wrapStringToMakeLowerCase(s: String): StringToLowerCaseWrapper
}

protected[util] object LowerCaseStringsImpl extends LowerCaseStringCapsule {
  override type LowerCaseString = String
  override object LowerCaseStringOpsImpl extends LowerCaseStringOps {
    override def lowerCase(s: String): LowerCaseString = s.toLowerCase
    override def +(s1: LowerCaseString, s2: LowerCaseString) = s1 + s2
    override def contains(s1: LowerCaseString, s2: LowerCaseString) = s1 contains s2
    override def startsWith(s1: LowerCaseString, s2: LowerCaseString) = s1 startsWith s2
    override def endsWith(s1: LowerCaseString, s2: LowerCaseString) = s1 endsWith s2
    override def substring(s: LowerCaseString, beginIndex: Int) = s.substring(beginIndex)
    override def substring(s: LowerCaseString, beginIndex: Int, endIndex: Int) = s.substring(beginIndex, endIndex)
  }
  override implicit def lowerCaseStringToString(lcs: LowerCaseString): String =
    lcs
  override implicit def wrapLowerCaseString(lcs: LowerCaseString) =
    new LowerCaseStringWrapper(lcs.asInstanceOf[LowerCaseStrings.LowerCaseString]) // refers to upcasted version of this object
  override implicit def wrapStringToMakeLowerCase(s: String) =
    new StringToLowerCaseWrapper(s)
}

// take value with opaque-sealed type from the package object
import LowerCaseStrings.LowerCaseString

protected[util] class LowerCaseStringWrapper(val lcs: LowerCaseString) extends AnyVal {
  def +(other: LowerCaseString): LowerCaseString = LowerCaseStrings.LowerCaseStringOpsImpl.+(lcs, other)
  def contains(other: LowerCaseString): Boolean = LowerCaseStrings.LowerCaseStringOpsImpl.contains(lcs, other)
  def startsWith(other: LowerCaseString): Boolean = LowerCaseStrings.LowerCaseStringOpsImpl.startsWith(lcs, other)
  def endsWith(other: LowerCaseString): Boolean = LowerCaseStrings.LowerCaseStringOpsImpl.endsWith(lcs, other)
  def substring(beginIndex: Int): LowerCaseString = LowerCaseStrings.LowerCaseStringOpsImpl.substring(lcs, beginIndex)
  def substring(beginIndex: Int, endIndex: Int): LowerCaseString = LowerCaseStrings.LowerCaseStringOpsImpl.substring(lcs, beginIndex, endIndex)
}
protected[util] class StringToLowerCaseWrapper(val s: String) extends AnyVal {
  def lowerCase = LowerCaseStrings.LowerCaseStringOpsImpl.lowerCase(s)
}
