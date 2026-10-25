package suft.android.hafiz.domain

/** Correct/wrong answer counts for one surah (or overall). */
data class Score(val correct: Int = 0, val wrong: Int = 0) {
    val total: Int get() = correct + wrong

    /** 0.0–1.0; a surah never answered counts as 0 %, like [percent]. */
    val accuracy: Double get() = if (total == 0) 0.0 else correct.toDouble() / total

    /** Whole percent, rounded down with integer arithmetic (no float error); no answers = 0 %. */
    val percent: Int get() = if (total == 0) 0 else correct * 100 / total
}

/** Aggregate of finished endless games in one [GameMode]. */
data class RunStats(val played: Int, val totalScore: Int, val best: Int, val bestStreak: Int) {
    /** Rounded to the nearest integer; 0 when nothing was played. */
    val average: Int get() = if (played == 0) 0 else (totalScore + played / 2) / played
}

data class SurahScore(val surahIndex: Int, val score: Score)

enum class SurahTier { NotPractised, Weak, Fair, Strong }

object Stats {
    const val FAIR_PERCENT = 60
    const val STRONG_PERCENT = 90
    const val STRONG_MIN_ANSWERS = 5
    const val STRONGEST_LIMIT = 10
    const val REVIEW_LIMIT = 10

    /** Colour bucket for the surah map: no answers → NotPractised, <60 % Weak, <90 % Fair, else Strong. */
    fun tier(score: Score?): SurahTier = when {
        score == null || score.total == 0 -> SurahTier.NotPractised
        score.percent < FAIR_PERCENT -> SurahTier.Weak
        score.percent < STRONG_PERCENT -> SurahTier.Fair
        else -> SurahTier.Strong
    }

    /** Best-known surahs: at least [minAnswers] answers and [STRONG_PERCENT] correct; highest accuracy, then most answers, then mushaf order. */
    fun strongestFirst(
        scores: Map<Int, Score>,
        minAnswers: Int = STRONG_MIN_ANSWERS,
        limit: Int = STRONGEST_LIMIT,
    ): List<SurahScore> =
        scores.asSequence()
            .filter { it.value.total >= minAnswers && it.value.percent >= STRONG_PERCENT }
            .map { SurahScore(it.key, it.value) }
            .sortedWith(
                compareByDescending<SurahScore> { it.score.accuracy }
                    .thenByDescending { it.score.total }
                    .thenBy { it.surahIndex },
            )
            .take(limit)
            .toList()

    /**
     * Surahs to review: at least one wrong answer and under [STRONG_PERCENT] correct, weakest first by accuracy
     * with one extra correct and one extra wrong answer (so a single miss ranks below consistent misses); then
     * most mistakes. At most [limit].
     */
    fun weakestFirst(scores: Map<Int, Score>, limit: Int = REVIEW_LIMIT): List<SurahScore> =
        scores.asSequence()
            .filter { it.value.wrong > 0 && it.value.percent < STRONG_PERCENT }
            .map { SurahScore(it.key, it.value) }
            .sortedWith(
                compareBy<SurahScore> { smoothedAccuracy(it.score) }
                    .thenByDescending { it.score.wrong }
                    .thenBy { it.surahIndex },
            )
            .take(limit)
            .toList()

    private fun smoothedAccuracy(s: Score): Double = (s.correct + 1.0) / (s.total + 2)
}
