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

def startTime = System.currentTimeMillis()

new File(inDir).eachFile { dumpFile ->
    // Move the file to the 'working' directory.
    dumpFile.renameTo(workingDir + "/" + dumpFile.getName())
    dumpFile = new File(workingDir + "/" + dumpFile.getName())
    
    // Do an insert for each deposit in the XML.
    def splitter = new DepositSplitter()
    splitter.parseFile(dumpFile)
    
    splitter.each { doc ->
        def bytes = writeXml(doc)
        
        //def doi = doc.getElementsByTagName("doi").item(0).getFirstChild().getData()
        //sql.execute("INSERT INTO deposits (doi, xml) VALUES (${doi}, ${bytes})")
        
        def root = new XmlParser().parse(new ByteArrayInputStream(bytes))
        new Transmogrifier().transmogrify(root, dumpFile)
    }
    
    // Move file into the 'out' directory.
    dumpFile.renameTo(outDir + "/" + dumpFile.getName())
}

def elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000

println "Took " + elapsedSeconds + " seconds."
println "At " + (133606 / elapsedSeconds) + " DOIs per second."
