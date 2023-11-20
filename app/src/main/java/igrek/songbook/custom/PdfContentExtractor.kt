package igrek.songbook.custom

import com.itextpdf.kernel.geom.Vector
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk
import java.io.InputStream
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.roundToInt

class PdfContentExtractor {
    // (0,0) is in bottom left corner
    private var leftMargin: Float? = null // min page X
    private var lineheight: Float? = null

    @Suppress("UNCHECKED_CAST")
    fun extractPdfContent(inputStream: InputStream): String {
        val reader = PdfReader(inputStream)
        val pdfDoc = PdfDocument(reader)

        val builder = StringBuilder()
        (1..pdfDoc.numberOfPages).forEach { pageIndex ->
            val strategy = LocationTextExtractionStrategy()
            val page = pdfDoc.getPage(pageIndex)

            PdfTextExtractor.getTextFromPage(page, strategy)
            val classObject = strategy.javaClass
            val fieldObject = classObject.getDeclaredField("locationalResult")
            fieldObject.isAccessible = true
            val textChunks: List<TextChunk> = (fieldObject.get(strategy) as? List<TextChunk>) ?: emptyList()

            measurePdfDocumentDimensions(textChunks)
            buildTextFromChunks(textChunks, builder)

            if (pageIndex < pdfDoc.numberOfPages)
                builder.append("\n")
        }
        return builder.toString().trim{ it == '\n' }
    }

    private fun measurePdfDocumentDimensions(textChunks: List<TextChunk>) {
        var lastLineY: Float? = null
        textChunks.forEach { chunk: TextChunk ->
            val startPoint: Vector = chunk.location.startLocation
            val textMinX = startPoint.get(0)
            val lineY = startPoint.get(1)
            leftMargin = min(leftMargin ?: textMinX, textMinX)
            lastLineY?.let { lastLineY ->
                val lastGap = abs(lineY - lastLineY)
                if (lastGap > 0) {
                    lineheight = min(lineheight ?: lastGap, lastGap)
                }
            }
            lastLineY = lineY
        }
    }

    private fun buildTextFromChunks(textChunks: List<TextChunk>, builder: StringBuilder) {
        val lineHeightN = this.lineheight ?: 0f

        val chunkLines: MutableList<MutableList<TextChunk>> = mutableListOf()
        var lastChunk: TextChunk? = null
        var currentLine: MutableList<TextChunk> = mutableListOf()
        if (textChunks.isNotEmpty())
            chunkLines.add(currentLine)
        for (chunk in textChunks) {
            if (lastChunk == null || chunk.sameLine(lastChunk)) {
                currentLine.add(chunk)
            } else {
                currentLine = mutableListOf()
                chunkLines.add(currentLine)
                currentLine.add(chunk)
            }
            lastChunk = chunk
        }

        var lastLineY: Float? = null
        for (line in chunkLines) {
            val lineY: Float = line.first().location.startLocation.get(1)

            if (lastLineY != null) {
                val lastGap = abs(lineY - lastLineY)
                if (lastGap > 0) {
                    val linesGap = (lastGap / lineHeightN).roundToInt()
                    if (linesGap > 1) { // Empty lines
                        val newLines = linesGap - 1
                        builder.append("\n".repeat(newLines))
                    }
                }
            }

            buildLineFromChunks(line, builder)
            builder.append('\n')

            lastLineY = lineY
        }
    }

    private fun buildLineFromChunks(textChunks: List<TextChunk>, builder: StringBuilder) {
        val pageMinX = this.leftMargin ?: 0f
        var lastXRight: Float? = null
        for (chunk in textChunks) {

            val textXLeft: Float = chunk.location.startLocation.get(0)
            val textXRight: Float = chunk.location.endLocation.get(0)
            val spaceWidth: Float = chunk.location.charSpaceWidth

            if (lastXRight == null) { // indentation
                val spacesIndent = ((textXLeft - pageMinX) / spaceWidth).roundToInt()
                if (spacesIndent > 0) {
                    builder.append(" ".repeat(spacesIndent))
                }
            } else { // spaces between words
                val spacesIndent = ((textXLeft - lastXRight) / spaceWidth).roundToInt()
                if (spacesIndent > 0) {
                    builder.append(" ".repeat(spacesIndent))
                }
            }

            builder.append(chunk.text)

            lastXRight = textXRight
        }
    }

    private fun (TextChunk).sameLine(lastChunk: TextChunk): Boolean {
        return location.sameLine(lastChunk.location)
    }
}