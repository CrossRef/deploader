import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.groovy.runtime.IteratorClosureAdapter;

import com.sun.tools.internal.ws.wsdl.document.Documentation;

class DepositSplitter implements Iterable, Iterator {
    
    private containerDoc
    
    private containerRoot
    
    private eleReader
    
    private reader
    
    private lastDoiRecordAppend
    
    def parseFile(File file) {
        def factory = XMLInputFactory.newInstance()
        reader = factory.createXMLEventReader(new FileInputStream(file))
        
        eleReader = factory.createFilteredReader(reader,
            new EventFilter() {
                public boolean accept(XMLEvent event) {
                    if (event.isStartElement()) {
                        return ("doi_record".equals(event.asStartElement()
                                                        .getName()
                                                        .getLocalPart())
                             || "publisher".equals(event.asStartElement()
                                                        .getName()
                                                        .getLocalPart())
                             || "publication".equals(event.asStartElement()
                                                          .getName()
                                                          .getLocalPart()))
                    }
                    return false
                }
            }
        )
        
        def docFact = DocumentBuilderFactory.newInstance()
        def builder = docFact.newDocumentBuilder()
        def impl = builder.getDOMImplementation()
        
        containerDoc = impl.createDocument(null, null, null)
        
        while (eleReader.hasNext()) {
            XMLEvent e = eleReader.next()
            
            def startEleEvent = e.asStartElement()
            def name = startEleEvent.getName().getLocalPart()
            
            if ("publication".equals(name)) {
                containerRoot = readElement(startEleEvent)
                containerDoc.appendChild(containerRoot)
            } else if ("publisher".equals(name)) {
                containerRoot.appendChild(readPartialTree(startEleEvent))
                break
                // Now we read doi_record elements via this class's 
                // iterator.
            }
        }
    }
    
    def readElement(e) {
        def ele = containerDoc.createElement(e.getName().getLocalPart())
        e.getAttributes().each {
            ele.setAttribute(it.getName().getLocalPart(),
                             it.getValue())
        }
        return ele
    }
    
    def readPartialTree(e) {
        def child = readElement(e)
        
        while (reader.hasNext()) {
            XMLEvent event = reader.next()
            if (event.isCharacters()) {
                def text = containerDoc.createTextNode()
                text.setData(event.asCharacters().data)
                child.appendChild(text)
            } else if (event.isStartElement()) {
                def ele = readPartialTree(event)
                child.appendChild(ele)
            } else if (event.isEndElement()) {
                break
            }
        }
        
        return child
    }
    
    boolean hasNext() {
        return eleReader.hasNext()
    }
    
    Object next() {
        if (lastDoiRecordAppend != null) {
            containerRoot.removeChild(lastDoiRecordAppend)
            lastDoiRecordAppend = null
        }
        
        XMLEvent e = eleReader.next()
        
        def startEleEvent = e.asStartElement()
        def name = startEleEvent.getName().getLocalPart()
        
        if ("doi_record".equals(name)) {
            lastDoiRecordAppend = readPartialTree(e)
            containerRoot.appendChild(lastDoiRecordAppend)
        }
        
        return containerDoc
    }
    
    void remove() {
        throw new UnsupportedOperationException()
    }
    
    Iterator<Map> iterator() {
        return this
	}
    
}
