import scala.xml._
import ru.circumflex.orm._

object DepLoader extends Application {
    
    val publication = new Publication()
    publication.title := depositXml\"@title"
    publication.publicationType := depositXml\"@pubType"
    publication.pIssn := depositXml\"@pissn"
    publication.eIssn := depositXml\"@eissn"
    publication.save()
        
    for (publisher <- depositXml\\"publisher") {
        val publisher = new Publisher()
        publisher.name := publisher\"publisher_name" text
        publisher.location := publisher\"publisher_location" text
        publisher.save()
        
        val publicationsPublisher = new PublicationsPublisher()
        publicationsPublisher.publisher = publisher
        publicationsPublisher.publication = publication
        publicationsPublisher.save()
    }
    
    for (doiElement <- depositXml\\"doi_record") {
        val doi = new Doi()
        doi.doi := doiElement\"doi" text
        doi.citationid := doiElement\"doi" text
        doi.dateStamp := doiElement\"@datestamp"
        doi.owner := doiElement\"@owner"
        doi.volume := doiElement\"volume" text
        doi.issue := doiElement\"issue" text
        doi.firstPage := doiElement\"first_page" text
        doi.lastPage := doiElement\"last_page" text
        doi.day := doiElement\"publication_date"\"day" text
        doi.month := doiElement\"publication_date"\"month" text
        doi.year := doiElement\"publication_date"\"year" text
        doi.title := doiElement\"article_title" text
        doi.fileDate := depositXml\"@filedate"
        doi.save()
    
        for (urlElement <- doiRecord\\"url") {
            val uri = new Uri()
            uri.url := urlElement text
            uri.uriType := urlElement\"@Type"
            uri.doi := doi
            uri.save()
        }
        
        for (authorElement <- doiRecord\\"contributors") {
            val author = new Author()
            author.givenName := authorElement\"given_name" text
            author.surname := authorElement\"surname" text
            author.contributorRole := authorElement\"@contributor_role"
            author.sequence := authorElement\"@sequence"
            author.doi := doi
            author.save()
        }
        
        val publicationsDoi = new PublicationsDoi()
        publicationsDoi.doi = doi
        publicationsDoi.publication = publication
        publicationsDoi.save()
    }
}

class Publication extends Record[Publication] {
    val eIssn = "eIssn" VARCHAR NULLABLE
    val pIssn = "pIssn" VARCHAR NULLABLE
    val title = "name" VARCHAR NOT_NULL
    val publicationType = "publicationType" VARCHAR NULLABLE
}

object Publication extends Table[Publication] {
    UNIQUE(title)
    INDEX(title)
}

class Doi extends Record[Doi] {
    val doi = "doi" VARCHAR(255) NOT_NULL
    val citationId = "citationId" VARCHAR NULLABLE
    val dateStamp = "dateStamp" VARCHAR NULLABLE
    val owner = "owner" VARCHAR NULLABLE
    val volume = "volume" VARCHAR NULLABLE
    val issue = "issue" VARCHAR NULLABLE
    val firstPage = "firstPage" VARCHAR NULLABLE
    val lastPage = "lastPage" VARCHAR NULLABLE
    val day = "day" VARCHAR NULLABLE
    val month = "month" VARCHAR NULLABLE
    val year = "year" VARCHAR NULLABLE
    val title = "title" TEXT NULLABLE
    val fileDate = "fileDate" VARCHAR NULLABLE
    val xml = "xml" TEXT NOT_NULL
    doi.save()
}

class Author extends Record[Author] {
    val givenName = "givenName" VARCHAR NULLABLE
    val surname = "surname" VARCHAR NULLABLE
    val contributorRole = "contributorRole" VARCHAR NULLABLE
    val sequence = "sequence" VARCHAR NULLABLE
    val doi = REFERENCS(Doi)
}

class Uri extends Record[Uri] {
    val url = "url" TEXT NOT_NULL
    val uriType = "uriType" VARCHAR NULLABLE
    val doi = REFERENCES(Doi)
}

class Publisher extends Record[Publisher] {
    val name = "name" VARCHAR NOT_NULL
    val location = "location" TEXT NULLABLE
}

object Publisher extends Table[Publisher] {
    UNIQUE(name)
    INDEX(name)
}

class PublicationsDoi extends Record[PublishersDois] {
    val publication = REFERENCES(Publisher)
    val doi = REFERENCES(Doi)
}

class PublicationsPublisher extends Record[PublishersPublications] {
    val publisher = REFERENCES(Publisher)
    val publication = REFERENCES(Publication)
}