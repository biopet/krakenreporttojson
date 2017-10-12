package nl.biopet.tools.krakenreporttojson

import java.io.File

import nl.biopet.utils.tool.AbstractOptParser

class ArgsParser(cmdName: String) extends AbstractOptParser[Args](cmdName) {
  head(s"""
          |$cmdName - Convert Kraken-report (full) output to JSON
      """.stripMargin)

  opt[File]('i', "krakenreport") required () unbounded () valueName "<krakenreport>" action {
    (x, c) =>
      c.copy(krakenreport = x)
  } validate { x =>
    if (x.exists) success else failure("Krakenreport not found")
  } text "Kraken report to generate stats from"

  opt[File]('o', "output") unbounded () valueName "<json>" action { (x, c) =>
    c.copy(outputJson = Some(x))
  } text "File to write output to, if not supplied output go to stdout"

  opt[Boolean]('n', "skipnames") unbounded () valueName "<skipnames>" action { (x, c) =>
    c.copy(skipNames = x)
  } text "Don't report the scientific name of the taxon."
}
