package dev.arke.app

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
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

/**
 * Azul candidato do ADR-006 e sua tinta de texto no escuro (mesma matiz, luminosidade maior).
 * O azul puro dá ≈3,3:1 sobre o cartão escuro; a tinta dá ≈6,9:1. No claro o próprio azul passa (≈5,1:1).
 */
object ArkeColors {
    val brand = Color(0xFF4C728A)
    val inkDark = Color(0xFF88AABE)
}

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
        onDismissRequest = onDismiss, sheetState = sheetState, modifier = Modifier.testTag("$tagPrefix.dismiss.card"),
        containerColor = arkeSheetColor(), contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = 0.dp,
        shape = RoundedCornerShape(36.dp),
        dragHandle = null,
    ) {
        Column(modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun Modifier.arkeScrollEdges(surface: Color = MaterialTheme.colorScheme.surface): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawRect(Brush.verticalGradient(listOf(surface, Color.Transparent), 0f, minOf(12.dp.toPx(), size.height * 0.12f)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, surface), (size.height - minOf(24.dp.toPx(), size.height * 0.12f)).coerceAtLeast(0f), size.height))
    }
)

/** Fundo de campo com contraste próprio sobre a superfície do sheet. */
@Composable
fun arkeFieldColor() = if (androidx.compose.foundation.isSystemInDarkTheme())
    MaterialTheme.colorScheme.surfaceContainerHighest
else MaterialTheme.colorScheme.surfaceContainerLow

/** Campo com rótulo persistente (DESIGN-007). */
@Composable
fun ArkeField(title: String, optional: Boolean = false, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (optional) Text("Opcional", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(20.dp)).background(arkeFieldColor())
            .padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.CenterStart) { content() }
    }
}

/** Segmentado com estado selecionado visível: cápsula da marca, texto branco e ✓ (não depende só de cor). */
@Composable
fun <T> ArkeSegmented(options: List<Pair<T, String>>, selected: T, tagPrefix: String, enabled: Boolean = true, onSelect: (T) -> Unit) {
    Row(Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f).clip(RoundedCornerShape(24.dp))
        .background(arkeFieldColor()).padding(3.dp)) {
        options.forEach { (value, title) ->
            val isSelected = value == selected
            Row(Modifier.weight(1f).heightIn(min = 42.dp).clip(RoundedCornerShape(21.dp))
                .background(if (isSelected) ArkeColors.brand else Color.Transparent)
                .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton) { onSelect(value) }
                .testTag("$tagPrefix.$title"),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                }
                Text(title, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * Sheet de formulário compacto: altura pelo conteúdo (sem área morta), fundo opaco, X no topo esquerdo
 * (descrição "Cancelar") e primária colada ao conteúdo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArkeFormSheet(
    title: String, tagPrefix: String, primaryTitle: String, primaryTag: String,
    primaryEnabled: Boolean = true, onCancel: () -> Unit, onPrimary: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ArkeFloatingSheet(onDismiss = onCancel, tagPrefix = tagPrefix, modifier = Modifier.imePadding()) {
        Box(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 12.dp, vertical = 4.dp)) {
            IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterStart).size(48.dp).testTag("sheet.cancel")) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = "Cancelar")
            }
            Text(title, Modifier.align(Alignment.Center).semantics { heading() }, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        Button(onClick = onPrimary, enabled = primaryEnabled, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 20.dp).heightIn(min = 56.dp).testTag(primaryTag)) {
            Text(primaryTitle, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
