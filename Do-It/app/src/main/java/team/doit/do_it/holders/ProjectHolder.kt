package team.doit.do_it.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import team.doit.do_it.R
import team.doit.do_it.extensions.setMaxLinesForEllipsizing
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProjectHolder(view: View) : RecyclerView.ViewHolder(view){

    private var view: View

    init {
        this.view = view
    }

    fun setProjectTitle(projectTitle: String) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorTitle)
        txt.text = projectTitle
    }

    fun setProjectSubtitle(projectSubtitle: String) {
        val txt: TextView = view.findViewById(R.id.txtItemProjectCreatorSubtitle)
        txt.text = projectSubtitle

        txt.setMaxLinesForEllipsizing(true)
    }


    fun setProjectCategory(projectCategory: String) {
        val txt : TextView = view.findViewById(R.id.txtItemProjectCreatorCategory)
        txt.text = projectCategory
    }

    fun setProjectGoal(projectTotalBudget: Double) {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        decimalFormatSymbols.decimalSeparator = ','
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)

        val txt : TextView = view.findViewById(R.id.txtItemProjectCeatorTotalBudget)
        val budgetString = view.context.getString(R.string.currency) + decimalFormat.format(projectTotalBudget)
        txt.text = budgetString
    }

    fun setProjectImage(projectImage: String, projectCreatorEmail: String) {
        val imageView = view.findViewById<ImageView>(R.id.imgItemProjectCreator)

        val storageReference = FirebaseStorage.getInstance().reference
            .child("images/$projectCreatorEmail/projects/$projectImage")

        // TODO ver por que tira error!
        Glide.with(view.context)
            .load(storageReference)
            .placeholder(R.drawable.img_not_img)
            .error(R.drawable.img_not_img)
            .into(imageView)

    }

    fun getCardLayout(): View {
        return view.findViewById(R.id.cardViewHomeCreatorProject)
    }
}