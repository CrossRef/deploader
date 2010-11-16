import scala.xml._
import java.io.File
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import _root_.java.sql._
import scala.xml._
import scala.xml.pull._
import scala.io.Source
import javax.xml.parsers._
import scala.collection.mutable._
import scala.xml.parsing._
import org.apache.xalan.xsltc.trax.DOM2SAX

class PublicationContext {
    var element : Elem = null
    var dbObject : Publication = null
}

object DepLoader extends Application {
    
    val inDirectory = "../in"
    val outDirectory = "../out"
    val workingDirectory = "../working"
        
    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    Schemifier.schemify(true, Schemifier.infoF _, Doi, Publication, Publisher, Author, 
			Uri, PublicationsPublisher, PublicationsDoi)
        
    for (depositFile <- new File(inDirectory).listFiles) {

        println("Processing " + depositFile.getName())

	val workingDepositFile = new File(workingDirectory + "/" + depositFile.getName())
        val outDepositFile = new File(outDirectory + "/" + depositFile.getName())

        depositFile.renameTo(workingDepositFile)
        
        val pc = new PublicationContext

        val startTime = System.currentTimeMillis()

        val events = new XMLEventReader(Source.fromFile(workingDepositFile))
        events.foreach((e : XMLEvent) => {
            e match {
                case EvElemStart(_, "publication", attrs, _) => {
		    pc.element = new Elem(null, "publication", attrs, TopScope)
		    pc.dbObject = writePublication(pc.element)
                }
                case EvElemStart(_, "publisher", attrs, _) => {
		    val n = collectBranch("publisher", attrs, events)
		    writePublisher(pc.dbObject, n)
                }
                case EvElemStart(_, "doi_record", attrs, _) => {
                    val n = collectBranch("doi_record", attrs, events)
		    writeDoi(pc.dbObject, n)
                }
	        case _ => Nil
            }
        })

	val endTime = System.currentTimeMillis()
        println("Successfully completed processing of " + depositFile.getName())
        println("It took " + ((endTime - startTime) / 1000) + " seconds")

        depositFile.renameTo(outDepositFile)
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

    def writePublication(publicationElement : Node) = {
        val publicationTitle = (publicationElement\"@title" text)
        
        val pubBox : Box[Publication] = Publication.find(By(Publication.title,
							    publicationTitle))

        val publication = if (pubBox.isDefined) pubBox.open_! else Publication.create
	    
        publication.title(publicationTitle)
        publication.pIssn(publicationElement\"@pissn" text)
        publication.eIssn(publicationElement\"@eissn" text)
        publication.publicationType(publicationElement\"@pubType" text)
        publication.save

        publication
    }

    def writePublisher(publication : Publication, publisherElement : Node) = {
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
    
    def writeDoi(publication : Publication, doiElement : Node) = {
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
        //doi.fileDate(depositXml\"@filedate" text)
        //doi.xml(doiElement toString)
        doi.save
        
            for (urlElement <- doiElement\\"url") {
                val uri = Uri.create
                uri.url(urlElement text)
                uri.uriType(urlElement\"@Type" text)
                uri.doi(doi)
                uri.save
            }
            
            for (authorElement <- doiElement\"contributors"\\"person_name") {
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
    object title extends MappedString(this, 50) {
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
    object location extends MappedString(this, 255)
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
