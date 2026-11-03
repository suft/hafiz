package suft.android.hafiz.ui.i18n

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import suft.android.hafiz.domain.AppLanguage

/** Android 13+ per-app language (Settings → Apps → Hafiz → Language), kept in step with the stored [AppLanguage]. */
object SystemAppLanguage {
    /** The language chosen in system settings; null below Android 13, for "System default", or for an unsupported locale. */
    fun get(context: Context): AppLanguage? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        val locales = context.getSystemService(LocaleManager::class.java)?.applicationLocales ?: return null
        if (locales.isEmpty) return null
        return AppLanguage.entries.firstOrNull { it.tag == locales[0].language }
    }

    /** Records [language] as the system per-app language; no-op below Android 13. */
    fun set(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val manager = context.getSystemService(LocaleManager::class.java) ?: return
        manager.applicationLocales = LocaleList.forLanguageTags(language.tag)
    }
}
