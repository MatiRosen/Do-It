package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

data class ChatEntity(
    var userName: String,
    var userEmail: String,
    var userImage: String,
    var userUUID: String,
    var messages: MutableList<MessageEntity>,
    var lastMessageDate: Long,
    var isWaiting: Boolean) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(MessageEntity.CREATOR) ?: mutableListOf<MessageEntity>(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    // Este constructor es usado internamente por Firebase. No se debe eliminar.
    constructor() : this("", "", "", "", mutableListOf<MessageEntity>(), 0, false)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userName)
        parcel.writeString(userEmail)
        parcel.writeString(userImage)
        parcel.writeString(userUUID)
        parcel.writeTypedList(messages)
        parcel.writeLong(lastMessageDate)
        parcel.writeByte(if (isWaiting) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatEntity> {
        override fun createFromParcel(parcel: Parcel): ChatEntity {
            return ChatEntity(parcel)
        }

        override fun newArray(size: Int): Array<ChatEntity?> {
            return arrayOfNulls(size)
        }
    }

}