@file:Suppress("DEPRECATION", "PrivateApi")

package wonton.utils

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.*
import kotlinx.atomicfu.atomic
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

private fun Window.setMIUIDarkStatusBar(dark: Boolean) {
    try {
        val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
        val field: Field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
        val darkModeFlag = field.getInt(layoutParams)
        val extraFlagField: Method = javaClass.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        extraFlagField.invoke(this, if (dark) darkModeFlag else 0, darkModeFlag)
    } catch (e: Exception) {
        e.printStackTrace()
        setAOSPDarkStatusBar()
    }
}

private fun Window.setFlymeDarkStatusBar(dark: Boolean) {
    try {
        val lp = attributes
        val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
        val flymeFlag = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
        darkFlag.isAccessible = true
        flymeFlag.isAccessible = true
        val bit = darkFlag.getInt(null)
        var value = flymeFlag.getInt(lp)
        value = if (dark) value or bit else value and bit.inv()
        flymeFlag.setInt(lp, value)
        attributes = lp
    } catch (e: Exception) {
        e.printStackTrace()
        setAOSPDarkStatusBar()
    }
}

private fun Window.setAOSPDarkStatusBar() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        WindowCompat.getInsetsController(this, decorView)
            ?.isAppearanceLightStatusBars = true
        statusBarColor =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Color.WHITE else Color.BLACK
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    else Unit

private fun Window.unSetAOSPDarkStatusBar() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        WindowCompat.getInsetsController(this, decorView)
            ?.isAppearanceLightStatusBars = false
        statusBarColor = Color.TRANSPARENT
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    else Unit

fun Window.setDarkStatusBar() {
    when (Build.MANUFACTURER.uppercase(Locale.CHINA)) {
        "XIAOMI" -> setMIUIDarkStatusBar(true)
        "MEIZU" -> setFlymeDarkStatusBar(true)
        else -> setAOSPDarkStatusBar()
    }
}

fun Window.setDarkNavigationBar() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        WindowCompat.getInsetsController(this, decorView)
            ?.isAppearanceLightNavigationBars = true
        navigationBarColor =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Color.WHITE else Color.BLACK
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    else Unit

fun Window.unSetDarkStatusBar() {
    when (Build.MANUFACTURER.uppercase(Locale.CHINA)) {
        "XIAOMI" -> setMIUIDarkStatusBar(false)
        "MEIZU" -> setFlymeDarkStatusBar(false)
        else -> unSetAOSPDarkStatusBar()
    }
}

fun Window.unSetDarkNavigationBar() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        WindowCompat.getInsetsController(this, decorView)
            ?.isAppearanceLightNavigationBars = false
        navigationBarColor = Color.TRANSPARENT
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    else Unit

fun Window.setTransparentStatusBar() {
    WindowCompat.setDecorFitsSystemWindows(this, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
    decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) statusBarColor = Color.TRANSPARENT
}

fun Window.setTransparentNavigationBar() {
    WindowCompat.setDecorFitsSystemWindows(this, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
    decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) navigationBarColor = Color.TRANSPARENT
}

val Window.statusBarHeight
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ViewCompat.getRootWindowInsets(decorView)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
        else {
            val height = atomic(0)
            ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top.let {
                    if (it > 0) ViewCompat.setOnApplyWindowInsetsListener(v, null)
                    height.getAndSet(it)
                }
                ViewCompat.onApplyWindowInsets(v, insets)
            }
            height.value
        }

private fun getSystemResourcesDimenPixelSize(name: String) =
    if (Resources.getSystem().getIdentifier(name, "dimen", "android") != 0)
        Resources.getSystem()
            .getDimensionPixelSize(Resources.getSystem().getIdentifier(name, "dimen", "android"))
    else 0

val Window.cutOutHeight
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ViewCompat.getRootWindowInsets(decorView)
                ?.getInsets(WindowInsetsCompat.Type.displayCutout())?.bottom
        else {
            val cutOutHeight = atomic(0)
            ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
                if (insets.displayCutout != null)
                    cutOutHeight.getAndSet(insets.getInsets(WindowInsetsCompat.Type.displayCutout()).bottom)
                ViewCompat.setOnApplyWindowInsetsListener(v, null)
                insets /*.consumeDisplayCutout()*/
            }
            cutOutHeight.value
        }

@Deprecated(message = "isNavBarVisibleCompat", replaceWith = ReplaceWith("Window.isNavBarVisibleCompat"))
fun Window.isNavBarVisible() =
    (decorView as ViewGroup).children.filter { it.id != View.NO_ID }.any {
        "navigationBarBackground" == context.resources.getResourceEntryName(it.id) && it.isVisible
    } && decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0