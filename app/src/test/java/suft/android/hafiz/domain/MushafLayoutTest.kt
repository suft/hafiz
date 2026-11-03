package suft.android.hafiz.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import suft.android.hafiz.testing.TestData

class MushafLayoutTest {

    private val quran = TestData.quran
    private val layout = TestData.mushafLayout()

    private fun ayah(surah: Int, number: Int) = quran.surahs[surah - 1].firstAyah + number - 1

    private val bundledLines: List<String> by lazy { TestData.asset(MushafEdition.V1.asset).readLines() }

    private fun parse(lines: List<String>) = MushafLayout.parse(lines.joinToString("\n").byteInputStream(), quran)

    @Test
    fun cellsFollowPageSideAndThird() {
        // Third boundaries: line 5 is the last of the top third, line 6 the first of the middle, line 11 of the bottom.
        assertEquals(MushafCell.RightTop, layout.cell(ayah(2, 9)))
        assertEquals(MushafCell.RightMiddle, layout.cell(ayah(2, 10)))
        assertEquals(MushafCell.RightBottom, layout.cell(ayah(2, 14)))
        assertEquals(MushafCell.LeftMiddle, layout.cell(ayah(2, 82)))
        assertEquals(12, layout.page(ayah(2, 82)))
        assertEquals(10, layout.line(ayah(2, 82)))
        // Ayat al-Kursi.
        assertEquals(42, layout.page(ayah(2, 255)))
        assertEquals(8, layout.line(ayah(2, 255)))
        assertEquals(MushafCell.LeftMiddle, layout.cell(ayah(2, 255)))
        assertEquals(293, layout.page(ayah(18, 1)))
        assertEquals(MushafCell.RightBottom, layout.cell(ayah(18, 1)))
        assertEquals(604, layout.page(ayah(114, 1)))
        assertEquals(MushafCell.LeftBottom, layout.cell(ayah(114, 1)))
    }

    @Test
    fun framedPagesHaveNoCell() {
        assertNull(layout.cell(ayah(1, 1)))
        assertNull(layout.cell(ayah(2, 5)))
        assertEquals(MushafCell.RightTop, layout.cell(ayah(2, 6)))
    }

    @Test
    fun pagesNeverDecrease() {
        for (edition in MushafEdition.entries) {
            val layout = TestData.mushafLayout(edition)
            for (a in 1 until quran.ayahCount) {
                assertTrue("$edition: ayah $a starts before ayah ${a - 1}", layout.page(a) >= layout.page(a - 1))
            }
        }
    }

    @Test
    fun editionsDifferWhereThePrintsDo() {
        // 2:35 is on page 6 (left) in both prints: line 11 in v1 (bottom third), line 10 in v2 (middle third).
        assertEquals(MushafCell.LeftBottom, TestData.mushafLayout(MushafEdition.V1).cell(ayah(2, 35)))
        assertEquals(MushafCell.LeftMiddle, TestData.mushafLayout(MushafEdition.V2).cell(ayah(2, 35)))
    }

    @Test
    fun parseRejectsAMissingAyah() {
        val data = bundledLines.indices.filter { !bundledLines[it].startsWith('#') }
        assertThrows(IllegalArgumentException::class.java) { parse(bundledLines.filterIndexed { i, _ -> i != data[100] }) }
    }

    @Test
    fun parseRejectsAyahsOutOfOrder() {
        val data = bundledLines.indices.filter { !bundledLines[it].startsWith('#') }
        val swapped = bundledLines.toMutableList().apply { this[data[100]] = this[data[101]].also { this[data[101]] = this[data[100]] } }
        assertThrows(IllegalArgumentException::class.java) { parse(swapped) }
    }
}
