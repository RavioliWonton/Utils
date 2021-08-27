@file:JvmName("ViewUtils")
@file:JvmMultifileClass

package wonton.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.app.DialogCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.TypefaceCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

inline val Context.layoutInflater: LayoutInflater get() = getSystemService()!!

fun View.setFadeTransition(clickTarget: View) {
    clickTarget.clicks().onEach {
        if (isGone) {
            alpha = 0f
            toVisible()
            ViewCompat.animate(this).alpha(1.0f)
        } else {
            ViewCompat.animate(this).alpha(0.0f).withEndAction {
                clearAnimation()
                toGone()
            }
        }
    }.launchIn(findViewLifecycleOwner().lifecycleScope)
}

fun View.findViewLifecycleOwner() =
    findViewTreeLifecycleOwner() ?: run {
        try {
            findFragment<Fragment>().viewLifecycleOwner
        } catch (e: IllegalStateException) {
            var owner = context
            while (owner is ContextWrapper) {
                if (owner is ComponentActivity) {
                    return@run owner
                } else owner = owner.baseContext
            }
            ProcessLifecycleOwner.get()
        }
    }


fun View.toVisible() {
    visibility = View.VISIBLE
}
fun View.toInVisible() {
    visibility = View.INVISIBLE
}
fun View.toGone() {
    visibility = View.GONE
}
fun View.flipVisibility(gone: Boolean = false) {
    if (isVisible) {
        if (gone) toGone()
        else toInVisible()
    } else toVisible()
}


fun View.setBackgroundCompat(background: Drawable?) = ViewCompat.setBackground(this, background)
var View.backgroundTintListCompat: ColorStateList?
    get() = ViewCompat.getBackgroundTintList(this)
    set(value) = ViewCompat.setBackgroundTintList(this, value)

fun Context.getDrawableCompat(@DrawableRes id: Int, useWrappedTheme: Boolean = true) =
    ResourcesCompat.getDrawable(resources, id, if(useWrappedTheme) theme else null)
@ColorInt
fun Context.getColorCompat(@ColorRes id: Int, useWrappedTheme: Boolean = true) =
    ResourcesCompat.getColor(resources, id, if(useWrappedTheme) theme else null)
fun Context.getColorStateListCompat(@ColorRes id: Int, useWrappedTheme: Boolean = true) =
    ResourcesCompat.getColorStateList(resources, id, if(useWrappedTheme) theme else null)
fun Context.getFontCompat(@FontRes id: Int, style: Int = Typeface.NORMAL)/*: Typeface? {
    var font : Typeface? = null
    var finish = false
    ResourcesCompat.getFont(this, id, object : ResourcesCompat.FontCallback() {
        override fun onFontRetrieved(typeface: Typeface) {
            font = TypefaceCompat.create(this@getFontCompat, typeface, style)
            finish = true
        }

        override fun onFontRetrievalFailed(reason: Int) {
            finish = true
        }
    }, HandlerCompat.createAsync(Looper.myLooper() ?: Looper.getMainLooper()))
    while (!finish) {

    }
    return font
}*/ = TypefaceCompat.create(this, ResourcesCompat.getFont(this, id), style)

var ImageView.imageTintListCompat
    get() = ImageViewCompat.getImageTintList(this)
    set(value) = ImageViewCompat.setImageTintList(this, value)

inline fun <reified T : View> Window.requireViewByIdCompat(@IdRes id: Int) = WindowCompat.requireViewById<T>(this, id)
inline fun <reified T : View> View.requireViewByIdCompat(@IdRes id: Int) = ViewCompat.requireViewById<T>(this, id)
inline fun <reified T : View> Activity.requireViewByIdCompat(@IdRes id: Int) = ActivityCompat.requireViewById<T>(this, id)
inline fun <reified T : View> Fragment.requireViewByIdCompat(@IdRes id: Int) = requireView().requireViewByIdCompat<T>(id)
inline fun <reified T : View> Dialog.requireViewByIdCompat(@IdRes id: Int) = DialogCompat.requireViewById(this, id) as T

fun ShapeableImageView.setCorner(@CornerFamily type: Int = CornerFamily.ROUNDED, @Dimension size: Float = 2f) {
    shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCorners(type, size).build()
}
