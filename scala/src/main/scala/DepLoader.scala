import scala.xml._
import java.io.File
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import _root_.java.sql._
import scala.xml._
import scala.xml.pull._
import scala.io.Source
import org.w3c.dom._
import javax.xml.parsers._
import scala.collection.mutable._
import scala.xml.parsing._
import org.apache.xalan.xsltc.trax.DOM2SAX

object DepLoader extends Application {
    
    def inDirectory = "../in"
    def outDirectory = "../out"
    def workingDirectory = "../working"
        
    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    Schemifier.schemify(true, Schemifier.infoF _, Doi, Publication, Publisher, Author, 
			Uri, PublicationsPublisher, PublicationsDoi)
        
    for (depositFile <- new File(inDirectory).listFiles) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val holderDoc = builder.newDocument()

        val docParts = new HashMap[String, org.w3c.dom.Element]
        
        val events = new XMLEventReader(Source.fromFile(depositFile))
        events.foreach((e : XMLEvent) => {
            e match {
                case EvElemStart(_, "publication", attrs, _) => {
		    val publicationElement = holderDoc.createElement("publication")
		    attrs.foreach(a => {
		        publicationElement.setAttribute(a.key, a.value.toString())
		    })
		    docParts.put("pub", publicationElement)
                }
                case EvElemStart(_, "publisher", a, _) => {
                    val n = collectBranch(holderDoc, "publisher", a, events)
		    val publicationElement = docParts.get("pub").head
                    publicationElement.appendChild(n)
                }
                case EvElemStart(_, "doi_record", a, _) => {
                    val n = collectBranch(holderDoc, "doi_record", a, events)
		    val publicationElement = docParts.get("pub").head
                    publicationElement.appendChild(n)
                    writeRecords(publicationElement)
                    publicationElement.removeChild(n)
                }
	        case _ => Nil
            }
        })
    }
    
    def collectBranch(d : org.w3c.dom.Document, endTag : String, as : MetaData, 
                      events : XMLEventReader) : org.w3c.dom.Element = {
        val top = d.createElement(endTag)
        as.foreach(a => {
            top.setAttribute(a.key, a.value.toString())
        })
        
        while (events.next() match {
            case EvElemStart(_, tag, attrs, _) => {
                top.appendChild(collectBranch(d, tag, attrs, events)); true
            }
            case EvText(text) => top.appendChild(d.createTextNode(text)); true
	    case EvElemEnd(_, endTag) => false
	    case _ => true
        }) Nil
        
        top
    }

    def domToNodeSeq(dom : org.w3c.dom.Node) : scala.xml.Node = {
      val dom2sax = new DOM2SAX(dom)
      val adapter = new NoBindingFactoryAdapter
      dom2sax.setContentHandler(adapter)
      dom2sax.parse()
      return adapter.rootElem
    } 
    
    def writeRecords(node : org.w3c.dom.Element) = {
        // TODO convert publicationElement to NodeSeq.
        var depositXml = domToNodeSeq(node)

        val publicationTitle = depositXml\"@title" text
        
        val pubBox : Box[Publication] = Publication.find(By(Publication.title,
							    publicationTitle))

        val publication = if (pubBox.isDefined) pubBox.open_! else Publication.create
	    
        publication.title(publicationTitle)
        publication.pIssn(depositXml\"@pissn" text)
        publication.eIssn(depositXml\"@eissn" text)
        publication.publicationType(depositXml\"@pubType" text)
        publication.save
        
        for (publisherElement <- depositXml\\"publisher") {
	    val publisherName = publisherElement\"publisher_name" text
	    val publisherBox : Box[Publisher]  = Publisher.find(By(Publisher.name,
								   publisherName))

	    val publisher = if (publisherBox.isDefined) publisherBox.open_! else Publisher.create

            publisher.name(publisherElement\"publisher_name" text)
            publisher.location(publisherElement\"publisher_location" text)
            publisher.save
            
            val publicationsPublisher = PublicationsPublisher.create
            publicationsPublisher.publisher(publisher)
            publicationsPublisher.publication(publication)
            publicationsPublisher.save
        }
        
        for (doiElement <- depositXml\\"doi_record") {
            val doiValue = doiElement\"doi" text
	    val doiBox : Box[Doi] = Doi.find(By(Doi.doi, doiValue))

            val doi = if (doiBox.isDefined) doiBox.open_! else Doi.create

            doi.doi(doiElement\"doi" text)
            doi.citationId(doiElement\"doi" text)
            doi.dateStamp(doiElement\"@datestamp" text)
            doi.owner(doiElement\"@owner" text)
            doi.volume(doiElement\"volume" text)
            doi.issue(doiElement\"issue" text)
            doi.firstPage(doiElement\"first_page" text)
            doi.lastPage(doiElement\"last_page" text)
            doi.day(doiElement\"publication_date"\"day" text)
            doi.month(doiElement\"publication_date"\"month" text)
            doi.year(doiElement\"publication_date"\"year" text)
            doi.title(doiElement\"article_title" text)
            doi.fileDate(depositXml\"@filedate" text)
            doi.xml(doiElement toString)
            doi.save
        
            for (urlElement <- doiElement\\"url") {
                val uri = Uri.create
                uri.url(urlElement text)
                uri.uriType(urlElement\"@Type" text)
                uri.doi(doi)
                uri.save
            }
            
            for (authorElement <- doiElement\\"contributors") {
                val author = Author.create
                author.givenName(authorElement\"given_name" text)
                author.surname(authorElement\"surname" text)
                author.contributorRole(authorElement\"@contributor_role" text)
                author.sequence(authorElement\"@sequence" text)
                author.doi(doi)
                author.save
            }
            
            val publicationsDoi = PublicationsDoi.create
            publicationsDoi.doi(doi)
            publicationsDoi.publication(publication)
            publicationsDoi.save
        }
    }
}

object DBVendor extends ConnectionManager {
    Class.forName("com.mysql.jdbc.Driver")
    
    def newConnection(name : ConnectionIdentifier) =
        try {
            Full(DriverManager.getConnection(
                    "jdbc:mysql://localhost/deploader",
                    "root", "root"))
        } catch {
            case e : Exception => e.printStackTrace(); Empty
        }
        
    def releaseConnection(conn : Connection) = conn.close()
}

class Publication extends LongKeyedMapper[Publication] with IdPK {
    def getSingleton = Publication
    object eIssn extends MappedString(this, 50)
    object pIssn extends MappedString(this, 50)
    object title extends MappedString(this, 255) {
        override def dbIndexed_? = true // & unique
    }
    object publicationType extends MappedString(this, 50)
}

object Publication extends Publication with LongKeyedMetaMapper[Publication]

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

class Author extends LongKeyedMapper[Author] with IdPK {
    def getSingleton = Author
    object givenName extends MappedString(this, 50)
    object surname extends MappedString(this, 50)
    object contributorRole extends MappedString(this, 50)
    object sequence extends MappedString(this, 50)
    object doi extends MappedLongForeignKey(this, Doi)
}

object Author extends Author with LongKeyedMetaMapper[Author]

class Uri extends LongKeyedMapper[Uri] with IdPK {
    def getSingleton = Uri
    object url extends MappedText(this)
    object uriType extends MappedString(this, 50)
    object doi extends MappedLongForeignKey(this, Doi)
}

object Uri extends Uri with LongKeyedMetaMapper[Uri]

class Publisher extends LongKeyedMapper[Publisher] with IdPK {
    def getSingleton = Publisher
    object name extends MappedString(this, 255) {
        override def dbIndexed_? = true // & unique
    }
    object location extends MappedText(this)
}

object Publisher extends Publisher with LongKeyedMetaMapper[Publisher]

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

object PublicationsPublisher extends PublicationsPublisher with LongKeyedMetaMapper[PublicationsPublisher]
