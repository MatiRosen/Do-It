package team.doit.do_it.extensions

import android.widget.TextView
import androidx.core.view.doOnPreDraw

fun TextView.setMaxLinesForEllipsize(ellipsize: Boolean) = doOnPreDraw {
    val numberOfCompletelyVisibleLines = (measuredHeight - paddingTop - paddingBottom) / lineHeight
    maxLines = numberOfCompletelyVisibleLines
    if (ellipsize) {
        this.ellipsize = android.text.TextUtils.TruncateAt.END
    }
}