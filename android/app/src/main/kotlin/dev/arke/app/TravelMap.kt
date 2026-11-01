package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    GoogleMap(
        modifier = modifier,
        cameraPositionState = camera,
        contentDescription = stringResource(R.string.map_description),
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false, mapToolbarEnabled = false),
    )
}
