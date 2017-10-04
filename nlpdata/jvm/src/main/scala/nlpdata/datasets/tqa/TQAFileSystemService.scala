package nlpdata.datasets.tqa

import java.nio.file.Path
import scala.util.Try

class TQAFileSystemService(location: Path) {
  def getDataset: Try[TQADataset] = Try {
    import argonaut._
    import Argonaut._
    val json = Parse.parse(
      io.Source.fromFile(location.toString).mkString
    ).right.get
    TQAFileSystemService.JsonParsing.datasetFromJson(json)
  }
}

object TQAFileSystemService {

  object JsonParsing {

    import argonaut._
    import Argonaut._

    def textFromJson(textJson: Json): TQAText = {
      TQAText(
        raw = textJson.field("rawText").get.string.get.toString,
        processed = textJson.field("processedText").get.string.get.toString)
    }

    def labeledTextFromJson(labeledTextJson: Json): TQALabeledText = {
      TQALabeledText(
        idStructural = labeledTextJson.field("idStructural").get.string.get.toString,
        raw = labeledTextJson.field("rawText").get.string.get.toString,
        processed = labeledTextJson.field("processedText").get.string.get.toString)
    }

    def figuresFromJson(figuresJson: Json): List[TQAFigure] = {
      figuresJson.array.get.iterator.map(figureJson =>
        TQAFigure(
          caption = figureJson.field("caption").get.string.get.toString,
          imagePath = figureJson.field("imagePath").get.string.get.toString)
      ).toList
    }

    def mediaLinksFromJson(mediaLinksJson: Json): List[String] = {
      mediaLinksJson.array.get.iterator.map(_.string.get.toString).toList
    }

    def adjunctTopicFromSectionNameAndJson(sectionName: String, adjTopicJson: Json): TQAAdjunctTopic = {
      val orderId = adjTopicJson.field("orderID").get.string.get.toString
      val contentJson = adjTopicJson.field("content").get
      TQAAdjunctTopic(
        sectionName = sectionName,
        text = contentJson.field("text").get.string.get.toString,
        figures = figuresFromJson(contentJson.field("figures").get),
        mediaLinks = mediaLinksFromJson(contentJson.field("mediaLinks").get),
        orderId = orderId)
    }

    def adjunctTopicsAndVocabularyFromJson(adjTopicsJson: Json): (List[TQAAdjunctTopic], List[TQAVocabularyItem]) = {
      val vocabObject = adjTopicsJson.field("Vocabulary").get
      val vocabulary = vocabObject.objectFields.get.iterator.map(word =>
        TQAVocabularyItem(word, vocabObject.field(word).get.string.get.toString)
      ).toList
      val adjunctTopics = adjTopicsJson.objectFields.get.iterator
        .filterNot(_ == "Vocabulary")
        .map(sectionName => adjunctTopicFromSectionNameAndJson(sectionName, adjTopicsJson.field(sectionName).get))
        .toList
      (adjunctTopics, vocabulary)
    }

    def answerChoicesFromJson(answerChoicesJson: Json): List[TQAAnswerChoice] = {
      answerChoicesJson.objectFields.get.iterator.map(answerLabel =>
        TQAAnswerChoice(answerLabel, labeledTextFromJson(answerChoicesJson.field(answerLabel).get))
      ).toList
    }

    def nonDiagramQuestionFromIdAndJson(globalId: String, nonDiagramQuestionJson: Json): TQANonDiagramQuestion = {
      TQANonDiagramQuestion(
        globalId = globalId,
        questionType = nonDiagramQuestionJson.field("questionType").get.string.get.toString,
        questionSubType = nonDiagramQuestionJson.field("questionSubType").get.string.get.toString,
        text = textFromJson(nonDiagramQuestionJson.field("beingAsked").get).withIdStructural(
          nonDiagramQuestionJson.field("idStructural").get.string.get.toString),
        answerChoices = answerChoicesFromJson(nonDiagramQuestionJson.field("answerChoices").get),
        correctAnswer = textFromJson(nonDiagramQuestionJson.field("correctAnswer").get))
    }

    def nonDiagramQuestionsFromJson(nonDiagramQuestionsJson: Json): Map[String, TQANonDiagramQuestion] = {
      nonDiagramQuestionsJson.objectFields.get.iterator.map { globalId =>
        globalId -> nonDiagramQuestionFromIdAndJson(globalId, nonDiagramQuestionsJson.field(globalId).get)
      }.toMap
    }

    def diagramQuestionsFromJson(diagramQuestionsJson: Json): Map[String, TQADiagramQuestion] = {
      diagramQuestionsJson.objectFields.get.iterator.map { globalId =>
        val diagramQuestionJson = diagramQuestionsJson.field(globalId).get
        globalId -> TQADiagramQuestion(
          globalId = globalId,
          questionType = diagramQuestionJson.field("questionType").get.string.get.toString,
          text = textFromJson(diagramQuestionJson.field("beingAsked").get),
          answerChoices = answerChoicesFromJson(diagramQuestionJson.field("answerChoices").get),
          correctAnswer = textFromJson(diagramQuestionJson.field("correctAnswer").get),
          imageName = diagramQuestionJson.field("imageName").get.string.get.toString,
          imagePath = diagramQuestionJson.field("imagePath").get.string.get.toString)
      }.toMap
    }

    def annotationsFromJson(annotationsJson: Json): List[TQADiagramAnnotation] = {
      annotationsJson.array.get.iterator.map { obj =>
        val rect = obj.field("rectangle").get.array.get
        val location =
          if(rect(0).number.nonEmpty) {
            Interval(begin = rect(0).number.get.toInt.get, end = rect(1).number.get.toInt.get)
          } else Rectangle(
            left = rect(0).array.get(0).number.get.toInt.get,
            top = rect(0).array.get(1).number.get.toInt.get,
            right = rect(1).array.get(0).number.get.toInt.get,
            bottom = rect(1).array.get(1).number.get.toInt.get)
        TQADiagramAnnotation(
          text = obj.field("text").get.string.get.toString,
          location = location)
      }.toList
    }

    def diagramsFromDiagramsAndAnnotationsJson(diagramsJson: Json, annotationsJson: Json): Map[String, TQADiagram] = {
      val annotations = annotationsJson.objectFields.get.map { imageName =>
        imageName -> annotationsFromJson(annotationsJson.field(imageName).get)
      }.toMap

      diagramsJson.objectFields.get.map { globalId =>
        val diagramJson = diagramsJson.field(globalId).get
        globalId -> TQADiagram(
          globalId = globalId,
          imageName = diagramJson.field("imageName").get.string.get.toString,
          imagePath = diagramJson.field("imagePath").get.string.get.toString,
          text = textFromJson(diagramJson),
          annotations = annotations.get(globalId).getOrElse(Nil))
      }.toMap
    }

    def topicsFromJson(topicsJson: Json): Map[String, TQATopic] = {
      topicsJson.objectFields.get.map { globalId =>
        val topicJson = topicsJson.field(globalId).get
        val contentJson = topicJson.field("content").get
        globalId -> TQATopic(
          globalId = globalId,
          topicName = topicJson.field("topicName").get.string.get.toString,
          text = contentJson.field("text").get.string.get.toString,
          figures = figuresFromJson(contentJson.field("figures").get),
          mediaLinks = mediaLinksFromJson(contentJson.field("mediaLinks").get))
      }.toMap
    }

    def lessonFromJson(lessonJson: Json): TQALesson = {
      val (adjunctTopics, vocabulary) = adjunctTopicsAndVocabularyFromJson(
        lessonJson.field("adjunctTopics").get
      )
      val diagrams = diagramsFromDiagramsAndAnnotationsJson(
        lessonJson.field("instructionalDiagrams").get,
        lessonJson.field("diagramAnnotations").get
      )
      TQALesson(
        globalId = lessonJson.field("globalID").get.string.get.toString,
        lessonName = lessonJson.field("lessonName").get.string.get.toString,
        topics = topicsFromJson(lessonJson.field("topics").get),
        adjunctTopics = adjunctTopics,
        vocabulary = vocabulary,
        diagramQuestions = diagramQuestionsFromJson(lessonJson.field("questions").get.field("diagramQuestions").get),
        nonDiagramQuestions = nonDiagramQuestionsFromJson(lessonJson.field("questions").get.field("nonDiagramQuestions").get),
        diagrams = diagrams)
    }

    def datasetFromJson(datasetJson: Json): TQADataset = TQADataset(
      datasetJson.array.get.iterator
        .map(lessonFromJson)
        .map(lesson => lesson.globalId -> lesson)
        .toMap
    )
  }
}
