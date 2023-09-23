package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

class UserEntity(name: String?, surname: String?, mail: String?, documentNumber: Int?,
                 birthdayDate: String?, gender: String?, telephoneNumber: Int?, direction: String?,
                 password: String?) : Parcelable {

    var name: String = ""
    var surname : String = ""
    var mail : String = ""
    var documentNumber : Int = 0
    var birthdayDate : String = ""
    var gender : String = ""
    var telephoneNumber : Int = 0
    var direction : String = ""
    var password : String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    )

    init {
        this.name = name!!
        this.surname = surname!!
        this.mail = mail!!
        this.documentNumber = documentNumber!!
        this.birthdayDate = birthdayDate!!
        this.gender = gender!!
        this.telephoneNumber = telephoneNumber!!
        this.direction = direction!!
        this.password = password!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(mail)
        parcel.writeInt(documentNumber)
        parcel.writeString(gender)
        parcel.writeInt(telephoneNumber)
        parcel.writeString(direction)
        parcel.writeString(password)
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