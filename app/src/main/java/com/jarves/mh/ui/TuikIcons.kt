package com.jarves.mh.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.remember
import android.util.TypedValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource as composePainterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon as M3Icon

/**
 * Tuik icon override system.
 *
 * Every `Icon(Icons.Default.Close, ...)` in the app goes through the `Icon` functions below.
 * For a Material icon we look for a drawable called `ic_tuik<name>` (all lowercase, no separators):
 *
 *   Icons.Default.Close                          -> ic_tuikclose
 *   Icons.Default.KeyboardArrowDown              -> ic_tuikkeyboardarrowdown
 *   Icons.AutoMirrored.Filled.ArrowBack          -> ic_tuikarrowback
 *   Icons.Outlined.Terminal                      -> ic_tuikterminaloutlined, then ic_tuikterminal
 *
 * If the drawable exists (drop the PNG in res/drawable-nodpi/), it is shown.
 * If not, the normal Material icon is shown. No code change is needed when adding icons later.
 */
private const val TUIK_PREFIX = "ic_tuik"
private val STYLES = setOf("Filled", "Outlined", "Rounded", "Sharp", "TwoTone")

// name -> resource id (0 = not present). Resources don't change at runtime, so caching is safe.
private val idCache = java.util.concurrent.ConcurrentHashMap<String, Int>()

/** Candidate drawable names for a Material ImageVector, most specific first. */
internal fun tuikCandidates(vector: ImageVector): List<String> {
    val parts = vector.name.split('.')
    var i = 0
    if (parts.getOrNull(0) == "AutoMirrored") i = 1
    val style = parts.getOrNull(i) ?: return emptyList()
    if (style !in STYLES) return emptyList() // custom vectors (ic_write, ic_rabbit, ...) are left alone
    val base = parts.drop(i + 1).joinToString("").lowercase()
    if (base.isEmpty()) return emptyList()
    val plain = TUIK_PREFIX + base
    return if (style == "Filled") listOf(plain) else listOf(plain + style.lowercase(), plain)
}

@Composable
internal fun rememberTuikPainter(vector: ImageVector): Painter {
    val context = LocalContext.current
    val density = LocalDensity.current
    val resId = remember(vector) {
        var found = 0
        for (name in tuikCandidates(vector)) {
            val id = idCache.getOrPut(name) {
                context.resources.getIdentifier(name, "drawable", context.packageName)
            }
            if (id != 0) { found = id; break }
        }
        found
    }
    val fallback = rememberVectorPainter(vector)
    if (resId == 0) return fallback
    val custom = painterResource(resId)
    val sizePx = with(density) { 24.dp.toPx() }
    return remember(custom, sizePx) { FixedSizePainter(custom, Size(sizePx, sizePx)) }
}

/**
 * Drop-in replacement for androidx.compose.ui.res.painterResource.
 * XML drawables behave exactly as before. PNGs are drawn with FilterQuality.High, because the
 * default (Low) looks jagged/blurry when a big PNG (e.g. 512px) is scaled down to an 18-24dp icon.
 */
@Composable
fun painterResource(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val isXml = remember(id) {
        val tv = TypedValue()
        context.resources.getValue(id, tv, true)
        tv.string?.endsWith(".xml") == true
    }
    if (isXml) return composePainterResource(id)
    val bitmap = ImageBitmap.imageResource(id)
    return remember(bitmap) { BitmapPainter(bitmap, filterQuality = FilterQuality.High) }
}

/** PNGs have a big pixel size; report 24dp like Material vectors so Icon() sizes them the same way. */
private class FixedSizePainter(private val delegate: Painter, private val size: Size) : Painter() {
    private var alpha = 1f
    private var colorFilter: ColorFilter? = null
    override val intrinsicSize: Size get() = size
    override fun applyAlpha(alpha: Float): Boolean { this.alpha = alpha; return true }
    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean { this.colorFilter = colorFilter; return true }
    override fun DrawScope.onDraw() {
        with(delegate) { draw(this@onDraw.size, alpha, colorFilter) }
    }
}

// ---- Drop-in replacements for androidx.compose.material3.Icon (same signatures) ----

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    M3Icon(rememberTuikPainter(imageVector), contentDescription, modifier, tint)
}

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    M3Icon(painter, contentDescription, modifier, tint)
}

@Composable
fun Icon(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    M3Icon(bitmap, contentDescription, modifier, tint)
}
