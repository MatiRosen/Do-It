package team.doit.do_it.extensions

import android.widget.TextView
import androidx.core.view.doOnPreDraw

// This extension function is used to set the max lines of a TextView to
// the number of completely visible lines
fun TextView.setMaxLinesForEllipsizing(ellipsize: Boolean) = doOnPreDraw {
    val numberOfCompletelyVisibleLines = (measuredHeight - paddingTop - paddingBottom) / lineHeight
    maxLines = numberOfCompletelyVisibleLines
    if (ellipsize) {
        this.ellipsize = android.text.TextUtils.TruncateAt.END
    }
}