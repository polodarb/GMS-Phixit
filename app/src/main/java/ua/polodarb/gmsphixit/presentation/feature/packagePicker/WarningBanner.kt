package ua.polodarb.gmsphixit.presentation.feature.packagePicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WarningBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
//            .padding(bottom = 16.dp)
//            .padding(horizontal = 16.dp)
            .statusBarsPadding()
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 18.dp, horizontal = 24.dp)
    ) {
        Text(
            text = "⚠️ WARNING: \n- All actions are at your own risk. \n- All flags will reset after 24 hours.",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

//        Box(
//            Modifier
//                .align(Alignment.TopEnd)
//                .size(256.dp)
//                .clip(MaterialShapes.Boom.toShape())
//                .background(MaterialTheme.colorScheme.surfaceContainer)
//        )
    }
}
