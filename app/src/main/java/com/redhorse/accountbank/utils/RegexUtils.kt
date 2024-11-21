import android.util.Log
import com.redhorse.accountbank.data.Payment
import java.time.LocalDate

object RegexUtils {
    // 금액 패턴 예시 (ex: "1,000원" 또는 "1000원" 형태)
    private val amountPattern = """(\d{1,3}(?:,\d{3})*)원""".toRegex() // 금액 추출 패턴
    private val merchantPattern = Regex("[가-힣a-zA-Z\\*]+") // 가맹점 이름 추출

    // 금액 추출 함수
    fun extractAmount(text: String): String? {
        return amountPattern.find(text)?.value?.replace(",", "")?.replace("원", "")
    }

    fun extractMerchant(text: String): String {
        // 가맹점명은 결제 정보의 마지막 부분에 있을 확률이 높지만, 누적 금액처럼 다른 데이터가 섞여 있을 수 있음
        val lines = text.split("\n")

        // "누적"이 들어간 라인은 제외하고 가맹점명 찾기
        val merchantLine = lines.firstOrNull {
            merchantPattern.containsMatchIn(it) && !it.contains("누적") && !it.contains("승인")
                    && !it.contains("일시불") && !it.contains("할부") && !it.contains("취소")
                    && !it.contains("*")
        }

        return merchantLine?.trim() ?: "미식별"
    }

    // 결제 관련 메시지 여부 확인 (결제, 일시불, 카드, 승인 등을 포함한 메시지 확인)
    fun isPaymentMessage(text: String): Boolean {
        val paymentKeywords = listOf("결제", "일시불", "송금", "승인")
        return paymentKeywords.any { text.contains(it) }
    }

    // 메시지에서 결제 정보를 추출하여 Payment 객체를 반환
    fun parsePaymentInfo(message: String, date: String): Payment {
        val title = extractMerchant(message) ?: "미식별"
        val amountString = extractAmount(message) ?: "0" // 금액이 없는 경우 기본값 0
        val amount = amountString.toIntOrNull() ?: 0
        val type = if (isPaymentMessage(message)) {
            "expense"
        } else {
            "income"
        }

        return Payment(title = title, type = type, amount = amount, date = date)
    }
}
