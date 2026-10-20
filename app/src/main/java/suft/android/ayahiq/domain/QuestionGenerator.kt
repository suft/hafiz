package suft.android.ayahiq.domain

import kotlin.math.abs
import kotlin.random.Random

/** One multiple-choice question. */
data class Question(
    val mode: GameMode,
    /** Global indices of the ayahs shown (and recited) in the prompt. */
    val shownAyahs: IntRange,
    /** The surah the shown ayahs belong to — the one stats are recorded against. */
    val surahIndex: Int,
    /** Prompt ayah texts, one entry per ayah. */
    val promptAyahs: List<String>,
    /** 1-based ayah numbers of [promptAyahs] inside the surah. */
    val promptAyahNumbers: List<Int>,
    /** e.g. "سورة البقرة · الآية ٢٥٥" / "Surah Al-Baqarah · Ayah 255" — only for [GameMode.NextAyah] (where the surah is given). */
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
 */
class QuestionGenerator(
    private val quran: Quran,
    private val level: Level,
    private val language: AppLanguage,
    private val random: Random = Random.Default,
) {
    private val recent = ArrayDeque<Int>()
    private val practiceDeck = ArrayDeque<Int>()
    private val practiceRecent = ArrayDeque<Int>()
    private val practicePrompts: List<Int> by lazy {
        level.surahRange.flatMap { quran.surahs[it].ayahRange }.filter { !quran.isLastOfSurah(it) }
    }
    private val practiceWindow: Int
        get() = if (practicePrompts.size > level.questionsPerRound) level.questionsPerRound - 1 else 1

    fun next(): Question {
        val shown = if (level.practice) nextPracticePrompt() else pickFreshPrompt()
        return when (level.mode) {
            GameMode.GuessSurah -> guessSurahQuestion(shown)
            GameMode.NextAyah -> nextAyahQuestion(shown)
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
        if (level.openings) {
            val surah = quran.surahs[level.surahRange.random(random)]
            return surah.firstAyah..surah.firstAyah + 1
        }
        val first = quran.surahs[level.firstSurah].firstAyah
        val last = quran.surahs[level.lastSurah].lastAyah
        return when (level.mode) {
            GameMode.GuessSurah -> random.nextInt(first, last + 1).let { it..it }
            GameMode.NextAyah -> {
                // Uniform over ayahs that have a following ayah in the same surah.
                var ayah: Int
                do {
                    ayah = random.nextInt(first, last + 1)
                } while (quran.isLastOfSurah(ayah))
                ayah..ayah
            }
        }
    }

    // ---------------------------------------------------------------- guess the surah

    private fun guessSurahQuestion(shown: IntRange): Question {
        val surah = quran.surahOf(shown.first)
        val alsoCorrect = quran.surahsContaining(shown)

        fun pool(range: IntRange) = range
            .filter { it != surah.index && it !in alsoCorrect }
            .sortedBy { abs(it - surah.index) }
            .take(SURAH_NEIGHBOURHOOD)

        val candidates = pool(level.surahRange).takeIf { it.size >= WRONG_OPTIONS }
            ?: pool(0 until Quran.SURAH_COUNT)
        val wrong = candidates.shuffled(random).take(WRONG_OPTIONS).map { quran.surahs[it].displayName(language) }

        val (options, correct) = shuffleWithCorrect(surah.displayName(language), wrong)
        return Question(
            mode = GameMode.GuessSurah,
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
        // Any ayah that follows an identical copy of the prompt anywhere in the Quran would be
        // a correct answer too (e.g. the refrains of سورة الرحمن / القمر / المرسلات).
        val forbiddenTexts = quran.occurrencesOf(quran.text(shown.last))
            .filter { !quran.isLastOfSurah(it) }
            .mapTo(mutableSetOf()) { quran.text(it + 1) }
            .apply { add(quran.text(answer)) }

        fun acceptable(g: Int): Boolean = g !in shown.first..answer && quran.text(g) !in forbiddenTexts

        // Nearby ayahs of the same surah first (as in the original), then neighbouring surahs.
        val ordered = (maxOf(0, answer - AYAH_SEARCH_RADIUS)..minOf(quran.ayahCount - 1, answer + AYAH_SEARCH_RADIUS))
            .asSequence()
            .sortedWith(compareBy<Int>({ quran.surahOf(it) != surah }, { abs(it - answer) }))
            .filter(::acceptable)
            .distinctBy(quran::text)
            .take(AYAH_NEIGHBOURHOOD)
            .toList()

        val wrongIdx = ordered.shuffled(random).take(WRONG_OPTIONS)
        val texts = (listOf(answer) + wrongIdx).map(quran::text)
        val labels = ArabicText.distinctPrefixes(texts, OPTION_MIN_WORDS)

        val (options, correct) = shuffleWithCorrect(labels.first(), labels.drop(1))
        return Question(
            mode = GameMode.NextAyah,
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
