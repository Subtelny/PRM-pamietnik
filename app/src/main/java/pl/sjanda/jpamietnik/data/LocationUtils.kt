package pl.sjanda.jpamietnik.data

import android.content.Context
import android.location.Geocoder
import android.location.Location
import pl.sjanda.jpamietnik.data.Location as DiaryLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {

    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): DiaryLocation = withContext(Dispatchers.IO) {
        return@withContext try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                DiaryLocation(
                    latitude = latitude,
                    longitude = longitude,
                    address = address.getAddressLine(0) ?: "",
                    city = address.locality ?: address.adminArea ?: ""
                )
            } else {
                DiaryLocation(latitude, longitude, "", "")
            }
        } catch (e: Exception) {
            DiaryLocation(latitude, longitude, "", "")
        }
    }

    fun calculateDistance(loc1: DiaryLocation, loc2: DiaryLocation): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude,
            results
        )
        return results[0]
    }
}