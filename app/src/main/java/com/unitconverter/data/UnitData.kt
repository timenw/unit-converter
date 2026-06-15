package com.unitconverter.data

/**
 * 单位换算数据
 * 所有单位都先转换为基准单位，再转换为目标单位
 */
data class Unit(
    val name: String,
    val symbol: String,
    val toBase: Double  // 乘以这个值得到基准单位
)

data class UnitCategory(
    val name: String,
    val icon: String,
    val units: List<Unit>
)

object UnitData {
    val categories = listOf(
        UnitCategory(
            name = "Length",
            icon = "📏",
            units = listOf(
                Unit("Millimeter", "mm", 0.001),
                Unit("Centimeter", "cm", 0.01),
                Unit("Meter", "m", 1.0),
                Unit("Kilometer", "km", 1000.0),
                Unit("Inch", "in", 0.0254),
                Unit("Foot", "ft", 0.3048),
                Unit("Yard", "yd", 0.9144),
                Unit("Mile", "mi", 1609.344)
            )
        ),
        UnitCategory(
            name = "Weight",
            icon = "⚖️",
            units = listOf(
                Unit("Milligram", "mg", 0.000001),
                Unit("Gram", "g", 0.001),
                Unit("Kilogram", "kg", 1.0),
                Unit("Ton", "t", 1000.0),
                Unit("Ounce", "oz", 0.0283495),
                Unit("Pound", "lb", 0.453592)
            )
        ),
        UnitCategory(
            name = "Area",
            icon = "📐",
            units = listOf(
                Unit("Sq. Millimeter", "mm²", 0.000001),
                Unit("Sq. Centimeter", "cm²", 0.0001),
                Unit("Sq. Meter", "m²", 1.0),
                Unit("Hectare", "ha", 10000.0),
                Unit("Sq. Kilometer", "km²", 1000000.0),
                Unit("Sq. Inch", "in²", 0.00064516),
                Unit("Sq. Foot", "ft²", 0.092903),
                Unit("Acre", "ac", 4046.86)
            )
        ),
        UnitCategory(
            name = "Volume",
            icon = "🧪",
            units = listOf(
                Unit("Milliliter", "ml", 0.001),
                Unit("Liter", "l", 1.0),
                Unit("Cubic Meter", "m³", 1000.0),
                Unit("Fluid Ounce (US)", "fl oz", 0.0295735),
                Unit("Cup (US)", "cup", 0.236588),
                Unit("Pint (US)", "pt", 0.473176),
                Unit("Quart (US)", "qt", 0.946353),
                Unit("Gallon (US)", "gal", 3.78541)
            )
        ),
        UnitCategory(
            name = "Temperature",
            icon = "🌡️",
            units = listOf(
                Unit("Celsius", "°C", 1.0),
                Unit("Fahrenheit", "°F", 1.0),
                Unit("Kelvin", "K", 1.0)
            )
        )
    )

    fun convert(amount: Double, from: Unit, to: Unit, categoryName: String): Double {
        return when (categoryName) {
            "Temperature" -> convertTemperature(amount, from.name, to.name)
            else -> {
                val baseValue = amount * from.toBase
                baseValue / to.toBase
            }
        }
    }

    private fun convertTemperature(amount: Double, from: String, to: String): Double {
        // 先转成 Celsius
        val celsius = when (from) {
            "Celsius" -> amount
            "Fahrenheit" -> (amount - 32) * 5.0 / 9.0
            "Kelvin" -> amount - 273.15
            else -> amount
        }
        // 再从 Celsius 转到目标
        return when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> celsius * 9.0 / 5.0 + 32
            "Kelvin" -> celsius + 273.15
            else -> celsius
        }
    }
}
