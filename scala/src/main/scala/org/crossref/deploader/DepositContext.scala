package org.crossref.deploader

import java.io.File
import java.io.FilenameFilter
import scala.xml._
import org.crossref.deploader.data.Publication

class DepositContext extends DirectoryStructure with Log {

  var inDepositFile : File = null
  var outDepositFile : File = null
  var workingDepositFile : File = null
  var errorDepositFile : File = null
  var currentDepositFile : File = null

  var element : Elem = null
  var publication : Publication = null

  val inD : File = new File(inDirectory)

  // TODO sync?
  def start() = {
    inDepositFile.renameTo(workingDepositFile)
    currentDepositFile = workingDepositFile
    recordEvent(DepositStartParseEvent(currentDepositFile.getName()))
  }

  def abort(reason: String) = {
    currentDepositFile.renameTo(errorDepositFile)
    currentDepositFile = errorDepositFile
    recordEvent(DepositFailParseEvent(currentDepositFile.getName(), reason))
  }

  def revert(reason: String) = {
    currentDepositFile.renameTo(inDepositFile)
    currentDepositFile = inDepositFile
    recordEvent(DepositFailParseEvent(currentDepositFile.getName(), reason))
  }

  def complete() = {
    currentDepositFile.renameTo(outDepositFile)
    currentDepositFile = outDepositFile
    recordEvent(DepositCompleteParseEvent(currentDepositFile.getName()))
  }

  def getFile() = currentDepositFile

}

object DepositContext extends DepositContext with Iterator[DepositContext] {
  def newFor(inName : String) : DepositContext = {
    val dc = new DepositContext
    dc.inDepositFile = new File(inDirectory + File.separator + inName)
    dc.outDepositFile = new File(outDirectory + File.separator + inName)
    dc.workingDepositFile = new File(workingDirectory + File.separator + inName)
    dc.errorDepositFile = new File(errorDirectory + File.separator + inName)
    dc.currentDepositFile = dc.inDepositFile
    dc
  }

  def next() : DepositContext = {
    if (inD.list.length == 0) {
      null
    } else {
      newFor(inD.list.head)
    }
  }

  def hasNext() = inD.list.length > 0
}

