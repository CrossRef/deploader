package org.crossref.deploader

trait Log {

  def recordEvent(e : LogEvent) = {

    e match {
      case DepositStartParseEvent(f) => null
      case DepositCompleteParseEvent(f) => null
      case DepositFailParseEvent(f, r) => null
    }

  }

}
