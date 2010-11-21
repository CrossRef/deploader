package org.crossref.deploader

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.Date

trait Log {

  val writer = new FileWriter("log.out")
  val buffWriter = new BufferedWriter(writer)
  val formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSSS")

  def recordEvent(e : LogEvent) = {
    buffWriter.write(formatter.format(new Date(e.atTime)) + ": " + e.toString())
    buffWriter.newLine()
    buffWriter.flush()
  }

}
