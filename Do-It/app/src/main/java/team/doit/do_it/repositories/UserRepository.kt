package team.doit.do_it.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val collection = FirebaseFirestore.getInstance().collection("usuarios")

    fun getUser(userEmail: String): Task<DocumentSnapshot> {
        return collection.document(userEmail).get()
    }
}