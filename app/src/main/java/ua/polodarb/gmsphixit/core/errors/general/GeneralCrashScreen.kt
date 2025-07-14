package ua.polodarb.gmsphixit.core.errors.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.polodarb.gmsphixit.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun GeneralCrashScreen(
    sendReport: () -> Unit,
    restartApp: () -> Unit
) {
    Surface {
        Column {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(MaterialShapes.Boom.toShape())
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.crash_title),
                fontSize = 46.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.crash_msg),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.crash_advice),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
            ) {
                OutlinedButton(
                    onClick = sendReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.crash_report),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Button(
                    onClick = restartApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.crash_restart),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}