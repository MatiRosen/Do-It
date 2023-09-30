package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable


// TODO: Categoria es string o enum? Imagen es string?
class ProjectEntity (title: String, subtitle: String, description: String, category: String, image: String,
                     minBudget: Double, goal: Double) : Parcelable {

    private var title: String
    private var subtitle: String
    private var description: String
    private var category: String
    private var image: String
    private var minBudget: Double
    private var goal: Double

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
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
        this.subtitle = subtitle
        this.description = description
        this.category = category
        this.image = image
        this.minBudget = minBudget
        this.goal = goal
    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(image)
        parcel.writeDouble(minBudget)
        parcel.writeDouble(goal)
    }

    companion object CREATOR : Parcelable.Creator<ProjectEntity> {
        override fun createFromParcel(parcel: Parcel): ProjectEntity {
            return ProjectEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProjectEntity?> {
            return arrayOfNulls(size)
        }
    }

    //region Getters
    fun getTitle(): String {
        return this.title
    }

    fun getSubtitle(): String {
        return this.subtitle
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

    fun getGoal(): Double {
        return this.goal
    }
    //endregion

    override fun toString(): String {
        return "ProjectEntity(title='$title', subtitle='$subtitle', description='$description', category='$category', image='$image', minBudget=$minBudget, totalBudget=$goal)"
    }
}