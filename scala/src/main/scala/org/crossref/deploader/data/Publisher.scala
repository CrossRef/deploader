package org.crossref.deploader.data

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

class Publisher extends LongKeyedMapper[Publisher] with IdPK {
  def getSingleton = Publisher
  object name extends MappedString(this, 255) {
    override def dbIndexed_? = true // & unique
  }
  object location extends MappedString(this, 255)
}

object Publisher extends Publisher with LongKeyedMetaMapper[Publisher]
