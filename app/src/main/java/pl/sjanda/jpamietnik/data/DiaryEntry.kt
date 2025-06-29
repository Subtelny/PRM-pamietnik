package pl.sjanda.jpamietnik.data

import java.util.Date

data class DiaryEntry(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val location: Location = Location(),
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = ""
)