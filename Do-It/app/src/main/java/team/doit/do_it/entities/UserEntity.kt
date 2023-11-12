package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class UserEntity(
    var firstName: String, var surname: String, val email: String, var birthDate: Date,
    var gender: String, var telephoneNumber: String, var address: String, var isPremium: Boolean,
    var uuid: String, var fcmToken: String, var userImage : String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        Date(parcel.readLong()),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor() : this("", "", "", Date(), "", "", "", false, "", "", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(surname)
        parcel.writeString(email)
        parcel.writeLong(birthDate.time)
        parcel.writeString(gender)
        parcel.writeString(telephoneNumber)
        parcel.writeString(address)
        if (isPremium) parcel.writeByte(1) else parcel.writeByte(0)
        parcel.writeString(uuid)
        parcel.writeString(fcmToken)
        parcel.writeString(userImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserEntity> {
        override fun createFromParcel(parcel: Parcel): UserEntity {
            return UserEntity(parcel)
        }

        override fun newArray(size: Int): Array<UserEntity?> {
            return arrayOfNulls(size)
        }
    }
}