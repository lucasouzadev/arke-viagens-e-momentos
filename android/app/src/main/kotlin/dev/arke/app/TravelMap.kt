package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun TravelMap(modifier: Modifier = Modifier) {
    if (!BuildConfig.MAPS_CONFIGURED) {
        Column(modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(stringResource(R.string.map_pending), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.map_pending_detail))
        }
        return
    }

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1.5f)
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
            LocationManagerCompat.getCurrentLocation(manager, LocationManager.NETWORK_PROVIDER, null, ContextCompat.getMainExecutor(context)) { location ->
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
    )
    TextButton(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) locate() else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) { Text("Minha localização") }
    }
    if (message != null) AlertDialog(onDismissRequest = { message = null }, title = { Text("Minha localização") }, text = { Text(message.orEmpty()) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Entendi") } })
}
