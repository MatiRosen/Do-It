package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable
import java.util.Date


// TODO: Categoria es string o enum? Imagen es string?
class ProjectEntity(
    creatorEmail: String, title: String, subtitle: String, description: String, category: String, image: String,
    minBudget: Double, goal: Double, visitorsCount: Int, followersCount: Int, creationDate: Date) : Parcelable {

    private var creatorEmail: String
    private var title: String
    private var subtitle: String
    private var description: String
    private var category: String
    private var image: String
    private var minBudget: Double
    private var goal: Double
    private var visitorsCount: Int
    private var followersCount: Int
    private var creationDate: Date

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong())
    ) {
    }

    init {
        this.creatorEmail = creatorEmail
        this.title = title
        this.subtitle = subtitle
        this.description = description
        this.category = category
        this.image = image
        this.minBudget = minBudget
        this.goal = goal
        this.visitorsCount = visitorsCount
        this.followersCount = followersCount
        this.creationDate = creationDate
    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(creatorEmail)
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(image)
        parcel.writeDouble(minBudget)
        parcel.writeDouble(goal)
        parcel.writeInt(visitorsCount)
        parcel.writeInt(followersCount)
        parcel.writeLong(creationDate.time)
    }

    companion object CREATOR : Parcelable.Creator<ProjectEntity> {
        override fun createFromParcel(parcel: Parcel): ProjectEntity {
            return ProjectEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProjectEntity?> {
            return arrayOfNulls(size)
        }
    }

    fun addFollower() {
        this.followersCount++
    }

    fun addVisitor() {
        this.visitorsCount++
    }

    //region Setters
    fun setCreatorEmail(creatorEmail: String) {
        this.creatorEmail = creatorEmail
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setSubtitle(subtitle: String) {
        this.subtitle = subtitle
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setCategory(category: String) {
        this.category = category
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun setMinBudget(minBudget: Double) {
        this.minBudget = minBudget
    }

    fun setGoal(goal: Double) {
        this.goal = goal
    }
    //endregion

    //region Getters
    fun getCreatorEmail(): String {
        return this.creatorEmail
    }
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

    fun getFollowersCount(): Int {
        return this.followersCount
    }

    fun getVisitorsCount(): Int {
        return this.visitorsCount
    }

    fun getCreationDate(): Date {
        return this.creationDate
    }

    fun hasFollowers(): Boolean {
        return this.followersCount > 0
    }
    //endregion

    override fun toString(): String {
        return "ProjectEntity(creatorEmail='$creatorEmail', title='$title', subtitle='$subtitle', description='$description', category='$category', image='$image', minBudget=$minBudget, goal=$goal, visitorsCount=$visitorsCount, followersCount=$followersCount, creationDate=$creationDate)"
    }
}