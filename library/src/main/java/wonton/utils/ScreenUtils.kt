@file:Suppress("DEPRECATION")

package wonton.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.Side
import dev.chrisbanes.insetter.sidesOf
import dev.chrisbanes.insetter.windowInsetTypesOf

val Context.screenWidthCompat: Int
    get() = getSystemService<WindowManager>()?.let {
        Point().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                it.defaultDisplay.getRealSize(this)
            else it.defaultDisplay.getSize(this)
        }.x
    } ?: -1

val Context.screenHeightCompat: Int
    get() = getSystemService<WindowManager>()?.let {
        Point().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                it.defaultDisplay.getRealSize(this)
            else it.defaultDisplay.getSize(this)
        }.y
    } ?: -1

val Activity.screenWidth: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        windowManager.currentWindowMetrics.let { windowMetrics ->
            windowMetrics.bounds.width() -
                    windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars()).let {
                        it.left + it.right
                    }
        }
    else DisplayMetrics().apply {
        windowManager.defaultDisplay.getMetrics(this)
    }.widthPixels

val Activity.screenHeight: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        windowManager.currentWindowMetrics.let { windowMetrics ->
            windowMetrics.bounds.height() -
                    windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars()).let {
                        it.top + it.bottom
                    }
        }
    else DisplayMetrics().apply {
        windowManager.defaultDisplay.getMetrics(this)
    }.heightPixels

val Activity.actualMetric: Rect
    get() = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }

fun View.applyInsetsToBottomSheetView() =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .paddingBottom(
            windowInsetTypesOf(navigationBars = true,
            mandatorySystemGestures = true, displayCutout = true)
        )
        .margin(
            windowInsetTypesOf(mandatorySystemGestures = true),
            sidesOf(left = true, right = true, top = true)
        )
        .applyToView(this)

fun View.applySystemTopInsets() =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .marginTop(windowInsetTypesOf(statusBars = true))
        .padding(
            windowInsetTypesOf(mandatorySystemGestures = true, displayCutout = true),
            sidesOf(left = true, right = true)
        ).build()
        .applyToView(this)

fun View.applySystemBottomInsets() =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .marginBottom(windowInsetTypesOf(navigationBars = true))
        .padding(
            windowInsetTypesOf(mandatorySystemGestures = true, displayCutout = true),
            sidesOf(left = true, right = true)
        ).build()
        .applyToView(this)

fun View.applyAllSystemInsets() =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .margin(
            windowInsetTypesOf(navigationBars = true, statusBars = true,
            mandatorySystemGestures = true, displayCutout = true), Side.ALL)
        .build().applyToView(this)

fun View.applyImeInsetsAnimated() =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .padding(insetType = windowInsetTypesOf(navigationBars = true,
            mandatorySystemGestures = true, ime = true), animated = true)
        .build().applyToView(this)

@SafeVarargs
fun View.applyImeInsetsAnimatedWithSync(vararg views: View) =
    Insetter.builder().consume(Insetter.CONSUME_AUTO)
        .padding(insetType = windowInsetTypesOf(navigationBars = true,
            mandatorySystemGestures = true, ime = true), animated = true)
        .syncTranslationTo(*views).build().applyToView(this)

private const val immersive = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)

fun Window.setFullScreenCompat() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH -> WindowCompat.getInsetsController(this, decorView)?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> decorView.systemUiVisibility = (decorView.systemUiVisibility or immersive
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        else -> decorView.systemUiVisibility = decorView.systemUiVisibility or immersive
    }
}