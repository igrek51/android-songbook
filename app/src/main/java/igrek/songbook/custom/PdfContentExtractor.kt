package igrek.songbook.custom

import com.itextpdf.kernel.geom.Vector
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk
import igrek.songbook.info.logger.LoggerFactory.logger
import java.io.InputStream
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.roundToInt

class PdfContentExtractor {

    private var pageMinX: Float? = null
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
        // (0,0) in bottom left corner
        var lastLineY: Float? = null

        textChunks.forEach { chunk: TextChunk ->

            val startPoint: Vector = chunk.location.startLocation
            val endPoint: Vector = chunk.location.endLocation

            val textMinX = startPoint.get(0)
            val lineY = startPoint.get(1)
            pageMinX = min(pageMinX ?: textMinX, textMinX)
            lastLineY?.let { lastLineY ->
                val lastGap = abs(lineY - lastLineY)
                if (lastGap > 0) {
                    lineheight = min(lineheight ?: lastGap, lastGap)
                }
            }
            lastLineY = lineY
        }

//        fun measureTextChunk(text: String, segment: LineSegment) {
//            val startPoint: Vector = segment.startPoint
//            val endPoint: Vector = segment.endPoint
//
//            val textMinX = startPoint.get(0)
//            val lineY = startPoint.get(1)
//            pageMinX = min(pageMinX ?: textMinX, textMinX)
//            lastLineY?.let { lastLineY ->
//                val lastGap = abs(lineY - lastLineY)
//                if (lastGap > 0) {
//                    minLineheight = min(minLineheight ?: lastGap, lastGap)
//                }
//            }
//            lastLineY = lineY
//
//            logger.debug("pdf text: ${text}, $startPoint - $endPoint")
//        }
//
//        val strategy = object : LocationTextExtractionStrategy() {
//            override fun eventOccurred(data: IEventData?, type: EventType?) {
//                if (type == EventType.RENDER_TEXT) {
//                    val renderInfo = data as? TextRenderInfo
//                    val text = renderInfo?.text
//                    val segment = renderInfo?.baseline
//                    if (text != null && segment != null) {
//                        measureTextChunk(text, segment)
//                    }
//                }
//                super.eventOccurred(data, type)
//            }
//        }
//
//        (1..pdfDoc.numberOfPages).forEach { pageIndex ->
//            val page = pdfDoc.getPage(pageIndex)
//            PdfTextExtractor.getTextFromPage(page, strategy)
//        }
    }

    private fun buildTextFromChunks(textChunks: List<TextChunk>, builder: StringBuilder) {
        val lineHeight = this.lineheight ?: 0f

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

                    val linesGap = (lastGap / lineHeight).roundToInt()
                    if (linesGap > 1) { // Empty lines
                        val newLines = linesGap - 1

                        logger.debug("NEW LINE")

                        builder.append("\n".repeat(newLines))
                    }
                }
            }

            buildLineFromChunks(line, builder)
            builder.append('\n')

            lastLineY = lineY
        }



//        for (chunk in textChunks) {
//
//            val textXLeft: Float = chunk.location.startLocation.get(0)
//            val textXRight: Float = chunk.location.endLocation.get(0)
//            val lineY: Float = chunk.location.startLocation.get(1)
//            val spaceWidth: Float = chunk.location.charSpaceWidth
//
////            logger.debug("chunk", chunk.text)
//
//            val spacesIndent = ((textXLeft - pageMinX) / spaceWidth).roundToInt()
//            if (spacesIndent > 0) {
//                builder.append(" ".repeat(spacesIndent))
//            }
//
//            if (lastChunk == null) {
//                builder.append(chunk.text)
//            } else {
//                if (chunk.sameLine(lastChunk)) {
//                    // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
//                    if (isChunkAtWordBoundary(chunk, lastChunk)
//                        && !startsWithSpace(chunk.text)
//                        && !endsWithSpace(lastChunk.text)
//                    ) {
//                        builder.append(' ')
//                    }
//                    builder.append(chunk.text)
//                } else {
//
//                    val lastGap = abs(lineY - (lastLineY ?: lineY))
//
//                    if (lastGap < 0) { // New page
//                        //super.writeString("\n")
//                    } else if (lastGap > 0) {
//
//                        val linesGap = (lastGap / lineHeight).roundToInt()
//                        if (linesGap > 1) { // Empty lines
//                            val newLines = linesGap - 1
//
//                            logger.debug("NEW LINE")
//                            builder.append("\n".repeat(newLines))
//                        }
//                    }
//
//
//                    builder.append('\n')
//                    builder.append(chunk.text)
//                }
//            }
//
//            lastChunk = chunk
//            lastLineY = lineY
//        }
    }

    private fun buildLineFromChunks(textChunks: List<TextChunk>, builder: StringBuilder) {
        val pageMinX = this.pageMinX ?: 0f
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

    private fun isChunkAtWordBoundary(chunk: TextChunk, previousChunk: TextChunk): Boolean {
        return chunk.location.isAtWordBoundary(previousChunk.location)
    }

    private fun startsWithSpace(str: String): Boolean {
        return str.isNotEmpty() && str[0] == ' '
    }

    private fun endsWithSpace(str: String): Boolean {
        return str.isNotEmpty() && str[str.length - 1] == ' '
    }
}