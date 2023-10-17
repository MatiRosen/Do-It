package team.doit.do_it.entities

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import java.util.Date

class UserEntity(firstName: String, surname: String, email: String,
                 birthDate: String, gender: String, telephoneNumber: String, address: String, isPremium: Boolean) : Parcelable {

    private var firstName: String
    private var surname : String
    private var email : String
    private var birthDate : String // TODO: Change to Date data type
    private var gender : String
    private var telephoneNumber : String
    private var address : String // TODO: Change to Address data type
    private var isPremium : Boolean

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    init {
        this.firstName = firstName
        this.surname = surname
        this.email = email
        this.birthDate = birthDate
        this.gender = gender
        this.telephoneNumber = telephoneNumber
        this.address = address
        this.isPremium = isPremium
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(surname)
        parcel.writeString(email)
        parcel.writeSerializable(birthDate)
        parcel.writeString(gender)
        parcel.writeString(telephoneNumber)
        parcel.writeString(address)
        if (isPremium) parcel.writeByte(1) else parcel.writeByte(0)
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

    //region Getters
    fun getFirstName(): String {
        return firstName
    }

    fun getSurname(): String {
        return surname
    }

    fun getEmail(): String {
        return email
    }

    fun getBirthDate(): String {
        return birthDate
    }

    fun getGender(): String {
        return gender
    }

    fun getTelephoneNumber(): String {
        return telephoneNumber
    }

    fun getAddress(): String {
        return address
    }

    fun getIsPremium(): Boolean {
        return isPremium
    }
    //endregion
}