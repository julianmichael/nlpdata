package nlpdata.datasets.tqa

case class TQADataset(lessons: Map[String, TQALesson]) {
  val topics = lessons.flatMap(_._2.topics)
  val diagramQuestions = lessons.flatMap(_._2.diagramQuestions)
  val nonDiagramQuestions = lessons.flatMap(_._2.nonDiagramQuestions)
  val diagrams = lessons.flatMap(_._2.diagrams)
}

case class TQALesson(
  globalId: String,
  lessonName: String,
  topics: Map[String, TQATopic],
  adjunctTopics: List[TQAAdjunctTopic],
  vocabulary: List[TQAVocabularyItem],
  diagramQuestions: Map[String, TQADiagramQuestion],
  nonDiagramQuestions: Map[String, TQANonDiagramQuestion],
  diagrams: Map[String, TQADiagram]
)

// text / figures

case class TQAVocabularyItem(word: String, definition: String) // might be empty

case class TQALabeledText(idStructural: String, raw: String, processed: String)

case class TQAText(raw: String, processed: String) {

  def withIdStructural(idStructural: String) =
    TQALabeledText(idStructural, raw, processed)
}

case class TQAFigure(imagePath: String, caption: String)

// diagrams

case class TQADiagram(
  globalId: String,
  imageName: String,
  imagePath: String,
  text: TQAText,
  annotations: List[TQADiagramAnnotation]
)

case class TQADiagramAnnotation(text: String, location: TQADiagramAnnotationLocation)

sealed trait TQADiagramAnnotationLocation
case class Rectangle(left: Int, top: Int, bottom: Int, right: Int)
    extends TQADiagramAnnotationLocation
case class Interval(begin: Int, end: Int) extends TQADiagramAnnotationLocation

// topics

case class TQATopic(
  globalId: String,
  topicName: String,
  text: String,
  figures: List[TQAFigure],
  mediaLinks: List[String]
)

case class TQAAdjunctTopic(
  sectionName: String,
  text: String,
  figures: List[TQAFigure],
  mediaLinks: List[String],
  orderId: String
)

// answer choices

case class TQAAnswerChoice(label: String, text: TQALabeledText)

// questions

sealed trait TQAQuestion

case class TQANonDiagramQuestion(
  globalId: String,
  questionType: String,
  questionSubType: String,
  text: TQALabeledText,
  answerChoices: List[TQAAnswerChoice],
  correctAnswer: TQAText
) extends TQAQuestion

case class TQADiagramQuestion(
  globalId: String,
  questionType: String,
  text: TQAText,
  answerChoices: List[TQAAnswerChoice],
  correctAnswer: TQAText,
  imagePath: String,
  imageName: String
) extends TQAQuestion
