package org.crossref.deploader.data

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

class Author extends LongKeyedMapper[Author] with IdPK {
  def getSingleton = Author
  object givenName extends MappedString(this, 50)
  object surname extends MappedString(this, 50)
  object contributorRole extends MappedString(this, 50)
  object sequence extends MappedString(this, 50)
  object doi extends MappedLongForeignKey(this, Doi)
}

object Author extends Author with LongKeyedMetaMapper[Author]
