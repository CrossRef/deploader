package org.crossref.deploader

class LogEvent() {

  val atTime = System.currentTimeMillis()

}

case class DepositStartParseEvent(xmlFile: String) extends LogEvent
case class DepositCompleteParseEvent(xmlFile: String) extends LogEvent
case class DepositFailParseEvent(xmlFile: String, description: String) extends LogEvent
case class DepositXmlErrorEvent(xmlFile: String, line: Int, col: Int, desc: String) 
		 extends LogEvent

