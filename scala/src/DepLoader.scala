import scala.xml._
import ru.circumflex.orm._

object DepLoader extends Application {
    
    val splitDepositXml = None
    
    var partialXml = None
    
    val depositXml = 
        <publication eissn="1550235X" filedate="3-APR-2009" mode="full" 
                     pissn="10980121" pubType="journal" title="Physical Review B">
            <publisher>
                <publisher_name>American Physical Society (APS)</publisher_name>
                <publisher_result_name>American Physical Society (APS)</publisher_result_name>
                <publisher_location>Ridge, NY USA</publisher_location>
                <email_address>rakelly@aps.org</email_address>
            </publisher>
            <doi_record citationid="5576726" datestamp="2007-08-05" owner="10.1103">
                <doi>10.1103/PhysRevB.66.132511</doi>
                <contributors>
                    <person_name contributor_role="author" sequence="first">
                        <given_name>Nobuhiko</given_name>
                        <surname>Hayashi</surname>
                    </person_name>
                    <person_name contributor_role="author" sequence="additional">
                        <given_name>Yusuke</given_name>
                        <surname>Kato</surname>
                    </person_name>
                </contributors>
                <volume>66</volume>
                <issue>13</issue>
                <first_page>132511</first_page>
                <publication_date>
                    <month>10</month>
                    <year>2002</year>
                </publication_date>
                <publication_type>full_text</publication_type>
                <article_title>Elementary vortex pinning potential in a chiral p-wave superconductor</article_title>
                <url type="xref:url:prime">http://link.aps.org/doi/10.1103/PhysRevB.66.132511</url>
            </doi_record>
        </publication>
        
    val name = depositXml\"@title"
    val publicationType = depositXml\"@pubType"
    val pIssn = depositXml\"@pissn"
    val eIssn = depositXml\"@eissn"
    
    val doi = depositXml\"doi_record"\"doi" text
    val citationId = depositXml\"doi_record"\"doi" text
    val dateStamp = depositXml\"doi_record"\"@datestamp"
    val owner = depositXml\"doi_record"\"@owner"
    val volume = depositXml\"doi_record"\"volume" text
    val issue = depositXml\"doi_record"\"issue" text
    val firstPage = depositXml\"doi_record"\"first_page" text
    val lastPage = depositXml\"doi_record"\"last_page" text
    val day = depositXml\"doi_record"\"publication_date"\"day" text
    val month = depositXml\"doi_record"\"publication_date"\"month" text
    val year = depositXml\"doi_record"\"publication_date"\"year" text
    val title = depositXml\"doi_record"\"article_title" text
    val fileDate = depositXml\"@filedate"
    
    for (publisher <- depositXml\\"publisher") {
        val name = publisher\"publisher_name" text
        val location = publisher\"publisher_location" text
    }
    
    for (uri <- depositXml\"doi_record"\\"url") {
        val url = uri text
        val uriType = uri\"@type"
    }
    
    for (author <- depositXml\"doi_record"\\"contributors") {
        val givenName = author\"given_name" text
        val surname = author\"surname" text
        val contributorRole = author\"@contributor_role"
        val sequence = author\"@sequence"
    }
    
    println(doi)
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
    val id = field(Doi.id)
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
}

class Author extends Record[Author] {
    val givenName = "givenName" VARCHAR NULLABLE
    val surname = "surname" VARCHAR NULLABLE
    val contributorRole = "contributorRole" VARCHAR NULLABLE
    val sequence = "sequence" VARCHAR NULLABLE
}

class Uri extends Record[Uri] {
    val url = "url" TEXT NOT_NULL
    val uriType = "uriType" VARCHAR NULLABLE
}

class Publisher extends Record[Publisher] {
    val name = "name" VARCHAR NOT_NULL
    val location = "location" TEXT NULLABLE
}

object Publisher extends Table[Publisher] {
    UNIQUE(name)
    INDEX(name)
}