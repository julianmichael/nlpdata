package nlpdata.structure

case class AlignedToken(
  token: String,
  originalText: String,
  whitespaceBefore: String,
  whitespaceAfter: String)
