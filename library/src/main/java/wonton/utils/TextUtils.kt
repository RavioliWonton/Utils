package wonton.utils

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.*
import android.text.method.PasswordTransformationMethod
import android.text.style.*
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import org.joda.money.BigMoneyProvider
import org.joda.money.format.MoneyFormatterBuilder
import java.net.InetAddress
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern

private fun getCharSequence(
    text: CharSequence,
    @ColorInt color: Int,
    @Px size: Int,
    style: Typeface
): CharSequence =
    SpannableStringBuilder(text).apply {
        setSpan(AbsoluteSizeSpan(size), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(StyleSpan(style.style), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

fun CharSequence.replaceRangeSingle(start: Int, end: Int, single: Char) =
    buildString {
        this@replaceRangeSingle.forEachIndexed { index, c ->
            if (index in start..end) append(single) else append(c)
        }
    }

fun CharSequence.addSpace(number: Int = 1) =
    buildString {
        val space = buildString { repeat(number) { append(" ") } }
        this@addSpace.forEachIndexed { index, c ->
            append(if (index == 0) c else space + c)
        }
    }

fun CharSequence.isPassword() = all { Character.isUnicodeIdentifierPart(it) }

fun BigMoneyProvider.toMoneyText(): String = MoneyFormatterBuilder().appendCurrencySymbolLocalized()
    .appendAmountLocalized().toFormatter(Locale.CHINA).print(this)

fun CharSequence.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

@SuppressLint("InlinedApi")
fun TextView.setTextAlignmentCompat() {
    setTextAlignmentCompat(Gravity.NO_GRAVITY, View.TEXT_ALIGNMENT_GRAVITY)
}

@SuppressLint("InlinedApi")
fun TextView.setTextAlignmentCompat(@TextViewGravity gravity: Int) {
    setTextAlignmentCompat(gravity, View.TEXT_ALIGNMENT_GRAVITY)
}

fun TextView.setTextAlignmentCompat(
    @TextViewGravity gravity: Int,
    @TextAlignment alignment: Int
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setGravity(Gravity.getAbsoluteGravity(gravity, View.LAYOUT_DIRECTION_LOCALE))
        textAlignment = alignment
    } else {
        setGravity(GravityCompat.getAbsoluteGravity(gravity, ViewCompat.LAYOUT_DIRECTION_LOCALE))
        setText(buildSpannedString {
            append(text)
            if (alignment != View.TEXT_ALIGNMENT_INHERIT) {
                setSpan(
                    AlignmentSpan.Standard(
                    when (alignment) {
                        View.TEXT_ALIGNMENT_TEXT_START -> Layout.Alignment.ALIGN_NORMAL
                        View.TEXT_ALIGNMENT_TEXT_END -> Layout.Alignment.ALIGN_OPPOSITE
                        View.TEXT_ALIGNMENT_VIEW_START -> getLayoutAlignmentFormDirection(ViewCompat.getLayoutDirection(this@setTextAlignmentCompat), gravity, true)
                        View.TEXT_ALIGNMENT_VIEW_END -> getLayoutAlignmentFormDirection(ViewCompat.getLayoutDirection(this@setTextAlignmentCompat), gravity, false)
                        View.TEXT_ALIGNMENT_CENTER -> Layout.Alignment.ALIGN_CENTER
                        else -> getLayoutAlignmentFormGravity(gravity, true)
                    }
                ), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }, TextView.BufferType.SPANNABLE)
    }
}

private fun getLayoutAlignmentFormDirection(layoutDirection: Int, gravity: Int, mode: Boolean) =
    when (layoutDirection) {
        ViewCompat.LAYOUT_DIRECTION_LTR -> if (mode) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
        ViewCompat.LAYOUT_DIRECTION_RTL -> if (mode) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        else -> getLayoutAlignmentFormGravity(gravity, false)
    }

@SuppressLint("RtlHardcoded")
private fun getLayoutAlignmentFormGravity(@TextViewGravity gravity: Int, mode: Boolean) =
    when (GravityCompat.getAbsoluteGravity(gravity, ViewCompat.LAYOUT_DIRECTION_LOCALE)) {
        Gravity.RIGHT -> if (mode) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER
        else -> if (mode) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
    }

fun TextView.setCompoundDrawablesRelativeCompat(start: Drawable? = drawableStartCompat,
                                                top: Drawable? = drawableTopCompat,
                                                end: Drawable? = drawableEndCompat,
                                                bottom: Drawable? = drawableBottomCompat,
                                                withIntrinsicBounds: Boolean = false) =
    if (withIntrinsicBounds) TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, start, top, end, bottom)
    else TextViewCompat.setCompoundDrawablesRelative(this, start, top, end, bottom)

fun TextView.getCompoundDrawablesRelativeCompat(): Array<Drawable?> = TextViewCompat.getCompoundDrawablesRelative(this)

var TextView.drawableStartCompat: Drawable?
    get() = getCompoundDrawablesRelativeCompat()[0]
    set(value) = setCompoundDrawablesRelativeCompat(start = value, withIntrinsicBounds = value?.copyBounds()?.isEmpty?.not()
        ?: false)
var TextView.drawableTopCompat: Drawable?
    get() = getCompoundDrawablesRelativeCompat()[1]
    set(value) = setCompoundDrawablesRelativeCompat(top = value, withIntrinsicBounds = value?.copyBounds()?.isEmpty?.not()
        ?: false)
var TextView.drawableEndCompat: Drawable?
    get() = getCompoundDrawablesRelativeCompat()[2]
    set(value) = setCompoundDrawablesRelativeCompat(end = value, withIntrinsicBounds = value?.copyBounds()?.isEmpty?.not()
        ?: false)
var TextView.drawableBottomCompat: Drawable?
    get() = getCompoundDrawablesRelativeCompat()[3]
    set(value) = setCompoundDrawablesRelativeCompat(bottom = value, withIntrinsicBounds = value?.copyBounds()?.isEmpty?.not()
        ?: false)
var TextView.drawableTintList
    get() = TextViewCompat.getCompoundDrawableTintList(this)
    set(value) = TextViewCompat.setCompoundDrawableTintList(this, value)

fun EditText.setPasswordInput() {
    isLongClickable = false
    val emptyActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
        override fun onDestroyActionMode(mode: ActionMode?) = Unit
    }
    customSelectionActionModeCallback = emptyActionModeCallback
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        customInsertionActionModeCallback = emptyActionModeCallback
    filters = arrayOf(object : InputFilter {
        override fun filter(
            source: CharSequence, start: Int, end: Int,
            dest: Spanned, dstart: Int, dend: Int
        ) = Pattern.compile("[^\\x21-\\x7E]").matcher(source).replaceAll("")
    }, InputFilter.LengthFilter(14))
    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
    transformationMethod = PasswordTransformationMethod.getInstance()
}

fun String.toInetAddress(): InetAddress =
    InetAddress.getByAddress(split(".").map { it.toInt().toByte() }.toByteArray())

fun AppCompatTextView.setPreComputedText(text: CharSequence) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            textMetricsParams = PrecomputedText.Params.Builder(paint).setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
                .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL)
                .setTextDirection(TextDirectionHeuristics.LOCALE).build()
        }
        TextViewCompat.setPrecomputedText(this, PrecomputedTextCompat.create(text, textMetricsParamsCompat))
    } else {
        setTextFuture(PrecomputedTextCompat.getTextFuture(text, textMetricsParamsCompat, Executors.newSingleThreadExecutor()))
    }
}

class FontSpan(private val font: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint?) {
        tp?.typeface = font
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = font
    }
}

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR
)
@IntDef(
    Gravity.NO_GRAVITY,
    Gravity.CENTER,
    Gravity.FILL,
    Gravity.START,
    Gravity.END,
    Gravity.TOP,
    Gravity.BOTTOM,
    Gravity.CENTER_HORIZONTAL,
    Gravity.CENTER_VERTICAL,
    Gravity.FILL_VERTICAL,
    Gravity.FILL_HORIZONTAL
)
@Retention(AnnotationRetention.SOURCE)
private annotation class TextViewGravity

@SuppressLint("InlinedApi")
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR
)
@IntDef(
    View.TEXT_ALIGNMENT_INHERIT,
    View.TEXT_ALIGNMENT_GRAVITY,
    View.TEXT_ALIGNMENT_CENTER,
    View.TEXT_ALIGNMENT_TEXT_START,
    View.TEXT_ALIGNMENT_TEXT_END,
    View.TEXT_ALIGNMENT_VIEW_START,
    View.TEXT_ALIGNMENT_VIEW_END
)
@Retention(AnnotationRetention.SOURCE)
private annotation class TextAlignment