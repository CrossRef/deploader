import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import groovy.sql.Sql
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.*

def sql = Sql.newInstance("jdbc:mysql://localhost/deploader", "root", "root")
def inDir = "../in"
def outDir = "../out"
def workingDir = "../working"

def writeXml(doc) {
    def source = new DOMSource(doc)
    def bout = new ByteArrayOutputStream()
    def result = new StreamResult(bout)
    def xformer = TransformerFactory.newInstance().newTransformer()
    xformer.transform(source, result)
    return bout.toByteArray()
}

def writeDoi(root) {
    def doi = root.publication.doi_record.doi.text()
    def articleTitle = root.publication.doi_record.article_title.text()
    def owner = root.publication.doi_record.'@owner'
    def citeId = root.publication.doi_record.'@citationid'
    def dateStamp = root.publication.doi_record.'@datestamp'
    def fileDate = root.publication.'@filedate'
    def volume = root.publication.doi_record.volume.text()
    def issue = root.publication.doi_record.issue.text()
    def year = root.publication.doi_record.publication_date.year.text()
    def month = root.publication.doi_record.publication_date.month.text()
    def day = root.publication.doi_record.publication_date.day.text()
    def firstPage = root.publication.doi_record.first_page.text()
    def lastPage = root.publication.doi_record.last_page.text()
}

def writeAuthor(doiId, root) {
    root.publication.doi_record.contributors.collect {
        def surname = it.surname.text()
        def givenName = it.given_name.text()
        def sequence = it.'@sequence'
        def role = it.'@contributor_role'
    }
}

def writeUrl(doiId, root) {
    root.publication.doi_record.url.collect {
        def uri = it.text()
        def host = "" // Split uri
    }
}

def associatePublisher(doiId, root) {
    root.publication.publisher.collect {
        def name = it.publisher_name.text()
        def location = it.publisher_localtion.text()
    }
}

def associatePublication(doiId, root) {
    def title = root.publication.'@title'
    def pIssn = root.publication.'@pissn'
    def eIssn = root.publication.'@eissn'
    def publicationType = root.publication.'@pubType'
}

def startTime = System.currentTimeMillis()

new File(inDir).eachFile {
    // Move the file to the 'working' directory.
    it.renameTo(workingDir + "/" + it.getName())
    it = new File(workingDir + "/" + it.getName())
    
    // Do an insert for each deposit in the XML.
    def splitter = new DepositSplitter()
    splitter.parseFile(it)
    
    splitter.each { doc ->
        def bytes = writeXml(doc)
        
        //def doi = doc.getElementsByTagName("doi").item(0).getFirstChild().getData()
        //sql.execute("INSERT INTO deposits (doi, xml) VALUES (${doi}, ${bytes})")
        
        def root = new XmlParser().parse(new ByteArrayInputStream(bytes))
        def doiId = writeDoi(root)
        writeAuthor(doiId, root)
        writeUrl(doiId, root)
        associatePublisher(doiId, root)
        associatePublication(doiId, root)
    }
    
    // Move file into the 'out' directory.
    it.renameTo(outDir + "/" + it.getName())
}

def elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000

println "Took " + elapsedSeconds + " seconds."
println "At " + (133606 / elapsedSeconds) + " DOIs per second."
