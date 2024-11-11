import com.redhorse.accountbank.data.Payment

object RegexUtils {
    // 금액 패턴 예시 (ex: "1,000원" 또는 "1000원" 형태)
    private val paymentPattern = """(?<=제목:)(.*?)(?=\s*금액)""".toRegex()
    private val amountPattern = """(?<=금액:)(\d+)(?=\s*원)""".toRegex()
    private val typePattern = """(?<=타입:)(income|expense)""".toRegex()
    // 가맹점 이름 추출 패턴 (ex: "스타벅스 강남점")
    private val merchantPattern = Regex("[가-힣\\s]+점")

    // 금액 추출 함수
    fun extractAmount(text: String): String? {
        return amountPattern.find(text)?.value
    }

    // 가맹점 추출 함수
    fun extractMerchant(text: String): String? {
        return merchantPattern.find(text)?.value
    }

    // 결제 관련 메시지 여부 확인 (간단히 "결제"라는 단어가 포함된 경우로 예시)
    fun isPaymentMessage(text: String): Boolean {
        return text.contains("결제")
    }

    // 메시지에서 결제 정보를 추출하여 Payment 객체를 반환
    fun parsePaymentInfo(message: String): Payment {
        val title = paymentPattern.find(message)?.value?.trim() ?: "Unknown Title"
        val amountString = amountPattern.find(message)?.value?.trim() ?: "0"
        val amount = amountString.toInt()
        val type = typePattern.find(message)?.value?.trim() ?: "expense"
        val date = "2024-11-04" // 예시: 날짜를 추출할 방법이 필요

        return Payment(title = title, type = type, amount = amount, date = date)
    }
}
