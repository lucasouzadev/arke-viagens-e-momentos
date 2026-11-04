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
import androidx.compose.ui.window.DialogProperties
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

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1.5f)
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
    AlertDialog(
        onDismissRequest = { if (!busy) onClose() },
        properties = DialogProperties(dismissOnBackPress = !busy, dismissOnClickOutside = !busy),
        title = { Text("Marcar no mapa") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(idea.name, style = MaterialTheme.typography.titleMedium)
                Text(idea.address)
                if (BuildConfig.MAPS_CONFIGURED) {
                    OutlinedTextField(query, { query = it }, label = { Text("Buscar endereço") }, singleLine = true)
                    TextButton(enabled = query.isNotBlank() && !searching, onClick = {
                        searching = true
                        error = null
                        scope.launch {
                            try {
                                matches = findAddresses(query)
                                if (matches.isEmpty()) error = "Endereço não encontrado. Você pode tocar no mapa."
                                matches.singleOrNull()?.let(::selectAddress)
                            } catch (_: java.io.IOException) {
                                error = "Não foi possível buscar o endereço."
                            } finally { searching = false }
                        }
                    }) { Text("Buscar") }
                    matches.forEach { address ->
                        TextButton(onClick = { selectAddress(address) }) {
                            Text(address.getAddressLine(0) ?: address.featureName ?: "Usar este ponto")
                        }
                    }
                    Text("Toque no mapa para posicionar a marca.")
                    GoogleMap(Modifier.fillMaxWidth().height(220.dp), cameraPositionState = camera,
                        onMapClick = { point = it }) {
                        point?.let { coordinate ->
                            val marker = remember(coordinate) { MarkerState(position = coordinate) }
                            Marker(state = marker, title = idea.name)
                        }
                    }
                } else {
                    Text("O mapa está indisponível. A ideia continua salva no aparelho.")
                }
                Text("Viagem")
                TextButton(onClick = { creating = false; tripID = null }) {
                    Text(if (!creating && tripID == null) "Sem viagem ✓" else "Sem viagem")
                }
                plan?.trips.orEmpty().forEach { trip ->
                    TextButton(onClick = { creating = false; tripID = trip.id }) {
                        Text(if (!creating && tripID == trip.id) trip.title + " ✓" else trip.title)
                    }
                }
                TextButton(onClick = { creating = true }) { Text("Criar viagem") }
                if (creating) OutlinedTextField(title, { title = it }, label = { Text("Nome da viagem") }, singleLine = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = point != null && plan != null && !busy && (!creating || title.isNotBlank()),
                onClick = {
                    val coordinate = point ?: return@TextButton
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
            ) { Text("Confirmar ponto e salvar Pin") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onClose) { Text("Cancelar") } },
    )
}
