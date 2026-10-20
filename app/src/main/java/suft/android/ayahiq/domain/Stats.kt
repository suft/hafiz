package suft.android.ayahiq.domain

/** Correct/wrong answer counts for one surah (or overall). */
data class Score(val correct: Int = 0, val wrong: Int = 0) {
    val total: Int get() = correct + wrong

    /** 0.0–1.0; a surah never answered counts as 100 % like in the original app. */
    val accuracy: Double get() = if (total == 0) 1.0 else correct.toDouble() / total

    val percent: Int get() = (accuracy * 100).toInt()
}

data class SurahScore(val surahIndex: Int, val score: Score)

object Stats {
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
