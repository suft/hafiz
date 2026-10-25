package suft.android.hafiz.games

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.games.PlayGames as Pgs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import suft.android.hafiz.AppContainer
import suft.android.hafiz.R
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Levels

object PlayGames {
    /** Release builds with a configured project id only; debug builds use another package that PGS won't authenticate. */
    fun isEnabled(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0 &&
            context.getString(R.string.game_services_project_id).isNotBlank()

    // Explicit R.string references (not getIdentifier) so resource shrinking keeps them.
    @StringRes
    fun leaderboardId(mode: GameMode, style: GameStyle): Int = when (mode) {
        GameMode.Regular -> when (style) {
            GameStyle.GuessSurah -> R.string.leaderboard_regular_surah
            GameStyle.NextAyah -> R.string.leaderboard_regular_ayah
        }
        GameMode.Survival -> when (style) {
            GameStyle.GuessSurah -> R.string.leaderboard_survival_surah
            GameStyle.NextAyah -> R.string.leaderboard_survival_ayah
        }
        GameMode.Mutashabihat -> when (style) {
            GameStyle.GuessSurah -> R.string.leaderboard_mutashabihat_surah
            GameStyle.NextAyah -> R.string.leaderboard_mutashabihat_ayah
        }
    }
}

/**
 * Keeps the six Play Games leaderboards (one per mode × style) in sync with local progress and opens
 * the Play Games leaderboards screen. Construct in `onCreate`, before the activity is STARTED.
 */
class PlayGamesLeaderboards(private val activity: ComponentActivity, container: AppContainer) {
    private val signIn = Pgs.getGamesSignInClient(activity)
    private val client = Pgs.getLeaderboardsClient(activity)

    /** Leaderboard id per mode × style; boards whose id is still blank are skipped. */
    private val boards: Map<Pair<GameMode, GameStyle>, String> = buildMap {
        for (mode in GameMode.entries) {
            for (style in GameStyle.entries) {
                val id = activity.getString(PlayGames.leaderboardId(mode, style))
                if (id.isNotBlank()) put(mode to style, id)
            }
        }
    }

    private val authenticated = MutableStateFlow(false)

    /** Last score submitted per board in this activity instance, so resumes don't resubmit unchanged scores. */
    private val submitted = mutableMapOf<String, Int>()

    // PGS needs startActivityForResult to identify the caller.
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    init {
        // PGS v2 signs in automatically at launch; this waits for that attempt.
        activity.lifecycleScope.launch {
            try {
                authenticated.value = signIn.isAuthenticated().await().isAuthenticated
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Play Games authentication check failed", e)
            }
        }

        // Submit the full current state rather than per-game deltas: uploads bests earned before this
        // integration existed and catches up after offline play. PGS ignores worse scores, so this is idempotent.
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val styleLevels = container.quran.filterNotNull()
                    .map { q -> GameStyle.entries.associateWith { Levels.forStyle(it, q) } }
                combine(authenticated, styleLevels, container.progress.progress) { auth, levels, progress ->
                    if (auth) boards.mapValues { (key, _) -> progress.leaderboardScore(key.first, levels.getValue(key.second)) } else null
                }.filterNotNull().distinctUntilChanged().collect { scores ->
                    for ((key, score) in scores) {
                        val id = boards.getValue(key)
                        if (score > 0 && submitted[id] != score) {
                            client.submitScore(id, score.toLong())
                            submitted[id] = score
                        }
                    }
                }
            }
        }
    }

    /** Signs in if needed and opens the Play Games leaderboards screen; `false` if that wasn't possible. */
    suspend fun showAll(): Boolean = try {
        if (!authenticated.value) authenticated.value = signIn.signIn().await().isAuthenticated
        if (!authenticated.value) {
            false
        } else {
            launcher.launch(client.allLeaderboardsIntent.await())
            true
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Play Games leaderboards failed", e)
        false
    }

    private companion object {
        const val TAG = "PlayGames"
    }
}
