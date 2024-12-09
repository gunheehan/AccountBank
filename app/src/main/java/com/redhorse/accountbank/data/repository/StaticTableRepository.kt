import com.redhorse.accountbank.data.dao.DynamicTableDao
import com.redhorse.accountbank.data.dao.StaticTableDao
import com.redhorse.accountbank.data.entity.MonthTable
import com.redhorse.accountbank.data.entity.YearTable

class StaticTableRepository(
    private val staticTableDao: StaticTableDao,
    private val dynamicTableDao: DynamicTableDao
) {
    suspend fun ensureYearExists(year: Int) {
        val exists = staticTableDao.yearExists(year) > 0
        if (!exists) {
            staticTableDao.insertYear(YearTable(year))
        }
    }

    suspend fun ensureMonthExists(year: Int, month: Int) {
        val exists = staticTableDao.monthExists(year, month) > 0
        if (!exists) {
            staticTableDao.insertMonth(MonthTable(year = year, month = month))
        }
    }

    suspend fun getAvailableYears(): List<YearTable> {
        return staticTableDao.getAllYears()
    }

    suspend fun getAvailableMonths(year: Int): List<MonthTable> {
        return staticTableDao.getMonthsByYear(year)
    }
}
