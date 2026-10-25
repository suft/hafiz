package suft.android.ayahiq.domain

/** How a game ends: after a fixed round (Regular) or when the lives run out (endless modes). */
enum class GameMode(val key: String, val lives: Int?, val trapOptions: Boolean = false) {
    Regular("regular", lives = null),
    Survival("survival", lives = 1),

    /** Three lives; the wrong options are mutashabihat — ayahs and surahs that resemble the right answer. */
    Mutashabihat("mutashabihat", lives = 3, trapOptions = true);

    val endless: Boolean get() = lives != null

    /** Survival's score is its streak, so only games with more lives show the longest streak separately. */
    val showsLongestStreak: Boolean get() = (lives ?: 0) > 1

    /** Regular: after [roundLength] answers. Endless: when [wrong] reaches [lives]. */
    fun isOver(answered: Int, wrong: Int, roundLength: Int): Boolean =
        if (lives == null) answered >= roundLength else wrong >= lives

    companion object {
        fun fromKey(key: String): GameMode = entries.first { it.key == key }
    }
}
