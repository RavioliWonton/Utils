package wonton.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.annotation.Size
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import androidx.core.view.TintableBackgroundView
import androidx.core.view.ViewCompat
import java.util.*
import kotlin.math.roundToInt

class DrawableUtils {
    companion object {
        fun View.setBackgroundTint(@ColorRes color: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || this is TintableBackgroundView)
                ViewCompat.setBackgroundTintList(this, context.getColorStateListCompat(color))
            else {
                val drawable = DrawableCompat.wrap(background).mutate()
                DrawableCompat.setTint(drawable, context.getColorCompat(color))
                //drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(view.getContext(), color), PorterDuff.Mode.SRC_IN));
                setBackgroundCompat(drawable)
            }
        }

        @SuppressLint("RestrictedApi")
        fun Drawable.setDrawableTintCompat(@ColorInt color: Int) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    mutate().setTintMode(PorterDuff.Mode.SRC_IN)
                    setTint(color)
                }
                this is TintAwareDrawable -> {
                    (mutate() as TintAwareDrawable).apply {
                        setTintMode(PorterDuff.Mode.SRC_IN)
                        setTint(color)
                    }
                }
                else -> mutate().colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)

            }
        }

        @SuppressLint("RestrictedApi")
        fun Drawable.setDrawableTintListCompat(colorStateList: ColorStateList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.mutate().setTintMode(PorterDuff.Mode.SRC_IN)
                this.setTintList(colorStateList)
            } else if (this is TintAwareDrawable) {
                (this.mutate() as TintAwareDrawable).setTintMode(PorterDuff.Mode.SRC_IN)
                (this as TintAwareDrawable).setTintList(colorStateList)
            } else if (!colorStateList.isStateful) {
                this.mutate().colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colorStateList.defaultColor, BlendModeCompat.SRC_IN)
            } else {
                DrawableCompat.wrap(this.mutate())
                DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
                DrawableCompat.setTintList(this, colorStateList)
                DrawableCompat.unwrap<Drawable>(this)
            }
        }

        fun getFillRadiusDefineSizeDrawable(@ColorInt color: Int, @Px width: Float, @Px height: Float, @Px radius: Float) =
            getFillRadiiDefineSizeDrawable(color, width, height, List(4) { radius }.toFloatArray())

        fun getFillRadiiDefineSizeDrawable(@ColorInt color: Int, @Px width: Float, @Px height: Float, @Size(value = 4) radii: FloatArray) =
            (GradientDrawable().mutate() as GradientDrawable).apply {
                shape = GradientDrawable.RECTANGLE
                setSize(width.roundToInt(), height.roundToInt())
                setColor(color)
                cornerRadii = transformInputRadii(radii)
            }.mutate()

        fun getFillRadiusDrawable(@ColorInt color: Int, @Px radius: Float) =
            getFillRadiiDrawable(color, List(4) { radius }.toFloatArray())

        fun getFillRadiiDrawable(@ColorInt color: Int, @Size(value = 4) radii: FloatArray) =
            (GradientDrawable().mutate() as GradientDrawable).apply {
                shape = GradientDrawable.RECTANGLE
                setColor(color)
                cornerRadii = transformInputRadii(radii)
            }.mutate()

        fun getStrokeRadiusDefineSizeDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, @Px width: Float, @Px height: Float, strokeWidth: Int, @Px radius: Float) =
            getStrokeRadiiDefineSizeDrawable(backgroundColor, strokeColor, width, height, strokeWidth, radii = List(4) { radius }.toFloatArray())

        fun getStrokeRadiusDefineSizeDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, @Px width: Float, @Px height: Float, strokeWidth: Int, @Px dashWidth: Float, @Px dashGap: Float, @Px radius: Float) =
            getStrokeRadiiDefineSizeDrawable(backgroundColor, strokeColor, width, height, strokeWidth, dashWidth, dashGap, List(4) { radius }.toFloatArray())

        fun getStrokeRadiiDefineSizeDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, @Px width: Float, @Px height: Float, strokeWidth: Int, @Px dashWidth: Float = 0f, @Px dashGap: Float = 0f, @Size(value = 4) radii: FloatArray) =
            (GradientDrawable().mutate() as GradientDrawable).apply {
                shape = GradientDrawable.RECTANGLE
                setSize(width.roundToInt(), height.roundToInt())
                setColor(backgroundColor)
                setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
                cornerRadii = transformInputRadii(radii)
            }.mutate()

        fun getStrokeRadiusDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, strokeWidth: Int, @Px radius: Float) =
            getStrokeRadiiDrawable(backgroundColor, strokeColor, strokeWidth, radii = List(4) { radius }.toFloatArray())

        fun getStrokeRadiusDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, strokeWidth: Int, @Px dashWidth: Float, @Px dashGap: Float, @Px radius: Float) =
            getStrokeRadiiDrawable(backgroundColor, strokeColor, strokeWidth, dashWidth, dashGap, List(4) { radius }.toFloatArray())

        fun getStrokeRadiiDrawable(@ColorInt backgroundColor: Int, @ColorInt strokeColor: Int, strokeWidth: Int, @Px dashWidth: Float = 0f, @Px dashGap: Float = 0f, @Size(value = 4) radii: FloatArray) =
            (GradientDrawable().mutate() as GradientDrawable).apply {
                shape = GradientDrawable.RECTANGLE
                setColor(backgroundColor)
                setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
                cornerRadii = transformInputRadii(radii)
            }.mutate()

        fun getRingDrawable(@ColorInt backgroundColor: Int, @Px radius: Float) =
            (GradientDrawable().mutate() as GradientDrawable).apply {
                shape = GradientDrawable.OVAL
                setSize(radius.toInt() * 2, radius.toInt() * 2)
                cornerRadius = radius
                setColor(backgroundColor)
                useLevel = false
            }.mutate()

        @Size(value = 8)
        fun transformInputRadii(@Size(value = 4) radii: FloatArray) =
            radii.flatMap { radius -> Collections.nCopies(2, radius) }.toFloatArray()

        fun getLinearGradientRadiusDrawable(orientation: GradientDrawable.Orientation, @ColorInt startColor: Int, @ColorInt endColor: Int, @Px radius: Float): Drawable {
            return getLinearGradientRadiiDrawable(orientation, startColor, endColor, List(4) { radius }.toFloatArray())
        }

        fun getLinearGradientRadiiDrawable(orientation: GradientDrawable.Orientation, @ColorInt startColor: Int, @ColorInt endColor: Int, @Size(4) radii: FloatArray): Drawable {
            val drawable = GradientDrawable(orientation, intArrayOf(startColor, endColor))
            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            drawable.cornerRadii = transformInputRadii(radii)
            return drawable.mutate()
        }

        fun getFillShadowRadiusDrawable(@ColorInt backgroundColor: Int, @ColorInt shadowColor: Int, @Size(4) shadowElevation: IntArray, @Px radius: Float): LayerDrawable {
            return getFillShadowRadiiDrawable(backgroundColor, shadowColor, shadowElevation, List(4) { radius }.toFloatArray())
        }

        fun getFillShadowRadiusDrawable(@ColorInt backgroundColor: Int, @ColorInt shadowColor: Int, @Px shadowWidth: Int, @Px radius: Float): LayerDrawable {
            return getFillShadowRadiiDrawable(backgroundColor, shadowColor, shadowWidth, List(4) { radius }.toFloatArray())
        }

        fun getFillShadowRadiiDrawable(@ColorInt backgroundColor: Int, @ColorInt shadowColor: Int, @Px shadowWidth: Int, @Size(4) radii: FloatArray): LayerDrawable {
            return getFillShadowRadiiDrawable(backgroundColor, shadowColor, List(4) { shadowWidth }.toIntArray(), radii)
        }

        fun getFillShadowRadiiDrawable(@ColorInt backgroundColor: Int, @ColorInt shadowColor: Int, @Size(4) shadowElevation: IntArray, @Size(4) radii: FloatArray): LayerDrawable {
            val colors = intArrayOf(backgroundColor, Color.WHITE)
            val shadow = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors).mutate() as GradientDrawable
            shadow.cornerRadii = transformInputRadii(radii)
            val back = GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(shadowColor, shadowColor)).mutate() as GradientDrawable
            back.cornerRadii = transformInputRadii(radii)
            shadowElevation.maxOrNull()?.let { back.setStroke(it, shadowColor) }
                ?.run { back.setStroke(shadowElevation[0], shadowColor) }
            val layerList = LayerDrawable(arrayOf<Drawable>(back, shadow)).mutate() as LayerDrawable
            layerList.setLayerInset(0, 0, 0, 0, 0)
            layerList.setLayerInset(1, shadowElevation[0], shadowElevation[1], shadowElevation[2], shadowElevation[3])
            return layerList
        }
    }
}