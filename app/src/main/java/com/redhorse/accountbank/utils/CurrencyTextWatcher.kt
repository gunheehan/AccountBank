import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private var currentText = ""

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != currentText) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
            if (cleanString.isNotEmpty()) {
                val formatted = DecimalFormat("#,###").format(cleanString.toLong())
                currentText = formatted
                editText.setText(formatted)
                editText.setSelection(formatted.length)
            }

            editText.addTextChangedListener(this)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
