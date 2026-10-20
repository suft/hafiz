package suft.android.ayahiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import suft.android.ayahiq.games.PlayGames
import suft.android.ayahiq.games.PlayGamesLeaderboards
import suft.android.ayahiq.ui.AyahIqRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val leaderboards = if (PlayGames.isEnabled(this)) PlayGamesLeaderboards(this, appContainer) else null
        setContent { AyahIqRoot(appContainer, onOpenLeaderboards = leaderboards?.let { it::showAll }) }
    }
}
