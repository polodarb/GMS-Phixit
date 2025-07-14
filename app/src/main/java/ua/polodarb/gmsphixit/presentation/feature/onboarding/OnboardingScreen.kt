package ua.polodarb.gmsphixit.presentation.feature.onboarding

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsphixit.R
import ua.polodarb.gmsphixit.core.shell.InitShell
import ua.polodarb.gmsphixit.presentation.MainActivity

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel,
    onRootConfirm: () -> Unit
) {
    val pagerState = rememberPagerState() { 2 }
    val context = LocalContext.current
    val activity = context as? MainActivity
    val coroutineScope = rememberCoroutineScope()
    val navigationManager = activity?.navigationManager

    var isButtonLoading by rememberSaveable { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WarningScreen(
                    onExit = { activity?.finish() },
                    onContinue = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )

                1 -> RootRequestScreen(
                    onExit = { activity?.finish() },
                    onRootRequest = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isButtonLoading = true

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                InitShell.initShell()

                                if (Shell.getShell().isRoot) {
                                    withContext(Dispatchers.Main) {
                                        activity?.rootDBInitializer?.initDB()
                                        delay(700)
                                        navigationManager?.markOnboardingCompleted()
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRootConfirm()
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                        isButtonLoading = false
                                        Toast.makeText(context, "ROOT IS DENIED!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (_: Exception) { }
                        }
                    },
                    isButtonLoading = isButtonLoading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WarningScreen(
    onExit: () -> Unit,
    onContinue: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Warning",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val annotatedText = buildAnnotatedString {
                        append("GMS Phixit is a continuation of ")
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = "https://github.com/Polodarb/GMS-Flags"
                        )
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("GMS Flags")
                        }
                        pop()
                        append(", but with support for the new DB schema. \n\nThis app is an intermediate result in the process of reversing the new DB schema, which broke GMS Flags. ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("\n\nThis is the first alpha, the app is untested and its impact on devices is unknown.")
                        }
                        append("\nBy continuing, you accept all risks.\n\nISSUE №1: ALL FLAGS RESET AFTER 24 HOURS. This app is released in hope that the community can find a solution.")
                    }
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = annotation.item.toUri()
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        onExit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Exit",
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onContinue()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RootRequestScreen(
    onExit: () -> Unit,
    onRootRequest: () -> Unit,
    isButtonLoading: Boolean
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.verticalScroll(state = rememberScrollState())
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(MaterialShapes.SoftBoom.toShape())
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp).size(56.dp)
            )
            Text(
                text = stringResource(id = R.string.root_title),
                fontSize = 46.sp,
                fontWeight = FontWeight.W600,
                lineHeight = 44.sp,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.root_msg),
                fontSize = 19.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.root_advice),
                fontSize = 19.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Spacer(modifier = Modifier.padding(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 50.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        onExit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.root_exit),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Button(
                    onClick = onRootRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (isButtonLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.root_request),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
