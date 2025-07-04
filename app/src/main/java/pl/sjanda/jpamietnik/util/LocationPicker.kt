package pl.sjanda.jpamietnik.util

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.data.Location
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPicker(
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit,
    locationPermissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    fun getCurrentLocation() {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
            return
        }

        isLoading = true

        scope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        scope.launch {
                            val addressInfo = withContext(Dispatchers.IO) {
                                try {
                                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                    addresses?.firstOrNull()
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            val currentLocation = Location(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                address = addressInfo?.getAddressLine(0) ?: "",
                                city = addressInfo?.locality ?: ""
                            )
                            onLocationSelected(currentLocation)
                            isLoading = false
                        }
                    } ?: run {
                        isLoading = false
                    }
                }.addOnFailureListener {
                    isLoading = false
                }
            } catch (e: SecurityException) {
                isLoading = false
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.lokalizacja),
                    style = MaterialTheme.typography.titleMedium
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { getCurrentLocation() }
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Location"
                        )
                    }
                }
            }

            selectedLocation?.let { location ->
                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (location.address.isNotEmpty()) {
                                    Text(
                                        location.address,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (location.city.isNotEmpty()) {
                                    Text(
                                        location.city,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onLocationSelected(Location()) }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear location"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}