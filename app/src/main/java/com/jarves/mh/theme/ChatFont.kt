package com.jarves.mh.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jarves.mh.R

/**
 * App-wide font: Space Grotesk (Google Fonts, downloaded on demand through Google Play services,
 * same mechanism as the old Poppins font, so the APK does not grow).
 * If the font can't be loaded (no Play services / offline on first launch) Android falls back to the
 * system font automatically. Code, terminal and file paths still use FontFamily.Monospace.
 *
 * To change the font later, change only the name below, e.g. GoogleFont("Sora").
 */
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val appGoogleFont = GoogleFont("Space Grotesk")

val AppFontFamily = FontFamily(
    Font(googleFont = appGoogleFont, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = appGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = appGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = appGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = appGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
)

/** Chat messages use the same app font. (Name kept so existing code keeps working.) */
val ChatFontFamily: FontFamily = AppFontFamily
