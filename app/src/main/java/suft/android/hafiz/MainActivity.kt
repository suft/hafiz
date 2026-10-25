package suft.android.hafiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import suft.android.hafiz.games.PlayGames
import suft.android.hafiz.games.PlayGamesLeaderboards
import suft.android.hafiz.ui.HafizRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val leaderboards = if (PlayGames.isEnabled(this)) PlayGamesLeaderboards(this, appContainer) else null
        setContent { HafizRoot(appContainer, onOpenLeaderboards = leaderboards?.let { it::showAll }) }
    }
}
