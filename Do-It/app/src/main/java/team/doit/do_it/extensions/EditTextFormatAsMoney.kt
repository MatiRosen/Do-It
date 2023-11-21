package team.doit.do_it.extensions

import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Extensión de EditText para darle al texto formato de dinero
 */
fun EditText.formatAsMoney() {
    val editText = this
    var text: String? = null

    val decFormat: DecimalFormat = DecimalFormat.getInstance(Locale.getDefault()) as DecimalFormat
    val symbols: DecimalFormatSymbols = decFormat.decimalFormatSymbols
    val decimalSeparator = symbols.decimalSeparator
    val thousandSeparator = symbols.groupingSeparator

    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            text = s.toString()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.toString() != text) {
                editText.keyListener =
                    DigitsKeyListener.getInstance("0123456789$decimalSeparator")
                editText.removeTextChangedListener(this)
                val cleanString: String =
                    s.toString().replace("[$$thousandSeparator]".toRegex(), "")
                if (cleanString.contains("$decimalSeparator")) {
                    val split = cleanString.split("$decimalSeparator").toTypedArray()
                    val first = split[0]
                    val second = split[1]
                    val formatted: String =
                        first.reversed().chunked(3).joinToString("$thousandSeparator") { it }
                    val formattedText = "${formatted.reversed()}$decimalSeparator$second"
                    editText.setText(formattedText)
                } else {
                    val formatted: String =
                        cleanString.reversed().chunked(3).joinToString("$thousandSeparator") { it }
                    editText.setText(formatted.reversed())
                }
                editText.setSelection(editText.text.toString().length)
                editText.addTextChangedListener(this)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    })
}