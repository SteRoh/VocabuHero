package com.zettl.vocabuhero.data.csv

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

data class RawImportRow(
    val frontText: String,
    val backText: String,
    val note: String? = null
)

object CsvParser {

    fun parse(input: String, delimiter: Char = detectDelimiter(input)): List<RawImportRow> {
        return parseLines(input.lines(), delimiter)
    }

    fun parse(stream: InputStream, charset: Charset = Charsets.UTF_8, delimiter: Char? = null): List<RawImportRow> {
        val text = stream.reader(charset).readText()
        val delim = delimiter ?: detectDelimiter(text)
        return parse(text, delim)
    }

    private fun detectDelimiter(firstLineOrFull: String): Char {
        val firstLine = firstLineOrFull.lines().firstOrNull() ?: return ','
        return when {
            firstLine.contains('\t') -> '\t'
            firstLine.contains(';') -> ';'
            else -> ','
        }
    }

    fun parseWithColumnMapping(
        input: String,
        frontColumn: Int,
        backColumn: Int,
        noteColumn: Int? = null,
        delimiter: Char = detectDelimiter(input)
    ): List<RawImportRow> {
        val lines = input.lines()
        if (lines.isEmpty()) return emptyList()
        val dataLines = lines.drop(1).filter { it.isNotBlank() }
        return dataLines.mapNotNull { line ->
            val cells = splitLine(line, delimiter)
            if (cells.size <= frontColumn || cells.size <= backColumn) return@mapNotNull null
            RawImportRow(
                frontText = cells[frontColumn].trim(),
                backText = cells[backColumn].trim(),
                note = noteColumn?.takeIf { it < cells.size }?.let { cells[it].trim().takeIf { n -> n.isNotEmpty() } }
            )
        }.filter { it.frontText.isNotBlank() && it.backText.isNotBlank() }
    }

    /** Expects header + data rows. With 3 columns: Front,Back,Note. With 2: Front,Back. */
    fun parseLines(lines: List<String>, delimiter: Char): List<RawImportRow> {
        if (lines.size < 2) return emptyList()
        val dataLines = lines.drop(1).filter { it.isNotBlank() }
        return dataLines.mapNotNull { line ->
            val cells = splitLine(line, delimiter)
            when {
                cells.size >= 3 -> RawImportRow(
                    frontText = cells[0].trim(),
                    backText = cells[1].trim(),
                    note = cells[2].trim().takeIf { it.isNotEmpty() }
                )
                cells.size == 2 -> RawImportRow(frontText = cells[0].trim(), backText = cells[1].trim())
                else -> null
            }
        }.filter { it.frontText.isNotBlank() && it.backText.isNotBlank() }
    }

    private fun splitLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                inQuotes -> current.append(c)
                c == delimiter -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }
}
