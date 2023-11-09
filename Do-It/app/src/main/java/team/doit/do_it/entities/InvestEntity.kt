package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable
import team.doit.do_it.enums.InvestStatus

data class InvestEntity (
    var creatorEmail : String,
    var investorEmail : String,
    var budgetInvest : Double,
    var projectID : String,
    var status : InvestStatus,
    var userName : String,
    var projectTitle : String): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        InvestStatus.from(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor() : this("","",0.0,"", InvestStatus.PENDING, "", "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(creatorEmail)
        parcel.writeString(investorEmail)
        parcel.writeDouble(budgetInvest)
        parcel.writeString(projectID)
        parcel.writeString(status.getDescription())
        parcel.writeString(userName)
        parcel.writeString(projectTitle)
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