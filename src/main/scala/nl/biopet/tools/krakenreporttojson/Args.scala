package nl.biopet.tools.krakenreporttojson

import java.io.File

case class Args(krakenreport: File = null,
                outputJson: Option[File] = None,
                skipNames: Boolean = false)
