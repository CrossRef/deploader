package org.crossref.deploader

import scala.io.Source
import scala.xml._
import scala.xml.pull._
import scala.collection.mutable.ListBuffer
import org.crossref.deploader.data._

import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._

class DepSplitter(dc: DepositContext) {

  def split() = {
    val events = new XMLEventReader(Source.fromFile(dc.getFile()))
    events.foreach((e : XMLEvent) => {
      e match {
        case EvElemStart(_, "publication", attrs, _) => {
	  dc.element = new Elem(null, "publication", attrs, TopScope)
	  dc.publication = writePublication(dc.element)
	  dc.fileDate = parseFileDate(dc.element)
        }
        case EvElemStart(_, "publisher", attrs, _) => {
	  val n = collectBranch("publisher", attrs, events)
	  writePublisher(dc.publication, n)
        }
        case EvElemStart(_, "doi_record", attrs, _) => {
          val n = collectBranch("doi_record", attrs, events)
	  writeDoi(dc, n)
        }
	case _ => Nil
      }
    })
  }

  def collectBranch(endTag : String, as : MetaData, events : XMLEventReader) : Node = {
    val kids = new ListBuffer[scala.xml.Node]
        
    while (events.next() match {
      case EvElemStart(_, tag, attrs, _) => {
	kids + collectBranch(tag, attrs, events); true
      }
      case EvText(text) => kids + new Text(text); true
      case EvElemEnd(_, endTag) => false
      case _ => true
    }) Nil

    new Elem(null, endTag, as, TopScope, kids : _*)
  }

  def parseFileDate(publicationElement : Node) = publicationElement \ "@filedate" text

  def writePublication(publicationElement : Node) = {
    val publicationTitle = (publicationElement \ "@title" text)
        
    val pubBox : Box[Publication] = Publication.find(By(Publication.title,
							publicationTitle))

    val publication = if (pubBox.isDefined) pubBox.open_! else Publication.create
	    
    publication.title(publicationTitle)
    publication.pIssn(publicationElement \ "@pissn" text)
    publication.eIssn(publicationElement \ "@eissn" text)
    publication.publicationType(publicationElement \ "@pubType" text)
    publication.save

    publication
  }

  def writePublisher(publication : Publication, publisherElement : Node) = {
    val publisherName = publisherElement \ "publisher_name" text
    val publisherBox : Box[Publisher]  = Publisher.find(By(Publisher.name,
							   publisherName))
    val publisher = if (publisherBox.isDefined) publisherBox.open_! else Publisher.create

    publisher.name(publisherElement \ "publisher_name" text)
    publisher.location(publisherElement \ "publisher_location" text)
    publisher.save
            
    val publicationsPublisher = PublicationsPublisher.create
    publicationsPublisher.publisher(publisher)
    publicationsPublisher.publication(publication)
    publicationsPublisher.save
  }
    
  def writeDoi(dc : DepositContext, doiElement : Node) = {
    val publication = dc.publication

    val doiValue = doiElement \ "doi" text
    val doiBox : Box[Doi] = Doi.find(By(Doi.doi, doiValue))
    val doi = if (doiBox.isDefined) doiBox.open_! else Doi.create
    
    doi.doi(doiElement \ "doi" text)
    doi.citationId(doiElement \ "doi" text)
    doi.dateStamp(doiElement \ "@datestamp" text)
    doi.owner(doiElement \ "@owner" text)
    doi.volume(doiElement \ "volume" text)
    doi.issue(doiElement \ "issue" text)
    doi.firstPage(doiElement \ "first_page" text)
    doi.lastPage(doiElement \ "last_page" text)
    doi.day(doiElement \ "publication_date" \ "day" text)
    doi.month(doiElement \ "publication_date" \ "month" text)
    doi.year(doiElement \ "publication_date" \ "year" text)
    doi.title(doiElement \ "article_title" text)
    doi.fileDate(dc.fileDate)
    doi.xml(doiElement toString)
    doi.save
        
    for (urlElement <- doiElement \\ "url") {
      val uri = Uri.create
      uri.url(urlElement text)
      uri.uriType(urlElement \ "@Type" text)
      uri.doi(doi)
      uri.save
    }
            
    for (authorElement <- doiElement \ "contributors" \\ "person_name") {
      val author = Author.create
      author.givenName(authorElement \ "given_name" text)
      author.surname(authorElement \ "surname" text)
      author.contributorRole(authorElement \ "@contributor_role" text)
      author.sequence(authorElement \ "@sequence" text)
      author.doi(doi)
      author.save
    }
            
    val publicationsDoi = PublicationsDoi.create
    publicationsDoi.doi(doi)
    publicationsDoi.publication(publication)
    publicationsDoi.save
  }

}
