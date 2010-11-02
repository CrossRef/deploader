import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.*

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

def trans = new Transmogrifier()
trans.setupTables()

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
        def root = new XmlParser().parse(new ByteArrayInputStream(bytes))
        trans.transmogrify(root, dumpFile, new String(bytes))
    }
    
    // Move file into the 'out' directory.
    dumpFile.renameTo(outDir + "/" + dumpFile.getName())
}

def elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000

println "Took " + elapsedSeconds + " seconds."
println "At " + (133606 / elapsedSeconds) + " DOIs per second."
