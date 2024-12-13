import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.redhorse.accountbank.R

class RegularlyInfoItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val titleView: TextView
    private val amountView: TextView
    private val descriptionView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_regularly_info_item, this, true)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        // View 연결
        titleView = findViewById(R.id.titleView)
        amountView = findViewById(R.id.amountView)
        descriptionView = findViewById(R.id.descriptionView)
    }

    fun setData(title: String, amount: String, description: String) {
        titleView.text = title
        amountView.text = amount
        descriptionView.text = description
    }
}
