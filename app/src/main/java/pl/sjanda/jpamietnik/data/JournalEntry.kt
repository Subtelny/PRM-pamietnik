// W pliku np. JournalEntry.kt w pakiecie data
package pl.sjanda.jpamietnik.data

import com.google.firebase.firestore.GeoPoint // Dla Firestore
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Jeśli używasz kotlinx.serialization do przesyłania między ekranami
// import kotlinx.serialization.Serializable
// import pl.sjanda.jpamietnik.util.LocalDateSerializer // Jeśli potrzebujesz niestandardowego serializatora dla LocalDate
// import java.time.LocalDate

// @Serializable // Jeśli potrzebne
data class JournalEntry(
    var id: String = "", // ID z Firestore
    var title: String = "",
    var textContent: String = "", // Zamiast opisu filmu

    // Lokalizacja
    var locationName: String? = null, // Nazwa miejscowości
    var geoPoint: GeoPoint? = null, // Współrzędne dla Firestore (lat, lon)

    // Media - przechowujemy URL do plików w Firebase Storage
    var photoUrl: String? = null,
    var voiceRecordingUrl: String? = null,

    // Możesz zachować datę utworzenia/modyfikacji
    // @Serializable(with = LocalDateSerializer::class) // Jeśli używasz LocalDate i kotlinx.serialization
    // var creationDate: LocalDate = LocalDate.now(),
    @ServerTimestamp // Automatycznie ustawiany przez Firestore przy tworzeniu
    var createdAt: Date? = null,
    @ServerTimestamp // Automatycznie ustawiany przez Firestore przy aktualizacji
    var updatedAt: Date? = null,

    // Możesz dodać inne pola, np. userId, jeśli masz system użytkowników
    // var userId: String = ""
) {
    // Konstruktor bezargumentowy wymagany przez Firestore
    constructor() : this("", "", "")
}