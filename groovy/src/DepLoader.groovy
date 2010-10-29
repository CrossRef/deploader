import groovy.sql.Sql
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.*

def sql = Sql.newInstance("jdbc:mysq://localhost/deploader", "root", "root")
def inDir = "/Users/karl/Proj/deploader/in"
def outDir = "/Users/karl/Proj/deploader/out"

new File(inDir).forEach {
    // Do an insert for each deposit in the XML.
    def splitter = new DepositSplitter()
    def reader = SAXParserFacory.newInstace().newSAXParser().XMLReader
    
    reader.setContentHandler(splitter)
    reader.parse(new InputSource(new FileInputStream(it)))
    
    splitter.forEach { doi, xml ->
        sql.execute("INSERT INTO deposits (doi, xml) VALUES (${doi}, ${xml})")
    }
    
    // Move file into the 'out' directory.
    it.renameTo(outDir + "/" + it.getName())
}



