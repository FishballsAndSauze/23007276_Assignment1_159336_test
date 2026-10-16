package com.example.a23007276_assignment1_159336
// Archibald Po
//dhairya rathod
//23007276
// hello my name is arno
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dialler.ui.theme.DiallerTheme


/**
 * Simple dialler application.
 *  Places call with Intent.ACTION_CALL and "tel:" + number
 *  Requests CALL_PHONE at runtime
 *  Accepts ACTION_DIAL (tel: scheme) and shows the number
 *  Works in landscape and on different screen sizes
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialNumber = extractNumberFromIntent(intent)

        setContent {
            DiallerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiallerScreen(initialNumber = initialNumber)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val number = extractNumberFromIntent(intent)
        setContent {
            DiallerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiallerScreen(initialNumber = number)
                }
            }
        }
    }

    private fun extractNumberFromIntent(intent: Intent?): String {
        if (intent == null) return ""
        val data = intent.data ?: return ""
        if (data.scheme.equals("tel", ignoreCase = true)) {
            return data.schemeSpecificPart ?: ""
        }
        return ""
    }
}

@Composable
fun DiallerScreen(initialNumber: String = "") {
    var number by rememberSaveable { mutableStateOf(initialNumber) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            placeCall(context, number)
        } else {
            Toast.makeText(context, "Permission to make phone calls is required", Toast.LENGTH_SHORT).show()
        }
    }

    fun onCallPressed() {
        if (number.isEmpty()) {
            Toast.makeText(context, "Enter a number to call", Toast.LENGTH_SHORT).show()
            return
        }
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED -> placeCall(context, number)
            else -> permissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        val maxContentWidth = if (isLandscape) 480.dp else maxWidth

        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NumberDisplay(
                number = number,
                onDelete = { if (number.isNotEmpty()) number = number.dropLast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isLandscape) 8.dp else 24.dp)
            )

            DialPad(
                onDigit = { digit -> number += digit },
                isLandscape = isLandscape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

            CallButton(
                onClick = { onCallPressed() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun NumberDisplay(number: String, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = number.ifEmpty { " " },
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        IconButton(onClick = onDelete, enabled = number.isNotEmpty()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_backspace),
                contentDescription = stringResource(R.string.backspace),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun DialPad(onDigit: (String) -> Unit, isLandscape: Boolean, modifier: Modifier = Modifier) {
    val keys = listOf(
        listOf(KeyData("1", showVoicemail = true), KeyData("2", "ABC"), KeyData("3", "DEF")),
        listOf(KeyData("4", "GHI"), KeyData("5", "JKL"), KeyData("6", "MNO")),
        listOf(KeyData("7", "PQRS"), KeyData("8", "TUV"), KeyData("9", "WXYZ")),
        listOf(KeyData("*"), KeyData("0", "+"), KeyData("#"))
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 16.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    DialButton(
                        digit = key.digit,
                        letters = key.letters,
                        showVoicemail = key.showVoicemail,
                        onClick = { onDigit(key.digit) },
                        isLandscape = isLandscape
                    )
                }
            }
        }
    }
}

@Composable
fun DialButton(
    digit: String,
    letters: String? = null,
    showVoicemail: Boolean = false,
    onClick: () -> Unit,
    isLandscape: Boolean
) {
    val size = if (isLandscape) 56.dp else 72.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF5C6BC0))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                color = Color.White,
                fontSize = if (isLandscape) 22.sp else 28.sp,
                fontWeight = FontWeight.Medium
            )
            when {
                showVoicemail -> Icon(
                    painter = painterResource(id = R.drawable.ic_voicemail),
                    contentDescription = stringResource(R.string.voicemail),
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(if (isLandscape) 10.dp else 12.dp)
                )
                !letters.isNullOrEmpty() -> Text(
                    text = letters,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = if (isLandscape) 8.sp else 10.sp
                )
            }
        }
    }
}

@Composable
fun CallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF4CAF50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_call),
                contentDescription = stringResource(R.string.call),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.call),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun placeCall(context: android.content.Context, number: String) {
    val callIntent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$number")
    }
    try {
        context.startActivity(callIntent)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Unable to place call", Toast.LENGTH_SHORT).show()
    }
}

private data class KeyData(
    val digit: String,
    val letters: String? = null,
    val showVoicemail: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun DiallerPreview() {
    DiallerTheme {
        DiallerScreen(initialNumber = "123456")
    }
}