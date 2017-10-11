package nl.biopet.tools.krakenreporttojson

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class KrakenReportToJsonTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      KrakenReportToJson.main(Array())
    }
  }
}
