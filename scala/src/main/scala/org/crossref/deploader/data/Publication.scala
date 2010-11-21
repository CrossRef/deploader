package org.crossref.deploader.data

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

class Publication extends LongKeyedMapper[Publication] with IdPK {
  def getSingleton = Publication
  object eIssn extends MappedString(this, 50)
  object pIssn extends MappedString(this, 50)
  object title extends MappedString(this, 50) {
    override def dbIndexed_? = true // & unique
  }
  object publicationType extends MappedString(this, 50)
}

object Publication extends Publication with LongKeyedMetaMapper[Publication]

class PublicationsDoi extends LongKeyedMapper[PublicationsDoi] with IdPK {
  def getSingleton = PublicationsDoi
  object publication extends MappedLongForeignKey(this, Publication) {
    override def dbIndexed_? = true
  }
  object doi extends MappedLongForeignKey(this, Doi) {
    override def dbIndexed_? = true
  }
}

object PublicationsDoi extends PublicationsDoi with LongKeyedMetaMapper[PublicationsDoi]

class PublicationsPublisher extends LongKeyedMapper[PublicationsPublisher] with IdPK {
  def getSingleton = PublicationsPublisher
  object publication extends MappedLongForeignKey(this, Publication) {
    override def dbIndexed_? = true
  }
  object publisher extends MappedLongForeignKey(this, Publisher) {
    override def dbIndexed_? = true
  }
}

object PublicationsPublisher extends PublicationsPublisher
			     with LongKeyedMetaMapper[PublicationsPublisher]
