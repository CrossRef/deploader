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

  val working : File = new File(workingDirectory)

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
  def newFor(in : File) : DepositContext = {
    val dc = new DepositContext
    dc.inDepositFile = new File(inDirectory + File.pathSeparator + in)
    dc.outDepositFile = new File(outDirectory + File.pathSeparator + in.getName())
    dc.workingDepositFile = new File(workingDirectory + File.pathSeparator + in.getName())
    dc.errorDepositFile = new File(errorDirectory + File.pathSeparator + in.getName())
    dc
  }

  def next() : DepositContext = {
    val working = new File(workingDirectory)
    if (working.list.length > 0) {
     null
    } else {
      newFor(new File(working.list.head))
    }
  }

  def hasNext() = working.list.length > 0
}

