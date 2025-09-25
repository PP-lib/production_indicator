package com.example.productionindicator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productionindicator.data.AppDatabase
import com.example.productionindicator.data.ProductionRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.productionindicator.data.ProductionRecord

class MainViewModel(private val repo: ProductionRepository): ViewModel() {
    var operatorId by mutableStateOf<String?>(null)
        private set
    var itemCode by mutableStateOf("")
    var quantityInput by mutableStateOf("")
    var recent by mutableStateOf<List<ProductionRecord>>(emptyList())
    var total by mutableStateOf(0)
    var message by mutableStateOf("")

    fun selectOperator(op: String) { operatorId = op }
    fun setItem(code: String) { itemCode = code }
    fun appendQuantity(digit: String) { quantityInput = (quantityInput + digit).take(6) }
    fun clearQuantity() { quantityInput = "" }

    fun submit() {
        val op = operatorId ?: return
        val qty = quantityInput.toIntOrNull() ?: return
        val code = itemCode
        viewModelScope.launch {
            repo.record(op, code, qty)
            message = "記録しました: $op $code $qty"
            clearQuantity()
            itemCode = ""
            refresh()
        }
    }

    fun refresh() { viewModelScope.launch { recent = repo.recent(); total = repo.total() } }
}

@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val vm = remember { MainViewModel(ProductionRepository(db.productionRecordDao())) }
    LaunchedEffect(Unit) { vm.refresh() }
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "operator") {
        composable("operator") { OperatorSelectScreen(onSelect = { vm.selectOperator(it); nav.navigate("item") }) }
        composable("item") { ItemInputScreen(vm = vm, toQty = { nav.navigate("qty") }, toSummary = { nav.navigate("summary") }) }
        composable("qty") { QuantityInputScreen(vm = vm, toSummary = { nav.navigate("summary") }) }
        composable("summary") { SummaryScreen(vm = vm, backToItem = { nav.navigate("item") }) }
    }
}

@Composable
fun OperatorSelectScreen(onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("オペレーター選択 (NFCエミュ)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Row { listOf("op1","op2","op3").forEach { op -> Button(onClick = { onSelect(op) }, Modifier.padding(4.dp)) { Text(op) } } }
    }
}

@Composable
fun ItemInputScreen(vm: MainViewModel, toQty: () -> Unit, toSummary: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
        Text("アイテム入力 (バーコードエミュ)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.itemCode, onValueChange = { vm.setItem(it) }, label = { Text("Item Code") })
        Spacer(Modifier.height(8.dp))
        Row { Button(onClick = { vm.setItem("ITEM123") }) { Text("ITEM123") }; Spacer(Modifier.width(8.dp)); Button(onClick = { vm.setItem("ITEM999") }) { Text("ITEM999") } }
        Spacer(Modifier.height(16.dp))
        Button(onClick = toQty, enabled = vm.itemCode.isNotBlank()) { Text("数量入力へ") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = toSummary) { Text("サマリへ") }
    }
}

@Composable
fun QuantityInputScreen(vm: MainViewModel, toSummary: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("数量入力", style = MaterialTheme.typography.titleMedium)
        Text(text = vm.quantityInput.ifBlank { "0" }, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        NumberPad(onDigit = { vm.appendQuantity(it) }, onClear = { vm.clearQuantity() }, onEnter = { vm.submit(); toSummary() })
    }
}

@Composable
fun NumberPad(onDigit: (String) -> Unit, onClear: () -> Unit, onEnter: () -> Unit) {
    val rows = listOf(
        listOf("1","2","3"),
        listOf("4","5","6"),
        listOf("7","8","9"),
        listOf("C","0","OK")
    )
    Column { rows.forEach { r ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            r.forEach { label ->
                Button(modifier = Modifier.weight(1f).padding(4.dp), onClick = {
                    when(label) {
                        "C" -> onClear()
                        "OK" -> onEnter()
                        else -> onDigit(label)
                    }
                }) { Text(label, textAlign = TextAlign.Center) }
            }
        }
    } }

@Composable
fun SummaryScreen(vm: MainViewModel, backToItem: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("サマリー", style = MaterialTheme.typography.titleMedium)
        Text("合計数量: ${vm.total}")
        Text(vm.message, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(vm.recent.size) { idx ->
                val r = vm.recent[idx]
                Text("#${r.id} ${r.operatorId} ${r.itemCode} x${r.quantity}")
            }
        }
        Button(onClick = { vm.refresh() }) { Text("更新") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = backToItem) { Text("戻る") }
    }
}
