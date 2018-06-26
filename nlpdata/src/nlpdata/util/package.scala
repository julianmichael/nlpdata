package nlpdata

import scala.util.{Failure, Success, Try}
import scala.collection.mutable
import scala.collection.TraversableOnce

import scala.language.implicitConversions

/** Provides miscellaneous utility classes and methods.
  *
  * This includes text rendering (Text),
  * type-level lowercase strings, extension methods for Scala stdlib types,
  * and some random stuff (the latter three on this object).
  */
package object util extends PackagePlatformExtensions {

  def simpleTokenize(s: String): Vector[String] =
    s.split("(\\s+|[.,;!?.'\"])").toVector

  // the only exposed part of the LowerCaseStrings API
  val LowerCaseStrings: LowerCaseStringCapsule = LowerCaseStringsImpl

  // == protected members / extensions useful for this project ==

  // == smart matchers ==

  protected[nlpdata] object IntMatch {
    val IntMatchRegex = "(\\d+)".r

    def unapply(s: String): Option[Int] = s match {
      case IntMatchRegex(num) => Some(num.toInt)
      case _                  => None
    }
  }

  // == Extension methods etc ==

  protected[nlpdata] def const[A](a: A): Any => A = _ => a

  protected[nlpdata] implicit class RichList[A](val a: List[A]) extends AnyVal {
    def remove(i: Int) = a.take(i) ++ a.drop(i + 1)
  }

  protected[nlpdata] implicit class RichValForOptions[A](val a: A) extends AnyVal {
    def onlyIf(p: (A => Boolean)): Option[A] = Some(a).filter(p)
    def ifNot(p: (A => Boolean)): Option[A] = Some(a).filterNot(p)

    def wrapNullable: Option[A] =
      if (a == null) None else Some(a) // TODO probably Option(A) works here
  }

  protected[nlpdata] implicit class RichValForLists[A](val a: A) extends AnyVal {

    def unfoldList[B](f: A => Option[(B, A)]): List[B] = f(a) match {
      case None                   => Nil
      case Some((head, tailToGo)) => head :: tailToGo.unfoldList(f)
    }

    def unfoldList[B](f: PartialFunction[A, (B, A)]): List[B] =
      a.unfoldList(f.lift)
  }
}
