import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBeanBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.absoluteValue

val myAccount1 = "32 2490 1044 0000 4200 6629 8566"
val myAccount2 = "63 2490 1044 0000 4200 4249 7700"

fun parseDate(s: String): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    return LocalDate.parse(s, formatter)
}

fun parseDouble(str: String) =
        NumberFormat.getInstance(Locale.forLanguageTag("pl")).parse(str).toDouble()

data class Entry(
        @CsvBindByPosition(position = 0)
        var whenStr: String = "",
        @CsvBindByPosition(position = 1)
        var c1: String = "",
        @CsvBindByPosition(position = 2)
        var from: String = "",
        @CsvBindByPosition(position = 3)
        var to: String = "",
        @CsvBindByPosition(position = 4)
        var title: String = "",
        @CsvBindByPosition(position = 5)
        var c5: String = "",
        @CsvBindByPosition(position = 6)
        var c6: String = "",
        @CsvBindByPosition(position = 7)
        var amountStr: String = "",
        @CsvBindByPosition(position = 8)
        var c8: String = "",
        @CsvBindByPosition(position = 9)
        var fromAccount: String = "",
        @CsvBindByPosition(position = 10)
        var toAccount: String = ""

) {
    val whenDate: LocalDate
        get() = parseDate(whenStr)

    val amount: Double
        get() = parseDouble(amountStr)
}

fun List<Entry>.sumByAmount(): Double = this.sumByDouble { it.amount }


fun main() {
//    val path = "/Users/kuba/Downloads/Historia_Operacji_2019-07-05_21-28-21.csv"
    val path = "/Users/kuba/Downloads/Historia_Operacji_2019-11-07_11-39-28.csv"
    val reader = Files.newBufferedReader(Paths.get(path))

//    print(reader.readLine())

    val csv = CsvToBeanBuilder<Entry>(reader)
            .withSkipLines(1)
            .withSeparator(';')
            .withType(Entry::class.java)
            .withIgnoreQuotations(true)
            .build()
            .toList()

//            .filterNot {
//                val fromLc = it.from.toLowerCase()
//                fromLc.matches(Regex("jakub.* trzebiatowski")) && fromLc.matches(Regex("jakub.* trzebiatowski"))
//            }

            .filterNot { setOf(it.fromAccount, it.toAccount) == setOf(myAccount1, myAccount2) }

            .filter { it.whenDate.year == 2019 }

//            .filter { it.whenDate.month == Month.AUGUST && it.whenDate.year == 2019 }


    val oldest = csv.sortedBy { it.whenDate }.first()
    val sorted = csv.sortedByDescending { it.amount.absoluteValue }
    val sum = sorted.sumByAmount()
    val biggestOutcomes = csv.sortedBy { it.amount }
    val biggestOutcomesAmounts = biggestOutcomes.map { it.amount }


    val biggestIncomes = csv.sortedByDescending { it.amount }

//    val lastMonth = csv.filter { it.whenDate.month == Month.JUNE && it.whenDate.year == 2019 }

    val incomes = csv.filter { it.amount > 0.0 }.sortedByDescending { it.amount }
    val outcomes = csv.filter { it.amount < 0.0 }.sortedBy { it.amount }.drop(1) // laser; USD


    val sumIn = incomes.sumByAmount()
    val sumOut = outcomes.sumByAmount()
    val gross = sumIn + sumOut

    fun topN(n: Int) =
            outcomes.take(n).sumByAmount()

    val sumOutTop10 = topN(10)
    val sumOutTop20 = topN(20)
    val sumOutTop100 = topN(100)

    val toSara = outcomes.filter { it.to.toLowerCase().contains("sara") }

    val toSaraSum = toSara.sumByAmount()
    val tos = outcomes.map { it.to }.toSet()

    data class A(val to: String, val count: Int, val sum: Double)

    val stats = tos.map { to ->
        val transactions = outcomes.filter { it.to == to }
        val count = transactions.size
        val sum = transactions.sumByAmount()
        A(to, count, sum)
    }.sortedBy { it.sum }

    val unknowns = outcomes.filter { it.to == "" }


    println()
}
