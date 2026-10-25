package suft.android.hafiz.audio

import java.util.Locale
import suft.android.hafiz.domain.AppLanguage

/** A reciter whose per-ayah MP3s are hosted on everyayah.com. */
data class Reciter(val arabicName: String, val englishName: String, val folder: String) {
    fun name(language: AppLanguage): String = if (language == AppLanguage.English) englishName else arabicName

    /** e.g. https://everyayah.com/data/Alafasy_64kbps/002255.mp3 (both numbers 1-based). */
    fun url(surahNumber: Int, ayahNumber: Int): String =
        // Locale.ROOT: an Arabic default locale would otherwise format Arabic-Indic digits.
        String.format(Locale.ROOT, "%s%s/%03d%03d.mp3", BASE_URL, folder, surahNumber, ayahNumber)

    private companion object {
        const val BASE_URL = "https://everyayah.com/data/"
    }
}

object Reciters {
    /** Reciters offered in the quiz; the first is the [default]. Saved choices are stored by folder, not position. */
    val all: List<Reciter> = listOf(
        Reciter("سعد الغامدي", "Saad Al-Ghamdi", "Ghamadi_40kbps"),
        Reciter("عبد الباسط (تجويد)", "Abdul Basit (Mujawwad)", "Abdul_Basit_Mujawwad_128kbps"),
        Reciter("عبد الباسط (ترتيل)", "Abdul Basit (Murattal)", "Abdul_Basit_Murattal_64kbps"),
        Reciter("عبد الرحمن السديس", "Abdurrahman As-Sudais", "Abdurrahmaan_As-Sudais_64kbps"),
        Reciter("أبو بكر الشاطري", "Abu Bakr Al-Shatri", "Abu_Bakr_Ash-Shaatree_64kbps"),
        Reciter("مشاري العفاسي", "Mishary Alafasy", "Alafasy_64kbps"),
        Reciter("محمود الحصري (معلم)", "Mahmoud Al-Husary (Muallim)", "Husary_Muallim_128kbps"),
        Reciter("محمود الحصري (ترتيل)", "Mahmoud Al-Husary (Murattal)", "Husary_64kbps"),
        Reciter("محمود الحصري (تجويد)", "Mahmoud Al-Husary (Mujawwad)", "Husary_Mujawwad_64kbps"),
        Reciter("ماهر المعيقلي", "Maher Al-Muaiqly", "Maher_AlMuaiqly_64kbps"),
        Reciter("خالد القحطاني", "Khalid Al-Qahtani", "Khaalid_Abdullaah_al-Qahtaanee_192kbps"),
        Reciter("عبدالله بصفر", "Abdullah Basfar", "Abdullah_Basfar_64kbps"),
        Reciter("محسن القاسم", "Muhsin Al-Qasim", "Muhsin_Al_Qasim_192kbps"),
        Reciter("ياسر الدوسري", "Yasser Al-Dosari", "Yasser_Ad-Dussary_128kbps"),
        Reciter("سعود الشريم", "Saud Al-Shuraim", "Saood_ash-Shuraym_64kbps"),
        Reciter("محمد جبريل", "Muhammad Jibreel", "Muhammad_Jibreel_64kbps"),
        Reciter("محمود علي البنا", "Mahmoud Ali Al-Banna", "mahmoud_ali_al_banna_32kbps"),
        Reciter("أحمد علي العجمي", "Ahmed Al-Ajmi", "Ahmed_ibn_Ali_al-Ajamy_64kbps_QuranExplorer.Com"),
        // The original app had these two folders swapped.
        Reciter("محمد صديق المنشاوي (ترتيل)", "Muhammad Siddiq Al-Minshawi (Murattal)", "Minshawy_Murattal_128kbps"),
        Reciter("محمد صديق المنشاوي (تجويد)", "Muhammad Siddiq Al-Minshawi (Mujawwad)", "Minshawy_Mujawwad_64kbps"),
        // New in 3.0
        Reciter("ناصر القطامي", "Nasser Al-Qatami", "Nasser_Alqatami_128kbps"),
        Reciter("هاني الرفاعي", "Hani Ar-Rifai", "Hani_Rifai_64kbps"),
        Reciter("علي الحذيفي", "Ali Al-Hudhaify", "Hudhaify_64kbps"),
        Reciter("فارس عباد", "Fares Abbad", "Fares_Abbad_64kbps"),
        Reciter("محمد أيوب", "Muhammad Ayyub", "Muhammad_Ayyoub_64kbps"),
        Reciter("صلاح البدير", "Salah Al-Budair", "Salah_Al_Budair_128kbps"),
    )

    val default: Reciter get() = all.first()

    /** The reciter stored under [folder]; the [default] for null or a folder that no longer exists. */
    fun byFolder(folder: String?): Reciter = all.firstOrNull { it.folder == folder } ?: default

    /**
     * What a progress file that stored a list index (the format before folders were stored) meant: the
     * folders of the 27 reciters of that version in order. The removed Mustafa Ismail (his files are missing
     * on everyayah.com) maps to Mishary Alafasy.
     */
    internal val legacyIndexFolders: List<String> = listOf(
        "Ghamadi_40kbps",
        "Abdul_Basit_Mujawwad_128kbps",
        "Abdul_Basit_Murattal_64kbps",
        "Abdurrahmaan_As-Sudais_64kbps",
        "Abu_Bakr_Ash-Shaatree_64kbps",
        "Alafasy_64kbps",
        "Husary_Muallim_128kbps",
        "Husary_64kbps",
        "Husary_Mujawwad_64kbps",
        "Maher_AlMuaiqly_64kbps",
        "Khaalid_Abdullaah_al-Qahtaanee_192kbps",
        "Abdullah_Basfar_64kbps",
        "Muhsin_Al_Qasim_192kbps",
        "Yasser_Ad-Dussary_128kbps",
        "Saood_ash-Shuraym_64kbps",
        "Muhammad_Jibreel_64kbps",
        "mahmoud_ali_al_banna_32kbps",
        "Alafasy_64kbps",
        "Ahmed_ibn_Ali_al-Ajamy_64kbps_QuranExplorer.Com",
        "Minshawy_Murattal_128kbps",
        "Minshawy_Mujawwad_64kbps",
        "Nasser_Alqatami_128kbps",
        "Hani_Rifai_64kbps",
        "Hudhaify_64kbps",
        "Fares_Abbad_64kbps",
        "Muhammad_Ayyoub_64kbps",
        "Salah_Al_Budair_128kbps",
    )
}
