package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

class UserEntity(firstName: String, surname: String, email: String, documentNumber: String,
                 birthDate: String, gender: String, telephoneNumber: String, address: String,
                 password: String) : Parcelable {

    private var firstName: String
    private var surname : String
    private var email : String
    private var documentNumber : String
    private var birthDate : String // TODO: Change to Date data type
    private var gender : String
    private var telephoneNumber : String
    private var address : String // TODO: Change to Address data type
    private var password : String

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    init {
        this.firstName = firstName
        this.surname = surname
        this.email = email
        this.documentNumber = documentNumber
        this.birthDate = birthDate
        this.gender = gender
        this.telephoneNumber = telephoneNumber
        this.address = address
        this.password = password
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(surname)
        parcel.writeString(email)
        parcel.writeString(documentNumber)
        parcel.writeString(gender)
        parcel.writeString(telephoneNumber)
        parcel.writeString(address)
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