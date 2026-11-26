package com.aibuilt.ludokingcoinslocked

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private data class PatchRow(
    val label: String,
    val rva: String,
    val fileOff: String,
    val hex: String
)

private val patchRows = listOf(
    PatchRow(
        "Coin getter A → return 999,999,999",
        "0x21B9E00",
        "0x21B5E00",
        "e0 3f 99 d2  40 73 a7 f2  00 00 c0 f2  00 00 e0 f2  c0 03 5f d6"
    ),
    PatchRow(
        "Coin getter B → return 999,999,999",
        "0x22DDC0C",
        "0x22D9C0C",
        "e0 3f 99 d2  40 73 a7 f2  00 00 c0 f2  00 00 e0 f2  c0 03 5f d6"
    ),
    PatchRow(
        "Coins-locked flag → mov w0,#1 ; ret",
        "0x20CA650",
        "0x20C6650",
        "20 00 80 52  c0 03 5f d6"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PatchInfoScreen()
                }
            }
        }
    }
}

@Composable
private fun PatchInfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ludo King · coins locked", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This app is a read-only note. It does not patch anything itself — the APK is patched " +
                "on a PC by tools/patch_apk.py (unzip → patch 3 byte ranges → zip → zipalign → " +
                "apksigner), never by a Gradle rebuild. tools/verify.sh then re-checks the result.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                "Recorded here only for reference.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        HorizontalDivider()

        Text(
            "Three patches in lib/arm64-v8a/libil2cpp.so",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "file_off = rva − 0x4000",
            style = MaterialTheme.typography.bodySmall
        )

        patchRows.forEach { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(row.label, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "RVA ${row.rva}   file ${row.fileOff}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        row.hex,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Text(
            "The bytes that stood at these offsets in the stock APK are captured by the patcher " +
                "into build/patch-report.json and tools/originals.json, and printed by " +
                "tools/verify.sh.",
            style = MaterialTheme.typography.bodySmall
        )

        HorizontalDivider()

        Section(
            "arm64 only",
            "Only lib/arm64-v8a/libil2cpp.so is patched. armeabi-v7a and x86_64 copies in the " +
                "stock APK keep the original code."
        )
        Section(
            "Client-side only",
            "The value lives in the local library. The game or its server can resync the real " +
                "balance, so the patched number is not permanent."
        )
        Section(
            "Uninstall the Play Store copy first",
            "The patched APK is signed with a different key (CN=Arin), so installing over the " +
                "existing app fails with a signature mismatch."
        )
        Section(
            "Diamonds are not patched",
            "Planned extension: the same constant-getter stub at the diamond getter's RVA."
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
