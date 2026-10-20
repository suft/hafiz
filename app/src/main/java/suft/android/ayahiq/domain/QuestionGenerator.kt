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
 * * The same prompt is not repeated within a round; practice rounds deal prompts from a shuffled deck, so a
 *   round of `questionsPerRound` never repeats one.
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
    private val recent = ArrayDeque<Int>()
    private val practiceDeck = ArrayDeque<Int>()
    private val practiceRecent = ArrayDeque<Int>()
    private val practicePrompts: List<Int> by lazy {
        level.surahRange.flatMap { quran.surahs[it].ayahRange }.filter { !quran.isLastOfSurah(it) }
    }
    private val practiceWindow: Int
        get() = if (practicePrompts.size > level.questionsPerRound) level.questionsPerRound - 1 else 1

    /**
     * Hafiz: the level's prompts that have at least one trap. Null outside Hafiz, or when fewer than
     * [RECENT_MEMORY] exist (then any prompt is used, with traps where it has some).
     */
    private val trapPrompts: List<IntRange>? by lazy {
        if (mutashabihat == null) null else allPrompts().filter(::hasTrap).takeIf { it.size >= RECENT_MEMORY }
    }

    fun next(): Question {
        val shown = if (level.practice) nextPracticePrompt() else pickFreshPrompt()
        return when (level.style) {
            GameStyle.GuessSurah -> guessSurahQuestion(shown)
            GameStyle.NextAyah -> nextAyahQuestion(shown)
        }
    }

    // ---------------------------------------------------------------- prompt selection

    private fun pickFreshPrompt(): IntRange {
        var shown = pickPrompt()
        repeat(MAX_REPEAT_TRIES) {
            if (shown.first !in recent) return@repeat
            shown = pickPrompt()
        }
        recent.addLast(shown.first)
        if (recent.size > RECENT_MEMORY) recent.removeFirst()
        return shown
    }

    /**
     * Each prompt ayah once per shuffled deck. Rounds are not aligned with decks (the next question
     * is prefetched), so a prompt is also skipped while it is among the last `questionsPerRound - 1`
     * dealt; for a surah with exactly one round of prompts only the one just dealt is avoided, so
     * every round is reshuffled.
     */
    private fun nextPracticePrompt(): IntRange {
        if (practiceDeck.isEmpty()) practiceDeck.addAll(practicePrompts.shuffled(random))
        val pick = practiceDeck.indexOfFirst { it !in practiceRecent }.coerceAtLeast(0)
        val prompt = practiceDeck.removeAt(pick)
        practiceRecent.addLast(prompt)
        while (practiceRecent.size > practiceWindow) practiceRecent.removeFirst()
        return prompt..prompt
    }

    private fun pickPrompt(): IntRange {
        trapPrompts?.let { return it[random.nextInt(it.size)] }
        if (level.openings) {
            val surah = quran.surahs[level.surahRange.random(random)]
            return surah.firstAyah..surah.firstAyah + 1
        }
        val first = quran.surahs[level.firstSurah].firstAyah
        val last = quran.surahs[level.lastSurah].lastAyah
        return when (level.style) {
            GameStyle.GuessSurah -> random.nextInt(first, last + 1).let { it..it }
            GameStyle.NextAyah -> {
                // Uniform over ayahs that have a following ayah in the same surah.
                var ayah: Int
                do {
                    ayah = random.nextInt(first, last + 1)
                } while (quran.isLastOfSurah(ayah))
                ayah..ayah
            }
        }
    }

    /** Every prompt [pickPrompt] can return for this level. */
    private fun allPrompts(): List<IntRange> {
        if (level.openings) return level.surahRange.map { quran.surahs[it].firstAyah.let { f -> f..f + 1 } }
        val first = quran.surahs[level.firstSurah].firstAyah
        val last = quran.surahs[level.lastSurah].lastAyah
        return (first..last).filter { level.style == GameStyle.GuessSurah || !quran.isLastOfSurah(it) }.map { it..it }
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
            .filter(acceptable).distinctBy(quran::text)
    }

    /** A wrong next-ayah option may not lie between the prompt and the answer, nor repeat the text of any valid answer. */
    private fun wrongNextAyahFilter(shown: IntRange): (Int) -> Boolean {
        val answer = shown.last + 1
        // Any ayah that follows an identical copy of the prompt anywhere in the Quran would be
        // a correct answer too (e.g. the refrains of سورة الرحمن / القمر / المرسلات).
        val forbiddenTexts = quran.occurrencesOf(quran.text(shown.last))
            .filter { !quran.isLastOfSurah(it) }
            .mapTo(mutableSetOf()) { quran.text(it + 1) }
            .apply { add(quran.text(answer)) }
        return { g -> g !in shown.first..answer && quran.text(g) !in forbiddenTexts }
    }

    // ---------------------------------------------------------------- guess the surah

    private fun guessSurahQuestion(shown: IntRange): Question {
        val surah = quran.surahOf(shown.first)
        val alsoCorrect = quran.surahsContaining(shown)
        val traps = trapSurahs(shown, alsoCorrect).take(WRONG_OPTIONS)
        val need = WRONG_OPTIONS - traps.size

        fun pool(range: IntRange) = range
            .filter { it != surah.index && it !in alsoCorrect && it !in traps }
            .sortedBy { abs(it - surah.index) }
            .take(SURAH_NEIGHBOURHOOD)

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
        val trapTexts = traps.mapTo(HashSet(), quran::text)

        // Nearby ayahs of the same surah first (as in the original), then neighbouring surahs.
        val ordered = (maxOf(0, answer - AYAH_SEARCH_RADIUS)..minOf(quran.ayahCount - 1, answer + AYAH_SEARCH_RADIUS))
            .asSequence()
            .sortedWith(compareBy<Int>({ quran.surahOf(it) != surah }, { abs(it - answer) }))
            .filter(acceptable)
            .filter { quran.text(it) !in trapTexts }
            .distinctBy(quran::text)
            .take(AYAH_NEIGHBOURHOOD)
            .toList()

        val wrongIdx = traps + ordered.shuffled(random).take(WRONG_OPTIONS - traps.size)
        val texts = (listOf(answer) + wrongIdx).map(quran::text)
        val labels = ArabicText.distinctPrefixes(texts, OPTION_MIN_WORDS)

        val (options, correct) = shuffleWithCorrect(labels.first(), labels.drop(1))
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
        const val SURAH_NEIGHBOURHOOD = 16 // original: ±8 surahs around the correct one
        const val AYAH_NEIGHBOURHOOD = 8   // original: window of 8 ayahs around the answer
        const val AYAH_SEARCH_RADIUS = 60
        const val OPTION_MIN_WORDS = 10
        const val RECENT_MEMORY = 20
        const val MAX_REPEAT_TRIES = 10
    }
}
