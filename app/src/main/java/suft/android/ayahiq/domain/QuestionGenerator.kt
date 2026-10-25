package suft.android.ayahiq.domain

import kotlin.math.abs
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
 *
 * In Hafiz mode ([mutashabihat] given) prompts are limited to ayahs that have a trap, and the wrong options are
 * mutashabihat: surahs/next ayahs that resemble the right answer.
 */
class QuestionGenerator(
    private val quran: Quran,
    private val level: Level,
    private val language: AppLanguage,
    private val random: Random = Random.Default,
    /** Hafiz mode: wrong options come from these similar ayahs first; null in the other modes. */
    private val mutashabihat: Mutashabihat? = null,
) {

    /** Hafiz: only the level's prompts that have a trap (every level has some); otherwise all of them. */
    internal val promptPool: List<IntRange> by lazy {
        val all = allPrompts()
        if (mutashabihat == null) all else all.filter(::hasTrap).ifEmpty { all }
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
        if (level.openings) return level.surahRange.map { quran.surahs[it].firstAyah.let { f -> f..f + 1 } }
        val first = quran.surahs[level.firstSurah].firstAyah
        val last = quran.surahs[level.lastSurah].lastAyah
        return (first..last)
            .filter { if (level.style == GameStyle.GuessSurah) it != 0 else !quran.isLastOfSurah(it) }
            .map { it..it }
    }

    private fun hasTrap(shown: IntRange): Boolean = when (level.style) {
        GameStyle.GuessSurah -> trapSurahs(shown, quran.surahsContaining(shown)).isNotEmpty()
        GameStyle.NextAyah -> trapAyahs(shown, wrongNextAyahFilter(shown)).isNotEmpty()
    }

    /** Hafiz: surahs holding an ayah that resembles one of [shown], most similar first, minus [exclude]. */
    private fun trapSurahs(shown: IntRange, exclude: Set<Int>): List<Int> {
        val similar = mutashabihat ?: return emptyList()
        return shown.flatMap(similar::of).sortedByDescending { it.strength }
            .map { quran.surahOf(it.ayah).index }.filter { it !in exclude }.distinct()
    }

    /**
     * Hafiz: tempting wrong next ayahs, most similar first — ayahs that resemble the answer, and ayahs that
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
        val traps = trapSurahs(shown, alsoCorrect).take(WRONG_OPTIONS)
        val need = WRONG_OPTIONS - traps.size

        fun pool(range: IntRange): List<Int> {
            val allowed = range.filter { it != surah.index && it !in alsoCorrect && it !in traps }
            if (allowed.size > SURAH_NEIGHBOURHOOD) {
                // The answer sits at a random position of the window, not always in its middle.
                val low = surah.index - random.nextInt(SURAH_NEIGHBOURHOOD + 1)
                val window = allowed.filter { it in low..low + SURAH_NEIGHBOURHOOD }
                if (window.size >= need) return window
            }
            return allowed.sortedBy { abs(it - surah.index) }.take(SURAH_NEIGHBOURHOOD)
        }

        val candidates = pool(level.surahRange).takeIf { it.size >= need }
            ?: pool(0 until Quran.SURAH_COUNT)
        val wrong = (traps + candidates.shuffled(random).take(need)).map { quran.surahs[it].displayName(language) }

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

    // ---------------------------------------------------------------- next ayah

    private fun nextAyahQuestion(shown: IntRange): Question {
        val surah = quran.surahOf(shown.first)
        val answer = shown.last + 1
        val acceptable = wrongNextAyahFilter(shown)
        val traps = trapAyahs(shown, acceptable).take(WRONG_OPTIONS)
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
        // The ۩ sign would mark the prostration ayah; the prompt and the share card keep it, the options do not.
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

    private fun shuffleWithCorrect(correct: String, wrong: List<String>): Pair<List<String>, Int> {
        val all = (wrong + correct).shuffled(random)
        return all to all.indexOf(correct)
    }

    private companion object {
        const val WRONG_OPTIONS = 3

        /**
         * Wrong surahs come from a window of this many surahs around the correct one, which sits at a random
         * position inside it (so it is not predictable from where it falls among the options).
         */
        const val SURAH_NEIGHBOURHOOD = 16
        const val AYAH_NEIGHBOURHOOD = 8   // original: window of 8 ayahs around the answer
        const val AYAH_SEARCH_RADIUS = 60
        const val RECENT_MEMORY = 20
    }
}
