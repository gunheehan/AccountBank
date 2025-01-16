import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.formatCurrency

class RegularlyInfoItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val titleView: TextView
    private val amountView: TextView
    private val descriptionView: TextView
    private val editButton: ImageView
    private val deleteButton: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_regularly_info_item, this, true)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        titleView = findViewById(R.id.titleView)
        amountView = findViewById(R.id.amountView)
        descriptionView = findViewById(R.id.descriptionView)
        editButton = findViewById(R.id.editIcon)
        deleteButton = findViewById(R.id.deleteIcon)
    }

    fun setData(payment: Payment,
                onClickEdit: (Payment) -> Unit,
                onClickDelete: (Payment) -> Unit) {

        titleView.text = payment.title
        amountView.text = formatCurrency(payment.amount) + "원"
        descriptionView.text = "매월 ${payment.date}일"

        editButton.setOnClickListener(){
            onClickEdit.invoke(payment)
        }

        deleteButton.setOnClickListener(){
            onClickDelete.invoke(payment)
        }
    }
}
