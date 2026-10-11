package com.example.model

enum class DataLayerGroup(val title: String, val subtitle: String, val badgeColorHex: Long) {
  DOMAIN_MODELS(
    title = "Domain Models",
    subtitle = "com.example.model • Immutable Kotlin Data Classes & Enums",
    badgeColorHex = 0xFF38BDF8
  ),
  SEED_DATASETS(
    title = "Seed Datasets",
    subtitle = "com.example.data.seed • Organized Initial ERP Records",
    badgeColorHex = 0xFF10B981
  ),
  LOCAL_STORAGE(
    title = "Room SQLite & Preferences",
    subtitle = "com.example.data.local • Offline-First Entities, DAOs & Session",
    badgeColorHex = 0xFFF59E0B
  ),
  CLOUD_FIRESTORE(
    title = "Cloud & Auth Services",
    subtitle = "com.example.data.firestore • Real-Time Sync, Retry & Diagnostics",
    badgeColorHex = 0xFF8B5CF6
  )
}

data class DataFileSchemaField(
  val name: String,
  val type: String,
  val description: String
)

data class OrganizedDataFileEntry(
  val id: String,
  val fileName: String,
  val packagePath: String,
  val group: DataLayerGroup,
  val summary: String,
  val primaryEntities: List<String>,
  val liveRecordCount: Int,
  val storageEngine: String,
  val syncMode: String,
  val schemaFields: List<DataFileSchemaField>,
  val sampleRecordPreview: List<Pair<String, String>>,
  val linkedTab: NavigationTab? = null
)
