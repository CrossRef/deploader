package org.crossref.deploader.data

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

class Uri extends LongKeyedMapper[Uri] with IdPK {
  def getSingleton = Uri
  object url extends MappedText(this)
  object uriType extends MappedString(this, 50)
  object doi extends MappedLongForeignKey(this, Doi)
}

object Uri extends Uri with LongKeyedMetaMapper[Uri]
