import com.redhorse.accountbank.data.Payment
import java.time.LocalDate

object RegexUtils {
    // 금액 패턴 예시 (ex: "1,000원" 또는 "1000원" 형태)
    private val paymentPattern = """(?<=제목:)(.*?)(?=\s*금액)""".toRegex()
    private val amountPattern = """(\d{1,3}(?:,\d{3})*)원""".toRegex() // 금액 추출
    private val typePattern = """(?<=타입:)(income|expense)""".toRegex()
    // 가맹점 이름 추출 패턴 (ex: "스타벅스 강남점")
    private val merchantPattern = Regex("[가-힣\\s]+점") // 가맹점 이름 추출

    // 금액 추출 함수
    fun extractAmount(text: String): String? {
        return amountPattern.find(text)?.value
    }

    // 가맹점 추출 함수
    fun extractMerchant(text: String): String? {
        return merchantPattern.find(text)?.value
    }

    // 결제 관련 메시지 여부 확인 (결제, 일시불, 카드, 승인 등을 포함한 메시지 확인)
    fun isPaymentMessage(text: String): Boolean {
        val paymentKeywords = listOf("결제", "일시불", "카드", "승인")
        return paymentKeywords.any { text.contains(it) }
    }

    // 메시지에서 결제 정보를 추출하여 Payment 객체를 반환
    fun parsePaymentInfo(message: String): Payment {
        val title = extractMerchant(message) ?: "Unknown Merchant"
        val amountString = extractAmount(message)?.replace(",", "") ?: "0" // 금액에 ','가 포함된 경우 제거
        val amount = amountString.toInt()
        val type = if (isPaymentMessage(message)) {
            "income"
        } else {
            "expense"
        }
        val date = LocalDate.now().toString()

        return Payment(title = title, type = type, amount = amount, date = date)
    }
}
