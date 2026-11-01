package suft.android.hafiz.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

/** One multiple-choice question. */
data class Question(
    val style: GameStyle,
    /** Global indices of the ayahs shown (and recited) in the prompt. */
    val shownAyahs: IntRange,
    /** The surah the shown ayahs belong to — the one stats are recorded against. */
    val surahIndex: Int,
    /** Prompt ayah texts, one entry per ayah. */
    val promptAyahs: List<String>,
    /** 1-based ayah numbers of [promptAyahs] inside the surah. */
    val promptAyahNumbers: List<Int>,
    /** e.g. "سورة البقرة · الآية ٢٥٥" / "Surah Al-Baqarah · Ayah 255" — only for [GameStyle.NextAyah] (where the surah is given). */
    val reference: String?,
    /** Answers; for [GameStyle.AyahPosition], the six [MushafCell] labels in entry order. */
    val options: List<String>,
    val correctOption: Int,
) {
    /** (surahNumber, ayahNumber) pairs, both 1-based — what the audio player needs. */
    val recitation: List<Pair<Int, Int>>
        get() = promptAyahNumbers.map { (surahIndex + 1) to it }
}

/**
 * Builds questions for a [Level]. The selection rules follow the original app, with fixes:
 *
 * * Wrong answers can never be a second correct answer (e.g. «الم» opens six surahs, and in
 *   سورة الرحمن «فبأي آلاء ربكما تكذبان» is followed by many different ayahs).
 * * Levels with very few surahs/ayahs fall back to a wider pool instead of looping forever.
 * * The same prompt is not dealt again within [RECENT_MEMORY] questions (within a round for practice rounds);
 *   prompts come from a shuffled deck, so every prompt is dealt once before any repeats.
 * * Wrong surahs are drawn from the whole level, weighted by length, the prompt's words, typical ayah length and place
 *   of revelation ([pickWrongSurahs]), so no option stands out by number, length, topic or Makki/Madani style.
 *
 * In Mutashabihat mode ([trapMode]) prompts are limited to ayahs that have a trap, and the wrong options are
 * mutashabihat: surahs/next ayahs that resemble the right answer. The other modes may be given [mutashabihat] too: they
 * deal every prompt and swap at most [LIGHT_TRAPS] wrong option for such a look-alike, when the prompt has one.
 */
class QuestionGenerator(
    private val quran: Quran,
    private val level: Level,
    private val language: AppLanguage,
    private val random: Random = Random.Default,
    /** Similar ayahs the wrong options may be taken from; null when no traps are wanted. */
    private val mutashabihat: Mutashabihat? = null,
    /** Mutashabihat mode: traps fill every wrong slot they can and prompts without a trap are never dealt. */
    private val trapMode: Boolean = mutashabihat != null,
    /** Where each ayah starts in the mushaf; required for [GameStyle.AyahPosition] levels. */
    private val layout: MushafLayout? = null,
) {

    /** How many wrong options may be traps. */
    private val trapSlots = if (trapMode) WRONG_OPTIONS else LIGHT_TRAPS

    /** Mutashabihat: only the level's prompts that have a trap; otherwise all of them. */
    internal val promptPool: List<IntRange> by lazy {
        val all = allPrompts()
        if (!trapMode) all else all.filter(::hasTrap).ifEmpty { all }
    }

    /** Pool indices still to deal from the current shuffle, and the ones dealt last. */
    private val deck = ArrayDeque<Int>()
    private val recent = ArrayDeque<Int>()
    private val window: Int by lazy {
        val span = if (level.practice) level.questionsPerRound else RECENT_MEMORY
        if (promptPool.size > span) span - 1 else 1
    }

    fun next(): Question = questionFor(nextPrompt())

    internal fun questionFor(shown: IntRange): Question = when (level.style) {
        GameStyle.GuessSurah -> guessSurahQuestion(shown)
        GameStyle.NextAyah -> nextAyahQuestion(shown)
        GameStyle.AyahPosition -> positionQuestion(shown)
    }

    // ---------------------------------------------------------------- prompt selection

    /**
     * Each prompt once per shuffled deck. Rounds are not aligned with decks (the next question is
     * prefetched), so a prompt is also skipped while it is among the last [window] dealt; with exactly one
     * round of prompts only the one just dealt is avoided, so every round is reshuffled.
     */
    private fun nextPrompt(): IntRange {
        if (deck.isEmpty()) deck.addAll(promptPool.indices.shuffled(random))
        val pick = deck.indexOfFirst { it !in recent }.coerceAtLeast(0)
        val prompt = deck.removeAt(pick)
        recent.addLast(prompt)
        while (recent.size > window) recent.removeFirst()
        return promptPool[prompt]
    }

    /** Every prompt this level can deal. 1:1 is not a guess-the-surah prompt: it is the bismillah that opens 112 more. */
    internal fun allPrompts(): List<IntRange> {
        val prompts = if (level.openings) {
            level.surahRange.map { quran.surahs[it].firstAyah.let { f -> f..f + 1 } }
        } else {
            val first = quran.surahs[level.firstSurah].firstAyah
            val last = quran.surahs[level.lastSurah].lastAyah
            (first..last)
                .filter {
                    when (level.style) {
                        GameStyle.GuessSurah -> it != 0
                        GameStyle.NextAyah -> !quran.isLastOfSurah(it)
                        GameStyle.AyahPosition -> true
                    }
                }
                .map { it..it }
        }
        return if (level.style == GameStyle.AyahPosition) prompts.filter(::hasOnePosition) else prompts
    }

    /**
     * Where's the Ayah? asks only about prompts with exactly one answer. An identical copy starting in another cell
     * (e.g. 2:47 / 2:122) would make two answers correct. The framed pages 1–2 have no cell.
     */
    private fun hasOnePosition(shown: IntRange): Boolean {
        val layout = checkNotNull(layout) { "Where's the Ayah? needs the mushaf layout" }
        val cell = layout.cell(shown.first) ?: return false
        if (shown.first != shown.last) return quran.surahsContaining(shown).size == 1
        return quran.occurrencesOf(shown.first).all { layout.cell(it) == cell }
    }

    private fun hasTrap(shown: IntRange): Boolean = when (level.style) {
        GameStyle.GuessSurah -> trapSurahs(shown, quran.surahsContaining(shown)).isNotEmpty()
        GameStyle.NextAyah -> trapAyahs(shown, wrongNextAyahFilter(shown)).isNotEmpty()
        GameStyle.AyahPosition -> false
    }

    /** Mutashabihat: surahs holding an ayah that resembles one of [shown], most similar first, minus [exclude]. */
    private fun trapSurahs(shown: IntRange, exclude: Set<Int>): List<Int> {
        val similar = mutashabihat ?: return emptyList()
        return shown.flatMap(similar::of).sortedByDescending { it.strength }
            .map { quran.surahOf(it.ayah).index }.filter { it !in exclude }.distinct()
    }

    /**
     * Mutashabihat: tempting wrong next ayahs, most similar first — ayahs that resemble the answer, and ayahs that
     * follow an ayah resembling the prompt (where a reciter slips into the other passage).
     */
    private fun trapAyahs(shown: IntRange, acceptable: (Int) -> Boolean): List<Int> {
        val similar = mutashabihat ?: return emptyList()
        val lookAlikes = similar.of(shown.last + 1)
        val slips = similar.of(shown.last).filter { !quran.isLastOfSurah(it.ayah) }.map { it.copy(ayah = it.ayah + 1) }
        return (lookAlikes + slips).sortedByDescending { it.strength }.map { it.ayah }
            .filter(acceptable).distinctBy(quran::key)
    }

    /**
     * Keys of every correct next ayah for [shown]: any ayah that follows an identical copy of the prompt
     * anywhere in the Quran is correct too (e.g. the refrains of سورة الرحمن / القمر / المرسلات).
     */
    private fun validAnswerKeys(shown: IntRange): Set<String> =
        quran.occurrencesOf(shown.last)
            .filter { !quran.isLastOfSurah(it) }
            .mapTo(mutableSetOf()) { quran.key(it + 1) }
            .apply { add(quran.key(shown.last + 1)) }

    /** A wrong next-ayah option may not lie between the prompt and the answer, nor repeat the text of any valid answer. */
    private fun wrongNextAyahFilter(shown: IntRange): (Int) -> Boolean {
        val forbidden = validAnswerKeys(shown)
        return { g -> g !in shown.first..shown.last + 1 && quran.key(g) !in forbidden }
    }

    // ---------------------------------------------------------------- guess the surah

    private fun guessSurahQuestion(shown: IntRange): Question {
        val surah = quran.surahOf(shown.first)
        val alsoCorrect = quran.surahsContaining(shown)
        val traps = trapSurahs(shown, alsoCorrect).filter { trapMode || it in level.surahRange }.take(trapSlots)
        val need = WRONG_OPTIONS - traps.size

        fun allowed(range: IntRange) = range.filter { it != surah.index && it !in alsoCorrect && it !in traps }
        val candidates = allowed(level.surahRange).takeIf { it.size >= need } ?: allowed(0 until Quran.SURAH_COUNT)
        val wrong = (traps + pickWrongSurahs(shown, surah.index, candidates, need)).map { quran.surahs[it].displayName(language) }

        val (options, correct) = shuffleWithCorrect(surah.displayName(language), wrong)
        return Question(
            style = GameStyle.GuessSurah,
            shownAyahs = shown,
            surahIndex = surah.index,
            promptAyahs = shown.map(quran::text),
            promptAyahNumbers = shown.map(quran::ayahNumberInSurah),
            reference = null,
            options = options,
            correctOption = correct,
        )
    }

    /** Prompts of each surah this generator deals (the Mutashabihat pool's trap filter is reflected). */
    private val promptsPerSurah: IntArray by lazy {
        IntArray(Quran.SURAH_COUNT).also { n -> promptPool.forEach { n[quran.surahOf(it.first).index]++ } }
    }

    /**
     * ln of the typical ayah length of each surah: the geometric mean of its ayahs' word counts (pause marks and the
     * sajdah sign are not words). Ayah lengths are skewed, so this tracks the median and ignores a few very long ayahs.
     */
    private val logAyahWords: DoubleArray by lazy {
        DoubleArray(Quran.SURAH_COUNT) { s ->
            val surah = quran.surahs[s]
            surah.ayahRange.sumOf { ln(ArabicText.equalityKey(quran.text(it)).split(' ').size.toDouble()) } / surah.ayahCount
        }
    }

    /**
     * [count] of [candidates], drawn without replacement with weight
     * `prompts^PROMPT_WEIGHT · e^(THEME_WEIGHT · relatedness) · e^(-AYAH_LENGTH_WEIGHT · |ln typical ayah length / answer's|)
     * · SAME_REVELATION_WEIGHT if revealed where the [answer] was`:
     * the answer is dealt per ayah and is usually the surah sharing most of the prompt's words, and its ayah length and
     * place of revelation set it apart in style, so wrong options follow the same tendencies and neither length, number,
     * topic, rhythm nor Makki/Madani singles the answer out. Mutashabihat mode weights by length only: its traps are
     * near-copies of the prompt, so topical or stylistic fillers would leave the answer as the least related option.
     */
    private fun pickWrongSurahs(shown: IntRange, answer: Int, candidates: List<Int>, count: Int): List<Int> {
        if (count <= 0) return emptyList()
        val theme = if (trapMode) 0.0 else THEME_WEIGHT
        val rel = if (theme == 0.0) null else quran.themes.relatedness(shown)
        val top = rel?.let { r -> candidates.maxOf { r[it] } } ?: 0.0
        val answerPlace = RevelationPlaces.of(answer)
        val pool = candidates.toMutableList()
        val weights = pool.mapTo(ArrayList()) { c ->
            var w = maxOf(promptsPerSurah[c], 1).toDouble().pow(PROMPT_WEIGHT)
            if (rel != null) {
                w *= exp(theme * (rel[c] - top))
                w *= exp(-AYAH_LENGTH_WEIGHT * abs(logAyahWords[c] - logAyahWords[answer]))
                if (RevelationPlaces.of(c) == answerPlace) w *= SAME_REVELATION_WEIGHT
            }
            w
        }
        return List(count) {
            var r = random.nextDouble() * weights.sum()
            var pick = weights.lastIndex
            for (i in weights.indices) {
                r -= weights[i]
                if (r < 0) {
                    pick = i
                    break
                }
            }
            weights.removeAt(pick)
            pool.removeAt(pick)
        }
    }

    // ---------------------------------------------------------------- next ayah

    private fun nextAyahQuestion(shown: IntRange): Question {
        val surah = quran.surahOf(shown.first)
        val answer = shown.last + 1
        val acceptable = wrongNextAyahFilter(shown)
        // Outside Mutashabihat mode a look-alike must come from the level, or it would stand out as the odd one.
        val trapFilter: (Int) -> Boolean =
            if (trapMode) acceptable else { g -> acceptable(g) && quran.surahOf(g).index in level.surahRange }
        val traps = trapAyahs(shown, trapFilter).take(trapSlots)
        val trapKeys = traps.mapTo(HashSet(), quran::key)

        // Nearby ayahs of the same surah first (as in the original), then neighbouring surahs.
        val ordered = (maxOf(0, answer - AYAH_SEARCH_RADIUS)..minOf(quran.ayahCount - 1, answer + AYAH_SEARCH_RADIUS))
            .asSequence()
            .sortedWith(compareBy<Int>({ quran.surahOf(it) != surah }, { abs(it - answer) }))
            .filter(acceptable)
            .filter { quran.key(it) !in trapKeys }
            .distinctBy(quran::key)
            .take(AYAH_NEIGHBOURHOOD)
            .toList()

        val wrongIdx = traps + ordered.shuffled(random).take(WRONG_OPTIONS - traps.size)
        // The ۩ sign would mark the prostration ayah; the prompt keeps it, the options do not.
        val texts = (listOf(answer) + wrongIdx).map { ArabicText.withoutSajdahSign(quran.text(it)) }

        val (options, correct) = shuffleWithCorrect(texts.first(), texts.drop(1))
        return Question(
            style = GameStyle.NextAyah,
            shownAyahs = shown,
            surahIndex = surah.index,
            promptAyahs = shown.map(quran::text),
            promptAyahNumbers = shown.map(quran::ayahNumberInSurah),
            reference = quran.reference(shown, language),
            options = options,
            correctOption = correct,
        )
    }

    // ---------------------------------------------------------------- where's the ayah

    private fun positionQuestion(shown: IntRange): Question {
        val cell = checkNotNull(checkNotNull(layout).cell(shown.first))
        return Question(
            style = GameStyle.AyahPosition,
            shownAyahs = shown,
            surahIndex = quran.surahOf(shown.first).index,
            promptAyahs = shown.map(quran::text),
            promptAyahNumbers = shown.map(quran::ayahNumberInSurah),
            reference = null,
            // Not shuffled: the grid is positional.
            options = MushafCell.entries.map { it.label(language) },
            correctOption = cell.ordinal,
        )
    }

    private fun shuffleWithCorrect(correct: String, wrong: List<String>): Pair<List<String>, Int> {
        val all = (wrong + correct).shuffled(random)
        return all to all.indexOf(correct)
    }

    private companion object {
        const val WRONG_OPTIONS = 3

        /** Trap options per question outside Mutashabihat mode. */
        const val LIGHT_TRAPS = 1

        /** Exponent on a surah's prompt count; below 1 because relatedness already favours long surahs. */
        const val PROMPT_WEIGHT = 0.75

        /** How strongly wrong options follow the prompt's words (relatedness is ~0..1.5). */
        const val THEME_WEIGHT = 6.0

        /** Per unit of |ln(typical ayah words / answer's)|: a surah with ayahs twice as long as the answer's weighs ~0.35x. */
        const val AYAH_LENGTH_WEIGHT = 1.5

        /** Factor for a wrong surah revealed in the same place (Makkah/Madinah) as the answer. */
        const val SAME_REVELATION_WEIGHT = 3.0

        const val AYAH_NEIGHBOURHOOD = 8   // original: window of 8 ayahs around the answer
        const val AYAH_SEARCH_RADIUS = 60
        const val RECENT_MEMORY = 20
    }
}
