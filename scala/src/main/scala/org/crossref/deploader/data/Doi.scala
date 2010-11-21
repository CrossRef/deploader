package org.crossref.deploader.data

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

class Doi extends LongKeyedMapper[Doi] with IdPK {
  def getSingleton = Doi
  object doi extends MappedString(this, 255) {
    override def dbIndexed_? = true // & unique
  }
  object citationId extends MappedString(this, 50)
  object dateStamp extends MappedString(this, 50)
  object owner extends MappedString(this, 50)
  object volume extends MappedString(this, 50)
  object issue extends MappedString(this, 50)
  object firstPage extends MappedString(this, 50)
  object lastPage extends MappedString(this, 50)
  object day extends MappedString(this, 50)
  object month extends MappedString(this, 50)
  object year extends MappedString(this, 50)
  object title extends MappedText(this)
  object fileDate extends MappedString(this, 50)
  object xml extends MappedText(this)
}

object Doi extends Doi with LongKeyedMetaMapper[Doi]
