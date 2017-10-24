# Manual

## Introduction
This tool converts the Kraken-report output into a JSON format.

## Example
To use this tool:
```bash
java -jar KrakenReportToJson-version.jar -i krakenreport
```

To get help:
```bash
java -jar KrakenReportToJson-version.jar --help
General Biopet options


Options for KrakenReportToJson


KrakenReportToJson - Convert Kraken-report (full) output to JSON

Usage: KrakenReportToJson [options]

  -l, --log_level <value>  Level of log information printed. Possible levels: 'debug', 'info', 'warn', 'error'
  -h, --help               Print usage
  -v, --version            Print version
  -i, --krakenreport <krakenreport>
                           Kraken report to generate stats from
  -o, --output <json>      File to write output to, if not supplied output go to stdout
  -n, --skipnames <skipnames>
                           Don't report the scientific name of the taxon.
```

## Output
A JSON object, either in stdout or in a specified file, containing the Kraken report.