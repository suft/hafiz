package suft.android.hafiz.testing

import java.io.File
import suft.android.hafiz.domain.MushafLayout
import suft.android.hafiz.domain.Mutashabihat
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.domain.QuranXmlParser

/** The bundled Quran, mutashabihat and mushaf layout, loaded from the assets directory like the app does. */
object TestData {
    // Gradle runs unit tests from the module directory.
    fun asset(name: String): File =
        listOf("src/main/assets/$name", "app/src/main/assets/$name").map(::File).first { it.exists() }

    val quran: Quran by lazy { asset("quran_simple.xml").inputStream().use(QuranXmlParser::parse) }

    val mutashabihat: Mutashabihat by lazy { asset("mutashabihat.txt").inputStream().use { Mutashabihat.parse(it, quran) } }

    val mushafLayout: MushafLayout by lazy { asset("mushaf_layout.txt").inputStream().use { MushafLayout.parse(it, quran) } }
}
