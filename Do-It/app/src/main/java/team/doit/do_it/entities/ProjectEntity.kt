package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

// TODO: Categoria es string o enum? Imagen es string?
data class ProjectEntity(
    val creatorEmail: String, var title: String, var subtitle: String, var description: String, var category: String, var image: String,
    var minBudget: Double, var goal: Double, var visitorsCount: Int, var followersCount: Int, val creationDate: Date, val followers : MutableList<String>,
    val comments: MutableList<Comment> ) : Parcelable {

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
        Date(parcel.readLong()),
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(Comment.CREATOR) ?: ArrayList<Comment>()
    )

    constructor() : this("", "", "", "", "", "", 0.0, 0.0, 0, 0, Date(), mutableListOf<String>(), mutableListOf<Comment>())

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
        parcel.writeStringList(followers)
    }

    companion object CREATOR : Parcelable.Creator<ProjectEntity> {
        override fun createFromParcel(parcel: Parcel): ProjectEntity {
            return ProjectEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProjectEntity?> {
            return arrayOfNulls(size)
        }
    }

    fun addFollower(email : String) {
        this.followers.add(email)
        this.followersCount = this.followersCount + 1
    }

    fun addVisitor() {
        this.visitorsCount = this.visitorsCount + 1
    }

    fun hasFollowers(): Boolean {
        return this.followersCount > 0
    }

    fun isFollowedBy(email: String): Boolean {
        return this.followers.contains(email)
    }

    fun removeFollower(email: String) {
        this.followers.remove(email)
        this.followersCount = this.followersCount - 1
    }

    fun addComment(comment: Comment) {
        this.comments.add(comment)
    }

    data class Comment(val userEmail: String = "", val commentText: String = "", val userName: String = "") : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(userEmail)
            parcel.writeString(userName)
            parcel.writeString(commentText)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Comment> {
            override fun createFromParcel(parcel: Parcel): Comment {
                return Comment(
                    parcel.readString() ?: "",
                    parcel.readString() ?: ""
                )
            }

            override fun newArray(size: Int): Array<Comment?> {
                return arrayOfNulls(size)
            }
        }
    }

}