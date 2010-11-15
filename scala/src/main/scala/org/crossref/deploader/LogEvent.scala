class LogEvent() {

  val atTime = System.currentTimeMillis()

}

case class DepositStartParseEvent(xmlFile: String) extends LogEvent
case class DepositCompleteParseEvent(xmlFile: String) extends LogEvent
case class DepositFailParseEvent(xmlFile: String, description: String) extends LogEvent
