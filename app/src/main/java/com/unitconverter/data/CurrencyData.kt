package com.unitconverter.data

/**
 * 非洲货币汇率数据（相对于 USD）
 * 汇率需要定期更新
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToUsd: Double
)

object CurrencyData {
    val currencies = listOf(
        Currency("USD", "US Dollar", "$", 1.0),
        Currency("NGN", "Nigerian Naira", "₦", 1550.0),
        Currency("KES", "Kenyan Shilling", "KSh", 153.0),
        Currency("GHS", "Ghanaian Cedi", "GH₵", 15.5),
        Currency("ZAR", "South African Rand", "R", 18.5),
        Currency("EGP", "Egyptian Pound", "E£", 48.5),
        Currency("ETB", "Ethiopian Br", "Br", 120.0),
        Currency("TZS", "Tanzanian Shilling", "TSh", 2520.0),
        Currency("UGX", "Ugandan Shilling", "USh", 3700.0),
        Currency("MAD", "Moroccan Dirham", "MAD", 10.0),
        Currency("XOF", "CFA Franc BCEAO", "CFA", 605.0),
        Currency("XAF", "CFA Franc BEAC", "FCFA", 605.0),
        Currency("RWF", "Rwandan Franc", "RF", 1280.0),
        Currency("ZMW", "Zambian Kwacha", "ZK", 26.5),
        Currency("BWP", "Botswana Pula", "P", 13.8),
        Currency("MZN", "Mozambican Metical", "MT", 63.5),
        Currency("AOA", "Angolan Kwanza", "Kz", 850.0),
        Currency("SDG", "Sudanese Pound", "SDG", 600.0),
        Currency("SSP", "South Sudanese Pound", "SSP", 1000.0),
        Currency("CDF", "Congolese Franc", "FC", 2700.0)
    )

    fun convert(amount: Double, from: Currency, to: Currency): Double {
        val usdAmount = amount / from.rateToUsd
        return usdAmount * to.rateToUsd
    }
}
