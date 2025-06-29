package pl.sjanda.jpamietnik.util

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.data.Location
import pl.sjanda.jpamietnik.data.LocationUtils

@OptIn(ExperimentalPermissionsApi::class, DelicateCoroutinesApi::class)
@Composable
fun LocationPicker(
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit,
    locationPermissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoadingLocation by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.lokalizacja),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        if (locationPermissionState.status.isGranted) {
                            isLoadingLocation = true
                            val fusedLocationClient =
                                LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let { loc ->
                                        kotlinx.coroutines.GlobalScope.launch {
                                            val diaryLocation =
                                                LocationUtils.getAddressFromLocation(
                                                    context,
                                                    loc.latitude,
                                                    loc.longitude
                                                )
                                            onLocationSelected(diaryLocation)
                                            isLoadingLocation = false
                                        }
                                    } ?: run {
                                        isLoadingLocation = false
                                    }
                                }
                            } catch (e: SecurityException) {
                                isLoadingLocation = false
                            }
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.location_get_loc)
                        )
                    }
                }
            }

            selectedLocation?.let { location ->
                if (location.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.lokalizacja),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = location.city,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = location.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}