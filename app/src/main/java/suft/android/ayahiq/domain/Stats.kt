package suft.android.ayahiq.domain

/** Correct/wrong answer counts for one surah (or overall). */
data class Score(val correct: Int = 0, val wrong: Int = 0) {
    val total: Int get() = correct + wrong

    /** 0.0–1.0; a surah never answered counts as 100 % like in the original app. */
    val accuracy: Double get() = if (total == 0) 1.0 else correct.toDouble() / total

    val percent: Int get() = (accuracy * 100).toInt()
}

data class SurahScore(val surahIndex: Int, val score: Score)

enum class SurahTier { NotPractised, Weak, Fair, Strong }

object Stats {
    const val FAIR_PERCENT = 60
    const val STRONG_PERCENT = 90
    const val STRONG_MIN_ANSWERS = 5
    const val STRONGEST_LIMIT = 10

    /** Colour bucket for the surah map: no answers → NotPractised, <60 % Weak, <90 % Fair, else Strong. */
    fun tier(score: Score?): SurahTier = when {
        score == null || score.total == 0 -> SurahTier.NotPractised
        score.percent < FAIR_PERCENT -> SurahTier.Weak
        score.percent < STRONG_PERCENT -> SurahTier.Fair
        else -> SurahTier.Strong
    }

    /** Best-known surahs: at least [minAnswers] answers, highest accuracy, then most answers, then mushaf order. */
    fun strongestFirst(
        scores: Map<Int, Score>,
        minAnswers: Int = STRONG_MIN_ANSWERS,
        limit: Int = STRONGEST_LIMIT,
    ): List<SurahScore> =
        scores.asSequence()
            .filter { it.value.total >= minAnswers }
            .map { SurahScore(it.key, it.value) }
            .sortedWith(
                compareByDescending<SurahScore> { it.score.accuracy }
                    .thenByDescending { it.score.total }
                    .thenBy { it.surahIndex },
            )
            .take(limit)
            .toList()

    /**
     * Surahs with at least one wrong answer, weakest first: lowest accuracy, then the
     * most mistakes (same ordering as the original statistics screen).
     */
    fun weakestFirst(scores: Map<Int, Score>): List<SurahScore> =
        scores.asSequence()
            .filter { it.value.wrong > 0 }
            .map { SurahScore(it.key, it.value) }
            .sortedWith(compareBy<SurahScore> { it.score.accuracy }.thenByDescending { it.score.wrong })
            .toList()
}
