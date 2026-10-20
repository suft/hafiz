package suft.android.ayahiq.domain

import java.io.InputStream
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * Parses the Tanzil "simple" Quran XML:
 *
 * ```xml
 * <quran>
 *   <sura index="1" name="الفاتحة">
 *     <aya index="1" text="..." />
 * ```
 *
 * Uses SAX (available on both Android and the JVM) so it is covered by plain unit tests.
 */
object QuranXmlParser {
    private const val AYAH_COUNT = 6236

    fun parse(input: InputStream): Quran {
        val surahs = ArrayList<Pair<String, MutableList<String>>>(Quran.SURAH_COUNT)
        val handler = object : DefaultHandler() {
            override fun startElement(
                uri: String?,
                localName: String?,
                qName: String?,
                attributes: Attributes,
            ) {
                when (qName?.takeIf { it.isNotEmpty() } ?: localName) {
                    "sura" -> surahs += attributes.getValue("name") to ArrayList()
                    "aya" -> surahs.last().second += attributes.getValue("text")
                }
            }
        }
        input.use { SAXParserFactory.newInstance().newSAXParser().parse(it, handler) }
        check(surahs.size == Quran.SURAH_COUNT) { "Expected 114 surahs, found ${surahs.size}" }
        val ayahs = surahs.sumOf { it.second.size }
        check(ayahs == AYAH_COUNT) { "Expected $AYAH_COUNT ayahs, found $ayahs" }
        return Quran.build(surahs)
    }
}
