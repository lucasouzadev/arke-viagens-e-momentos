package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.location.LocationManagerCompat
import android.location.LocationManager
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import dev.arke.core.PlaceIdea
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun TravelMap(modifier: Modifier = Modifier, pins: List<Pair<LocalMapPin, String>> = emptyList(), revealPin: LocalMapPin? = null, onPinSelected: (LocalMapPin) -> Unit = {}) {
    if (!BuildConfig.MAPS_CONFIGURED) {
        Column(modifier.padding(20.dp).arkeScrollEdges().verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 24.dp)) {
            Text(stringResource(R.string.map_pending), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.map_pending_detail))
        }
        return
    }

    // D4: sem Pins nem localização o mapa abre no Brasil; com Pins salvos, enquadra todos. Nunca pede permissão sozinho.
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-14.2, -51.9), 3.4f)
    }
    var framedPins by remember { mutableStateOf(false) }
    LaunchedEffect(pins.size) {
        if (!framedPins && pins.isNotEmpty()) {
            framedPins = true
            val points = pins.map { LatLng(it.first.latitude, it.first.longitude) }
            if (points.size == 1) {
                camera.position = CameraPosition.fromLatLngZoom(points.first(), 12f)
            } else {
                val bounds = com.google.android.gms.maps.model.LatLngBounds.builder().apply { points.forEach { include(it) } }.build()
                runCatching { camera.move(CameraUpdateFactory.newLatLngBounds(bounds, 96)) }
            }
        }
    }
    LaunchedEffect(revealPin) {
        revealPin?.let { camera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)) }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permitted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var message by remember { mutableStateOf<String?>(null) }
    fun locate() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { permitted = false; return }
        val manager = context.getSystemService(LocationManager::class.java)
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { message = "Ative a localização para mostrar sua posição. Você pode continuar explorando o mapa."; return }
        try {
            LocationManagerCompat.getCurrentLocation(manager, LocationManager.NETWORK_PROVIDER, android.os.CancellationSignal(), ContextCompat.getMainExecutor(context)) { location: android.location.Location? ->
                if (location != null) scope.launch { camera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14f)) }
                else message = "Não foi possível obter sua posição agora. Tente novamente."
            }
        } catch (_: SecurityException) { permitted = false; message = "Permita a localização para mostrar sua posição." }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permitted = granted
        if (granted) locate() else message = "Você pode continuar explorando o mapa sem compartilhar sua localização."
    }
    Box(modifier) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        contentDescription = stringResource(R.string.map_description),
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        properties = MapProperties(isMyLocationEnabled = permitted),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false, mapToolbarEnabled = false),
    ) {
        pins.forEach { (pin, name) ->
            key(pin.ideaID, pin.latitude, pin.longitude) {
                val marker = remember { MarkerState(position = LatLng(pin.latitude, pin.longitude)) }
                Marker(state = marker, title = name, onClick = { onPinSelected(pin); true })
            }
        }
    }
    IconButton(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) locate() else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        modifier = Modifier.align(Alignment.CenterEnd).padding(12.dp).size(48.dp).background(arkeSheetColor(), CircleShape)) {
        Icon(painterResource(R.drawable.ic_location), contentDescription = "Minha localização")
    }
    }
    if (message != null) AlertDialog(onDismissRequest = { message = null }, title = { Text("Minha localização") }, text = { Text(message.orEmpty()) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Entendi") } })
}


@Composable
fun PinEditor(idea: PlaceIdea, places: PlaceIdeasViewModel, onClose: () -> Unit, onSaved: (LocalPinPlan) -> Unit) {
    val context = LocalContext.current
    var query by remember(idea.id) { mutableStateOf(idea.address) }
    var searching by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<android.location.Address>>(emptyList()) }
    var plan by remember { mutableStateOf<LocalPinPlan?>(null) }
    var point by remember { mutableStateOf<LatLng?>(null) }
    var approximateAddress by remember { mutableStateOf<String?>(null) }
    var tripID by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val newTripID = remember { java.util.UUID.randomUUID().toString() }
    val scope = rememberCoroutineScope()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }
    fun locatePin() {
        val manager = context.getSystemService(LocationManager::class.java)
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            error = "Ative a localização para usar sua posição."
            return
        }
        try {
            LocationManagerCompat.getCurrentLocation(
                manager, LocationManager.NETWORK_PROVIDER, android.os.CancellationSignal(),
                ContextCompat.getMainExecutor(context),
            ) { location ->
                if (location == null) {
                    error = "Não foi possível obter sua posição."
                } else {
                    val coordinate = LatLng(location.latitude, location.longitude)
                    point = coordinate
                    camera.position = CameraPosition.fromLatLngZoom(coordinate, 14f)
                    error = null
                }
            }
        } catch (_: SecurityException) {
            error = "Permita a localização para usar sua posição."
        }
    }
    val pinPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locatePin() else error = "Você pode escolher um ponto no mapa sem usar sua localização."
    }
    fun selectAddress(address: android.location.Address) {
        val coordinate = LatLng(address.latitude, address.longitude)
        point = coordinate
        camera.position = CameraPosition.fromLatLngZoom(coordinate, 14f)
        matches = emptyList()
    }
    @Suppress("DEPRECATION")
    suspend fun findAddresses(phrase: String): List<android.location.Address> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (android.location.Geocoder.isPresent()) {
                android.location.Geocoder(context).getFromLocationName(phrase, 5).orEmpty()
                    .filter { it.hasLatitude() && it.hasLongitude() }
            } else emptyList()
        }
    LaunchedEffect(idea.id) {
        try {
            val loaded = places.loadPinPlan()
            loaded.pins.firstOrNull { it.ideaID == idea.id }?.let { pin ->
                val center = LatLng(pin.latitude, pin.longitude)
                point = center
                tripID = pin.tripID
                camera.position = CameraPosition.fromLatLngZoom(center, 14f)
            }
            plan = loaded
            if (BuildConfig.MAPS_CONFIGURED && point == null) {
                matches = try { findAddresses(idea.address) }
                    catch (_: java.io.IOException) { emptyList() }
                if (point == null) matches.singleOrNull()?.let(::selectAddress)
            }
        } catch (failure: Exception) {
            if (failure is kotlinx.coroutines.CancellationException) throw failure
            error = "Não foi possível abrir os Pins."
        }
    }
    // ADR-014: geocodificação reversa nativa, só após o ponto parar de mudar; o texto não é salvo nem enviado a ninguém.
    LaunchedEffect(point) {
        approximateAddress = null
        val coordinate = point ?: return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        approximateAddress = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                @Suppress("DEPRECATION")
                if (android.location.Geocoder.isPresent()) {
                    android.location.Geocoder(context).getFromLocation(coordinate.latitude, coordinate.longitude, 1)
                        ?.firstOrNull()?.let { address ->
                            listOfNotNull(address.thoroughfare ?: address.featureName, address.subLocality ?: address.locality)
                                .filter { it.isNotBlank() }.joinToString(", ").ifEmpty { null }
                        }
                } else null
            }
        } catch (failure: Exception) {
            if (failure is kotlinx.coroutines.CancellationException) throw failure
            null
        }
    }
    val canConfirm = point != null && plan != null && !busy && (!creating || title.isNotBlank())
    // Explica por que a confirmação está indisponível; o botão nunca fica apagado sem motivo.
    val hint = when {
        plan == null && error == null -> "Abrindo seus Pins…"
        !BuildConfig.MAPS_CONFIGURED -> "O mapa está indisponível nesta versão. A ideia continua salva no aparelho."
        point == null -> "Toque no mapa ou busque um endereço para marcar o ponto."
        creating && title.isBlank() -> "Dê um nome à nova viagem para continuar."
        else -> null
    }
    Dialog(
        onDismissRequest = { if (!busy) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = !busy, dismissOnClickOutside = false,
            usePlatformDefaultWidth = false, decorFitsSystemWindows = false,
        ),
    ) {
        // Marcar no mapa: o mapa é o conteúdo; o painel inferior concentra a decisão.
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.fillMaxSize()) {
                if (BuildConfig.MAPS_CONFIGURED) {
                    GoogleMap(Modifier.fillMaxSize(), cameraPositionState = camera, onMapClick = { point = it }) {
                        point?.let { coordinate ->
                            val marker = remember(coordinate) { MarkerState(position = coordinate) }
                            Marker(state = marker, title = idea.name)
                        }
                    }
                }
                Column(
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            enabled = !busy, onClick = onClose,
                            modifier = Modifier.size(40.dp).background(arkeSheetColor(), CircleShape).testTag("pin.cancel"),
                        ) { Icon(painterResource(R.drawable.ic_close), "Cancelar", Modifier.size(22.dp)) }
                        Text(
                            "Marcar no mapa", Modifier.weight(1f).semantics { heading() },
                            style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        if (BuildConfig.MAPS_CONFIGURED) {
                            IconButton(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        locatePin()
                                    } else {
                                        pinPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                },
                                modifier = Modifier.size(40.dp).background(arkeSheetColor(), CircleShape).testTag("pin.location"),
                            ) { Icon(painterResource(R.drawable.ic_location), "Minha localização", Modifier.size(22.dp)) }
                        } else {
                            Spacer(Modifier.size(40.dp))
                        }
                    }
                    if (BuildConfig.MAPS_CONFIGURED) {
                        Column(
                            Modifier.fillMaxWidth().background(arkeSheetColor(), RoundedCornerShape(20.dp)).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(query, { query = it }, Modifier.weight(1f).testTag("pin.search"),
                                    label = { Text("Buscar endereço") }, singleLine = true)
                                TextButton(enabled = query.isNotBlank() && !searching, onClick = {
                                    searching = true
                                    error = null
                                    scope.launch {
                                        try {
                                            matches = findAddresses(query)
                                            if (matches.isEmpty()) error = "Endereço não encontrado. Toque no mapa para marcar o ponto."
                                            matches.singleOrNull()?.let(::selectAddress)
                                        } catch (_: java.io.IOException) {
                                            error = "Não foi possível buscar o endereço."
                                        } finally { searching = false }
                                    }
                                }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (searching) "Buscando…" else "Buscar") }
                            }
                            matches.forEach { address ->
                                TextButton(onClick = { selectAddress(address) }, modifier = Modifier.heightIn(min = 48.dp)) {
                                    Text(address.getAddressLine(0) ?: address.featureName ?: "Usar este ponto")
                                }
                            }
                        }
                    }
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(12.dp)
                        .background(arkeSheetColor(), RoundedCornerShape(28.dp))
                        .padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column {
                        Text(idea.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(idea.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Text(
                        point?.let { approximateAddress?.let { place -> "Perto de $place" } ?: "Ponto marcado no mapa" } ?: "Nenhum ponto marcado ainda",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("pin.point"),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Vincular a uma viagem (opcional)", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PinChip("Sem viagem", !creating && tripID == null, !busy) { creating = false; tripID = null }
                            plan?.trips.orEmpty().forEach { trip ->
                                PinChip(trip.title, !creating && tripID == trip.id, !busy) { creating = false; tripID = trip.id }
                            }
                            PinChip("+ Nova viagem", creating, !busy) { creating = true; tripID = null }
                        }
                        if (creating) OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth().testTag("pin.tripTitle"),
                            label = { Text("Nome da viagem") }, singleLine = true)
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("pin.hint")) }
                    Button(
                        enabled = canConfirm,
                        onClick = {
                            val coordinate = point ?: return@Button
                            val choice = if (creating) LocalPinTripChoice.Create(newTripID, title)
                                else tripID?.let { LocalPinTripChoice.Existing(it) } ?: LocalPinTripChoice.None
                            busy = true
                            error = null
                            scope.launch {
                                try { onSaved(places.savePin(idea.id, coordinate.latitude, coordinate.longitude, choice)) }
                                catch (failure: Exception) {
                                    if (failure is kotlinx.coroutines.CancellationException) throw failure
                                    error = "Não foi possível salvar o Pin. Tente novamente."
                                } finally { busy = false }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("pin.confirm"),
                    ) { Text(if (busy) "Salvando…" else "Confirmar ponto") }
                }
            }
        }
    }
}

@Composable
private fun PinChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) ArkeColors.brand else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (selected) "✓ $label" else label, maxLines = 1,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
