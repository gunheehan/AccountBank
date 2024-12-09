import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.dao.DynamicTableDao

class PaymentRepository(
    private val staticTableRepository: StaticTableRepository,
    private val dynamicTableDao: DynamicTableDao
) {
    suspend fun addPaymentToDynamicTable(
        year: Int,
        month: Int,
        title: String,
        type: String,
        amount: Int,
        date: String
    ) {
        // 1. 연도와 월 확인
        staticTableRepository.ensureYearExists(year)
        staticTableRepository.ensureMonthExists(year, month)

        // 3. 테이블 존재 여부 확인 후 생성
        dynamicTableDao.createYearMonthTable(date)

        val payment = Payment(0,title,type,amount,date)
        // 4. 데이터 삽입
        dynamicTableDao.insertPayment(payment)
    }
}
