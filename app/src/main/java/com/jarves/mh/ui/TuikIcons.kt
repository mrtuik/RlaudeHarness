package com.jarves.mh.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.remember
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.util.TypedValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
 * XML drawables behave exactly as before.
 *
 * PNGs: Android's canvas only does plain bilinear filtering (FilterQuality.High is the same as Low
 * there), so drawing a 512px PNG at ~20dp (about 10x smaller) looks jagged. Instead the PNG is
 * scaled down in steps of 2x (proper averaging) to the exact pixel size it is drawn at, once, and cached.
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
    val bitmap = remember(id) { decodedBitmap(context, id) }
    if (bitmap == null) return composePainterResource(id)
    return remember(bitmap) { CrispBitmapPainter(bitmap, id) }
}

private val decodedCache = java.util.concurrent.ConcurrentHashMap<Int, Bitmap>()
private val scaledCache = LruCache<String, androidx.compose.ui.graphics.ImageBitmap>(96)

private fun decodedBitmap(context: android.content.Context, id: Int): Bitmap? {
    decodedCache[id]?.let { return it }
    val opts = BitmapFactory.Options().apply {
        inScaled = false
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val bmp = BitmapFactory.decodeResource(context.resources, id, opts) ?: return null
    decodedCache[id] = bmp
    return bmp
}

private fun scaledBitmap(src: Bitmap, id: Int, w: Int, h: Int): androidx.compose.ui.graphics.ImageBitmap {
    val key = "$id:$w:$h"
    scaledCache.get(key)?.let { return it }
    var cur = src
    if (w < src.width || h < src.height) {
        while (cur.width / 2 >= w && cur.height / 2 >= h) {
            cur = Bitmap.createScaledBitmap(cur, cur.width / 2, cur.height / 2, true)
        }
        if (cur.width != w || cur.height != h) {
            cur = Bitmap.createScaledBitmap(cur, w, h, true)
        }
    }
    val out = cur.asImageBitmap()
    scaledCache.put(key, out)
    return out
}

private class CrispBitmapPainter(private val src: Bitmap, private val resId: Int) : Painter() {
    private var alpha = 1f
    private var colorFilter: ColorFilter? = null
    override val intrinsicSize: Size = Size(src.width.toFloat(), src.height.toFloat())
    override fun applyAlpha(alpha: Float): Boolean { this.alpha = alpha; return true }
    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean { this.colorFilter = colorFilter; return true }
    override fun DrawScope.onDraw() {
        val w = size.width.roundToInt().coerceAtLeast(1)
        val h = size.height.roundToInt().coerceAtLeast(1)
        val img = scaledBitmap(src, resId, w, h)
        drawImage(
            image = img,
            dstSize = IntSize(w, h),
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Low,
        )
    }
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
