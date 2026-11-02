package dev.arke.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Base roles from DESIGN-006; native fonts and font scaling remain active. */
val ArkeTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
)

@androidx.compose.runtime.Composable
fun arkeSheetColor() = if (androidx.compose.foundation.isSystemInDarkTheme())
    androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest

@androidx.compose.runtime.Composable
fun arkeInsetColor() = if (androidx.compose.foundation.isSystemInDarkTheme())
    androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest
else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArkeFloatingSheet(
    onDismiss: () -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = 0.dp,
        shape = RectangleShape,
        dragHandle = {
            val dismissLabel = stringResource(dev.arke.app.R.string.sheet_dismiss)
            TextButton(onClick = onDismiss, enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("$tagPrefix.dismiss").semantics { contentDescription = dismissLabel },
            ) {
                Box(Modifier.size(64.dp, 16.dp).background(arkeSheetColor(), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                }
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            Column(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(arkeSheetColor())
                .testTag("$tagPrefix.dismiss.card"), content = content)
        }
    }
}
