package org.crossref.deploader

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import java.sql._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._

import org.crossref.deploader.data._
import org.crossref.deploader._

class SplitterSpec extends FunSuite with ShouldMatchers with DirectoryStructure {

  val testXmlDirectory = "/Users/karl/Proj/deploader/test-data"

  val shortIncompleteXml = "short-incomplete.xml"
  val shortCompleteXml = "short-complete.xml"
  val twoPublishers = "two-publishers.xml"
  val nonAscii = "non-ascii.xml"
  val malformed = "malformed.xml"

  def prepareXml(name : String) : DepositContext = {
    val f = new File(testXmlDirectory + "/" + name)
    DepositContext.newTestFor(f)
  }

  def prepareDatabase() = {
    DB.defineConnectionManager(DefaultConnectionIdentifier, TestDBVendor)

    Schemifier.destroyTables_!!(Schemifier.infoF _, Publication, Publisher, Doi, 
				Uri, Author, PublicationsDoi, PublicationsPublisher)
    Schemifier.schemify(true, Schemifier.infoF _, Doi, Publication, Publisher, 
			Author, Uri, PublicationsPublisher, PublicationsDoi)
  }

  test("Publisher is created for non-existant publisher name") {
    prepareDatabase
    val dc = prepareXml(shortCompleteXml)
    new DepSplitter(dc) split
    // Check that a publisher has been created
  }

  test("Publisher is updated for existing publisher name") {
    prepareDatabase
    val dc = prepareXml(shortIncompleteXml)
    new DepSplitter(dc) split
    val dc2 = prepareXml(shortCompleteXml)
    new DepSplitter(dc2) split
    // Check that publisher has details from complete xml
  }

  test("Doi is created for non-existant doi") {
    prepareDatabase
    val dc = prepareXml(shortCompleteXml)
    new DepSplitter(dc) split
    // Check that doi has been created
  }

  test("Doi is updated for existing doi") {
    prepareDatabase
    val dc = prepareXml(shortIncompleteXml)
    new DepSplitter(dc) split
    val dc2 = prepareXml(shortCompleteXml)
    new DepSplitter(dc) split
    // Check that doi has details from complete xml
  }

  test("Publication is created for non-existant publication title") {
    prepareDatabase
  }

  test("Publication is updated for existing publication title") (pending)
  test("Ancillary records are created for a doi record element") (pending)
  test("Multiple publishers are created for XML with multiple pub elements") (pending)
  test("Malformed XML syntax doesn't kill the parser process") (pending)
  test("Non-ascii characters are represented correctly") (pending)

}

object TestDBVendor extends ConnectionManager {
  Class.forName("com.mysql.jdbc.Driver")
    
  def newConnection(name : ConnectionIdentifier) =
    try {
      Full(DriverManager.getConnection(
        "jdbc:mysql://localhost/deploader",
        "root", 
	"root"))
    } catch {
      case e : Exception => e.printStackTrace(); Empty
    }
        
  def releaseConnection(conn : Connection) = conn.close()
}
