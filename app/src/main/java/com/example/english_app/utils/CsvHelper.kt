package com.example.english_app.utils

import com.example.english_app.data.local.entity.WordEntity

object CsvHelper {

    // ── Header & template ─────────────────────────────────────────────────────
    const val CSV_HEADER =
        "word,pronunciation,meaning,description,example,collocation,relatedWords,note"

    val CSV_TEMPLATE_ROWS = listOf(
        listOf(
            "abandon", "/əˈbændən/", "từ bỏ",
            "to leave a place, person or thing",
            "He abandoned his car on the motorway.",
            "abandon hope / abandon ship",
            "forsake, desert, give up",
            "Irregular verb - irregular past tense"
        ),
        listOf(
            "acquire", "/əˈkwaɪər/", "có được; thu thập",
            "to gain something, especially knowledge or a skill",
            "She acquired a good knowledge of English.",
            "acquire knowledge / acquire skills",
            "obtain, gain, get",
            "Commonly used in formal/academic context"
        )
    )

    fun buildTemplate(): String {
        val sb = StringBuilder()
        sb.appendLine(CSV_HEADER)
        CSV_TEMPLATE_ROWS.forEach { cols ->
            sb.appendLine(cols.joinToString(",") { escapeCsvField(it) })
        }
        return sb.toString()
    }

    // ── Export ────────────────────────────────────────────────────────────────
    fun exportToCsv(words: List<WordEntity>): String {
        val sb = StringBuilder()
        sb.appendLine(CSV_HEADER)
        words.forEach { w ->
            sb.appendLine(
                listOf(
                    w.word, w.pronunciation, w.meaning,
                    w.description, w.example, w.collocation,
                    w.relatedWords, w.note
                ).joinToString(",") { escapeCsvField(it) }
            )
        }
        return sb.toString()
    }

    // ── Import ────────────────────────────────────────────────────────────────
    /** Returns list of WordEntity (id=0, setId = provided value) */
    fun parseWords(csvContent: String, setId: Int): List<WordEntity> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        // Skip header row if present
        val startIndex = if (lines.first().trim().lowercase().startsWith("word")) 1 else 0

        return lines.drop(startIndex).mapNotNull { line ->
            val cols = parseCsvLine(line)
            val word = cols.getOrElse(0) { "" }.trim()
            val meaning = cols.getOrElse(2) { "" }.trim()
            if (word.isBlank() || meaning.isBlank()) null
            else WordEntity(
                setId = setId,
                word = word,
                pronunciation = cols.getOrElse(1) { "" }.trim(),
                meaning = meaning,
                description = cols.getOrElse(3) { "" }.trim(),
                example = cols.getOrElse(4) { "" }.trim(),
                collocation = cols.getOrElse(5) { "" }.trim(),
                relatedWords = cols.getOrElse(6) { "" }.trim(),
                note = cols.getOrElse(7) { "" }.trim()
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun escapeCsvField(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"'); i++ // escaped quote ""
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

