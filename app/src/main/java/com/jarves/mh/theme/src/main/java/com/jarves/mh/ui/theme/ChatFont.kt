package com.jarves.mh.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.jarves.mh.R

/**
 * Tiempos (Text cut) — used only for chat message text (user bubbles + AI markdown replies).
 * Font files live in res/font/ as tiempos_text_*.otf.
 */
val ChatFontFamily = FontFamily(
    Font(R.font.tiempos_text_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.tiempos_text_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.tiempos_text_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.tiempos_text_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.tiempos_text_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.tiempos_text_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.tiempos_text_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.tiempos_text_bold_italic, FontWeight.Bold, FontStyle.Italic),
)
