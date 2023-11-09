package team.doit.do_it.entities

import android.os.Parcel
import android.os.Parcelable

data class CommentEntity(val userEmail: String = "", val commentText: String = "", val userName: String = "",
    val userImage: String = "", val commentDate: String = "") :
    Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userEmail)
        parcel.writeString(userName)
        parcel.writeString(commentText)
        parcel.writeString(userImage)
        parcel.writeString(commentDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommentEntity> {
        override fun createFromParcel(parcel: Parcel): CommentEntity {
            return CommentEntity(
                parcel.readString() ?: "",
                parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<CommentEntity?> {
            return arrayOfNulls(size)
        }
    }
}