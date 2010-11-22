import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class SplitterSpec extends FunSuite with ShouldMatchers, DirectoryStructure {

  val testXmlDirectory = "/Users/karl/Proj/deploader/test-data"

  def prepareXml(name : String) : DepositContext = {
    val f = new File(testXmlDirectory + "/" + name)
    DepositContext.newTestFor(name)
  }

  test("Publisher is created for non-existant publisher name") (pending)
  test("Publisher is updated for existing publisher name") (pending)
  test("Doi is created for non-existant doi") (pending)
  test("Doi is updated for existing doi") (pending)
  test("Publication is created for non-existant publication title") (pending)
  test("Publication is updated for existing publication title") (pending)
  test("Ancillary records are created for a doi record element") (pending)
  test("Multiple publications are created for XML with multiple pub elements") (pending)
  test("Malformed XML syntax doesn't kill the parser process") (pending)
  test("Non-ascii characters are represented correctly") (pending)

}
