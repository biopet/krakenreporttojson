package nl.biopet.tools.krakenreporttojson

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

object KrakenReportToJsonTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ToolTemplate.main(Array())
    }
  }
}
