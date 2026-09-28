package com.jarves.mh.ui.theme

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font as ResFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jarves.mh.R

/**
 * App-wide font: Space Grotesk.
 *
 * 1) If font files are bundled in res/font/ they are used (works offline, on every phone):
 *      spacegrotesk_light.ttf, spacegrotesk_regular.ttf, spacegrotesk_medium.ttf,
 *      spacegrotesk_semibold.ttf, spacegrotesk_bold.ttf   (any subset is fine, at least "regular")
 * 2) Otherwise it is downloaded from Google Fonts through Google Play services.
 *    If that also fails, Android silently shows the system font (Roboto).
 *
 * Code / terminal / file paths still use FontFamily.Monospace.
 * To change font later: rename the files (spacegrotesk_ prefix in BUNDLED_PREFIX) and the Google name below.
 */
object AppFonts {
    private const val BUNDLED_PREFIX = "spacegrotesk_"
    private const val GOOGLE_NAME = "Space Grotesk"

    @Volatile private var appContext: Context? = null

    /** Call once at startup (MainActivity.onCreate) before any UI is drawn. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val family: FontFamily by lazy { bundled() ?: google() }

    private fun bundled(): FontFamily? {
        val ctx = appContext ?: return null
        val weights = listOf(
            "light" to FontWeight.Light,
            "regular" to FontWeight.Normal,
            "medium" to FontWeight.Medium,
            "semibold" to FontWeight.SemiBold,
            "bold" to FontWeight.Bold,
        )
        val fonts = weights.mapNotNull { (suffix, weight) ->
            val id = ctx.resources.getIdentifier(BUNDLED_PREFIX + suffix, "font", ctx.packageName)
            if (id != 0) ResFont(id, weight) else null
        }
        return if (fonts.isEmpty()) null else FontFamily(fonts)
    }

    private fun google(): FontFamily {
        val provider = GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs,
        )
        val name = GoogleFont(GOOGLE_NAME)
        return FontFamily(
            listOf(
                FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
            ).map { GoogleFontEntry(googleFont = name, fontProvider = provider, weight = it) }
        )
    }
}

val AppFontFamily: FontFamily get() = AppFonts.family

/** Chat messages use the same app font. (Name kept so existing code keeps working.) */
val ChatFontFamily: FontFamily get() = AppFonts.family
