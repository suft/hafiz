package suft.android.hafiz.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import suft.android.hafiz.testing.TestData

/**
 * Walks every prompt a level can deal (not a random sample), so a rare ambiguous or untrapped question
 * cannot slip through. Text comparison uses a test-local normalisation that ignores pause marks.
 */
class QuestionCorrectnessTest {

    private val quran = TestData.quran
    private val mutashabihat = TestData.mutashabihat

    private val pauseMarks = Regex("[\u06D6-\u06ED]")
    private val spaces = Regex("\\s+")
    private fun norm(s: String) = s.replace(pauseMarks, " ").trim().split(spaces).joinToString(" ")
    private val normalised: List<String> by lazy { List(quran.ayahCount) { norm(quran.text(it)) } }

    private fun generator(level: Level, traps: Boolean, seed: Int = level.index) =
        QuestionGenerator(quran, level, AppLanguage.Arabic, Random(seed), if (traps) mutashabihat else null)

    private fun ayah(surah: Int, number: Int) = quran.surahs[surah - 1].firstAyah + number - 1

    @Test
    fun waqfVariantsAreTheSameAyah() {
        assertTrue(2 in quran.surahsContaining(ayah(2, 162).let { it..it }))
        assertTrue(40 in quran.surahsContaining(ayah(11, 110).let { it..it }))
        assertTrue(29 in quran.surahsContaining(ayah(16, 55).let { it..it }))
    }

    @Test
    fun everyGuessSurahPromptIsUnambiguous() {
        for (level in Levels.forStyle(GameStyle.GuessSurah, quran)) {
            for (traps in listOf(false, true)) {
                val gen = generator(level, traps)
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val where = "${level.arabicName} traps=$traps ${quran.reference(p, AppLanguage.English)}"
                    assertEquals(where, 4, q.options.toSet().size)
                    val surah = quran.surahOf(p.first)
                    assertEquals(where, surah.name, q.options[q.correctOption])
                    val prompt = p.map { normalised[it] }
                    q.options.forEachIndexed { i, name ->
                        if (i == q.correctOption) return@forEachIndexed
                        val other = quran.surahs.first { it.name == name }
                        val holdsPrompt = (other.firstAyah..other.lastAyah - p.count() + 1).any { start ->
                            prompt.indices.all { k -> normalised[start + k] == prompt[k] }
                        }
                        assertFalse("$where: «$name» also contains the prompt", holdsPrompt)
                    }
                }
            }
        }
    }

    @Test
    fun everyNextAyahPromptIsUnambiguous() {
        // normalised ayah -> normalised texts of every ayah that follows an identical ayah.
        val followers = HashMap<String, MutableList<String>>()
        for (g in 0 until quran.ayahCount) {
            if (!quran.isLastOfSurah(g)) followers.getOrPut(normalised[g]) { ArrayList() } += normalised[g + 1]
        }
        for (level in Levels.forStyle(GameStyle.NextAyah, quran)) {
            for (traps in listOf(false, true)) {
                val gen = generator(level, traps)
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val where = "${level.arabicName} traps=$traps ${quran.reference(p, AppLanguage.English)}"
                    val answer = p.last + 1
                    assertEquals(where, quran.surahOf(p.first), quran.surahOf(answer))
                    val labels = q.options.map(::norm)
                    assertEquals("$where: options not distinct", 4, labels.toSet().size)
                    assertEquals(where, normalised[answer], labels[q.correctOption])
                    val valid = followers.getValue(normalised[p.last])
                    labels.forEachIndexed { i, label ->
                        if (i == q.correctOption) return@forEachIndexed
                        assertFalse("$where: «${q.options[i]}» is also a valid next ayah", label in valid)
                    }
                }
            }
        }
    }

    @Test
    fun mutashabihatPromptsAllHaveTraps() {
        for (style in GameStyle.entries) {
            for (level in Levels.forStyle(style, quran)) {
                val gen = generator(level, traps = true)
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val wrong = q.options.filterIndexed { i, _ -> i != q.correctOption }
                    val hasTrap = when (style) {
                        GameStyle.GuessSurah -> {
                            val trapNames = p.flatMap { mutashabihat.of(it) }.map { quran.surahOf(it.ayah).name }
                            wrong.any { it in trapNames }
                        }
                        GameStyle.NextAyah -> {
                            val candidates = mutashabihat.of(p.last + 1).map { it.ayah } +
                                mutashabihat.of(p.last).filter { !quran.isLastOfSurah(it.ayah) }.map { it.ayah + 1 }
                            wrong.any { label -> candidates.any { normalised[it] == norm(label) } }
                        }
                    }
                    assertTrue(
                        "${style.name} «${level.arabicName}» ${quran.reference(p, AppLanguage.English)}: no trap option",
                        hasTrap,
                    )
                }
            }
        }
    }

    @Test
    fun bismillahIsNeverAGuessSurahPrompt() {
        for (level in Levels.forStyle(GameStyle.GuessSurah, quran).filter { !it.openings }) {
            for (traps in listOf(false, true)) {
                assertFalse("${level.arabicName} traps=$traps deals 1:1", (0..0) in generator(level, traps).promptPool)
            }
        }
    }

    @Test
    fun sajdahSignIsNotAnAnswerCue() {
        val level = Levels.forStyle(GameStyle.NextAyah, quran)[24]
        val gen = generator(level, traps = false)
        val sajdah = (0 until quran.ayahCount).filter { '\u06E9' in quran.text(it) && quran.ayahNumberInSurah(it) > 1 }
        assertTrue(sajdah.isNotEmpty())
        for (s in sajdah) {
            val q = gen.questionFor((s - 1)..(s - 1))
            for (option in q.options) {
                assertFalse("${quran.reference(s..s, AppLanguage.English)}: «$option» carries ۩", '\u06E9' in option)
            }
        }
    }

    @Test
    fun promptsDoNotRepeatWithinTwentyQuestions() {
        val level = Levels.forStyle(GameStyle.NextAyah, quran)[0]
        for (seed in 0 until 50) {
            val gen = generator(level, traps = true, seed = seed)
            val prompts = List(20) { gen.next().shownAyahs }
            assertEquals("seed $seed repeats a prompt", 20, prompts.toSet().size)
        }
    }

    @Test
    fun guessSurahAnswerIsNotTheCentreOfTheOptions() {
        val level = Levels.forStyle(GameStyle.GuessSurah, quran)[18]
        val gen = generator(level, traps = false, seed = 3)
        var credit = 0.0
        val rounds = 4000
        repeat(rounds) {
            val q = gen.next()
            val surahs = q.options.map { name -> quran.surahs.first { it.name == name }.index }
            val answer = surahs[q.correctOption]
            val mid = (surahs.min() + surahs.max()) / 2.0
            val nearest = surahs.minOf { kotlin.math.abs(it - mid) }
            val centre = surahs.filter { kotlin.math.abs(it - mid) == nearest }
            if (answer in centre) credit += 1.0 / centre.size
        }
        val rate = credit / rounds
        assertTrue("answer is the middle option in %.3f of questions".format(rate), rate < 0.35)
    }

    @Test
    fun everySurahHasMutashabihat() {
        val without = quran.surahs.filter { s -> s.ayahRange.none { mutashabihat.of(it).isNotEmpty() } }
        assertTrue("surahs without mutashabihat: ${without.map { it.number }}", without.isEmpty())
    }
}
