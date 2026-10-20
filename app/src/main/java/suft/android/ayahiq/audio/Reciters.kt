package suft.android.ayahiq.audio

import java.util.Locale
import suft.android.ayahiq.domain.AppLanguage

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
    /** The original 21 reciters (same order, so saved choices stay meaningful) plus newer ones. */
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
        Reciter("مصطفى إسماعيل", "Mustafa Ismail", "Mustafa_Ismail_48kbps"),
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

    fun get(index: Int): Reciter = all.getOrElse(index) { all.first() }
}
