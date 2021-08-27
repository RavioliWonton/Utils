package wonton.utils

import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString
import android.text.TextUtils

class ParcelableSpannableString(source: CharSequence?) : SpannableString(source), Parcelable {
    constructor(parcel: Parcel) : this(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel) as SpannableString)

    override fun writeToParcel(parcel: Parcel, flags: Int) = TextUtils.writeToParcel(this, parcel, flags)

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ParcelableSpannableString> {
        override fun createFromParcel(parcel: Parcel) = ParcelableSpannableString(parcel)
        override fun newArray(size: Int): Array<ParcelableSpannableString?> = arrayOfNulls(size)
    }
}

fun CharSequence.toParcelable() = ParcelableSpannableString(this)