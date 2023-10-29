package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

class InvestEntity ( investorEmail:String,
                     creatorEmail:String,
                     budgetInvest:Double,
                     projectTitle:String,
                    estado:String): Parcelable {
   private var investorEmail:String
   private var creatorEmail:String
   private var budgetInvest:Double
   private var projectTitle:String
   private var estado:String
    constructor(parcel: Parcel):this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }
    init {
        this.investorEmail = investorEmail
        this.creatorEmail = creatorEmail
        this.budgetInvest = budgetInvest
        this.projectTitle = projectTitle
        this.estado = estado
    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(investorEmail)
        parcel.writeString(creatorEmail)
        parcel.writeDouble(budgetInvest)
        parcel.writeString(projectTitle)
        parcel.writeString(estado)
    }
    companion object CREATOR : Parcelable.Creator<InvestEntity> {
        override fun createFromParcel(parcel: Parcel): InvestEntity {
            return InvestEntity(parcel)
        }

        override fun newArray(size: Int): Array<InvestEntity?> {
            return arrayOfNulls(size)
        }
    }
 //region Setters
    fun setInvestorEmail(investorEmail: String) {
     this.investorEmail = investorEmail
    }
    fun setCreatorEmail(creatorEmail: String) {
     this.creatorEmail = creatorEmail
    }
    fun setBudgetInvest(budgetInvest: Double) {
        this.budgetInvest = budgetInvest
    }
    fun setProjectTitle(projectTitle: String) {
        this.projectTitle = projectTitle
    }
    fun setEstado(estado: String) {
        this.projectTitle = estado
    }
    //endregion

    //region Getters
    fun getInvestorEmail(): String {
        return this.investorEmail
    }
    fun getCreatorEmail(): String {
        return this.creatorEmail
    }
    fun getBudgetInvest(): Double {
        return this.budgetInvest
    }
    fun getProjectTitle(): String {
        return this.projectTitle
    }
    fun getEstado(): String {
        return this.estado
    }
}