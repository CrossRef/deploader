package org.crossref.deploader

import _root_.java.sql._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import org.crossref.deploader.data._

object DepLoader extends Application {
        
  DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

  Schemifier.schemify(true, Schemifier.infoF _, Doi, Publication, Publisher, Author, 
		      Uri, PublicationsPublisher, PublicationsDoi)
        
  while (DepositContext.hasNext()) {
    val context : DepositContext = DepositContext.next()
    context.start()
    val ds = new DepSplitter(context)
    ds.split()
    context.complete()
  }

}

object DBVendor extends ConnectionManager {
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
