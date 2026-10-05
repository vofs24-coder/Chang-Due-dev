package com.changedue.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.changedue.app.ui.theme.ChangeDueTheme
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

enum class Field { TOTAL, RECEIVED }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChangeDueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChangeDueScreen()
                }
            }
        }
    }
}

@Composable
fun ChangeDueScreen() {
    val context = LocalContext.current

    var totalInput by remember { mutableStateOf("") }
    var receivedInput by remember { mutableStateOf("") }
    var activeField by remember { mutableStateOf(Field.TOTAL) }
    var cursorVisible by remember { mutableStateOf(true) }
    var cursorKey by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }

    LaunchedEffect(cursorKey) {
        cursorVisible = true
        while (true) {
            delay(500)
            cursorVisible = !cursorVisible
        }
    }

    fun resetCursor() { cursorKey++ }
    fun activeBuffer(): String = if (activeField == Field.TOTAL) totalInput else receivedInput
    fun setActiveBuffer(value: String) {
        if (activeField == Field.TOTAL) totalInput = value else receivedInput = value
    }

    fun appendDigit(d: String) {
        val sb = activeBuffer()
        val dot = sb.indexOf('.')
        if (dot >= 0 && sb.length - dot - 1 >= 2) return
        if (sb.length >= 15) return
        setActiveBuffer(sb + d)
        resetCursor()
    }

    fun appendDecimal() {
        val sb = activeBuffer()
        if (sb.contains('.')) return
        setActiveBuffer(if (sb.isEmpty()) "0." else "$sb.")
        resetCursor()
    }

    fun backspace() {
        val sb = activeBuffer()
        if (sb.isNotEmpty()) {
            setActiveBuffer(sb.substring(0, sb.length - 1))
            resetCursor()
        }
    }

    fun clearActive() {
        setActiveBuffer("")
        resetCursor()
    }

    fun switchField(f: Field) {
        activeField = f
        resetCursor()
    }

    fun nextField() {
        switchField(if (activeField == Field.TOTAL) Field.RECEIVED else Field.TOTAL)
    }

    val decimalSep = getDecimalSeparator()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.content_desc_menu),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_about)) },
                        onClick = { menuOpen = false; aboutOpen = true }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_share)) },
                        onClick = {
                            menuOpen = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Change Due — ${context.getString(R.string.app_name)}\n" +
                                    "https://play.google.com/store/apps/details?id=${context.packageName}"
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    context.getString(R.string.menu_share)
                                )
                            )
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF131C26))
                .border(1.dp, Color(0xFF1F2A38), RoundedCornerShape(18.dp))
                .padding(10.dp)
        ) {
            ScreenField(
                label = stringResource(R.string.total),
                labelColor = MaterialTheme.colorScheme.primary,
                valueColor = MaterialTheme.colorScheme.primary,
                text = buildFieldText(totalInput, Field.TOTAL, activeField, cursorVisible, decimalSep),
                isActive = activeField == Field.TOTAL,
                onClick = { switchField(Field.TOTAL) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ScreenField(
                label = stringResource(R.string.received),
                labelColor = MaterialTheme.colorScheme.tertiary,
                valueColor = MaterialTheme.colorScheme.tertiary,
                text = buildFieldText(receivedInput, Field.RECEIVED, activeField, cursorVisible, decimalSep),
                isActive = activeField == Field.RECEIVED,
                onClick = { switchField(Field.RECEIVED) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val total = parseCents(totalInput)
        val received = parseCents(receivedInput)
        val labelText: String
        val valueText: String
        val valueColor: Color

        if (total < 0 || received < 0) {
            labelText = stringResource(R.string.change_due)
            valueText = stringResource(R.string.empty)
            valueColor = MaterialTheme.colorScheme.secondary
        } else if (received >= total) {
            labelText = stringResource(R.string.change_due)
            valueText = formatCurrency(received - total)
            valueColor = MaterialTheme.colorScheme.secondary
        } else {
            labelText = stringResource(R.string.still_due)
            valueText = formatCurrency(total - received)
            valueColor = MaterialTheme.colorScheme.error
        }

        ScreenField(
            label = labelText,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            valueColor = valueColor,
            text = valueText,
            isActive = false,
            onClick = {},
            autoFit = true,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Keypad(
            decimalSeparator = decimalSep,
            onDigit = ::appendDigit,
            onDecimal = ::appendDecimal,
            onBackspace = ::backspace,
            onClear = ::clearActive,
            onNext = ::nextField,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A0E13))
                .border(1.dp, Color(0xFF232B3D), RoundedCornerShape(8.dp))
        )
    }

    if (aboutOpen) {
        val versionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

        AlertDialog(
            onDismissRequest = { aboutOpen = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Text(
                    text = stringResource(R.string.about_description) + "\n\n" +
                           stringResource(R.string.about_feature_offline) + "\n" +
                           stringResource(R.string.about_feature_locale) + "\n" +
                           stringResource(R.string.about_feature_simple) + "\n\n" +
                           stringResource(R.string.about_version) + " " + versionName
                )
            },
            confirmButton = {
                TextButton(onClick = { aboutOpen = false }) {
                    Text(stringResource(R.string.about_close))
                }
            }
        )
    }
}

@Composable
fun ScreenField(
    label: String,
    labelColor: Color,
    valueColor: Color,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    autoFit: Boolean = false
) {
    val borderColor = if (isActive) Color(0xFF7A8BA3) else Color(0xFF2A313B)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A2430))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = label,
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (autoFit) {
                AutoFitText(text = text, color = valueColor)
            } else {
                Text(
                    text = text,
                    color = valueColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AutoFitText(text: String, color: Color) {
    var fontSize by remember { mutableStateOf(36f) }
    LaunchedEffect(text) { fontSize = 36f }
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.fillMaxWidth(),
        onTextLayout = { layout ->
            if (layout.didOverflowWidth && fontSize > 10f) {
                fontSize -= 1f
            }
        }
    )
}

@Composable
fun Keypad(
    decimalSeparator: String,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            KeyButton("7", Modifier.weight(1f)) { onDigit("7") }
            KeyButton("8", Modifier.weight(1f)) { onDigit("8") }
            KeyButton("9", Modifier.weight(1f)) { onDigit("9") }
            KeyButton("⌫", Modifier.weight(1f), textColor = Color(0xFF8A7CFF)) { onBackspace() }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            KeyButton("4", Modifier.weight(1f)) { onDigit("4") }
            KeyButton("5", Modifier.weight(1f)) { onDigit("5") }
            KeyButton("6", Modifier.weight(1f)) { onDigit("6") }
            KeyIconButton(Icons.Default.Delete, Color(0xFFE74C3C), Modifier.weight(1f)) { onClear() }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            KeyButton("1", Modifier.weight(1f)) { onDigit("1") }
            KeyButton("2", Modifier.weight(1f)) { onDigit("2") }
            KeyButton("3", Modifier.weight(1f)) { onDigit("3") }
            KeyIconButton(Icons.Default.SwapVert, Color(0xFF8A7CFF), Modifier.weight(1f)) { onNext() }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            KeyButton("0", Modifier.weight(2f)) { onDigit("0") }
            KeyButton(decimalSeparator, Modifier.weight(1f)) { onDecimal() }
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFFFFFFF),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2E323A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KeyIconButton(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF181C22))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
    }
}

private fun getDecimalSeparator(): String =
    DecimalFormatSymbols(Locale.getDefault()).decimalSeparator.toString()

private fun getGroupingSeparator(): String =
    DecimalFormatSymbols(Locale.getDefault()).groupingSeparator.toString()

private fun buildFieldText(
    raw: String,
    field: Field,
    activeField: Field,
    cursorVisible: Boolean,
    decimalSep: String
): String {
    val isActive = (field == activeField)
    if (raw.isEmpty()) {
        return if (isActive) (if (cursorVisible) "|" else " ") else "—"
    }
    val dotIdx = raw.indexOf('.')
    val intPart: String
    val decPart: String?
    if (dotIdx < 0) {
        intPart = raw
        decPart = null
    } else {
        intPart = raw.substring(0, dotIdx)
        decPart = raw.substring(dotIdx + 1)
    }
    val sb = StringBuilder(groupDigits(intPart))
    if (decPart != null) sb.append(decimalSep).append(decPart)
    if (isActive && cursorVisible) sb.append("|")
    return sb.toString()
}

private fun groupDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val sep = getGroupingSeparator()
    val sb = StringBuilder()
    val len = digits.length
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) sb.append(sep)
        sb.append(digits[i])
    }
    return sb.toString()
}

private fun parseCents(s: String): Long {
    if (s.isEmpty()) return -1L
    return try {
        Math.round(s.toDouble() * 100.0)
    } catch (e: NumberFormatException) {
        -1L
    }
}

private fun formatCurrency(cents: Long): String {
    val locale = Locale.getDefault()
    val country = locale.country
    return try {
        val nf = NumberFormat.getCurrencyInstance(locale)
        val override = everydaySymbol(country)
        if (override != null && nf is DecimalFormat) {
            val dfs = nf.decimalFormatSymbols
            dfs.currencySymbol = override
            nf.decimalFormatSymbols = dfs
        }
        nf.format(cents / 100.0)
    } catch (e: Exception) {
        val nf = NumberFormat.getNumberInstance(locale)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        nf.format(cents / 100.0)
    }
}

private fun everydaySymbol(country: String): String? = when (country) {
    "US" -> "$"; "CA" -> "C$"; "MX" -> "$"; "BR" -> "R$"
    "AR" -> "$"; "CL" -> "$"; "CO" -> "$"; "PE" -> "S/"
    "UY" -> "\$U"; "PY" -> "₲"; "BO" -> "Bs"; "VE" -> "Bs"
    "EC" -> "$"; "DO" -> "RD$"; "JM" -> "J$"; "TT" -> "TT$"

    "GB" -> "£"; "IE" -> "€"; "FR" -> "€"; "DE" -> "€"
    "IT" -> "€"; "ES" -> "€"; "PT" -> "€"; "NL" -> "€"
    "BE" -> "€"; "AT" -> "€"; "GR" -> "€"; "FI" -> "€"
    "CH" -> "CHF"; "SE" -> "kr"; "NO" -> "kr"; "DK" -> "kr"
    "PL" -> "zł"; "CZ" -> "Kč"; "HU" -> "Ft"; "RO" -> "lei"
    "BG" -> "лв"; "RU" -> "₽"; "UA" -> "₴"; "TR" -> "₺"

    "MZ" -> "MT"; "AO" -> "Kz"; "ZA" -> "R"; "NG" -> "₦"
    "KE" -> "KSh"; "TZ" -> "TSh"; "UG" -> "USh"; "GH" -> "₵"
    "EG" -> "E£"; "MA" -> "DH"; "DZ" -> "DA"; "TN" -> "DT"
    "ET" -> "Br"; "CV" -> "CVE"; "ST" -> "Db"; "GW" -> "CFA"
    "SN" -> "CFA"; "CI" -> "CFA"; "CM" -> "FCFA"; "MU" -> "₨"
    "ZW" -> "Z\$"; "ZM" -> "ZK"; "MW" -> "MK"; "BW" -> "P"
    "NA" -> "N\$"

    "JP" -> "¥"; "CN" -> "¥"; "KR" -> "₩"; "TW" -> "NT$"
    "HK" -> "HK$"; "SG" -> "S$"; "IN" -> "₹"; "PK" -> "₨"
    "BD" -> "৳"; "LK" -> "Rs"; "NP" -> "₨"; "TH" -> "฿"
    "VN" -> "₫"; "ID" -> "Rp"; "MY" -> "RM"; "PH" -> "₱"
    "MM" -> "K"; "KH" -> "៛"; "LA" -> "₭"

    "SA" -> "﷼"; "AE" -> "د.إ"; "QA" -> "﷼"; "KW" -> "د.ك"
    "BH" -> ".د.ب"; "OM" -> "﷼"; "JO" -> "د.ا"; "LB" -> "ل.ل"
    "SY" -> "ل.س"; "IQ" -> "ع.د"; "IR" -> "﷼"; "IL" -> "₪"
    "YE" -> "﷼"

    "AU" -> "A\$"; "NZ" -> "NZ\$"; "FJ" -> "FJ\$"
    else -> null
}
