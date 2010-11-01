import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import groovy.sql.Sql
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.*

def sql = Sql.newInstance("jdbc:mysql://localhost/deploader", "root", "root")
def inDir = "/Users/karl/Proj/deploader/in"
def outDir = "/Users/karl/Proj/deploader/out"

def writeXml(doc) {
    def source = new DOMSource(doc)
    def bout = new ByteArrayOutputStream()
    def result = new StreamResult(bout)
    def xformer = TransformerFactory.newInstance().newTransformer()
    xformer.transform(source, result)
    return bout.toByteArray()
}

def startTime = System.currentTimeMillis()

new File(inDir).eachFile {
    // Do an insert for each deposit in the XML.
    def splitter = new DepositSplitter()
    splitter.parseFile(it)
    
    splitter.each { doc ->
        def doi = doc.getElementsByTagName("doi").item(0).getFirstChild().getData()
        
        def bytes = writeXml(doc)
        sql.execute("INSERT INTO deposits (doi, xml) VALUES (${doi}, ${bytes})")
    }
    
    // Move file into the 'out' directory.
    //it.renameTo(outDir + "/" + it.getName())
}

def elapsedSeconds = (System.currentTimeMillis() - startTime) / 60

println "Took " + elapsedSeconds + " seconds."
println "At " + (133606 / elapsedSeconds) + " DOIs per second."
