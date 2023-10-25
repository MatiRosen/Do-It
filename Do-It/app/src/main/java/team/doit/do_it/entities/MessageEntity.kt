package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

data class MessageEntity(
    val message: String,
    val sender: String,
    val date: Long) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
    )

    constructor() : this("", "", 0)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(sender)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MessageEntity> {
        override fun createFromParcel(parcel: Parcel): MessageEntity {
            return MessageEntity(parcel)
        }

        override fun newArray(size: Int): Array<MessageEntity?> {
            return arrayOfNulls(size)
        }
    }

}
