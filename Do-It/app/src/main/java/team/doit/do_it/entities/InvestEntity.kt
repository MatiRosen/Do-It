package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable
import team.doit.do_it.enums.InvestStatus
import java.util.Date

data class InvestEntity (
    var uuid : String,
    var creatorEmail : String,
    var investorEmail : String,
    var budgetInvest : Double,
    var projectID : String,
    var status : InvestStatus,
    var userName : String,
    var projectTitle : String,
    var date : Date): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        InvestStatus.from(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        Date(parcel.readLong())
    )

    // Este constructor es usado internamente por Firebase. No se debe eliminar.
    constructor() : this("","","",0.0,"", InvestStatus.PENDING, "", "", Date())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uuid)
        parcel.writeString(creatorEmail)
        parcel.writeString(investorEmail)
        parcel.writeDouble(budgetInvest)
        parcel.writeString(projectID)
        parcel.writeString(status.getDescription())
        parcel.writeString(userName)
        parcel.writeString(projectTitle)
        parcel.writeLong(date.time)
    }
    
    companion object CREATOR : Parcelable.Creator<InvestEntity> {
        override fun createFromParcel(parcel: Parcel): InvestEntity {
            return InvestEntity(parcel)
        }

        override fun newArray(size: Int): Array<InvestEntity?> {
            return arrayOfNulls(size)
        }
    }
}