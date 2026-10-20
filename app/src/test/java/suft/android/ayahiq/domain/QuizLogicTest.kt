package suft.android.ayahiq.domain

import java.io.File
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizLogicTest {

    private val quran: Quran by lazy {
        // Gradle runs unit tests from the module directory.
        val file = listOf("src/main/assets/quran_simple.xml", "app/src/main/assets/quran_simple.xml")
            .map(::File).first { it.exists() }
        file.inputStream().use(QuranXmlParser::parse)
    }

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
        val surahLevels = Levels.forMode(GameMode.GuessSurah, quran)
        val ayahLevels = Levels.forMode(GameMode.NextAyah, quran)
        assertEquals(20, surahLevels.size)
        assertEquals(25, ayahLevels.size)
        assertEquals((surahLevels + ayahLevels).size, (surahLevels + ayahLevels).map { it.id }.toSet().size)
        assertEquals("سورة يس", ayahLevels[13].name(AppLanguage.Arabic))
        assertEquals("سورتا التكوير والإنفطار", ayahLevels[3].name(AppLanguage.Arabic))
        assertEquals("فواتح السور", ayahLevels[23].name(AppLanguage.Arabic))
        assertTrue(ayahLevels[23].openings)
    }

    @Test
    fun guessSurahQuestionsAreAlwaysUnambiguous() {
        for (level in Levels.forMode(GameMode.GuessSurah, quran)) {
            val gen = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(level.index))
            repeat(300) {
                val q = gen.next()
                assertEquals(4, q.options.toSet().size)
                assertEquals(quran.surahs[q.surahIndex].name, q.options[q.correctOption])
                val owners = quran.surahsContaining(q.shownAyahs)
                q.options.forEachIndexed { i, name ->
                    if (i != q.correctOption) {
                        val idx = quran.surahs.indexOfFirst { it.name == name }
                        assertFalse("$name also contains the prompt", idx in owners)
                    }
                }
                if (level.openings) {
                    assertEquals(quran.surahs[q.surahIndex].firstAyah, q.shownAyahs.first)
                    assertEquals(2, q.promptAyahs.size)
                } else {
                    assertTrue(q.surahIndex in level.surahRange)
                }
            }
        }
    }

    @Test
    fun nextAyahQuestionsAreAlwaysUnambiguous() {
        for (level in Levels.forMode(GameMode.NextAyah, quran)) {
            val gen = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(100 + level.index))
            repeat(300) {
                val q = gen.next()
                assertEquals(4, q.options.toSet().size)
                assertTrue(q.surahIndex in level.surahRange)
                val answer = q.shownAyahs.last + 1
                assertEquals(quran.surahOf(q.shownAyahs.first), quran.surahOf(answer))
                val correct = q.options[q.correctOption].removeSuffix(" …")
                assertTrue(quran.text(answer).startsWith(correct))

                // No wrong option may be an ayah that follows the same prompt text.
                val validFollowers = quran.occurrencesOf(quran.text(q.shownAyahs.last))
                    .filter { !quran.isLastOfSurah(it) }
                    .map { quran.text(it + 1) }
                q.options.forEachIndexed { i, label ->
                    if (i != q.correctOption) {
                        val prefix = label.removeSuffix(" …")
                        assertFalse(
                            "«$label» is also a valid next ayah",
                            validFollowers.any { it == prefix || (label.endsWith("…") && it.startsWith(prefix)) },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun ayahsAreNotRepeatedWithinARound() {
        val level = Levels.forMode(GameMode.GuessSurah, quran)[3] // جزء عمَّ — 564 ayahs
        val gen = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(7))
        val prompts = List(20) { gen.next().shownAyahs.first }
        assertEquals(20, prompts.toSet().size)
    }

    @Test
    fun englishLabels() {
        assertEquals(114, SurahNames.english.toSet().size)
        assertEquals("Al-Baqarah", quran.surahs[1].displayName(AppLanguage.English))
        assertEquals("An-Nas", quran.surahs[113].displayName(AppLanguage.English))
        assertEquals("الفاتحة", quran.surahs[0].displayName(AppLanguage.Arabic))

        val b = quran.surahs[1].firstAyah + 254
        assertEquals("Surah Al-Baqarah · Ayah 255", quran.reference(b..b, AppLanguage.English))
        assertEquals("سورة البقرة · الآية ٢٥٥", quran.reference(b..b, AppLanguage.Arabic))
        assertEquals("Surah Al-Fatihah · Ayahs 1–2", quran.reference(0..1, AppLanguage.English))

        val ayahLevels = Levels.forMode(GameMode.NextAyah, quran)
        val surahLevels = Levels.forMode(GameMode.GuessSurah, quran)
        assertEquals("Surah Ya-Sin", ayahLevels[13].name(AppLanguage.English))
        assertEquals("Surahs At-Takwir and Al-Infitar", ayahLevels[3].name(AppLanguage.English))
        assertEquals("Al-'Adiyat to An-Nas", surahLevels[0].name(AppLanguage.English))
        assertEquals("Second half of the Quran (Al-Kahf to An-Nas)", surahLevels[14].name(AppLanguage.English))
    }

    @Test
    fun englishQuestionsKeepQuranTextArabic() {
        val surahLevels = Levels.forMode(GameMode.GuessSurah, quran)
        val surahGen = QuestionGenerator(quran, surahLevels[0], AppLanguage.English, Random(1))
        repeat(50) {
            val q = surahGen.next()
            assertEquals(quran.surahs[q.surahIndex].displayName(AppLanguage.English), q.options[q.correctOption])
            assertEquals(q.shownAyahs.map(quran::text), q.promptAyahs)
        }

        val ayahLevels = Levels.forMode(GameMode.NextAyah, quran)
        val ayahGen = QuestionGenerator(quran, ayahLevels[0], AppLanguage.English, Random(2))
        repeat(50) {
            val q = ayahGen.next()
            assertEquals(quran.reference(q.shownAyahs, AppLanguage.English), q.reference)
            // The option is Arabic ayah text, never translated.
            assertTrue(quran.text(q.shownAyahs.last + 1).startsWith(q.options[q.correctOption].removeSuffix(" …")))
        }
    }

    @Test
    fun arabicHelpers() {
        assertEquals("٢٥٥", ArabicText.digits(255))
        assertEquals("﴿٧﴾", ArabicText.ayahMarker(7))
        assertEquals("أ ب …", ArabicText.firstWords("أ ب ج د", 2))
        val labels = ArabicText.distinctPrefixes(listOf("a b c d e", "a b c x y", "q"), 2)
        assertEquals(3, labels.toSet().size)
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
        for (mode in GameMode.entries) {
            for (level in Levels.forMode(mode, quran)) {
                assertEquals(level, Levels.byId(level.id, quran))
            }
        }
        val practice = Levels.surahPractice(1, quran)
        assertEquals("ayah:2-2:practice", practice.id)
        val resolved = Levels.byId(practice.id, quran)!!
        assertTrue(resolved.practice)
        assertEquals(1, resolved.firstSurah)
        assertEquals(null, Levels.byId("surah:999-999", quran))
        assertEquals("ayah:1-114:openings", Levels.forMode(GameMode.NextAyah, quran)[23].id)
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
        assertEquals(9, GameMode.NextAyah.masteryFor(10))
        assertEquals(2, GameMode.NextAyah.masteryFor(2))
        assertEquals(16, GameMode.GuessSurah.masteryFor(20))
        assertEquals(2, Levels.surahPractice(107, quran).questionsPerRound)
        assertEquals(10, Levels.surahPractice(1, quran).questionsPerRound)
    }
}
