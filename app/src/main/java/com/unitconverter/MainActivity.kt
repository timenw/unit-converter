package com.unitconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unitconverter.billing.BillingManager
import com.unitconverter.data.CurrencyData
import com.unitconverter.data.UnitCategory
import com.unitconverter.data.UnitData
import com.unitconverter.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(application)
        billingManager.startConnection()

        setContent {
            MaterialTheme {
                UnitConverterApp(billingManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterApp(billingManager: BillingManager) {
    var isPremium by remember { mutableStateOf(billingManager.isPremium) }
    
    // Listen for premium changes
    billingManager.setPremiumCallback { premium ->
        isPremium = premium
    }
    
    var selectedCategory by remember { mutableStateOf("currency") }

    val categories = listOf(
        CategoryItem("Currency", Icons.Default.CurrencyExchange, "currency"),
        CategoryItem("Length", Icons.Default.Straighten, "length"),
        CategoryItem("Weight", Icons.Default.Scale, "weight"),
        CategoryItem("Area", Icons.Default.SquareFoot, "area"),
        CategoryItem("Volume", Icons.Default.LocalDrink, "volume"),
        CategoryItem("Temperature", Icons.Default.Thermostat, "temperature"),
        CategoryItem("Calculator", Icons.Default.Calculate, "calculator")
    )

    val activity = LocalContext.current as? MainActivity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter", color = White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue700)
            )
        },
        bottomBar = {
            if (!isPremium) {
                Surface(
                    color = Orange500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activity?.let {
                                billingManager.launchPurchaseFlow(it)
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        "🚫 Remove Ads - $0.99",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Surface(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "✅ Premium Active - No Ads",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Grey100)
        ) {
            // Category tabs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(4).forEach { cat ->
                            CategoryChip(
                                name = cat.name,
                                icon = cat.icon,
                                selected = selectedCategory == cat.route,
                                onClick = { selectedCategory = cat.route },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(4).forEach { cat ->
                            CategoryChip(
                                name = cat.name,
                                icon = cat.icon,
                                selected = selectedCategory == cat.route,
                                onClick = { selectedCategory = cat.route },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Content
            when (selectedCategory) {
                "currency" -> CurrencyScreen()
                "length" -> UnitScreen(UnitData.categories[0])
                "weight" -> UnitScreen(UnitData.categories[1])
                "area" -> UnitScreen(UnitData.categories[2])
                "volume" -> UnitScreen(UnitData.categories[3])
                "temperature" -> UnitScreen(UnitData.categories[4])
                "calculator" -> CalculatorScreen()
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (selected) Blue700 else White,
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = name,
                tint = if (selected) White else Grey600,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                name,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) White else Grey600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen() {
    val currencies = CurrencyData.currencies
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1) }
    var amount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    fun convert() {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val res = CurrencyData.convert(amt, currencies[fromIndex], currencies[toIndex])
        result = String.format("%.2f", res)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // From
        Text("From", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ExposedDropdownMenuBox(
            expanded = showFromDropdown,
            onExpandedChange = { showFromDropdown = it }
        ) {
            OutlinedTextField(
                value = "${currencies[fromIndex].symbol} ${currencies[fromIndex].code} - ${currencies[fromIndex].name}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFromDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showFromDropdown,
                onDismissRequest = { showFromDropdown = false }
            ) {
                currencies.forEachIndexed { index, currency ->
                    DropdownMenuItem(
                        text = { Text("${currency.symbol} ${currency.code} - ${currency.name}") },
                        onClick = {
                            fromIndex = index
                            showFromDropdown = false
                            convert()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                convert()
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // To
        Text("To", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ExposedDropdownMenuBox(
            expanded = showToDropdown,
            onExpandedChange = { showToDropdown = it }
        ) {
            OutlinedTextField(
                value = "${currencies[toIndex].symbol} ${currencies[toIndex].code} - ${currencies[toIndex].name}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showToDropdown,
                onDismissRequest = { showToDropdown = false }
            ) {
                currencies.forEachIndexed { index, currency ->
                    DropdownMenuItem(
                        text = { Text("${currency.symbol} ${currency.code} - ${currency.name}") },
                        onClick = {
                            toIndex = index
                            showToDropdown = false
                            convert()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Blue700)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Result", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    "${currencies[toIndex].symbol} $result",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "1 ${currencies[fromIndex].code} = ${String.format("%.4f", CurrencyData.convert(1.0, currencies[fromIndex], currencies[toIndex]))} ${currencies[toIndex].code}",
                    color = White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitScreen(category: UnitCategory) {
    val units = category.units
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1) }
    var amount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    fun convert() {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val res = UnitData.convert(amt, units[fromIndex], units[toIndex], category.name)
        result = String.format("%.6f", res).trimEnd('0').trimEnd('.')
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("${category.icon} ${category.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("From", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ExposedDropdownMenuBox(
            expanded = showFromDropdown,
            onExpandedChange = { showFromDropdown = it }
        ) {
            OutlinedTextField(
                value = "${units[fromIndex].name} (${units[fromIndex].symbol})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFromDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = showFromDropdown, onDismissRequest = { showFromDropdown = false }) {
                units.forEachIndexed { index, unit ->
                    DropdownMenuItem(
                        text = { Text("${unit.name} (${unit.symbol})") },
                        onClick = { fromIndex = index; showFromDropdown = false; convert() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it; convert() },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("To", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ExposedDropdownMenuBox(
            expanded = showToDropdown,
            onExpandedChange = { showToDropdown = it }
        ) {
            OutlinedTextField(
                value = "${units[toIndex].name} (${units[toIndex].symbol})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = showToDropdown, onDismissRequest = { showToDropdown = false }) {
                units.forEachIndexed { index, unit ->
                    DropdownMenuItem(
                        text = { Text("${unit.name} (${unit.symbol})") },
                        onClick = { toIndex = index; showToDropdown = false; convert() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Blue700)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Result", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    "$result ${units[toIndex].symbol}",
                    color = White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableDoubleStateOf(0.0) }
    var operator by remember { mutableStateOf("") }
    var waitingForSecond by remember { mutableStateOf(false) }

    fun onNumber(num: String) {
        if (waitingForSecond) {
            display = num
            waitingForSecond = false
        } else {
            display = if (display == "0") num else display + num
        }
    }

    fun onOp(op: String) {
        firstOperand = display.toDoubleOrNull() ?: 0.0
        operator = op
        waitingForSecond = true
    }

    fun onEquals() {
        val second = display.toDoubleOrNull() ?: 0.0
        val result = when (operator) {
            "+" -> firstOperand + second
            "-" -> firstOperand - second
            "×" -> firstOperand * second
            "÷" -> if (second != 0.0) firstOperand / second else 0.0
            else -> second
        }
        display = String.format("%.8f", result).trimEnd('0').trimEnd('.')
        operator = ""
    }

    fun onClear() {
        display = "0"
        firstOperand = 0.0
        operator = ""
        waitingForSecond = false
    }

    fun onDot() {
        if (!display.contains(".")) display += "."
    }

    fun onPercent() {
        val value = display.toDoubleOrNull() ?: 0.0
        display = String.format("%.6f", value / 100).trimEnd('0').trimEnd('.')
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Grey900)
        ) {
            Text(
                text = display,
                color = White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { btn ->
                    Button(
                        onClick = {
                            when (btn) {
                                "C" -> onClear()
                                "±" -> {
                                    val v = display.toDoubleOrNull() ?: 0.0
                                    display = String.format("%.6f", -v).trimEnd('0').trimEnd('.')
                                }
                                "%" -> onPercent()
                                "." -> onDot()
                                "=" -> onEquals()
                                "+", "-", "×", "÷" -> onOp(btn)
                                else -> onNumber(btn)
                            }
                        },
                        modifier = Modifier
                            .weight(if (btn == "0") 2f else 1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (btn) {
                                "C", "±", "%" -> Grey600
                                "÷", "×", "-", "+", "=" -> Orange500
                                else -> Blue700
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(btn, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
