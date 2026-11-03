package suft.android.hafiz.domain

import suft.android.hafiz.testing.TestData
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizLogicTest {

    private val mutashabihat: Mutashabihat get() = TestData.mutashabihat
    private val quran: Quran get() = TestData.quran

    @Test
    fun parsesWholeQuran() {
        assertEquals(114, quran.surahs.size)
        assertEquals(6236, quran.ayahCount)
        assertEquals("الفاتحة", quran.surahs[0].name)
        assertEquals(7, quran.surahs[0].ayahCount)
        assertEquals(286, quran.surahs[1].ayahCount)
        assertEquals("الناس", quran.surahs[113].name)
        assertEquals(6235, quran.surahs[113].lastAyah)
        assertEquals(255, quran.ayahNumberInSurah(quran.surahs[1].firstAyah + 254))
    }

    @Test
    fun detectsAyahsSharedBetweenSurahs() {
        // «الم» opens البقرة، آل عمران، العنكبوت، الروم، لقمان، السجدة
        val alm = quran.surahs[1].firstAyah
        val owners = quran.surahsContaining(alm..alm).map { it + 1 }.sorted()
        assertEquals(listOf(2, 3, 29, 30, 31, 32), owners)
        // …but the first two ayahs together are unique to البقرة
        assertEquals(setOf(1), quran.surahsContaining(alm..alm + 1))
    }

    @Test
    fun levelTablesMatchOriginal() {
        val surahLevels = Levels.forStyle(GameStyle.GuessSurah, quran)
        val ayahLevels = Levels.forStyle(GameStyle.NextAyah, quran)
        assertEquals(20, surahLevels.size)
        assertEquals(25, ayahLevels.size)
        assertEquals((surahLevels + ayahLevels).size, (surahLevels + ayahLevels).map { it.id }.toSet().size)
        assertEquals("سورة يس", ayahLevels[13].name(AppLanguage.Arabic))
        assertEquals("سورتا التكوير والإنفطار", ayahLevels[3].name(AppLanguage.Arabic))
        assertEquals("فواتح السور", ayahLevels[23].name(AppLanguage.Arabic))
        assertTrue(ayahLevels[23].openings)
        assertEquals("جزء قد سمع (المجادلة حتى التحريم)", surahLevels[6].name(AppLanguage.Arabic))
        assertEquals("Juz' Qad Sami' (Al-Mujadila to At-Tahrim)", surahLevels[6].name(AppLanguage.English))
    }

    @Test
    fun levelNamesAreDistinctInEveryLanguage() {
        for (style in GameStyle.entries) {
            val levels = Levels.forStyle(style, quran)
            for (language in AppLanguage.entries) {
                val names = levels.map { it.name(language) }
                assertTrue("$style $language: blank name", names.none { it.isBlank() })
                assertEquals("$style $language: duplicate names", names.size, names.toSet().size)
            }
        }
    }

    @Test
    fun mutashabihatParsesBothDirectionsStrongestFirst() {
        val m = Mutashabihat.parse("# header\n\n1:1 1:3 100\n1:1 27:30 50\n2:1 1:1 70\n".byteInputStream(), quran)
        assertEquals(
            listOf(SimilarAyah(2, 100), SimilarAyah(7, 70), SimilarAyah(quran.surahs[26].firstAyah + 29, 50)),
            m.of(0),
        )
        assertEquals(listOf(SimilarAyah(0, 100)), m.of(2))
        assertTrue(m.of(1).isEmpty())
        for (bad in listOf("2:287 1:1 5", "1:1 1:2")) {
            assertThrows(IllegalArgumentException::class.java) { Mutashabihat.parse(bad.byteInputStream(), quran) }
        }
    }

    @Test
    fun ayahsAreNotRepeatedWithinARound() {
        val level = Levels.forStyle(GameStyle.GuessSurah, quran)[3] // جزء عمَّ — 564 ayahs
        val gen = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(7))
        val prompts = List(20) { gen.next().shownAyahs.first }
        assertEquals(20, prompts.toSet().size)
    }

    @Test
    fun englishLabels() {
        assertEquals(114, SurahNames.transliterated.toSet().size)
        assertEquals("Al-Baqarah", quran.surahs[1].displayName(AppLanguage.English))
        assertEquals("An-Nas", quran.surahs[113].displayName(AppLanguage.English))
        assertEquals("الفاتحة", quran.surahs[0].displayName(AppLanguage.Arabic))

        val b = quran.surahs[1].firstAyah + 254
        assertEquals("Surah Al-Baqarah · Ayah 255", quran.reference(b..b, AppLanguage.English))
        assertEquals("سورة البقرة · الآية ٢٥٥", quran.reference(b..b, AppLanguage.Arabic))
        assertEquals("Surah Al-Fatihah · Ayahs 1–2", quran.reference(0..1, AppLanguage.English))
        assertEquals("Sure Al-Baqarah · Vers 255", quran.reference(b..b, AppLanguage.German))

        val ayahLevels = Levels.forStyle(GameStyle.NextAyah, quran)
        val surahLevels = Levels.forStyle(GameStyle.GuessSurah, quran)
        assertEquals("Surah Ya-Sin", ayahLevels[13].name(AppLanguage.English))
        assertEquals("Surahs At-Takwir and Al-Infitar", ayahLevels[3].name(AppLanguage.English))
        assertEquals("Suren At-Takwir und Al-Infitar", ayahLevels[3].name(AppLanguage.German))
        assertEquals("Al-'Adiyat to An-Nas", surahLevels[0].name(AppLanguage.English))
        assertEquals("Al-'Adiyat à An-Nas", surahLevels[0].name(AppLanguage.French))
        assertEquals("Second half of the Quran (Al-Kahf to An-Nas)", surahLevels[14].name(AppLanguage.English))
    }

    @Test
    fun englishQuestionsKeepQuranTextArabic() {
        val surahLevels = Levels.forStyle(GameStyle.GuessSurah, quran)
        val surahGen = QuestionGenerator(quran, surahLevels[0], AppLanguage.English, Random(1))
        repeat(50) {
            val q = surahGen.next()
            assertEquals(quran.surahs[q.surahIndex].displayName(AppLanguage.English), q.options[q.correctOption])
            assertEquals(q.shownAyahs.map(quran::text), q.promptAyahs)
        }

        val ayahLevels = Levels.forStyle(GameStyle.NextAyah, quran)
        val ayahGen = QuestionGenerator(quran, ayahLevels[0], AppLanguage.English, Random(2))
        repeat(50) {
            val q = ayahGen.next()
            assertEquals(quran.reference(q.shownAyahs, AppLanguage.English), q.reference)
            // The option is Arabic ayah text, never translated.
            assertEquals(ArabicText.withoutSajdahSign(quran.text(q.shownAyahs.last + 1)), q.options[q.correctOption])
        }
    }

    @Test
    fun arabicHelpers() {
        assertEquals("٢٥٥", ArabicText.digits(255))
        assertEquals("﴿٧﴾", ArabicText.ayahMarker(7))
    }

    @Test
    fun weakestSurahsOrdering() {
        val ranked = Stats.weakestFirst(
            mapOf(
                0 to Score(correct = 5, wrong = 0),
                1 to Score(correct = 1, wrong = 1),
                2 to Score(correct = 2, wrong = 2),
                3 to Score(correct = 0, wrong = 3),
            ),
        )
        assertEquals(listOf(3, 2, 1), ranked.map { it.surahIndex })
        assertEquals(50, ranked[1].score.percent)
    }

    @Test
    fun weakestRankingSmoothsSmallSamples() {
        val ranked = Stats.weakestFirst(mapOf(0 to Score(0, 1), 1 to Score(2, 8)))
        assertEquals(listOf(1, 0), ranked.map { it.surahIndex })
    }

    @Test
    fun surahTiers() {
        assertEquals(SurahTier.Weak, Stats.tier(Score(59, 41)))
        assertEquals(SurahTier.Fair, Stats.tier(Score(60, 40)))
        assertEquals(SurahTier.Fair, Stats.tier(Score(89, 11)))
        assertEquals(SurahTier.Strong, Stats.tier(Score(9, 1)))
        assertEquals(SurahTier.NotPractised, Stats.tier(null))
        assertEquals(SurahTier.NotPractised, Stats.tier(Score()))
    }

    @Test
    fun strongestSurahsOrdering() {
        val scores = mapOf(0 to Score(5, 0), 1 to Score(9, 1), 2 to Score(4, 0), 3 to Score(10, 0))
        assertEquals(listOf(3, 0, 1), Stats.strongestFirst(scores).map { it.surahIndex })
    }

    @Test
    fun levelIdsResolveAndStayStable() {
        for (style in GameStyle.entries) {
            for (level in Levels.forStyle(style, quran)) {
                assertEquals(level, Levels.byId(level.id, quran))
            }
        }
        val practice = Levels.surahPractice(1, quran)
        assertEquals("ayah:2-2:practice", practice.id)
        val resolved = Levels.byId(practice.id, quran)!!
        assertTrue(resolved.practice)
        assertEquals(1, resolved.firstSurah)
        assertEquals(null, Levels.byId("surah:999-999", quran))
        assertEquals("ayah:1-114:openings", Levels.forStyle(GameStyle.NextAyah, quran)[23].id)
    }

    @Test
    fun practiceRoundsStayInTheSurah() {
        val gen = QuestionGenerator(quran, Levels.surahPractice(35, quran), AppLanguage.Arabic)
        repeat(30) { assertEquals(35, gen.next().surahIndex) }
    }

    @Test
    fun practiceRoundsWorkForEverySurah() {
        for (surah in 0 until Quran.SURAH_COUNT) {
            val level = Levels.surahPractice(surah, quran)
            val n = level.questionsPerRound
            val gen = QuestionGenerator(quran, level, AppLanguage.English, Random(surah))
            val questions = List(3 * n) { gen.next() }
            val prompts = questions.map { it.shownAyahs.first }
            for (q in questions) {
                assertEquals(surah, q.surahIndex)
                assertEquals(4, q.options.toSet().size)
            }
            assertEquals("surah $surah", n, prompts.take(n).toSet().size)
            if (n > 1) {
                for (i in 1 until prompts.size) assertTrue("surah $surah", prompts[i] != prompts[i - 1])
            }
            // More prompts than one round: any n consecutive draws (rounds are not deck-aligned) are distinct.
            if (quran.surahs[surah].ayahCount - 1 > n) {
                for (i in 0..prompts.size - n) {
                    assertEquals("surah $surah window $i", n, prompts.subList(i, i + n).toSet().size)
                }
            }
        }
    }

    @Test
    fun masteryScalesWithRoundLength() {
        assertEquals(9, GameStyle.NextAyah.masteryFor(10))
        assertEquals(2, GameStyle.NextAyah.masteryFor(2))
        assertEquals(16, GameStyle.GuessSurah.masteryFor(20))
        assertEquals(2, Levels.surahPractice(107, quran).questionsPerRound)
        assertEquals(10, Levels.surahPractice(1, quran).questionsPerRound)
    }

    private fun ayah(surah: Int, number: Int) = quran.surahs[surah - 1].firstAyah + number - 1

    @Test
    fun themeIndexRelatesTheSameStory() {
        val vocab = ThemeIndex.vocabulary(quran)
        assertEquals("فرعون", ThemeIndex.normalise("وَفِرْعَوْنَ", vocab))
        assertEquals("فرعون", ThemeIndex.normalise("فِرْعَوْنَ", vocab)) // the bare form: the ف is part of the word
        assertEquals("فتنه", ThemeIndex.normalise("فِتْنَةٌ", vocab))
        assertEquals("كهف", ThemeIndex.normalise("الْكَهْفِ", vocab))
        assertEquals("رقيم", ThemeIndex.normalise("وَالرَّقِيمِ", vocab))
        assertEquals("لله", ThemeIndex.normalise("لِلَّهِ", vocab))
        assertEquals("", ThemeIndex.normalise("ۚ", vocab))

        // 20:24 («اذهب إلى فرعون إنه طغى»): An-Nazi'at 79:17 repeats it; Taha itself is scored without the prompt.
        val taha = ayah(20, 24)
        val rel = quran.themes.relatedness(taha..taha)
        assertEquals(78, rel.indices.maxBy { rel[it] })
        assertTrue(rel[19] < rel[78])

        // 7:103 (Musa sent to Pharaoh): the same story in Yunus, An-Naml and Al-Qasas.
        val a = ayah(7, 103)
        val story = quran.themes.relatedness(a..a)
        val top = story.indices.filter { it != 6 }.sortedByDescending { story[it] }.take(5)
        assertTrue("top 5 for 7:103: $top", top.containsAll(listOf(9, 26, 27)))
    }

    @Test
    fun gameOverRules() {
        assertTrue(GameMode.Regular.isOver(20, 0, 20))
        assertFalse(GameMode.Regular.isOver(19, 5, 20))
        assertTrue(GameMode.Survival.isOver(50, 1, 20))
        assertFalse(GameMode.Survival.isOver(50, 0, 20))
        assertFalse(GameMode.Mutashabihat.isOver(7, 2, 20))
        assertTrue(GameMode.Mutashabihat.isOver(7, 3, 20))
    }
}
