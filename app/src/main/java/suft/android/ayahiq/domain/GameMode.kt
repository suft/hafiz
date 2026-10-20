package suft.android.ayahiq.domain

/** How a game ends: after a fixed round (Regular) or when the lives run out (endless modes). */
enum class GameMode(val key: String, val lives: Int?) {
    Regular("regular", lives = null),
    Survival("survival", lives = 1),
    ThreeLives("lives", lives = 3);

    val endless: Boolean get() = lives != null

    /** Regular: after [roundLength] answers. Endless: when [wrong] reaches [lives]. */
    fun isOver(answered: Int, wrong: Int, roundLength: Int): Boolean =
        if (lives == null) answered >= roundLength else wrong >= lives

    companion object {
        fun fromKey(key: String): GameMode = entries.first { it.key == key }
    }
}
