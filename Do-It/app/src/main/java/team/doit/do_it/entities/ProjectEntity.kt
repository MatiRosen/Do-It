package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable


// TODO: Categoria es string o enum? Imagen es string?
class ProjectEntity (title: String, description: String, category: String, image: String,
                     minBudget: Double, totalBudget: Double) : Parcelable {

    private var title: String
    private var description: String
    private var category: String
    private var image: String
    private var minBudget: Double
    private var totalBudget: Double

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    init {
        this.title = title
        this.description = description
        this.category = category
        this.image = image
        this.minBudget = minBudget
        this.totalBudget = totalBudget
    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(image)
        parcel.writeDouble(minBudget)
        parcel.writeDouble(totalBudget)
    }

    companion object CREATOR : Parcelable.Creator<ProjectEntity> {
        override fun createFromParcel(parcel: Parcel): ProjectEntity {
            return ProjectEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProjectEntity?> {
            return arrayOfNulls(size)
        }
    }

    fun getTitle(): String {
        return this.title
    }

    fun getDescription(): String {
        return this.description
    }

    fun getCategory(): String {
        return this.category
    }

    fun getImage(): String {
        return this.image
    }

    fun getMinBudget(): Double {
        return this.minBudget
    }

    fun getTotalBudget(): Double {
        return this.totalBudget
    }
}