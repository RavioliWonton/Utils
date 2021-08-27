@file:Suppress("DEPRECATION")

package wonton.utils

import android.content.res.Resources
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.SpannableStringBuilder
import android.util.TypedValue
import androidx.annotation.IntRange
import androidx.annotation.Px
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.joda.money.format.MoneyFormatterBuilder
import java.text.NumberFormat
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.roundToInt

@Px
fun Number.dp2px() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)

@Px
fun Number.sp2px() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, toFloat(), Resources.getSystem().displayMetrics)

fun Long.toStorageUnit() =
    when {
        this < 0 -> "错误"
        this in 0..1023 -> toString() + "B"
        this in 1024 until 1024 * 1024 -> String.format(
            Locale.CHINA,
            "%.2fK",
            toDouble().div(1024)
        )
        this in 1024 * 1024 until 1024 * 1024 * 1024 -> String.format(
            Locale.CHINA,
            "%.2fM",
            toDouble().div(1024 * 1024)
        )
        this >= 1024 * 1024 * 1024 -> String.format(
            Locale.CHINA,
            "%.2fG",
            toDouble().div(1024 * 1024 * 1024)
        )
        else -> "错误"
    }

@IntRange(from = 0x00, to = 0xFF)
fun Float.transformAlphaToInt() =
    if (this in 0.0..1.0) (0xFF * this).roundToInt() else this.toInt()

fun Long.UNIXTimetoLocalDateTime(): LocalDateTime = LocalDateTime.ofEpochSecond(this / 1000, (this % 1000 * 1000).toInt(), ZonedDateTime.now(
    ZoneId.of(Constants.CST)).offset)
fun LocalDateTime.toUNIXTime() = toEpochSecond(ZonedDateTime.now(ZoneId.of(Constants.CST)).offset) * 1000

fun CharSequence.checkPhoneNumber() =
    Pattern.matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[235-8]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|66\\d{2})\\d{6}$", this)

fun CharSequence.formatPhoneNumber(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        PhoneNumberUtils.formatNumber(trim { it <= ' ' }.toString(), Locale.CHINA.country)
    } else {
        val result = SpannableStringBuilder(trim { it <= ' ' })
        PhoneNumberUtils.formatNumber(result, PhoneNumberUtils.getFormatTypeForLocale(Locale.CHINA))
        result.toStringCompat()
    }

fun Duration.getHourMinuteSecond() =
    (if (toHours() == 0L) "" else String.format(Locale.CHINA, "%02d:", toHours())) +
            String.format(Locale.CHINA, "%02d:", toMinutePart()) +
            String.format(Locale.CHINA, "%02d", toSecondPart())

fun Duration.getHourMinuteChinese() =
    (if (toHours() == 0L) "" else String.format(Locale.CHINA, "%02d小时", toHours())) +
            String.format(Locale.CHINA, "%02d分钟", toMinutePart())

fun Duration.getHourMinuteSecondChinese() =
    (if (toHours() == 0L) "" else String.format(Locale.CHINA, "%02d小时", toHours())) +
            (if (toMinutes() == 0L) "" else String.format(Locale.CHINA, "%02d分钟", toMinutePart())) +
            String.format(Locale.CHINA, "%02d秒", toSecondPart())

fun Duration.getHourMinuteSecondChineseShort() =
    (if (toHours() == 0L) "" else String.format(Locale.CHINA, "%02d小时", toHours())) +
            (if (toMinutes() == 0L) "" else String.format(Locale.CHINA, "%02d分", toMinutePart())) +
            String.format(Locale.CHINA, "%02d秒", toSecondPart())

private fun Duration.toMinutePart() = toMinutes() - toHours() * Duration.ofHours(1).toMinutes()
private fun Duration.toSecondPart() = seconds - toMinutes() * Duration.ofMinutes(1).seconds

@OptIn(ExperimentalStdlibApi::class)
fun String.checkIDNumber(): Boolean {
    val pattern = Pattern.compile("^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$")
    if (!pattern.matcher(this).matches()) return false
    //TODO: 地区代码验证
    val part = pattern.split(this).toList()
    try {
        LocalDate.of(part[1].toInt(), part[2].toInt(), part[3].toInt())
    } catch (e: DateTimeException) {
        return false
    }
    val checkCode = (12 - (substring(0, 17)
        .mapIndexed { index, c -> c.digitToInt() * (2.0.pow(17 - index) % 11) }
        .sum() % 11)) % 11
    return part[4] == if (checkCode < 10) checkCode.toString() else "X"
}

@Deprecated(replaceWith = ReplaceWith("MoneyFormatterBuilder().print()"), message = "No need")
fun Long.getMoneyStringFromMinor(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        MoneyFormatterBuilder()
            .appendCurrencySymbolLocalized()
            .appendAmountLocalized().toFormatter(Locale.CHINA).print(
                Money.ofMinor(CurrencyUnit.of(Locale.CHINA), this))
        /*MonetaryFormats.getAmountFormat(AmountFormatQueryBuilder
            .of(Locale.CHINA).set(CurrencyStyle.SYMBOL).build())
            .format(FastMoney.ofMinor(
                    Monetary.getCurrency(Locale.CHINA), minor))*/
    } else NumberFormat.getCurrencyInstance(Locale.CHINA)
        .format(this / 100.0)

fun LocalDateTime.compareDateBeforeThisYear() = toLocalDate().isBefore(
    LocalDate.now(ZoneId.of(Constants.CST))
    .with(TemporalAdjusters.lastDayOfYear()))