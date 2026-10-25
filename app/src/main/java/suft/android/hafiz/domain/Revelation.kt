package suft.android.hafiz.domain

import kotlin.random.Random

enum class RevelationPlace { Makkah, Madinah }

object RevelationPlaces {
    /** quran.com `revelation_place` (api.quran.com/api/v4/chapters) is "madinah" for these 1-based surah numbers, "makkah" for the rest. */
    private val madinah = setOf(2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110)

    /** [surahIndex] is 0-based. */
    fun of(surahIndex: Int): RevelationPlace =
        if (surahIndex + 1 in madinah) RevelationPlace.Madinah else RevelationPlace.Makkah
}

/** One question: [surahIndex] is 0-based, [place] the correct answer. */
data class RevelationQuestion(val surahIndex: Int, val place: RevelationPlace)

object RevelationQuiz {
    const val QUESTIONS = 5

    /** Key of the best score in Progress.bestScores and RoundRecord.levelId; no Level.id has this shape ("style:a-b…"). */
    const val LEVEL_ID = "revelation"

    /** RoundRecord.mode of a finished game; not a GameMode.key. */
    const val MODE_KEY = "revelation"

    /** A game with at least this many correct answers shows as "good" (green) in the history; same 80 % as Guess-the-Surah mastery. */
    const val GOOD_SCORE = 4

    /** [QUESTIONS] distinct surahs; each question is Makki or Madani with equal chance (86 vs 28 surahs would otherwise favour Makki). */
    fun deal(random: Random = Random.Default): List<RevelationQuestion> {
        val pools = RevelationPlace.entries.associateWith { place ->
            (0 until Quran.SURAH_COUNT).filter { RevelationPlaces.of(it) == place }.shuffled(random).toMutableList()
        }
        return List(QUESTIONS) {
            val place = if (random.nextBoolean()) RevelationPlace.Makkah else RevelationPlace.Madinah
            val pool = pools.getValue(place)
            RevelationQuestion(surahIndex = pool.removeAt(pool.lastIndex), place = place)
        }
    }
}
