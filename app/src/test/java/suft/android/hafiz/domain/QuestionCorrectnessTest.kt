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

    /** Regular/Survival generator: look-alike data available, but only one wrong option may use it. */
    private fun lightGenerator(level: Level, seed: Int = level.index) =
        QuestionGenerator(quran, level, AppLanguage.Arabic, Random(seed), mutashabihat, trapMode = false)

    private fun positionGenerator(level: Level, edition: MushafEdition = MushafEdition.V1) =
        QuestionGenerator(quran, level, AppLanguage.Arabic, Random(level.index), layout = TestData.mushafLayout(edition))

    /** The same level dealt without traps (Regular/Survival default), with light traps, and in Mutashabihat mode. */
    private fun everyTrapSetting(level: Level) = listOf(
        "none" to generator(level, traps = false),
        "light" to lightGenerator(level),
        "full" to generator(level, traps = true),
    )

    /** normalised ayah -> normalised texts of every ayah that follows an identical ayah. */
    private val followers: Map<String, List<String>> by lazy {
        val map = HashMap<String, MutableList<String>>()
        for (g in 0 until quran.ayahCount) {
            if (!quran.isLastOfSurah(g)) map.getOrPut(normalised[g]) { ArrayList() } += normalised[g + 1]
        }
        map
    }

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
            for ((traps, gen) in everyTrapSetting(level)) {
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val where = "${level.name(AppLanguage.Arabic)} traps=$traps ${quran.reference(p, AppLanguage.English)}"
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
        for (level in Levels.forStyle(GameStyle.NextAyah, quran)) {
            for ((traps, gen) in everyTrapSetting(level)) {
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val where = "${level.name(AppLanguage.Arabic)} traps=$traps ${quran.reference(p, AppLanguage.English)}"
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
        for (style in GameStyle.entries.filter(GameMode.Mutashabihat::offers)) {
            for (level in Levels.forMode(GameMode.Mutashabihat, style, quran)) {
                val gen = generator(level, traps = true)
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val wrong = q.options.filterIndexed { i, _ -> i != q.correctOption }
                    val hasTrap = when (style) {
                        GameStyle.GuessSurah -> {
                            val trapNames = p.flatMap { mutashabihat.of(it) }.map { quran.surahOf(it.ayah).name }
                            wrong.any { it in trapNames }
                        }
                        GameStyle.AyahPosition -> error("not offered")
                        GameStyle.NextAyah -> {
                            val candidates = mutashabihat.of(p.last + 1).map { it.ayah } +
                                mutashabihat.of(p.last).filter { !quran.isLastOfSurah(it.ayah) }.map { it.ayah + 1 }
                            wrong.any { label -> candidates.any { normalised[it] == norm(label) } }
                        }
                    }
                    assertTrue(
                        "${style.name} «${level.name(AppLanguage.Arabic)}» ${quran.reference(p, AppLanguage.English)}: no trap option",
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
                assertFalse("${level.name(AppLanguage.Arabic)} traps=$traps deals 1:1", (0..0) in generator(level, traps).promptPool)
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

    /** 1/ties if [answer] is among the best of [surahs] by [key] (highest or lowest), else 0. */
    private fun credit(surahs: List<Int>, answer: Int, key: (Int) -> Double, highest: Boolean): Double {
        val best = if (highest) surahs.maxOf(key) else surahs.minOf(key)
        val winners = surahs.filter { key(it) == best }
        return if (answer in winners) 1.0 / winners.size else 0.0
    }

    /** ln of the geometric mean of each surah's ayah word counts. */
    private val logWords: List<Double> by lazy {
        quran.surahs.map { s -> s.ayahRange.sumOf { kotlin.math.ln(normalised[it].split(' ').size.toDouble()) } / s.ayahCount }
    }

    @Test
    fun guessSurahOptionsGiveNoShortcut() {
        for ((index, light) in listOf(3, 14, 18).flatMap { listOf(it to false, it to true) }) {
            val level = Levels.forStyle(GameStyle.GuessSurah, quran)[index]
            val gen = if (light) lightGenerator(level, seed = 11) else generator(level, traps = false, seed = 11)
            val count = IntArray(Quran.SURAH_COUNT)
            gen.promptPool.forEach { count[quran.surahOf(it.first).index]++ }
            val rounds = 3000
            val totals = mutableMapOf(
                "longest" to 0.0, "most related" to 0.0, "least related" to 0.0, "isolated" to 0.0,
                "odd ayah length" to 0.0, "typical ayah length" to 0.0, "odd revelation place" to 0.0,
            )
            repeat(rounds) {
                val q = gen.next()
                val surahs = q.options.map { name -> quran.surahs.first { it.name == name }.index }
                val answer = surahs[q.correctOption]
                val rel = quran.themes.relatedness(q.shownAyahs)
                fun add(name: String, key: (Int) -> Double, highest: Boolean) {
                    totals[name] = totals.getValue(name) + credit(surahs, answer, key, highest)
                }
                add("longest", { count[it].toDouble() }, true)
                add("most related", { rel[it] }, true)
                add("least related", { rel[it] }, false)
                add("isolated", { s -> surahs.filter { it != s }.minOf { kotlin.math.abs(it - s) }.toDouble() }, true)
                fun lengthDeviation(s: Int): Double {
                    val others = surahs.filter { it != s }
                    return kotlin.math.abs(logWords[s] - others.map { logWords[it] }.average())
                }
                add("odd ayah length", ::lengthDeviation, true)
                add("typical ayah length", ::lengthDeviation, false)
                add("odd revelation place", { s ->
                    if (surahs.filter { it != s }.none { RevelationPlaces.of(it) == RevelationPlaces.of(s) }) 1.0 else 0.0
                }, true)
            }
            for ((name, total) in totals) {
                val rate = total / rounds
                assertTrue("${level.name(AppLanguage.Arabic)} light=$light: $name picks the answer in %.3f".format(rate), rate < 0.42)
            }
        }
    }

    @Test
    fun lightTrapsAddOneLookAlikeInsideTheLevel() {
        for (style in GameStyle.entries.filter { it.hasLookAlikes }) {
            for (level in Levels.forStyle(style, quran).filter { !it.openings }) {
                val gen = lightGenerator(level)
                assertEquals("${level.name(AppLanguage.Arabic)}: light traps must not shrink the pool", generator(level, false).promptPool, gen.promptPool)
                val range = level.surahRange
                for (p in gen.promptPool.shuffled(Random(level.index)).take(300)) {
                    val q = gen.questionFor(p)
                    val wrong = q.options.filterIndexed { i, _ -> i != q.correctOption }
                    val where = "$style «${level.name(AppLanguage.Arabic)}» ${quran.reference(p, AppLanguage.English)}"
                    when (style) {
                        GameStyle.GuessSurah -> {
                            val optionSurahs = q.options.map { name -> quran.surahs.first { it.name == name }.index }
                            assertTrue("$where: option outside the level", optionSurahs.all { it in range })
                            val alsoCorrect = quran.surahsContaining(p)
                            val traps = p.flatMap { mutashabihat.of(it) }.map { quran.surahOf(it.ayah).index }
                                .filter { it !in alsoCorrect && it in range }
                            if (traps.isNotEmpty()) {
                                assertTrue("$where: no look-alike surah offered", optionSurahs.any { it in traps })
                            }
                        }
                        GameStyle.AyahPosition -> error("no look-alikes")
                        GameStyle.NextAyah -> {
                            val valid = followers.getValue(normalised[p.last])
                            val traps = (mutashabihat.of(p.last + 1).map { it.ayah } +
                                mutashabihat.of(p.last).filter { !quran.isLastOfSurah(it.ayah) }.map { it.ayah + 1 })
                                .filter { it !in p.first..p.last + 1 && quran.surahOf(it).index in range && normalised[it] !in valid }
                            if (traps.isNotEmpty()) {
                                assertTrue(
                                    "$where: no look-alike ayah offered",
                                    wrong.any { label -> traps.any { normalised[it] == norm(label) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun everySurahHasMutashabihat() {
        val without = quran.surahs.filter { s -> s.ayahRange.none { mutashabihat.of(it).isNotEmpty() } }
        assertTrue("surahs without mutashabihat: ${without.map { it.number }}", without.isEmpty())
    }

    @Test
    fun everyPositionPromptIsUnambiguous() {
        val labels = MushafCell.entries.map { it.label(AppLanguage.Arabic) }
        for (edition in MushafEdition.entries) {
            val layout = TestData.mushafLayout(edition)
            for (level in Levels.forStyle(GameStyle.AyahPosition, quran)) {
                val gen = positionGenerator(level, edition)
                for (p in gen.promptPool) {
                    val q = gen.questionFor(p)
                    val where = "$edition ${level.name(AppLanguage.Arabic)} ${quran.reference(p, AppLanguage.English)}"
                    assertEquals(where, labels, q.options)
                    assertEquals(where, layout.cell(p.first)!!.ordinal, q.correctOption)
                    assertTrue("$where: framed page", layout.page(p.first) > 2)
                    if (p.first == p.last) {
                        val copies = (0 until quran.ayahCount).filter { normalised[it] == normalised[p.first] }
                        assertTrue("$where: a copy starts in another cell", copies.all { layout.cell(it) == layout.cell(p.first) })
                    }
                }
            }
        }
    }

    @Test
    fun positionPoolDropsCopiesInOtherCells() {
        val whole = positionGenerator(Levels.wholeQuran(GameStyle.AyahPosition, quran)).promptPool
        fun single(surah: Int, number: Int) = ayah(surah, number).let { it..it }
        assertFalse("2:47 starts in another cell than its copy 2:122", single(2, 47) in whole)
        assertFalse("2:122 starts in another cell than its copy 2:47", single(2, 122) in whole)
        assertFalse("1:1 is on the framed page 1", single(1, 1) in whole)
        assertTrue("59:1 and 61:1 are identical but start in the same cell", single(59, 1) in whole && single(61, 1) in whole)
        assertFalse("3:1 repeats 2:1, which is on the framed page 2", single(3, 1) in whole)
        val openings = positionGenerator(Levels.forStyle(GameStyle.AyahPosition, quran).single { it.openings }).promptPool
        assertTrue(ayah(3, 1)..ayah(3, 2) in openings)
    }
}
