package nl.biopet.tools.krakenreporttojson

import java.io.{File, PrintWriter}

import nl.biopet.utils.tool.ToolCommand
import nl.biopet.utils.conversions
import play.api.libs.json.Json

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex

object KrakenReportToJson extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(toolName)
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser(toolName)
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

    logger.info("Start")

    val jsonString: String =
      reportToJson(cmdArgs.krakenreport, skipNames = cmdArgs.skipNames)
    cmdArgs.outputJson match {
      case Some(file) =>
        val writer = new PrintWriter(file)
        writer.println(jsonString)
        writer.close()
      case _ => println(jsonString)
    }

    logger.info("Done")
  }

  case class KrakenHit(taxonomyID: Long,
                       taxonomyName: String,
                       cladeCount: Long,
                       cladeSize: Long, // size of parent - including itself
                       taxonRank: String,
                       cladeLevel: Int,
                       parentTaxonomyID: Long,
                       children: ListBuffer[KrakenHit]) {
    def toJSON(withChildren: Boolean = false): Map[String, Any] = {
      val childJSON =
        if (withChildren) children.toList.map(entry => entry.toJSON(withChildren)) else List()
      Map(
        "name" -> taxonomyName,
        "taxid" -> taxonomyID,
        "taxonrank" -> taxonRank,
        "cladelevel" -> cladeLevel,
        "count" -> cladeCount,
        "size" -> cladeSize,
        "children" -> childJSON
      )
    }

  }

  var cladeIDs: mutable.ArrayBuffer[Long] = mutable.ArrayBuffer.fill(32)(0)
  val spacePattern: Regex = "^( +)".r
  private var lines: Map[Long, KrakenHit] = Map.empty

  /**
    * Takes a line from the kraken report, converts into Map with taxonID and
    * information on this hit as `KrakenHit`. `KrakenHit` is used later on for
    * building the tree
    *
    * @param krakenRawHit Line from the KrakenReport output
    * @param skipNames Specify to skip names in the report output to reduce size of JSON
    * @return
    */
  def parseLine(krakenRawHit: String, skipNames: Boolean): Map[Long, KrakenHit] = {
    val values: Array[String] = krakenRawHit.stripLineEnd.split("\t")

    assert(values.length == 6)

    val scientificName: String = values(5)
    val cladeLevel = spacePattern.findFirstIn(scientificName).getOrElse("").length / 2

    if (cladeIDs.length <= cladeLevel + 1) {
      cladeIDs ++= mutable.ArrayBuffer.fill(10)(0L)
    }

    cladeIDs(cladeLevel + 1) = values(4).toLong
    Map(
      values(4).toLong -> KrakenHit(
        taxonomyID = values(4).toLong,
        taxonomyName = if (skipNames) "" else scientificName.trim,
        cladeCount = values(2).toLong,
        cladeSize = values(1).toLong,
        taxonRank = values(3),
        cladeLevel = cladeLevel,
        parentTaxonomyID = cladeIDs(cladeLevel),
        children = ListBuffer()
      ))
  }

  /**
    * Read the `KrakenReport` output and transform into `Map` by TaxonID and `KrakenHit`
    * A JSON-string output is given.
    *
    * @param reportRaw The `KrakenReport` output
    * @param skipNames Specify to skip names in the report output to reduce size of JSON
    * @return
    */
  def reportToJson(reportRaw: File, skipNames: Boolean): String = {
    val reader = Source.fromFile(reportRaw)

    /*
     * http://ccb.jhu.edu/software/kraken/MANUAL.html
     * The header layout is:
     * 1. Percentage of reads covered by the clade rooted at this taxon
     * 2. Number of reads covered by the clade rooted at this taxon
     * 3. Number of reads assigned directly to this taxon
     * 4. A rank code, indicating (U)nclassified, (D)omain, (K)ingdom, (P)hylum, (C)lass, (O)rder, (F)amily, (G)enus, or (S)pecies. All other ranks are simply '-'.
     * 5. NCBI taxonomy ID
     * 6. indented scientific name
     * */

    lines = reader
      .getLines()
      .map(line => parseLine(line, skipNames))
      .filter(p => (p.head._2.cladeSize > 0) || List(0L, 1L).contains(p.head._2.taxonomyID))
      .foldLeft(Map.empty[Long, KrakenHit])((a, b) => {
        a + b.head
      })

    lines.keys.foreach(k => {
      // append itself to the children attribute of the parent
      if (lines(k).parentTaxonomyID > 0L) {
        // avoid the root and unclassified appending to the unclassified node
        lines(lines(k).parentTaxonomyID).children += lines(k)
      }
    })

    val result = Map("unclassified" -> lines(0).toJSON(),
      "classified" -> lines(1).toJSON(withChildren = true))
    Json.stringify(conversions.mapToJson(result))
  }
}
