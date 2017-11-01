package nl.biopet.tools.krakenreporttojson

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class KrakenReportToJsonTest extends ToolTest[Args] {
  def toolCommand: KrakenReportToJson.type = KrakenReportToJson
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      KrakenReportToJson.main(Array())
    }
  }
}
