package suft.android.hafiz.testing

import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.fail
import org.junit.rules.TemporaryFolder
import org.robolectric.shadows.ShadowLooper
import suft.android.hafiz.AppContainer
import suft.android.hafiz.data.Progress

/** Runs the main looper until [condition] holds, so coroutines posted to Dispatchers.Main make progress. */
fun idleMainUntil(timeoutMs: Long = 10_000, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (true) {
        ShadowLooper.idleMainLooper()
        if (condition()) return
        if (System.currentTimeMillis() > deadline) fail("Condition not met within $timeoutMs ms")
        Thread.sleep(10)
    }
}

/** Polls the stored progress until [condition] holds; fails with the last value on timeout. */
fun awaitProgress(container: AppContainer, timeoutMs: Long = 5_000, condition: (Progress) -> Boolean): Progress {
    val deadline = System.currentTimeMillis() + timeoutMs
    var last: Progress
    while (true) {
        last = runBlocking { container.progress.progress.first() }
        if (condition(last)) return last
        if (System.currentTimeMillis() > deadline) fail("Progress condition not met within $timeoutMs ms; last = $last")
        Thread.sleep(25)
    }
}

/** A container backed by a progress file that does not exist yet. */
fun testContainer(tmp: TemporaryFolder) =
    AppContainer(ApplicationProvider.getApplicationContext(), File(tmp.root, "progress.json"))
